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

import com.skysql.manager.SystemRecord;
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

public class SystemDialog implements Window.CloseListener {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private Window dialogWindow;
	private HorizontalLayout wrapper;
	private HorizontalLayout buttonsBar;
	private final SystemRecord systemRecord;
	private final SystemForm systemForm;
	private final ComponentButton button;

	public SystemDialog(final SystemRecord systemRecord, final ComponentButton button) {
		this.button = button;

		String windowTitle = (systemRecord != null) ? "Edit System: " + systemRecord.getName() : "Add System";
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

		if (systemRecord == null) {
			this.systemRecord = new SystemRecord(SystemInfo.SYSTEM_ROOT);
			systemForm = new SystemForm(this.systemRecord, "Add a System");
			saveSystem("Add System");

		} else {
			this.systemRecord = systemRecord;
			systemForm = new SystemForm(systemRecord, "Edit an existing System");
			saveSystem("Save Changes");
		}

		wrapper.addComponent(systemForm);

	}

	private void saveSystem(final String okButtonCaption) {

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
				if (systemForm.validateSystem()) {
					if (systemRecord.save()) {
						if (button != null) {
							button.setName(systemRecord.getName());
							button.setDescription(systemRecord.ToolTip());
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
