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
 * Copyright 2012-2014 SkySQL Corporation Ab
 */

package com.skysql.manager.ui;

import com.skysql.manager.api.UserInfo;
import com.skysql.manager.api.UserObject;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

public class UserDialog implements Window.CloseListener {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private Window dialogWindow;
	private HorizontalLayout buttonsBar;
	private Button commitButton;
	private final UserInfo userInfo;
	private final UserObject userObject;
	private final UserForm userForm;
	private final UsersSettings usersSettings;
	private boolean isAdding = false;

	public UserDialog(final UserInfo userInfo, final UserObject userObject, final UsersSettings usersSettings) {
		this.userInfo = userInfo;
		this.usersSettings = usersSettings;

		String windowTitle = (userObject != null) ? "Edit User: " + userObject.getName() : "Add User";
		dialogWindow = new ModalWindow(windowTitle, "350px");
		dialogWindow.addCloseListener(this);
		UI.getCurrent().addWindow(dialogWindow);

		HorizontalLayout wrapper = new HorizontalLayout();
		wrapper.setWidth("100%");
		wrapper.setMargin(true);

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

		commitButton = new Button();

		if (userObject == null) {
			isAdding = true;
			this.userObject = new UserObject();
			userForm = new UserForm(userInfo, this.userObject, "Add a new User", commitButton);
			saveUser("Add User");

		} else {
			this.userObject = userObject;
			userForm = new UserForm(userInfo, userObject, "Edit an existing User", commitButton);
			saveUser("Save Changes");
		}

		wrapper.addComponent(userForm);

	}

	private void saveUser(final String commitButtonCaption) {

		final Button cancelButton = new Button("Cancel");
		buttonsBar.addComponent(cancelButton);
		buttonsBar.setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT);
		cancelButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				windowClose(null);
			}
		});

		commitButton.setCaption(commitButtonCaption);
		commitButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				if (userForm.validateUser()) {
					boolean success = userInfo.setUser(userObject);
					if (success) {
						if (isAdding) {
							usersSettings.addToSelect(userObject.getUserID());
						} else {
							usersSettings.updateSelect(userObject.getUserID());
						}
						windowClose(null);
					} else {
						return;
					}

				}

			}
		});

		buttonsBar.addComponent(commitButton);
		buttonsBar.setComponentAlignment(commitButton, Alignment.MIDDLE_RIGHT);

	}

	public void windowClose(CloseEvent e) {
		dialogWindow.close();
	}
}
