package com.skysql.consolev.ui;

import com.skysql.consolev.SessionData;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

public class SettingsDialog implements Window.CloseListener {

	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	Window dialogWindow;
	Button openButton;
	Button closebutton;
	Label explanation;
	private TabSheet tabsheet;
	private HorizontalLayout accountTab, backupsTab, monitorsTab;
	private String selectedTab;
	private String userID;
	private String systemID;

	private ClickListener settingsDialogOpenListener = new ClickListener() {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		public void buttonClick(Button.ClickEvent event) {

			dialogWindow = new DialogWindow("Settings");
			VerticalLayout windowLayout = (VerticalLayout) dialogWindow.getContent();

			UI.getCurrent().addWindow(dialogWindow);
			// dialogWindow.addListener(this);

			tabsheet = new TabSheet();
			tabsheet.setImmediate(true);

			// Account Tab
			accountTab = new HorizontalLayout();
			AccountSettings accountSettings = new AccountSettings(accountTab, systemID, userID);
			Tab tab = tabsheet.addTab(accountTab, "Account");
			if (selectedTab != null && selectedTab.equalsIgnoreCase("Account")) {
				tabsheet.setSelectedTab(tab);
			}

			// Backups Tab
			backupsTab = new HorizontalLayout();
			BackupSettings backupSettings = new BackupSettings(backupsTab, systemID);
			tab = tabsheet.addTab(backupsTab, "Backups");
			if (selectedTab != null && selectedTab.equalsIgnoreCase("Backups")) {
				tabsheet.setSelectedTab(tab);
			}

			// Monitors Tab
			monitorsTab = new HorizontalLayout();
			monitorsTab.setSizeFull();
			MonitorsSettings monitorsSettings = new MonitorsSettings(monitorsTab, systemID);
			tab = tabsheet.addTab(monitorsTab, "Monitors");
			if (selectedTab != null && selectedTab.equalsIgnoreCase("Monitors")) {
				tabsheet.setSelectedTab(tab);
			}

			windowLayout.addComponent(tabsheet);
		}
	};

	public SettingsDialog(String label) {

		SessionData userData = VaadinSession.getCurrent().getAttribute(SessionData.class);
		this.userID = userData.getUserLogin().getUserID();
		this.systemID = userData.getSystemInfo().getSystemID();

		openButton = new Button(label, settingsDialogOpenListener);

	}

	public SettingsDialog(String label, String selectedTab) {

		SessionData userData = VaadinSession.getCurrent().getAttribute(SessionData.class);
		this.userID = userData.getUserLogin().getUserID();
		this.systemID = userData.getSystemInfo().getSystemID();

		this.selectedTab = selectedTab;

		openButton = new Button(label, settingsDialogOpenListener);

	}

	public Button getButton() {
		return (openButton);
	}

	/** Handle Close button click and close the window. */
	public void closeButtonClick(Button.ClickEvent event) {
		/* Windows are managed by the application object. */
		dialogWindow.close();
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
		setWidth("500px");
		center();
		setCaption(caption);
		VerticalLayout layout = new VerticalLayout();
		setContent(layout);
		layout.setSpacing(true);
		layout.setMargin(true);
	}
}
