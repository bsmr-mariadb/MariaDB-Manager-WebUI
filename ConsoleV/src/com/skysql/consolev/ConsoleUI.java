package com.skysql.consolev;

import java.util.LinkedHashMap;
import java.util.concurrent.ScheduledFuture;

import org.vaadin.artur.icepush.ICEPush;

import com.skysql.consolev.api.SystemInfo;
import com.skysql.consolev.api.UserObject;
import com.skysql.consolev.ui.LoginView;
import com.skysql.consolev.ui.OverviewPanel;
import com.skysql.consolev.ui.TabbedPanel;
import com.skysql.consolev.ui.TopPanel;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("skystyle1")
@Title("SkySQL Console")
public class ConsoleUI extends UI {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private ScheduledFuture<?> mainTimerFuture;

	private ICEPush icePush;
	private OverviewPanel overviewPanel;
	private TabbedPanel tabbedPanel;
	private String systemName, systemVersion;

	@Override
	public void init(VaadinRequest request) {

		icePush = new ICEPush();
		VaadinSession.getCurrent().setAttribute(ICEPush.class, icePush);

		SystemInfo systemInfo = new SystemInfo(null);
		VaadinSession.getCurrent().setAttribute(SystemInfo.class, systemInfo);

		LinkedHashMap<String, String> properties = systemInfo.getProperties();
		// use system without user authentication
		if (properties.containsKey(SystemInfo.PROPERTY_SKIPLOGIN)) {
			String skipLogin = properties.get(SystemInfo.PROPERTY_SKIPLOGIN);
			if (skipLogin != null && skipLogin.equalsIgnoreCase("true")) {
				UserObject userObject = new UserObject("0", "Debugging");
				VaadinSession.getCurrent().setAttribute(UserObject.class, userObject);
			}
		}

		systemName = systemInfo.getName();
		systemVersion = properties.get(SystemInfo.PROPERTY_VERSION);

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
		 *  To force our application to reuse the same UI instance, we can add the @PreserveOnRefresh-annotaion to our UI class
		 */
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
		icePush.extend(this);

		VerticalLayout main = new VerticalLayout();
		main.setMargin(true);
		main.setSpacing(true);
		main.setSizeFull();

		TopPanel topPanel = new TopPanel();
		main.addComponent(topPanel);

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
		final long fDelayBetweenRuns = 10;
		Runnable runTimerTask = new RunMainTimerTask();
		mainTimerFuture = ExecutorFactory.addTimer(runTimerTask, fDelayBetweenRuns);

	}

	private final class RunMainTimerTask implements Runnable {

		public void run() {

			VaadinSession vaadinSession = getSession();
			vaadinSession.lock();
			try {
				overviewPanel.refresh();
				tabbedPanel.refresh();
				icePush.push();
			} finally {
				vaadinSession.unlock();
			}

		}
	}

	private static void log(String aMsg) {
		// System.out.println(aMsg);
	}

}
