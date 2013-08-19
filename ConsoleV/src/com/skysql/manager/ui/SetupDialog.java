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
import com.skysql.manager.SystemRecord;
import com.skysql.manager.api.NodeInfo;
import com.skysql.manager.api.SystemInfo;
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

	private int nodeCount = 1;
	private Component currentForm = new VerticalLayout();
	private HorizontalLayout wrapper;
	private HorizontalLayout buttonsBar;

	public SetupDialog() {

		dialogWindow = new SetupWindow("Initial System Setup");
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
		SystemInfo systemInfo = new SystemInfo(null);
		UserInfo userInfo = new UserInfo(null);
		if (systemInfo == null || systemInfo.getCurrentID() == null) {
			inputSystem(systemInfo);
		} else if (systemInfo.getCurrentSystem().getNodes().length == 0) {
			inputNodes(systemInfo.getCurrentID());
		} else if (userInfo.getUsersList() == null || userInfo.getUsersList().size() == 0) {
			inputUser();
		} else {
			// we are done
			windowClose(null);
			VaadinSession.getCurrent().setAttribute(SystemInfo.class, systemInfo);
			VaadinSession.getCurrent().setAttribute(UserInfo.class, userInfo);
			ManagerUI current = (ManagerUI) UI.getCurrent();
			current.refreshContentBasedOnSessionData();
		}

	}

	SystemInfo systemInfo;
	SystemForm systemForm;

	private void inputSystem(SystemInfo inputSystemInfo) {
		if (inputSystemInfo == null) {
			this.systemInfo = new SystemInfo();
		} else {
			this.systemInfo = inputSystemInfo;
		}
		final SystemRecord system = new SystemRecord();
		system.setID("1"); // API requires us to create a unique, numerical ID
		systemForm = new SystemForm(system, "Add System");
		wrapper.replaceComponent(currentForm, systemForm);
		currentForm = systemForm;

		final Button finishedButton = new Button("Add System");
		buttonsBar.addComponent(finishedButton);
		buttonsBar.setComponentAlignment(finishedButton, Alignment.MIDDLE_RIGHT);

		finishedButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				if (systemForm.validateSystem()) {
					systemInfo.add(system);
					buttonsBar.removeComponent(finishedButton);
					nextForm();
				}
			}
		});

	}

	NodeInfo nodeInfo;
	NodeForm nodeForm;

	private void inputNodes(final String systemID) {
		nodeInfo = new NodeInfo(systemID);
		nodeForm = new NodeForm(nodeInfo, "Add first Node to the System");
		wrapper.replaceComponent(currentForm, nodeForm);
		currentForm = nodeForm;

		final Button addNodeButton = new Button("Add Node");
		final Button finishedButton = new Button("Finished");
		finishedButton.setEnabled(false);
		buttonsBar.addComponent(finishedButton);
		buttonsBar.setComponentAlignment(finishedButton, Alignment.MIDDLE_RIGHT);

		finishedButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				buttonsBar.removeComponent(addNodeButton);
				buttonsBar.removeComponent(finishedButton);
				nextForm();
			}
		});

		addNodeButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				if (nodeForm.validateNode()) {
					nodeInfo.setID(String.valueOf(nodeCount));
					if (nodeInfo.saveNode()) {
						finishedButton.setEnabled(true);
						nodeCount++;
						nodeInfo = new NodeInfo(systemID);
						NodeForm newNodeForm = new NodeForm(nodeInfo, "Press Finished if done, or Add Node No. " + nodeCount + " to the System");
						wrapper.replaceComponent(nodeForm, newNodeForm);
						nodeForm = newNodeForm;
						currentForm = nodeForm;
					}
				}

			}
		});

		buttonsBar.addComponent(addNodeButton);
		buttonsBar.setComponentAlignment(addNodeButton, Alignment.MIDDLE_RIGHT);

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

class SetupWindow extends Window {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	public SetupWindow(String caption) {
		setModal(true);
		setWidth("350px");
		center();
		setCaption(caption);
		VerticalLayout layout = new VerticalLayout();
		setContent(layout);
		layout.setSpacing(true);
		layout.setMargin(true);
	}
}
