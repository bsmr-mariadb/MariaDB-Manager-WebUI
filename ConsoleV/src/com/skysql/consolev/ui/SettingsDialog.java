package com.skysql.consolev.ui;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

public class SettingsDialog extends Button implements Window.CloseListener {

	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	Window mainWindow;
    Window dialogWindow; 
    Button openButton; 
    Button closebutton;
    Label  explanation;
	private TabSheet tabsheet;
	private HorizontalLayout accountTab, backupsTab, monitorsTab;
	private String userID;
	private String systemID;
	
    public SettingsDialog(String label, VerticalLayout layout, String systemID, String userID) {
    	mainWindow = layout.getWindow();
    	this.userID = userID;
    	this.systemID = systemID;
    	
    	openButton = new Button(label, this, "openButtonClick");
    	layout.addComponent(openButton);
    	layout.setComponentAlignment(openButton, Alignment.MIDDLE_CENTER);    	

    }
    
    public void openButtonClick(Button.ClickEvent event) {
    	dialogWindow = new DialogWindow("Settings");
    	mainWindow.addWindow(dialogWindow);
        dialogWindow.addListener(this);

        tabsheet = new TabSheet();
        tabsheet.setImmediate(true);
        
        //  Account Tab
        accountTab = new HorizontalLayout();
        AccountSettings accountSettings = new AccountSettings(accountTab, systemID, userID);
        tabsheet.addTab(accountTab).setCaption("Account");
        
        //  Backups Tab
        backupsTab = new HorizontalLayout();
        BackupSettings backupSettings = new BackupSettings(backupsTab, systemID);
        tabsheet.addTab(backupsTab).setCaption("Backups");
        
        //  Monitors Tab
        monitorsTab = new HorizontalLayout();
        MonitorSettings monitorSettings = new MonitorSettings(monitorsTab, systemID);
        tabsheet.addTab(monitorsTab).setCaption("Monitors");
        
        dialogWindow.addComponent(tabsheet);

    }

    /** Handle Close button click and close the window. */
    public void closeButtonClick(Button.ClickEvent event) {
        /* Windows are managed by the application object. */
    	mainWindow.removeWindow(dialogWindow);
    }

    /** In case the window is closed otherwise. */
    public void windowClose(CloseEvent e) {
       // anything special goes here
    } 
}

class DialogWindow extends Window {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	public DialogWindow(String caption) {
        setModal(true);
        setWidth("400px");
        center();
        setCaption(caption);
        VerticalLayout layout = (VerticalLayout)getContent();
    	layout.setSpacing(true);
    	layout.setMargin(true);
    }
}

