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

package com.skysql.manager.ui.components;

import java.util.Arrays;
import java.util.List;

import com.skysql.manager.ClusterComponent;
import com.skysql.manager.SystemRecord;
import com.skysql.manager.TaskRecord;
import com.skysql.manager.api.CommandStates;
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

	public static final float COMPONENT_HEIGHT = 70;
	private static final float NODE_WIDTH = 50;
	private static final float SYSTEM_WIDTH = 69;
	private static final List<String> capacityStates = Arrays.asList("master", "slave", "joined", "synced", "donor");

	private boolean isSelected = false, isEditable = false;
	private Embedded editButton, deleteButton;
	private VerticalLayout imageLayout;
	private Label nameLabel, commandLabel;
	private ClusterComponent componentInfo;
	private ComponentButton thisButton;
	private Embedded info, alert;
	private ThemeResource cancelledResource = new ThemeResource("img/cancelled.png");
	private ThemeResource errorResource = new ThemeResource("img/alert.png");
	private ThemeResource runningResource = new ThemeResource("img/running.gif");

	public ComponentButton(ClusterComponent componentInfo) {
		thisButton = this;
		this.componentInfo = componentInfo;

		addStyleName("componentButton");

		componentInfo.setButton(this);
		setData(componentInfo);

		setHeight(COMPONENT_HEIGHT + 4, Unit.PIXELS);
		float componentWidth = (componentInfo.getType() == ClusterComponent.CCType.system) ? SYSTEM_WIDTH : NODE_WIDTH;
		setWidth(componentWidth + 8, Unit.PIXELS);

		imageLayout = new VerticalLayout();
		imageLayout.setHeight(COMPONENT_HEIGHT + 4, Unit.PIXELS);
		imageLayout.setWidth(componentWidth, Unit.PIXELS);
		//imageLayout.setMargin(new MarginInfo(true, true, false, true));
		imageLayout.setImmediate(true);

		if (componentInfo.getParentID() != null) {
			String icon = null;
			switch (componentInfo.getType()) {
			case system:
				icon = "system";
				break;

			case node:
				icon = NodeStates.getNodeIcon(componentInfo.getSystemType(), componentInfo.getState());
				break;

			default:
				// unknown component type
				break;
			}
			imageLayout.addStyleName(icon);
			//imageLayout.addStyleName(componentInfo.getType().toString());

			commandLabel = new Label();
			commandLabel.setSizeUndefined();
			imageLayout.addComponent(commandLabel);
			imageLayout.setComponentAlignment(commandLabel, Alignment.TOP_LEFT);
			//imageLayout.setExpandRatio(commandLabel, 1.0f);
			//				NodeInfo nodeInfo = (NodeInfo) componentInfo;
			//				TaskRecord taskRecord = nodeInfo.getTask();
			//				setCommandLabel(taskRecord);

			Label padding = new Label("");
			imageLayout.addComponent(padding);
			imageLayout.setComponentAlignment(padding, Alignment.MIDDLE_CENTER);

			HorizontalLayout iconsStrip = new HorizontalLayout();
			iconsStrip.addStyleName("componentInfo");
			iconsStrip.setWidth(componentInfo.getType() == ClusterComponent.CCType.node ? "60px" : "76px");
			imageLayout.addComponent(iconsStrip);
			imageLayout.setComponentAlignment(iconsStrip, Alignment.MIDDLE_CENTER);

			info = new Embedded(null, new ThemeResource("img/info.png"));
			iconsStrip.addComponent(info);
			iconsStrip.setComponentAlignment(info, Alignment.MIDDLE_LEFT);

			alert = new Embedded();
			alert.setVisible(false);
			iconsStrip.addComponent(alert);
			iconsStrip.setComponentAlignment(alert, Alignment.MIDDLE_RIGHT);

			nameLabel = new Label(componentInfo.getName());
			nameLabel.setStyleName("componentName");
			nameLabel.setSizeUndefined();
			imageLayout.addComponent(nameLabel);
			imageLayout.setComponentAlignment(nameLabel, Alignment.BOTTOM_CENTER);

		}
		addComponent(imageLayout);
		setComponentAlignment(imageLayout, Alignment.TOP_CENTER);
		setExpandRatio(imageLayout, 1.0f);

	}

	public void setName(String name) {
		nameLabel.setValue(name);
	}

	public void setCommandLabel(TaskRecord taskRecord) {
		if (taskRecord != null) {
			switch (CommandStates.States.valueOf(taskRecord.getState())) {
			case running:
				setAlert("<h3>Running command \"" + taskRecord.getCommand() + "\"", runningResource);
				break;
			//			case paused:
			//				setAlert("<h3>Command \"" + taskRecord.getCommand() + "\" was paused.", pausedResource);
			//				break;
			case done:
				setAlert(null, null);
				break;
			case cancelled:
				setAlert("<h3>Last command \"" + taskRecord.getCommand() + "\" was cancelled.</h3>", cancelledResource);
				break;
			case missing:
				setAlert("<h3>Last command \"" + taskRecord.getCommand() + "\" was missing.</h3>", errorResource);
				break;
			case error:
				setAlert("<h3>Last command \"" + taskRecord.getCommand() + "\" failed:</h3>" + taskRecord.getError(), errorResource);
				break;
			default:
				break;
			}
		}
	}

	private void setAlert(String msg, ThemeResource resource) {
		alert.setDescription(msg);
		alert.setSource(resource);
		alert.setVisible(msg != null);
	}

	public void setDescription(String description) {
		info.setDescription(description);
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

	public void setIcon(String type, String state, String capacity) {
		if (type.equals("node")) {
			String icon = NodeStates.getNodeIcon(componentInfo.getSystemType(), state);
			if (capacity != null && capacityStates.contains(state)) {

				double capacity_num = Double.parseDouble(capacity);
				if (capacity_num > 0 && capacity_num <= 20)
					icon += "-20";
				else if (capacity_num <= 40)
					icon += "-40";
				else if (capacity_num <= 60)
					icon += "-60";
				else if (capacity_num <= 80)
					icon += "-80";
				else
					icon += "-100";
			}

			imageLayout.setStyleName(icon);
			imageLayout.addStyleName(type);
			setSelected(isSelected);
		}

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

			editButton = new Embedded(null, new ThemeResource("img/edit.png"));
			editButton.addStyleName("edit" + componentType);
			editButton.setDescription("Edit " + componentType);
			editButton.setData(this);
			addComponent(editButton);
			editButton.addClickListener(new MouseEvents.ClickListener() {
				private static final long serialVersionUID = 0x4C656F6E6172646FL;

				public void click(ClickEvent event) {
					new ComponentDialog((ClusterComponent) thisButton.getData(), thisButton);
				}
			});

			deleteButton = new Embedded(null, new ThemeResource("img/delete.png"));
			deleteButton.addStyleName("delete" + componentType);
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
		Label label = new Label("Delete this component?");
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
