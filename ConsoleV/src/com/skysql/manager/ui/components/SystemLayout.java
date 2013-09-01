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
import com.skysql.manager.ManagerUI;
import com.skysql.manager.SystemRecord;
import com.skysql.manager.api.SystemInfo;
import com.skysql.manager.ui.ComponentDialog;
import com.skysql.manager.ui.OverviewPanel;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.Sizeable;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class SystemLayout extends VerticalLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private boolean isEditable;
	private final HorizontalLayout systemSlot;
	private ComponentButton systemButton;
	private String systemID;

	private LayoutClickListener componentListener = new LayoutClickListener() {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		@Override
		public void layoutClick(LayoutClickEvent event) {
			//clickLayout((ComponentButton) event.getComponent());

			ComponentButton button = (ComponentButton) event.getComponent();
			if (event.isDoubleClick() && isEditable) {
				new ComponentDialog((ClusterComponent) button.getData(), button);

			} else {
				OverviewPanel overviewPanel = getSession().getAttribute(OverviewPanel.class);
				overviewPanel.clickLayout(button);
			}

		}
	};

	public SystemLayout(SystemRecord systemRecord) {

		addStyleName("systemLayout");
		setWidth(Sizeable.SIZE_UNDEFINED, Sizeable.Unit.PERCENTAGE);
		setMargin(new MarginInfo(false, true, false, false));

		final HorizontalLayout systemHeader = new HorizontalLayout();
		systemHeader.addStyleName("panelHeaderLayout");
		systemHeader.setSpacing(true);
		systemHeader.setWidth("100%");
		systemHeader.setHeight("23px");
		addComponent(systemHeader);

		final NativeButton backButton = new NativeButton();
		backButton.setStyleName("backButton");
		backButton.setDescription("Back to Systems");
		systemHeader.addComponent(backButton);
		backButton.addClickListener(new Button.ClickListener() {

			public void buttonClick(ClickEvent event) {

				VaadinSession session = getSession();
				SystemInfo systemInfo = session.getAttribute(SystemInfo.class);
				OverviewPanel overviewPanel = session.getAttribute(OverviewPanel.class);
				ComponentButton button = systemInfo.getCurrentSystem().getButton();
				String parentID = systemInfo.getCurrentSystem().getParentID();
				systemInfo.setCurrentSystem(parentID);
				session.setAttribute(SystemInfo.class, systemInfo);
				ManagerUI.log("new systemID: " + parentID);
				overviewPanel.clickLayout(button);
				overviewPanel.refresh();
			}

		});

		final Label systemLabel = new Label("Systems");
		systemLabel.setSizeUndefined();
		systemHeader.addComponent(systemLabel);
		systemHeader.setComponentAlignment(systemLabel, Alignment.MIDDLE_LEFT);
		systemHeader.setExpandRatio(systemLabel, 1.0f);

		systemSlot = new HorizontalLayout();
		addComponent(systemSlot);

		// initialize System button
		/***
		systemButton = new ComponentButton(systemRecord);
		systemButton.addLayoutClickListener(componentListener);
		systemSlot.addComponent(systemButton);
		systemSlot.addLayoutClickListener(new LayoutClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void layoutClick(LayoutClickEvent event) {

				Component child;
				if (event.isDoubleClick() && (child = event.getChildComponent()) != null && (child instanceof ComponentButton)) {
					// Get the child component which was double-clicked
					ComponentButton button = (ComponentButton) child;
					if (isEditable) {
						new ComponentDialog((ClusterComponent) button.getData(), button);
					}

				} else if (!isEditable && (child = event.getChildComponent()) != null && (child instanceof ComponentButton)) {
					// Get the child component which was clicked
					ComponentButton button = (ComponentButton) child;
					OverviewPanel overviewPanel = getSession().getAttribute(OverviewPanel.class);
					overviewPanel.clickLayout(button);
				}

			}
		});
		***/
		refresh(null, null);
	}

	public ComponentButton getButton() {
		return systemButton;
	}

	public void setEditable(boolean editable) {
		isEditable = editable;

		if (systemButton != null) {
			systemButton.setEditable(editable);
		}

	}

	public void deleteComponent(ComponentButton button) {
		removeComponent(button);

		//		OverviewPanel overviewPanel = getSession().getAttribute(OverviewPanel.class);
		//		if (button.isSelected()) {
		//			overviewPanel.clickComponentButton(0);
		//		}
		//		overviewPanel.refresh();

		VaadinSession session = getSession();
		SystemInfo systemInfo = session.getAttribute(SystemInfo.class);
		String parentID = systemInfo.getCurrentSystem().getParentID();
		systemInfo.setCurrentSystem(parentID);
		session.setAttribute(SystemInfo.class, systemInfo);
		ManagerUI.log("new systemID: " + parentID);
		OverviewPanel overviewPanel = session.getAttribute(OverviewPanel.class);
		if (button.isSelected()) {
			overviewPanel.clickLayout(null);
		}
		overviewPanel.refresh();

	}

	public void refresh(final OverviewPanel.UpdaterThread updaterThread, SystemRecord systemRecord) {

		VaadinSession session = getSession();

		if (session != null) {
			session.lock();
		}
		try {

			if (systemRecord == null) {
				setVisible(false);
				systemID = null;
				return;
			}

			if (!systemRecord.getID().equals(systemID)) {
				systemID = systemRecord.getID();

				systemSlot.removeAllComponents();

				if ((systemButton = systemRecord.getButton()) == null) {
					systemButton = new ComponentButton(systemRecord);
				}
				systemButton.addLayoutClickListener(componentListener);
				systemSlot.addComponent(systemButton);
				systemButton.setSelected(systemButton.isSelected());
				setEditable(isEditable);

				setVisible(systemRecord.getParentID() == null ? false : true);
			}

		} finally {
			if (session != null) {
				session.unlock();
			}
		}

	}
}
