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

package com.skysql.manager.ui.components;

import com.skysql.manager.ClusterComponent;
import com.skysql.manager.SystemRecord;
import com.skysql.manager.api.NodeStates;
import com.skysql.manager.ui.ComponentDialog;
import com.vaadin.event.MouseEvents;
import com.vaadin.event.MouseEvents.ClickEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class ComponentButton extends VerticalLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	public static final String COMPONENT_HEIGHT = "120px";

	private static final String ICON_MASTER = "master";
	private static final String ICON_SLAVE = "slave";

	private boolean isSelected = false, isEditable = false;
	private Embedded editButton, deleteButton;
	private VerticalLayout imageLayout;
	private Label nodeName;
	private ClusterComponent componentInfo;
	private ComponentButton thisButton;

	public ComponentButton(ClusterComponent componentInfo) {
		thisButton = this;
		this.componentInfo = componentInfo;

		addStyleName("componentButton");

		componentInfo.setButton(this);
		setData(componentInfo);

		setHeight(COMPONENT_HEIGHT);
		setWidth(componentInfo.getType() == ClusterComponent.CCType.system ? "120px" : "96px");

		imageLayout = new VerticalLayout();
		//imageLayout.setWidth(componentInfo.getType() == ClusterComponent.CCType.system ? "160px" : "96px");
		imageLayout.setHeight(COMPONENT_HEIGHT);
		imageLayout.setImmediate(true);

		if (componentInfo.getParentID() != null) {
			String icon = NodeStates.getNodeIcon(componentInfo.getSystemType(), componentInfo.getState());
			//imageLayout.addStyleName(icon);
			imageLayout.addStyleName(componentInfo.getType().toString());

			nodeName = new Label(componentInfo.getName());
			nodeName.setSizeUndefined();
			imageLayout.addComponent(nodeName);
			imageLayout.setComponentAlignment(nodeName, Alignment.BOTTOM_CENTER);
		}
		addComponent(imageLayout);
		setComponentAlignment(imageLayout, Alignment.TOP_CENTER);
		setExpandRatio(imageLayout, 1.0f);

	}

	public String getName() {
		return nodeName.getValue();
	}

	public void setName(String name) {
		nodeName.setValue(name);
	}

	public void setDescription(String description) {
		imageLayout.setDescription(description);
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;

		boolean wasSelected = getStyleName().contains("selected");
		if (isSelected && !wasSelected) {
			addStyleName("selected");
		} else if (!isSelected && wasSelected) {
			setStyleName(getStyleName().replace("selected", ""));
		}
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setIcon(String type, String status, String capacity) {
		String icon = NodeStates.getNodeIcon(componentInfo.getSystemType(), status);
		if (capacity != null && (icon.equals(ICON_MASTER) || icon.equals(ICON_SLAVE))) {

			double capacity_num = Double.parseDouble(capacity);
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

		imageLayout.setStyleName(icon);
		imageLayout.addStyleName(type);
		setSelected(isSelected);

	}

	public void setEditable(boolean editable) {
		if (editable && !this.isEditable) {
			imageLayout.setEnabled(false);

			String componentType;
			switch (componentInfo.getType()) {
			case system:
				componentType = "System";
				break;
			case node:
				componentType = "Node";
				break;
			default:
				componentType = "Unknown Component";
				break;
			}

			editButton = new Embedded(null, new ThemeResource("img/edit24.png"));
			editButton.addStyleName("editNode");
			editButton.setDescription("Edit " + componentType);
			editButton.setData(this);
			addComponent(editButton);
			editButton.addClickListener(new MouseEvents.ClickListener() {
				private static final long serialVersionUID = 0x4C656F6E6172646FL;

				public void click(ClickEvent event) {
					new ComponentDialog((ClusterComponent) thisButton.getData(), thisButton);
				}
			});

			deleteButton = new Embedded(null, new ThemeResource("img/delete24.png"));
			switch (componentInfo.getType()) {
			case system:
				deleteButton.addStyleName("deleteSystem");
				break;
			case node:
				deleteButton.addStyleName("deleteNode");
				break;
			}
			deleteButton.setDescription("Delete " + componentType);
			deleteButton.setData(this);
			addComponent(deleteButton);
			deleteButton.addClickListener(new MouseEvents.ClickListener() {
				private static final long serialVersionUID = 0x4C656F6E6172646FL;

				public void click(ClickEvent event) {
					deleteComponent((ClusterComponent) thisButton.getData());
				}
			});

		} else if (!editable && this.isEditable) {
			imageLayout.setEnabled(true);

			if (editButton != null) {
				removeComponent(editButton);
				editButton = null;
			}

			if (deleteButton != null) {
				removeComponent(deleteButton);
				deleteButton = null;
			}
		}

		this.isEditable = editable;
	}

	public void deleteComponent(final ClusterComponent clusterComponent) {

		final Window dialogWindow = new DialogWindow("Delete: " + clusterComponent.getName());
		UI.getCurrent().addWindow(dialogWindow);

		HorizontalLayout wrapper = new HorizontalLayout();
		wrapper.setWidth("100%");
		wrapper.setMargin(true);
		VerticalLayout iconLayout = new VerticalLayout();
		iconLayout.setWidth("100px");
		wrapper.addComponent(iconLayout);
		Embedded image = new Embedded(null, new ThemeResource("img/warning.png"));
		iconLayout.addComponent(image);
		VerticalLayout textLayout = new VerticalLayout();
		textLayout.setSizeFull();
		wrapper.addComponent(textLayout);
		wrapper.setExpandRatio(textLayout, 1.0f);
		Label label = new Label("WARNING: if you delete this component, all its related data will be deleted as well.");
		label.addStyleName("warning");
		textLayout.addComponent(label);
		textLayout.setComponentAlignment(label, Alignment.MIDDLE_CENTER);

		HorizontalLayout buttonsBar = new HorizontalLayout();
		buttonsBar.setStyleName("buttonsBar");
		buttonsBar.setSizeFull();
		buttonsBar.setSpacing(true);
		buttonsBar.setMargin(true);
		buttonsBar.setHeight("49px");

		Label filler = new Label();
		buttonsBar.addComponent(filler);
		buttonsBar.setExpandRatio(filler, 1.0f);

		Button cancelButton = new Button("Cancel");
		buttonsBar.addComponent(cancelButton);
		buttonsBar.setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT);

		cancelButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(Button.ClickEvent event) {
				dialogWindow.close();
			}
		});

		Button okButton = new Button("Delete");
		okButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(Button.ClickEvent event) {
				boolean success = false;
				switch (clusterComponent.getType()) {
				case system:
					success = ((SystemRecord) clusterComponent).delete();
					break;
				case node:
					success = ((com.skysql.manager.api.NodeInfo) clusterComponent).delete();
					break;
				}
				if (success) {
					dialogWindow.close();
					removeComponent(deleteButton);
					removeComponent(editButton);
					switch (clusterComponent.getType()) {
					case system:
						if (thisButton.getParent() instanceof NodesLayout) {
							NodesLayout nodesLayout = (NodesLayout) thisButton.getParent();
							nodesLayout.deleteComponent(thisButton);
						} else {
							SystemLayout systemLayout = (SystemLayout) thisButton.getParent().getParent();
							systemLayout.deleteComponent(thisButton);
						}
						break;
					case node:
						NodesLayout nodesLayout = (NodesLayout) thisButton.getParent();
						nodesLayout.deleteComponent(thisButton);
						break;
					}
				}
			}
		});
		buttonsBar.addComponent(okButton);
		buttonsBar.setComponentAlignment(okButton, Alignment.MIDDLE_RIGHT);

		VerticalLayout windowLayout = (VerticalLayout) dialogWindow.getContent();
		windowLayout.setSpacing(false);
		windowLayout.setMargin(false);
		windowLayout.addComponent(wrapper);
		windowLayout.addComponent(buttonsBar);

	}
}

class DialogWindow extends Window {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	public DialogWindow(String caption) {
		setModal(true);
		center();
		setCaption(caption);
		VerticalLayout layout = new VerticalLayout();
		setContent(layout);
		layout.setSpacing(true);
		layout.setMargin(true);
	}
}
