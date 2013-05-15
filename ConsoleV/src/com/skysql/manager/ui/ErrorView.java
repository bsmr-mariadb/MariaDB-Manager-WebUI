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
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class ErrorView extends VerticalLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private Button retryButton = new Button("Retry");

	private Button.ClickListener listener = new Button.ClickListener() {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		@Override
		public void buttonClick(ClickEvent event) {

			//Tell the UI to refresh itself - this is not the only way to do this, just one possibility
			ManagerUI current = (ManagerUI) UI.getCurrent();
			current.close();
		}

	};

	public ErrorView(Type type, String errorMsg) {

		addStyleName("loginView");
		setSizeFull();
		setMargin(true);
		setSpacing(true);

		if (errorMsg != null) {
			Notification.show(errorMsg, type);
		}

		Embedded logo = new Embedded(null, new ThemeResource("img/combovertical.png"));
		addComponent(logo);
		setComponentAlignment(logo, Alignment.TOP_CENTER);

		String reloadMsg = (type == Notification.Type.ERROR_MESSAGE) ? "To try again, please refresh/reload the current page."
				: "When done, please refresh/reload the current page.";
		Label refreshLabel = new Label(reloadMsg);
		refreshLabel.setSizeUndefined();
		refreshLabel.addStyleName("instructions");
		addComponent(refreshLabel);
		setComponentAlignment(refreshLabel, Alignment.TOP_CENTER);
		//		retryButton.setClickShortcut(KeyCode.ENTER);
		//		retryButton.addClickListener(listener);
		//		addComponent(retryButton);
		//		setComponentAlignment(retryButton, Alignment.TOP_CENTER);

	}
}
