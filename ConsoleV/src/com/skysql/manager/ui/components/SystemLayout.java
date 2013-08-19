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
import com.skysql.manager.ui.ComponentDialog;
import com.skysql.manager.ui.OverviewPanel;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

public class SystemLayout extends HorizontalLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private boolean isEditable;
	private ComponentButton systemButton;

	private LayoutClickListener componentListener = new LayoutClickListener() {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		@Override
		public void layoutClick(LayoutClickEvent event) {
			//clickLayout((ComponentButton) event.getComponent());
		}
	};

	public SystemLayout(SystemRecord systemRecord) {

		// initialize System button
		systemButton = new ComponentButton(systemRecord, systemRecord);
		systemButton.addLayoutClickListener(componentListener);
		addComponent(systemButton);

		addLayoutClickListener(new LayoutClickListener() {
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

	}

	public ComponentButton getButton() {
		return systemButton;
	}

	public void setEditable(boolean editable) {
		isEditable = editable;

		systemButton.setEditable(editable);
	}

	public void refresh(SystemRecord systemRecord) {

		boolean refresh = false;

		if (systemRecord != null) {
			refresh = true;

			removeAllComponents();
			systemButton = new ComponentButton(systemRecord, systemRecord);
			systemButton.addLayoutClickListener(componentListener);
			addComponent(systemButton);
			if (isEditable) {
				setEditable(isEditable);
			}

		}

	}
}
