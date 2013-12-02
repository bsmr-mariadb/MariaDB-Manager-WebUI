/*
 * This file is distributed as part of the MariaDB Manager.  It is free
 * software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation,
 * version 2.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Copyright 2012-2014 SkySQL Ab
 */

package com.skysql.manager;

import java.util.concurrent.ScheduledFuture;

import org.vaadin.jouni.animator.AnimatorProxy;

import com.skysql.manager.AppData.Debug;
import com.skysql.manager.api.APIrestful;
import com.skysql.manager.api.SystemInfo;
import com.skysql.manager.api.UserInfo;
import com.skysql.manager.api.UserObject;
import com.skysql.manager.api.Versions;
import com.skysql.manager.ui.ErrorDialog;
import com.skysql.manager.ui.ErrorView;
import com.skysql.manager.ui.GeneralSettings;
import com.skysql.manager.ui.LoginView;
import com.skysql.manager.ui.OverviewPanel;
import com.skysql.manager.ui.SetupDialog;
import com.skysql.manager.ui.TabbedPanel;
import com.skysql.manager.ui.TopPanel;
import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("skystyle1")
@Title("MariaDB Manager")
@Push
@PreserveOnRefresh
public class ManagerUI extends UI {

	private static final String GUI_VERSION = "1.0.74 Beta";
	private static final String NOT_AVAILABLE = "n/a";

	private ScheduledFuture<?> mainTimerFuture;

	private OverviewPanel overviewPanel;
	private TabbedPanel tabbedPanel;

	@Override
	protected void init(VaadinRequest request) {

		VaadinSession session = getSession();

		log("init session: - " + session + " UI: " + this.toString());

		try {

			do {
				session.setAttribute(ManagerUI.class, this);

				AppData appData = AppData.newInstance();
				if (appData == null) {
					setContent(new ErrorView(Notification.Type.ERROR_MESSAGE, null));
					return;
				}
				session.setAttribute(AppData.class, appData);

				APIrestful api = APIrestful.newInstance(appData.getApiURI(), appData.getApiKeys());
				if (api == null) {
					setContent(new ErrorView(Notification.Type.ERROR_MESSAGE, null));
					return;
				}
				session.setAttribute(APIrestful.class, api);

				UserInfo userInfo = new UserInfo(null);
				if (userInfo.getUsersList() == null || userInfo.getUsersList().isEmpty()) {
					break;
				}
				session.setAttribute(UserInfo.class, userInfo);

			} while (false);

			refreshContentBasedOnSessionData();

		} catch (RuntimeException e) {
			System.err.println("RunTime error: " + e.getLocalizedMessage());
			close();
		}

	}

	@Override
	public void close() {
		log("close");
		ExecutorFactory.removeTimer(mainTimerFuture);
		super.close();
	}

	public void refreshContentBasedOnSessionData() {
		/*
		 *  As the UI is regenerated upon browser refresh, we should always check in the init what content to set to our UI. 
		 * 
		 *  To force our application to reuse the same UI instance, we can add the @PreserveOnRefresh-annotation to our UI class
		 */

		VaadinSession session = getSession();

		AppData appData = session.getAttribute(AppData.class);
		if (appData == null) {
			setContent(new ErrorView(Notification.Type.ERROR_MESSAGE, "Cannot find configuration file: " + AppData.configPATH));
			return;
		}

		APIrestful api = session.getAttribute(APIrestful.class);
		if (api == null) {
			setContent(new ErrorView(Notification.Type.ERROR_MESSAGE, null));
			return;
		}

		String systemName = "{ Installation/API name goes here }";
		String guiVersion = GUI_VERSION;
		String apiVersion = api.getVersion();
		Versions monitor = new Versions("monitor");
		String monitorVersion = (monitor.getVersion() != null ? monitor.getVersion() : NOT_AVAILABLE);
		AboutRecord aboutRecord = new AboutRecord(systemName, guiVersion, apiVersion, monitorVersion);
		session.setAttribute(AboutRecord.class, aboutRecord);

		SystemInfo systemInfo = new SystemInfo(SystemInfo.SYSTEM_ROOT);
		session.setAttribute(SystemInfo.class, systemInfo);

		UserInfo userInfo = session.getAttribute(UserInfo.class);
		if (userInfo == null || userInfo.getUsersList() == null || userInfo.getUsersList().isEmpty()) {
			setContent(new ErrorView(Notification.Type.HUMANIZED_MESSAGE, "Initial System Setup - Please provide your configuration information."));
			new SetupDialog();
			return;
		}

		UserObject userObject = session.getAttribute(UserObject.class);
		if (userObject == null) {
			setContent(new LoginView(aboutRecord));
		} else {
			String adjust = userObject.getProperty(UserObject.PROPERTY_TIME_ADJUST);
			DateConversion dateConversion = new DateConversion((adjust == null ? GeneralSettings.DEFAULT_TIME_ADJUST : Boolean.valueOf(adjust)),
					userObject.getProperty(UserObject.PROPERTY_TIME_FORMAT));
			session.setAttribute(DateConversion.class, dateConversion);

			session.setAttribute("isChartsEditing", false);

			initLayout();
			initExecutor();
		}
	}

	private void initLayout() {

		VerticalLayout main = new VerticalLayout();
		main.setMargin(new MarginInfo(false, true, false, true));
		main.setSpacing(true);
		main.setSizeFull();

		setContent(main);

		VaadinSession session = getSession();

		AnimatorProxy proxy = new AnimatorProxy();
		main.addComponent(proxy);
		session.setAttribute(AnimatorProxy.class, proxy);

		VerticalLayout topMiddleLayout = new VerticalLayout();
		main.addComponent(topMiddleLayout);
		session.setAttribute(VerticalLayout.class, topMiddleLayout);

		TopPanel topPanel = new TopPanel();
		topMiddleLayout.addComponent(topPanel);
		session.setAttribute(TopPanel.class, topPanel);

		overviewPanel = new OverviewPanel();
		topMiddleLayout.addComponent(overviewPanel);
		session.setAttribute(OverviewPanel.class, overviewPanel);

		tabbedPanel = new TabbedPanel(session);
		main.addComponent(tabbedPanel.getTabSheet());
		main.setExpandRatio(tabbedPanel.getTabSheet(), 1f);
		session.setAttribute(TabbedPanel.class, tabbedPanel);

		overviewPanel.refresh();

	}

	private void initExecutor() {

		// setup scheduler that will keep refreshing the UI until the end of the session
		log("timer - init");
		final long fDelayBetweenRuns = 20;
		Runnable runTimerTask = new RunMainTimerTask();
		mainTimerFuture = ExecutorFactory.addTimer(runTimerTask, fDelayBetweenRuns);
	}

	private final class RunMainTimerTask implements Runnable {

		private long count = 0;

		public void run() {

			VaadinSession session = getSession();
			if (session == null) {
				log("Vaadin session is null");
				close();
				return;
			}

			if (Debug.ON) {
				log("");
				log("Heartbeat: " + count++);
			}

			Boolean isChartsRefreshing;
			if ((isChartsRefreshing = (Boolean) session.getAttribute("ChartsRefresh")) != null && isChartsRefreshing == true) {
				log("Charts is still refreshing: skipping heartbeat");
				return;
			}

			OverviewPanel overviewPanel = session.getAttribute(OverviewPanel.class);

			try {
				overviewPanel.refresh();
			} catch (RuntimeException e) {
				new ErrorDialog(e, null);
			} catch (Exception e) {
				new ErrorDialog(e, null);
			} finally {
				// Mark Riddoch - 4th Oct 2013
				// Comment out floowing call to close as this causes the user interface to almost
				// constantly become grey out, making it practically unusable
				//				close();
			}

		}
	}

	public static void log(String msg) {
		if (Debug.ON) {
			System.out.println(msg);
		}
	}

}
