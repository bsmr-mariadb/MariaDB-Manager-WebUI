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

package com.skysql.manager.ui;

import com.skysql.manager.AboutRecord;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.api.BackupStates;
import com.skysql.manager.api.CommandStates;
import com.skysql.manager.api.NodeStates;
import com.skysql.manager.api.Steps;
import com.skysql.manager.api.SystemTypes;
import com.skysql.manager.api.UserObject;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class LoginView extends VerticalLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private UpdaterThread updaterThread;

	private TextField userName = new TextField();
	private PasswordField password = new PasswordField();
	final private Button login = new Button("Sign In");

	private Button.ClickListener loginListener = new Button.ClickListener() {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		@Override
		public void buttonClick(ClickEvent event) {

			String userNameInput;
			if ((userNameInput = userName.getValue()) != null && !userNameInput.equals("")) {
				UserObject userObject = new UserObject();
				boolean success = userObject.login(userName.getValue().trim(), password.getValue());
				if (success) {
					getSession().setAttribute(UserObject.class, userObject);

					//Tell the UI to refresh itself - this is not the only way to do this, just one possibility
					ManagerUI current = (ManagerUI) UI.getCurrent();
					current.refreshContentBasedOnSessionData();
				} else {
					Notification.show("Login failed");
					userName.focus();
				}
			} else {
				Notification.show("Login failed");
				userName.focus();
			}
		}

	};

	public LoginView(AboutRecord aboutRecord) {

		setSizeFull();
		setMargin(true);
		addStyleName("loginView");

		Embedded logo = new Embedded(null, new ThemeResource("img/loginlogo.png"));
		addComponent(logo);
		setComponentAlignment(logo, Alignment.BOTTOM_CENTER);

		VerticalLayout loginBox = new VerticalLayout();
		loginBox.addStyleName("loginBox");
		loginBox.setSizeUndefined();
		loginBox.setMargin(true);
		loginBox.setSpacing(true);
		addComponent(loginBox);
		setComponentAlignment(loginBox, Alignment.MIDDLE_CENTER);

		//		HorizontalLayout featuresLayout = new HorizontalLayout();
		//		Embedded cloud = new Embedded(null, new ThemeResource("img/productlogo.png"));
		//		featuresLayout.addComponent(cloud);
		//		featuresLayout.setComponentAlignment(cloud, Alignment.MIDDLE_CENTER);
		//		addComponent(featuresLayout);
		//		setComponentAlignment(featuresLayout, Alignment.MIDDLE_CENTER);

		VerticalLayout loginFormLayout = new VerticalLayout();
		loginFormLayout.addStyleName("loginForm");
		loginFormLayout.setMargin(true);
		loginFormLayout.setSpacing(true);
		loginBox.addComponent(loginFormLayout);
		loginBox.setComponentAlignment(loginFormLayout, Alignment.MIDDLE_CENTER);

		//		Label welcome = new Label("Welcome to " + name);
		//		welcome.setSizeUndefined();
		//		loginFormLayout.addComponent(welcome);
		//		loginFormLayout.setComponentAlignment(welcome, Alignment.TOP_CENTER);

		// userName.focus();
		userName.setStyleName("loginControl");
		userName.setInputPrompt("Username");
		userName.setImmediate(true);
		userName.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {
				password.focus();
				login.setClickShortcut(KeyCode.ENTER);
			}
		});
		loginFormLayout.addComponent(userName);
		loginFormLayout.setComponentAlignment(userName, Alignment.MIDDLE_CENTER);

		// spacer
		loginFormLayout.addComponent(new Label(""));

		password.setStyleName("loginControl");
		password.setInputPrompt("Password");
		password.setImmediate(true);
		password.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {
				login.focus();
			}
		});
		loginFormLayout.addComponent(password);
		loginFormLayout.setComponentAlignment(password, Alignment.MIDDLE_CENTER);

		// spacer
		loginFormLayout.addComponent(new Label(" "));

		login.setStyleName("loginControl");
		login.setEnabled(false);

		loginFormLayout.addComponent(login);
		loginFormLayout.setComponentAlignment(login, Alignment.BOTTOM_CENTER);

		String versionString = "GUI Version: " + aboutRecord.getVersionGUI() + "<br/>API Version: " + aboutRecord.getVersionAPI() + "<br/>Monitor Version: "
				+ aboutRecord.getVersionMonitor() + "<br/>AddOns Version: " + aboutRecord.getVersionAddOns();
		System.err.println(versionString);
		Label versionLabel = new Label(versionString, ContentMode.HTML);
		versionLabel.setSizeUndefined();
		addComponent(versionLabel);
		setComponentAlignment(versionLabel, Alignment.BOTTOM_LEFT);

		preload();

	}

	public void preload() {

		ManagerUI.log("LoginView preload()");
		updaterThread = new UpdaterThread(updaterThread);
		updaterThread.start();
		ManagerUI.log("LoginView after preload()");

	}

	class UpdaterThread extends Thread {
		UpdaterThread oldUpdaterThread;
		volatile boolean flagged = false;

		UpdaterThread(UpdaterThread oldUpdaterThread) {
			this.oldUpdaterThread = oldUpdaterThread;
		}

		@Override
		public void run() {
			if (oldUpdaterThread != null && oldUpdaterThread.isAlive()) {
				ManagerUI.log("LoginView - Old thread is alive: " + oldUpdaterThread);
				oldUpdaterThread.flagged = true;
				oldUpdaterThread.interrupt();
				try {
					ManagerUI.log("LoginView - Before Join");
					oldUpdaterThread.join();
					ManagerUI.log("LoginView - After Join");
				} catch (InterruptedException iex) {
					ManagerUI.log("LoginView - Interrupted Exception");
					return;
				}

			}

			ManagerUI.log("LoginView - UpdaterThread.this: " + this);

			// pre-load all global values
			BackupStates.load();
			CommandStates.load();
			Steps.load();
			NodeStates.load();
			SystemTypes.load();

			// enable Login button when all is done
			login.addClickListener(loginListener);
			login.setEnabled(true);

		}
	}

}
