package com.skysql.consolev;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.vaadin.artur.icepush.ICEPush;

import com.skysql.consolev.api.AppData;
import com.skysql.consolev.api.MonitorData;
import com.skysql.consolev.api.Monitors;
import com.skysql.consolev.api.NodeInfo;
import com.skysql.consolev.api.NodeStates;
import com.skysql.consolev.api.SystemInfo;
import com.skysql.consolev.api.SystemProperties;
import com.skysql.consolev.api.UserInfo;
import com.skysql.consolev.api.UserLogin;
import com.skysql.consolev.ui.SettingsDialog;
import com.skysql.consolev.ui.TabbedPanel;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("skystyle1")
@Title("SkySQL Console")
public class ConsoleUI extends UI {

	private static final String STATE_MASTER = "1";
	private static final String STATE_ONLINE = "2";
	private static final String SYSTEM_NODEID = "0";
	private static final String SYSTEM_PROPERTY_SKIP_LOGIN = "SKIP_LOGIN";
	private static final String SYSTEM_PROPERTY_VERSION = "VERSION";

	private static final int NUM_THREADS = 6;
	private static final boolean DONT_INTERRUPT_IF_RUNNING = false;
	private ScheduledExecutorService fScheduler = ExecutorFactory.getScheduler(NUM_THREADS);
	private ScheduledFuture<?> mainTimerFuture;

	private ICEPush pusher;
	private SystemInfo systemInfo;
	private LinkedHashMap<String, String> sysProperties;
	private ArrayList<NodeInfo> nodes = new ArrayList<NodeInfo>();
	private ArrayList<VerticalLayout> buttons = new ArrayList<VerticalLayout>();
	private VerticalLayout selectedButton;
	private TabbedPanel tabbedPanel;

	@Override
	public void init(VaadinRequest request) {

		systemInfo = new SystemInfo(AppData.getGson());
		SystemProperties systemProperties = new SystemProperties(systemInfo.getSystemID());

		SessionData sessionData = new SessionData(systemInfo, systemProperties);
		VaadinSession.getCurrent().setAttribute(SessionData.class, sessionData);

		pusher = sessionData.getICEPush();

		sysProperties = systemProperties.getProperties();
		// backdoor to use system without authentication
		/***
		 * if (sysProperties.containsKey(SYSTEM_PROPERTY_SKIP_LOGIN)) { String
		 * skipLogin = sysProperties.get(SYSTEM_PROPERTY_SKIP_LOGIN); if
		 * (skipLogin.equalsIgnoreCase("true")) { UserLogin userLogin = new
		 * UserLogin(); sessionData.setUserLogin(userLogin);
		 * loadProtectedResources(); return ; } }
		 * 
		 * String version = sysProperties.get(SYSTEM_PROPERTY_VERSION);
		 * //setMainWindow(new LoginWindow(systemInfo.getSystemName(),
		 * version));
		 ***/
		UserLogin userLogin = new UserLogin();
		sessionData.setUserLogin(userLogin);
		loadProtectedResources();

	}

	@Override
	public void close() {
		System.out.println("closing");
		if (mainTimerFuture != null)
			mainTimerFuture.cancel(DONT_INTERRUPT_IF_RUNNING);

		super.close();
	}

	private void loadProtectedResources() {
		initLayout();
		initExecutor();

	}

	private void initLayout() {
		setSizeFull();

		VerticalLayout main = new VerticalLayout();
		main.setMargin(true);
		main.setSpacing(true);
		main.setSizeFull();

		// add ICEPush
		pusher.extend(this);

		HorizontalLayout titleLayout = new HorizontalLayout();
		titleLayout.setSpacing(true);
		titleLayout.addStyleName("titleLayout");
		titleLayout.setWidth("100%");
		main.addComponent(titleLayout);

		Embedded image = new Embedded(null, new ThemeResource("img/SkySQL.png"));
		titleLayout.addComponent(image);

		// LINKS AREA (TOP-RIGHT)
		HorizontalLayout linksLayout = new HorizontalLayout();
		linksLayout.setSizeUndefined();
		linksLayout.setSpacing(true);
		titleLayout.addComponent(linksLayout);
		titleLayout.setComponentAlignment(linksLayout, Alignment.MIDDLE_RIGHT);

		SessionData userData = VaadinSession.getCurrent().getAttribute(SessionData.class);
		String userID = userData.getUserLogin().getUserID();
		String systemID = userData.getSystemInfo().getSystemID();
		UserInfo userInfo = new UserInfo("dummy");
		String userName = userInfo.findNameByID(userID);
		Embedded userIcon = new Embedded(userName, new ThemeResource("img/user.png"));
		linksLayout.addComponent(userIcon);
		linksLayout.setComponentAlignment(userIcon, Alignment.TOP_CENTER);

		VerticalLayout buttonsLayout = new VerticalLayout();
		buttonsLayout.setSizeUndefined();
		linksLayout.addComponent(buttonsLayout);
		linksLayout.setComponentAlignment(buttonsLayout, Alignment.TOP_CENTER);

		// Settings
		SettingsDialog settingsDialog = new SettingsDialog("Settings");
		Button settingsButton = settingsDialog.getButton();
		buttonsLayout.addComponent(settingsButton);

		// Logout
		Button logoutButton = new Button("Logout");
		logoutButton.setSizeUndefined();
		buttonsLayout.addComponent(logoutButton);
		buttonsLayout.setComponentAlignment(logoutButton, Alignment.MIDDLE_CENTER);
		logoutButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				// getMainWindow().getApplication().close();
			}
		});

		// *** TOP PANEL
		final Panel overviewPanel = new Panel();
		overviewPanel.setHeight("176px");
		main.addComponent(overviewPanel);

		HorizontalLayout strip = new HorizontalLayout();
		strip.addStyleName("systemPanel");
		strip.setMargin(true);
		strip.setWidth("100%");
		overviewPanel.setContent(strip);

		// create tabbed panel
		tabbedPanel = new TabbedPanel();
		main.addComponent(tabbedPanel.getTabSheet());
		main.setExpandRatio(tabbedPanel.getTabSheet(), 1f);

		// initialize System button
		NodeInfo systemNodeInfo = new NodeInfo(systemInfo.getSystemID(), SYSTEM_NODEID);
		nodes.add(systemNodeInfo);

		// systemInfo.getSystemName()
		selectedButton = createButton(strip, systemNodeInfo);
		clickLayout(selectedButton);

		// initialize Node buttons
		for (String nodeID : systemInfo.getNodes()) {
			NodeInfo nodeInfo = new NodeInfo(systemInfo.getSystemID(), nodeID);
			nodes.add(nodeInfo);

			createButton(strip, nodeInfo);

		}

		setContent(main);
	}

	private VerticalLayout createButton(HorizontalLayout strip, NodeInfo nodeInfo) {
		final VerticalLayout button_node = new VerticalLayout();
		button_node.setWidth(nodeInfo.getType().equalsIgnoreCase("system") ? "128px" : "96px");
		button_node.setHeight("120px");
		Label nodeName = new Label(nodeInfo.getName());
		nodeName.setSizeUndefined();
		button_node.addComponent(nodeName);
		button_node.setComponentAlignment(nodeName, Alignment.BOTTOM_CENTER);

		nodeInfo.setButton(button_node);

		button_node.addStyleName(nodeInfo.getType());
		String status = nodeInfo.getStatus();
		String icon = NodeStates.getNodeIcon(status);
		button_node.addStyleName(icon);
		button_node.setImmediate(true);
		button_node.setDescription(nodeInfo.ToolTip());
		button_node.setData(nodeInfo);
		strip.addComponent(button_node);
		buttons.add(button_node);

		button_node.addLayoutClickListener(new LayoutClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			@Override
			public void layoutClick(LayoutClickEvent event) {
				clickLayout(button_node);
			}
		});

		return button_node;
	}

	private void clickLayout(final VerticalLayout button_node) {
		if (selectedButton != null) {
			String styleName = selectedButton.getStyleName();
			if (styleName.contains("selected")) {
				selectedButton.setStyleName(styleName.replace("selected", ""));
			}
		}

		selectedButton = button_node;
		button_node.addStyleName("selected");
		NodeInfo nodeInfo = (NodeInfo) button_node.getData();
		SessionData userData = VaadinSession.getCurrent().getAttribute(SessionData.class);
		userData.setNodeInfo(nodeInfo);
		tabbedPanel.refresh(nodeInfo);
	}

	private void initExecutor() {
		final long fInitialDelay = 0;
		final long fDelayBetweenRuns = 10;

		// setup scheduler that will keep refreshing the UI forever
		Runnable runTimerTask = new RunMainTimerTask();
		mainTimerFuture = fScheduler.scheduleWithFixedDelay(runTimerTask, fInitialDelay, fDelayBetweenRuns, TimeUnit.SECONDS);

	}

	private final class RunMainTimerTask implements Runnable {
		long f = 0;

		public void run() {
			boolean refresh = false;

			log("main timer " + f++);

			for (NodeInfo node : nodes) {
				NodeInfo newInfo = new NodeInfo(node.getSystemID(), node.getNodeID());

				// copies the Button object to new nodeInfo and replaces old one
				// with it in the button Data and in the array
				VerticalLayout button = node.getButton();
				newInfo.setButton(button);
				button.setData(newInfo);

				// carry over RunningTask(s)
				newInfo.setBackupTask(node.getBackupTask());
				newInfo.setCommandTask(node.getCommandTask());

				// fetch current capacity from monitor
				MonitorData monitorData = new MonitorData(Monitors.getMonitor(Monitors.MONITOR_CAPACITY), newInfo.getSystemID(), newInfo.getNodeID(), null,
						null, "1");
				String dataPoints[][] = monitorData.getDataPoints();
				newInfo.setCapacity((dataPoints == null) ? null : dataPoints[0][1]);

				nodes.set(nodes.indexOf(node), newInfo);

				String newName;
				if ((newName = newInfo.getName()) != null && !newName.equals(node.getName())) {
					Label buttonLabel = (Label) button.getComponent(0);
					buttonLabel.setValue(newName);
					refresh = true;
				}

				String newStatus, newCapacity;
				if (((newStatus = newInfo.getStatus()) != null && !newStatus.equals(node.getStatus()))
						|| ((newStatus != null) && (newCapacity = newInfo.getCapacity()) != null && !newCapacity.equals(node.getCapacity()))) {

					String icon = NodeStates.getNodeIcon(newStatus);

					if ((newCapacity = newInfo.getCapacity()) != null && (newInfo.getStatus().equals(STATE_MASTER) || newInfo.getStatus().equals(STATE_ONLINE))) {
						int capacity_num = Integer.parseInt(newCapacity);
						if (capacity_num > 0 && capacity_num < 20)
							icon += "-20";
						else if (capacity_num < 40)
							icon += "-40";
						else if (capacity_num < 60)
							icon += "-60";
						else if (capacity_num < 80)
							icon += "-80";
						else if (capacity_num <= 100)
							icon += "-100";
					}

					button.setStyleName(icon);
					button.addStyleName(node.getType());
					if (button == selectedButton) {
						button.addStyleName("selected");
					}
					refresh = true;
				}

				String newTask = newInfo.getTask();
				String oldTask = node.getTask();
				if (((newTask == null) && (oldTask != null)) || (newTask != null) && ((oldTask == null) || (!newTask.equals(oldTask)))) {
					refresh = true;
				}

				if (refresh) {
					if (node.getNodeID().equalsIgnoreCase(SYSTEM_NODEID))
						button.setDescription(node.ToolTip() + systemInfo.ToolTip());
					else
						button.setDescription(node.ToolTip());
				}

			} // for all nodes

			tabbedPanel.refresh((NodeInfo) selectedButton.getData());

			pusher.push();
		}
	}

	private static void log(String aMsg) {
		// System.out.println(aMsg);
	}

}
