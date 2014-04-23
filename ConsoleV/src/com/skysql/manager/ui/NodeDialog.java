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

import java.util.HashMap;
import java.util.Map;

import com.skysql.java.Encryption;
import com.skysql.manager.api.APIrestful;
import com.skysql.manager.api.NodeInfo;
import com.skysql.manager.api.SystemInfo;
import com.skysql.manager.api.TaskRun;
import com.skysql.manager.api.UserObject;
import com.skysql.manager.ui.components.ComponentButton;
import com.skysql.manager.ui.components.ParametersLayout;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

/**
 * The Class NodeDialog is used for the create/edit node dialog.
 */
public class NodeDialog implements Window.CloseListener {

	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private Window dialogWindow;
	private HorizontalLayout buttonsBar;
	private NodeInfo nodeInfo;
	private final NodeForm nodeForm;
	private final ComponentButton button;

	/**
	 * Instantiates a new node dialog.
	 *
	 * @param nodeInfo the node info
	 * @param button the button
	 */
	public NodeDialog(NodeInfo nodeInfo, ComponentButton button) {
		this.button = button;

		String windowTitle = (nodeInfo != null) ? "Edit Node: " + nodeInfo.getName() : "Add Node";
		dialogWindow = new ModalWindow(windowTitle, (nodeInfo != null) ? "350px" : "650px");
		dialogWindow.addCloseListener(this);
		UI.getCurrent().addWindow(dialogWindow);

		buttonsBar = new HorizontalLayout();
		buttonsBar.setStyleName("buttonsBar");
		buttonsBar.setSizeFull();
		buttonsBar.setSpacing(true);
		buttonsBar.setMargin(true);
		buttonsBar.setHeight("49px");

		Label filler = new Label();
		buttonsBar.addComponent(filler);
		buttonsBar.setExpandRatio(filler, 1.0f);

		if (nodeInfo == null) {
			SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
			this.nodeInfo = new NodeInfo(systemInfo.getCurrentID(), systemInfo.getCurrentSystem().getSystemType());
			nodeForm = new NodeForm(this.nodeInfo, "Add a Node to the System: " + systemInfo.getCurrentSystem().getName());
			saveNode("Add Node");
		} else {
			this.nodeInfo = nodeInfo;
			nodeForm = new NodeForm(nodeInfo, "Edit an existing Node");
			saveNode("Save Changes");
		}

		VerticalLayout windowLayout = (VerticalLayout) dialogWindow.getContent();
		windowLayout.setSpacing(false);
		windowLayout.setMargin(false);
		windowLayout.addComponent(nodeForm);
		windowLayout.addComponent(buttonsBar);

	}

	/**
	 * Save node.
	 *
	 * @param okButtonCaption the ok button caption
	 */
	private void saveNode(final String okButtonCaption) {

		final Button okButton = new Button(okButtonCaption);
		final Button cancelButton = new Button("Cancel");
		buttonsBar.addComponent(cancelButton);
		buttonsBar.setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT);

		cancelButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				windowClose(null);
			}
		});

		okButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				if (nodeForm.validateNode()) {
					if (nodeInfo.save()) {
						if (button != null) {
							button.setName(nodeInfo.getName());
							button.setDescription(nodeInfo.ToolTip());
							if (button.isSelected()) {
								TabbedPanel tabbedPanel = VaadinSession.getCurrent().getAttribute(TabbedPanel.class);
								tabbedPanel.refresh();
							}
						} else {
							OverviewPanel overviewPanel = VaadinSession.getCurrent().getAttribute(OverviewPanel.class);
							overviewPanel.refresh();
						}
						windowClose(null);

						if (nodeForm.runConnect) {
							UserObject userObject = VaadinSession.getCurrent().getAttribute(UserObject.class);
							String userID = userObject.getUserID();
							APIrestful api = new APIrestful();
							String password = nodeForm.connectPassword.getValue();
							String sshkey = nodeForm.connectKey.getValue();
							if (!password.isEmpty() || !sshkey.isEmpty()) {
								Map<String, String> params = new HashMap<String, String>();
								Encryption encryption = new Encryption();
								if (nodeForm.usePassword) {
									params.put(ParametersLayout.PARAM_CONNECT_ROOTPASSWORD, encryption.encrypt(password, APIrestful.getKey()));
								} else {
									params.put(ParametersLayout.PARAM_CONNECT_SSHKEY, encryption.encrypt(sshkey, APIrestful.getKey()));
								}
								TaskRun taskRun = new TaskRun(nodeInfo.getParentID(), nodeInfo.getID(), userID, "connect", params, null);
							}
						}
					}
				}

			}
		});

		buttonsBar.addComponent(okButton);
		buttonsBar.setComponentAlignment(okButton, Alignment.MIDDLE_RIGHT);

	}

	/* (non-Javadoc)
	 * @see com.vaadin.ui.Window.CloseListener#windowClose(com.vaadin.ui.Window.CloseEvent)
	 */
	public void windowClose(CloseEvent e) {
		dialogWindow.close();
	}
}
