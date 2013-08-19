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

import com.skysql.manager.api.NodeInfo;
import com.skysql.manager.api.SystemInfo;
import com.skysql.manager.ui.components.ComponentButton;
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

public class NodeDialog implements Window.CloseListener {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private Window dialogWindow;
	private HorizontalLayout wrapper;
	private HorizontalLayout buttonsBar;
	private NodeInfo nodeInfo;
	private final NodeForm nodeForm;
	private final ComponentButton button;

	public NodeDialog(NodeInfo nodeInfo, ComponentButton button) {
		this.button = button;

		String windowTitle = (nodeInfo != null) ? "Edit Node: " + nodeInfo.getName() : "Add Node";
		dialogWindow = new ModalWindow(windowTitle, "350px");
		dialogWindow.addCloseListener(this);
		UI.getCurrent().addWindow(dialogWindow);

		wrapper = new HorizontalLayout();
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

		if (nodeInfo == null) {
			SystemInfo systemInfo = VaadinSession.getCurrent().getAttribute(SystemInfo.class);
			this.nodeInfo = new NodeInfo(systemInfo.getCurrentID());
			nodeForm = new NodeForm(this.nodeInfo, "Add a Node to the System");
			saveNode("Add Node");
		} else {
			this.nodeInfo = nodeInfo;
			nodeForm = new NodeForm(nodeInfo, "Edit an existing Node");
			saveNode("Save Changes");
		}

		wrapper.addComponent(nodeForm);

	}

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
					if (nodeInfo.saveNode()) {
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
					}
				}

			}
		});

		buttonsBar.addComponent(okButton);
		buttonsBar.setComponentAlignment(okButton, Alignment.MIDDLE_RIGHT);

	}

	public void windowClose(CloseEvent e) {
		dialogWindow.close();
	}
}
