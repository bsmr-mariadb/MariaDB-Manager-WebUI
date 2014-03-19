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

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.VerticalLayout;

public class ErrorView extends VerticalLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	public ErrorView(Type type, String errorMsg) {

		addStyleName("loginView");
		setSizeFull();
		setMargin(true);
		setSpacing(true);

		if (errorMsg != null) {
			Notification.show(errorMsg, type);
		}

		Embedded logo = new Embedded(null, new ThemeResource("img/productlogo.png"));
		addComponent(logo);
		setComponentAlignment(logo, Alignment.TOP_CENTER);

		if (type == Notification.Type.ERROR_MESSAGE) {
			Label refreshLabel = new Label("To try again, please refresh/reload the current page.");
			refreshLabel.setSizeUndefined();
			refreshLabel.addStyleName("instructions");
			addComponent(refreshLabel);
			setComponentAlignment(refreshLabel, Alignment.TOP_CENTER);
		}
	}
}
