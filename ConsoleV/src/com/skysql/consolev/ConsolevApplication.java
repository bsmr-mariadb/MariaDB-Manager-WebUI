package com.skysql.consolev;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledFuture;

import org.vaadin.artur.icepush.ICEPush;

import com.skysql.consolev.api.AppData;
import com.skysql.consolev.api.MonitorData;
import com.skysql.consolev.api.NodeInfo;
import com.skysql.consolev.api.NodeStates;
import com.skysql.consolev.api.SystemInfo;
import com.skysql.consolev.api.SystemProperties;
import com.skysql.consolev.api.UserInfo;
import com.skysql.consolev.api.UserLogin;
import com.skysql.consolev.ui.SettingsDialog;
import com.skysql.consolev.ui.TabbedPanel;
import com.vaadin.Application;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.service.ApplicationContext;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;

public class ConsolevApplication extends Application implements ApplicationContext.TransactionListener {

	private static final long serialVersionUID = 0x4C656F6E6172646FL;
	
	private static ThreadLocal<ConsolevApplication> currentApplication = new ThreadLocal<ConsolevApplication>();
	
	private static final String STATE_MASTER = "1";
	private static final String STATE_ONLINE = "2";
	private static final String MONITOR_CAPACITY = "5";
	private static final String SYSTEM_NODEID = "0";
	private static final String SYSTEM_PROPERTY_SKIP_LOGIN = "SKIP_LOGIN";
	private static final String SYSTEM_PROPERTY_VERSION = "VERSION";
	
	private static final int NUM_THREADS = 6;
	private static final boolean DONT_INTERRUPT_IF_RUNNING = false;
	private ScheduledExecutorService fScheduler = ExecutorFactory.getScheduler(NUM_THREADS);
	private ScheduledFuture<?> mainTimerFuture;

	//private final static Logger logger = Logger.getLogger(ConsolevApplication.class.getName());

	private ICEPush pusher;
	private SystemInfo systemInfo;
	private LinkedHashMap<String, String> sysProperties;
	private ArrayList<NodeInfo> nodes = new ArrayList<NodeInfo>();
	private ArrayList<Button> buttons = new ArrayList<Button>();
	private Button selectedButton;
	private TabbedPanel tabbedPanel;

	@Override
	public void init() {
		
		getContext().addTransactionListener(this);
    	
		setTheme("skystyle1");
        
		systemInfo = new SystemInfo(AppData.getGson());		
		SystemProperties systemProperties = new SystemProperties(systemInfo.getSystemID());
		SessionData sessionData = new SessionData(systemInfo, systemProperties);
		setUser(sessionData);
		pusher = sessionData.getICEPush();
		
        sysProperties = systemProperties.getProperties();
        // backdoor to use system without authentication
        if (sysProperties.containsKey(SYSTEM_PROPERTY_SKIP_LOGIN)) {
        	String skipLogin = sysProperties.get(SYSTEM_PROPERTY_SKIP_LOGIN);
        	if (skipLogin.equalsIgnoreCase("true")) {
                UserLogin userLogin = new UserLogin();
                sessionData.setUserLogin(userLogin);
            	loadProtectedResources();
            	return ;
        	}
        }
        
        String version = sysProperties.get(SYSTEM_PROPERTY_VERSION);
        setMainWindow(new LoginWindow(systemInfo.getSystemName(), version));
        
	}
	
	@Override
	public void close() {
	    System.out.println("closing");
	    if (mainTimerFuture != null)
	    	mainTimerFuture.cancel(DONT_INTERRUPT_IF_RUNNING);

		if (pusher != null)
			getMainWindow().removeComponent(pusher);
		
		super.close();
	}

	
	public void transactionStart(Application application, Object transactionData) {
		if (application == ConsolevApplication.this) {
			currentApplication.set(this);
		}		
	}

	public void transactionEnd(Application application, Object transactionData) {
		if (application == ConsolevApplication.this) {
			currentApplication.set(null);
			currentApplication.remove();
		}
	}

    public static ConsolevApplication getInstance() {
        return currentApplication.get();
    }

    public void authenticate(String username, String password) throws Exception {
    	SessionData sessionData = (SessionData)getUser();
        UserLogin userLogin = new UserLogin(sessionData.getSystemInfo().getSystemID(), username, password);
    	String userID = userLogin.getUserID();
        if (userID != null) {
        	sessionData.setUserLogin(userLogin);
        	loadProtectedResources();
            return;
        }
       
       throw new Exception("");

    }

    private void loadProtectedResources () {
        setMainWindow(initLayout(getUser()));
        initExecutor();
        buttons.get(0).click();

    }

    private Window initLayout(Object userData) {
    	final Window main;
     	    	
    	main = new Window("SkySQL Console");
 
        // add ICEPush
        main.addComponent(pusher);
        
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setSpacing(true);
        titleLayout.addStyleName("titleLayout");
        titleLayout.setSizeFull();
        main.addComponent(titleLayout);

        Embedded image = new Embedded(null, new ThemeResource("img/SkySQL.png"));
        titleLayout.addComponent(image);


        // LINKS AREA (TOP-RIGHT)
        HorizontalLayout linksLayout = new HorizontalLayout();
        linksLayout.setSpacing(true);
        titleLayout.addComponent(linksLayout);
		titleLayout.setComponentAlignment(linksLayout, Alignment.MIDDLE_RIGHT);

		String userID = ((SessionData)getUser()).getUserLogin().getUserID();
		String systemID = ((SessionData)getUser()).getSystemInfo().getSystemID();
		UserInfo userInfo = new UserInfo("dummy");
		String userName = userInfo.findNameByID(userID);
		Embedded userIcon = new Embedded(userName, new ThemeResource("img/user.png"));
    	linksLayout.addComponent(userIcon);
    	linksLayout.setComponentAlignment(userIcon, Alignment.TOP_CENTER);

    	VerticalLayout buttonsLayout = new VerticalLayout();
    	buttonsLayout.setSizeFull();
    	linksLayout.addComponent(buttonsLayout);
    	linksLayout.setComponentAlignment(buttonsLayout, Alignment.TOP_CENTER);
    	
    	// Settings
    	Button settingsButton = new SettingsDialog("Settings", buttonsLayout, systemID, userID);

    	// Logout
		Button logoutButton = new Button("Logout");
		buttonsLayout.addComponent(logoutButton);
		buttonsLayout.setComponentAlignment(logoutButton, Alignment.MIDDLE_CENTER);
        logoutButton.addListener(new Button.ClickListener() {
        	private static final long serialVersionUID = 0x4C656F6E6172646FL;
            public void buttonClick(ClickEvent event) {
                getMainWindow().getApplication().close();
            } 
        });
        
        
        //*** TOP PANEL
        // create top panel
        final Panel overviewPanel = new Panel("System Overview");
		main.addComponent(overviewPanel);

        HorizontalLayout strip = new HorizontalLayout();
        strip.addStyleName("systemPanel");
        strip.setSizeFull();
        strip.setHeight("166px");
        overviewPanel.addComponent(strip);

        // create tabbed panel
		tabbedPanel = new TabbedPanel(userData);		
        main.addComponent(tabbedPanel.getTabSheet());

        // initialize System button
		NodeInfo systemNodeInfo = new NodeInfo(systemInfo.getSystemID(), SYSTEM_NODEID);
		nodes.add(systemNodeInfo);
		
        final NativeButton button_system = new NativeButton(systemInfo.getSystemName());
        systemNodeInfo.setButton(button_system);
		button_system.setStyleName("system");
        button_system.setImmediate(true);
        button_system.setWidth("-1px");
        button_system.setHeight("-1px");
        button_system.setDescription(systemNodeInfo.ToolTip() + systemInfo.ToolTip());
		button_system.setData(systemNodeInfo);
		strip.addComponent(button_system);
		buttons.add(button_system);
		
		button_system.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;
            public void buttonClick(ClickEvent event) {
				if (selectedButton != null) {
					String styleName = selectedButton.getStyleName();
					if (styleName.contains("-selected")) {
						selectedButton.setStyleName(styleName.substring(0, styleName.indexOf("-selected")));
					}
				}
				Button button = event.getButton();
				selectedButton = button;
				button.setStyleName(button.getStyleName() + "-selected");
            	tabbedPanel.refresh((NodeInfo)button.getData());
             }
		});
		
		// initialize Node buttons
		for (String nodeID : systemInfo.getNodes()) {
			NodeInfo nodeInfo = new NodeInfo(systemInfo.getSystemID(), nodeID);
			nodes.add(nodeInfo);
			
			final NativeButton button_node = new NativeButton(nodeInfo.getName());
			nodeInfo.setButton(button_node);
			String status = nodeInfo.getStatus();
			String icon = NodeStates.getNodeStatesIcons().get(status);
			if (icon == null) {
				icon = "invalid"; // we don't have a valid icon to display for this state (DB:NodeStates) which means it's an invalid state
			}
			
			button_node.setStyleName(icon);
			button_node.setImmediate(true);
			button_node.setWidth("-1px");
			button_node.setHeight("-1px");
			button_node.setDescription(nodeInfo.ToolTip());
			button_node.setData(nodeInfo);
			strip.addComponent(button_node);
			buttons.add(button_node);
			
			button_node.addListener(new Button.ClickListener() {
				private static final long serialVersionUID = 0x4C656F6E6172646FL;
				public void buttonClick(ClickEvent event) {
					if (selectedButton != null) {
						String styleName = selectedButton.getStyleName();
						if (styleName.contains("-selected")) {
							selectedButton.setStyleName(styleName.substring(0, styleName.indexOf("-selected")));
						}
					}
					Button button = event.getButton();
					selectedButton = button;
					button.setStyleName(button.getStyleName() + "-selected");
	            	tabbedPanel.refresh((NodeInfo)button.getData());
 	            }
			});
		}
		
        //main.addComponent(StatusPanel.getStatusLayout());
        
        
        return main;
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
				
				// copies the Button object to new nodeInfo and replaces old one with it in the button Data and in the array
				Button button = node.getButton();
				newInfo.setButton(button);
				button.setData(newInfo);
				
				// carry over RunningTask(s)
				newInfo.setBackupTask(node.getBackupTask());
				newInfo.setCommandTask(node.getCommandTask());
				
				// fetch current capacity from monitor
				MonitorData monitorData = new MonitorData(MONITOR_CAPACITY, newInfo.getSystemID(), newInfo.getNodeID(), null, null, "1");
				String dataPoints[][] = monitorData.getDataPoints();
				newInfo.setCapacity((dataPoints == null) ? null : dataPoints[0][1]);
				
				nodes.set(nodes.indexOf(node), newInfo);
				
				String newName;
				if ((newName = newInfo.getName()) != null && !newName.equals(node.getName())) {
					button.setCaption(newName);
					refresh = true;
				} 
				
				String newStatus, newCapacity;
				if ( ((newStatus = newInfo.getStatus()) != null && !newStatus.equals(node.getStatus())) ||
					 ((newStatus != null) && (newCapacity = newInfo.getCapacity()) != null && !newCapacity.equals(node.getCapacity())) ) {
											
					String icon = NodeStates.getNodeStatesIcons().get(newStatus);
					if (icon == null) {
						icon = "invalid"; // we don't have a valid icon to display for this state (DB:NodeStates) which means it's an invalid state
					}

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
					
					// need to restore "selected" status into styleName for button in case it was changed by scripting
					if (button == selectedButton) {	
						if (!icon.contains("-selected")) {
							icon += "-selected";
						}
					}
					button.setStyleName(icon);
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

			tabbedPanel.refresh((NodeInfo)selectedButton.getData());

	    } 

	}
	
	private static void log(String aMsg){
		//System.out.println(aMsg);
	}		

}

class LoginWindow extends Window {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;
	private Button btnLogin = new Button("Login");
    private TextField username = new TextField ("Username");
    private PasswordField password = new PasswordField("Password");
    
    public LoginWindow(String systemName, String version) {
        super("SkySQL Cloud Data Suite");
        setName ("Login");
        initUI(systemName, version);
    }

    private void initUI(String systemName, String version) {
    	VerticalLayout layout = (VerticalLayout)this.getContent();
    	layout.setSizeFull();
        layout.setSpacing(true);
        layout.addStyleName("login");
    	Embedded logo = new Embedded(null, new ThemeResource("img/SkySQL.png"));
    	layout.addComponent(logo);
		layout.setComponentAlignment(logo, Alignment.TOP_CENTER);

    	Embedded cloud = new Embedded(null, new ThemeResource("img/cloud_data_suite.png"));
    	layout.addComponent(cloud);
		layout.setComponentAlignment(cloud, Alignment.TOP_CENTER);

		// LOGIN BOX
		VerticalLayout loginBox = new VerticalLayout();
    	layout.addComponent(loginBox);
		loginBox.setSizeUndefined();
        loginBox.setSpacing(true);
		loginBox.addStyleName("loginbox");
		layout.setComponentAlignment(loginBox, Alignment.TOP_CENTER);
		
		// welcome
        Label welcome = new Label("Welcome to " + systemName, Label.CONTENT_PREFORMATTED);
        loginBox.addComponent(welcome);

		{
	        Label spacer = new Label();
	        spacer.setWidth("20px");
	        loginBox.addComponent(spacer);
		}

        // username
		username.focus();
		username.setImmediate(true);
		username.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;
	        public void valueChange(ValueChangeEvent event) {
	            password.focus();
	            btnLogin.setClickShortcut(KeyCode.ENTER);
	        }
	    });
		loginBox.addComponent(username);
		loginBox.setComponentAlignment(username, Alignment.MIDDLE_CENTER);
		
		// password
		password.setImmediate(true);
		password.addListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;
	        public void valueChange(ValueChangeEvent event) {
	        	btnLogin.focus();
	        }
	    });
		loginBox.addComponent(password);
		loginBox.setComponentAlignment(password, Alignment.MIDDLE_CENTER);
		
		// Login button
		loginBox.addComponent(btnLogin);
		loginBox.setComponentAlignment(btnLogin, Alignment.BOTTOM_CENTER);
		btnLogin.addListener (new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;
            public void buttonClick(Button.ClickEvent event) {
                try {
                	ConsolevApplication.getInstance().authenticate((String)username.getValue(), (String)password.getValue());
                    open (new ExternalResource (ConsolevApplication.getInstance().getURL()));
                }
                catch (Exception e) {
                    showNotification("Login failed");
                    password.focus();
                }
            }
        });
		
		// Version number
        if (version != null) {
        	Label versionLabel = new Label("Version " + version);
        	layout.addComponent(versionLabel);
        	layout.setComponentAlignment(versionLabel, Alignment.BOTTOM_RIGHT);
        }
		
    }
}



