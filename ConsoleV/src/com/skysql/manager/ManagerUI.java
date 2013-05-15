/*
 * This file is distributed as part of the SkySQL Cloud Data Suite.  It is free
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
 * Copyright 2012-2013 SkySQL Ab
 */

package com.skysql.manager;

import java.util.LinkedHashMap;
import java.util.concurrent.ScheduledFuture;

import org.vaadin.artur.icepush.ICEPush;

import com.skysql.manager.api.APIrestful;
import com.skysql.manager.api.SystemInfo;
import com.skysql.manager.api.UserInfo;
import com.skysql.manager.api.UserObject;
import com.skysql.manager.ui.ErrorView;
import com.skysql.manager.ui.LoginView;
import com.skysql.manager.ui.OverviewPanel;
import com.skysql.manager.ui.SetupDialog;
import com.skysql.manager.ui.TabbedPanel;
import com.skysql.manager.ui.TopPanel;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
@Theme("skystyle1")
@Title("SkySQL Manager")
public class ManagerUI extends UI {

	private ScheduledFuture<?> mainTimerFuture;

	private ICEPush icePush;
	private OverviewPanel overviewPanel;
	private TabbedPanel tabbedPanel;
	private String systemName, systemVersion;

	@Override
	protected void init(VaadinRequest request) {

		do {
			AppData appData = AppData.newInstance();
			if (appData == null) {
				break;
			}
			VaadinSession.getCurrent().setAttribute(AppData.class, appData);

			APIrestful.setURI(appData.getApiURI());
			APIrestful.setKeys(appData.getApiKeys());

			APIrestful api = APIrestful.newInstance();
			if (api == null) {
				break;
			}
			VaadinSession.getCurrent().setAttribute(APIrestful.class, api);

			SystemInfo systemInfo = new SystemInfo(null);
			if (systemInfo.getCurrentID() == null) {
				break;
			}
			VaadinSession.getCurrent().setAttribute(SystemInfo.class, systemInfo);

			if (systemInfo.getCurrentSystem().getNodes().length == 0) {
				break;
			}

			UserInfo userInfo = new UserInfo(null);
			if (userInfo.getUsersList() == null || userInfo.getUsersList().isEmpty()) {
				break;
			}

			if (systemInfo != null && systemInfo.getCurrentSystem() != null) {
				LinkedHashMap<String, String> properties = systemInfo.getCurrentSystem().getProperties();
				//				// use system without user authentication
				//				if (properties.containsKey(SystemInfo.PROPERTY_SKIPLOGIN)) {
				//					String skipLogin = properties.get(SystemInfo.PROPERTY_SKIPLOGIN);
				//					if (skipLogin != null && skipLogin.equalsIgnoreCase("true")) {
				//						UserObject userObject = new UserObject("0", "Debugging");
				//						VaadinSession.getCurrent().setAttribute(UserObject.class, userObject);
				//					}
				//				}

				systemName = systemInfo.getCurrentSystem().getName();
				systemVersion = properties.get(SystemInfo.PROPERTY_VERSION);
			}

		} while (false);

		refreshContentBasedOnSessionData();

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
		AppData appData = VaadinSession.getCurrent().getAttribute(AppData.class);
		if (appData == null) {
			setContent(new ErrorView(Notification.Type.ERROR_MESSAGE, "Cannot find configuration file: " + AppData.configPATH));
			return;
		}

		APIrestful api = VaadinSession.getCurrent().getAttribute(APIrestful.class);
		if (api == null) {
			setContent(new ErrorView(Notification.Type.ERROR_MESSAGE, "API not responding at: " + APIrestful.getURI()));
			return;
		}

		SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
		if (systemInfo == null || systemInfo.getCurrentID() == null || systemInfo.getCurrentSystem().getNodes().length == 0) {
			setContent(new ErrorView(Notification.Type.HUMANIZED_MESSAGE,
					"Please setup your system, node(s) and user. When done, please refresh/reload the current page."));
			new SetupDialog();
			return;
		}

		UserInfo userInfo = new UserInfo(null);
		if (userInfo.getUsersList() == null || userInfo.getUsersList().isEmpty()) {
			setContent(new ErrorView(Notification.Type.HUMANIZED_MESSAGE,
					"Please setup your system, node(s) and user. When done, please refresh/reload the current page."));
			new SetupDialog();
			return;
		}

		UserObject userObject = VaadinSession.getCurrent().getAttribute(UserObject.class);
		if (userObject == null) {
			setContent(new LoginView(systemName, systemVersion));
		} else {
			initLayout();
			initExecutor();
		}
	}

	private void initLayout() {

		// add ICEPush
		icePush = new ICEPush();
		icePush.extend(this);
		VaadinSession.getCurrent().setAttribute(ICEPush.class, icePush);

		VerticalLayout main = new VerticalLayout();
		main.setMargin(true);
		main.setSpacing(true);
		main.setSizeFull();

		TopPanel topPanel = new TopPanel();
		main.addComponent(topPanel);
		VaadinSession.getCurrent().setAttribute(TopPanel.class, topPanel);

		overviewPanel = new OverviewPanel();
		main.addComponent(overviewPanel);
		VaadinSession.getCurrent().setAttribute(OverviewPanel.class, overviewPanel);

		tabbedPanel = new TabbedPanel();
		main.addComponent(tabbedPanel.getTabSheet());
		main.setExpandRatio(tabbedPanel.getTabSheet(), 1f);
		VaadinSession.getCurrent().setAttribute(TabbedPanel.class, tabbedPanel);

		setContent(main);

		overviewPanel.clickLayout(0);

	}

	private void initExecutor() {

		// setup scheduler that will keep refreshing the UI until the end of the session
		log("init");
		final long fDelayBetweenRuns = 15;
		Runnable runTimerTask = new RunMainTimerTask();
		mainTimerFuture = ExecutorFactory.addTimer(runTimerTask, fDelayBetweenRuns);

	}

	private final class RunMainTimerTask implements Runnable {

		public void run() {

			log("timer - run");

			VaadinSession vaadinSession = getSession();
			vaadinSession.lock();
			try {
				overviewPanel.refresh();
				tabbedPanel.refresh();
				icePush.push();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e.getMessage());

			} finally {
				vaadinSession.unlock();
			}

		}
	}

	public static void log(String aMsg) {
		//System.out.println(aMsg);
	}

}
