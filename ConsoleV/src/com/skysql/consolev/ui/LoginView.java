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
 * Copyright SkySQL Ab
 */

package com.skysql.consolev.ui;

import com.skysql.consolev.ConsoleUI;
import com.skysql.consolev.api.UserObject;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
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

	private TextField userName = new TextField("Username");
	private PasswordField password = new PasswordField("Password");
	private Button login = new Button("Login");

	private Button.ClickListener loginListener = new Button.ClickListener() {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		@Override
		public void buttonClick(ClickEvent event) {

			String userNameInput;
			if ((userNameInput = userName.getValue()) != null && !userNameInput.equalsIgnoreCase("")) {
				UserObject userObject = new UserObject();
				boolean success = userObject.login(userName.getValue(), password.getValue());
				if (success) {
					VaadinSession.getCurrent().setAttribute(UserObject.class, userObject);

					//Tell the UI to refresh itself - this is not the only way to do this, just one possibility
					ConsoleUI current = (ConsoleUI) UI.getCurrent();
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

	public LoginView(String name, String version) {

		setSizeFull();
		addStyleName("loginView");
		setMargin(true);

		Embedded logo = new Embedded(null, new ThemeResource("img/SkySQL.png"));
		addComponent(logo);
		setComponentAlignment(logo, Alignment.MIDDLE_CENTER);

		Embedded cloud = new Embedded(null, new ThemeResource("img/cloud_data_suite.png"));
		addComponent(cloud);
		setComponentAlignment(cloud, Alignment.MIDDLE_CENTER);

		VerticalLayout loginFormLayout = new VerticalLayout();
		loginFormLayout.addStyleName("loginForm");
		loginFormLayout.setMargin(true);
		loginFormLayout.setSpacing(true);
		addComponent(loginFormLayout);
		setComponentAlignment(loginFormLayout, Alignment.MIDDLE_CENTER);

		Label welcome = new Label("Welcome to " + name);
		welcome.setSizeUndefined();
		loginFormLayout.addComponent(welcome);
		loginFormLayout.setComponentAlignment(welcome, Alignment.TOP_CENTER);

		userName.focus();
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

		password.setImmediate(true);
		password.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {
				login.focus();
			}
		});
		loginFormLayout.addComponent(password);
		loginFormLayout.setComponentAlignment(password, Alignment.MIDDLE_CENTER);

		login.addClickListener(loginListener);
		loginFormLayout.addComponent(login);
		loginFormLayout.setComponentAlignment(login, Alignment.BOTTOM_CENTER);

		if (version != null) {
			Label versionLabel = new Label("Version " + version);
			versionLabel.setSizeUndefined();
			addComponent(versionLabel);
			setComponentAlignment(versionLabel, Alignment.BOTTOM_LEFT);
		}

	}

}
