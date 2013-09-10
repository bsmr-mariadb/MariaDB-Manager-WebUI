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

import com.skysql.manager.ManagerUI;
import com.skysql.manager.api.UserInfo;
import com.skysql.manager.api.UserObject;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

public class SetupDialog implements Window.CloseListener {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	Window dialogWindow;
	Button openButton;
	Button closebutton;

	private Component currentForm = new VerticalLayout();
	private HorizontalLayout wrapper;
	private HorizontalLayout buttonsBar;

	public SetupDialog() {

		dialogWindow = new ModalWindow("Initial System Setup", "350px");
		dialogWindow.addCloseListener(this);
		UI.getCurrent().addWindow(dialogWindow);

		wrapper = new HorizontalLayout();
		wrapper.setWidth("100%");
		wrapper.setMargin(true);

		wrapper.addComponent(currentForm);

		buttonsBar = new HorizontalLayout();
		buttonsBar.setStyleName("buttonsBar");
		buttonsBar.setSizeFull();
		buttonsBar.setSpacing(true);
		buttonsBar.setMargin(true);
		buttonsBar.setHeight("49px");

		Label filler = new Label();
		buttonsBar.addComponent(filler);
		buttonsBar.setExpandRatio(filler, 1.0f);

		VerticalLayout windowLayout = (VerticalLayout) dialogWindow.getContent();
		windowLayout.setSpacing(false);
		windowLayout.setMargin(false);
		windowLayout.addComponent(wrapper);
		windowLayout.addComponent(buttonsBar);

		nextForm();

	}

	int i = 0;

	private void nextForm() {
		UserInfo userInfo = new UserInfo(null);
		if (userInfo == null || userInfo.getUsersList() == null || userInfo.getUsersList().size() == 0) {
			inputUser();
		} else {
			// we are done
			windowClose(null);
			VaadinSession.getCurrent().setAttribute(UserInfo.class, userInfo);
			ManagerUI current = (ManagerUI) UI.getCurrent();
			current.refreshContentBasedOnSessionData();
		}

	}

	UserInfo userInfo;
	UserForm userForm;

	private void inputUser() {

		final UserObject user = new UserObject();
		userForm = new UserForm(user, "Add User to the System");
		wrapper.replaceComponent(currentForm, userForm);
		currentForm = userForm;

		final Button finishedButton = new Button("Add User");
		buttonsBar.addComponent(finishedButton);
		buttonsBar.setComponentAlignment(finishedButton, Alignment.MIDDLE_RIGHT);

		finishedButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				if (userForm.validateUser() && user.set()) {
					VaadinSession.getCurrent().setAttribute(UserObject.class, user);
					nextForm();
				}
			}
		});

	}

	public void windowClose(CloseEvent e) {
		dialogWindow.close();
	}
}
