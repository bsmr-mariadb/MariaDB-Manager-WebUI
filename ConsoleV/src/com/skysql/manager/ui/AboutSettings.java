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

import com.skysql.manager.AboutRecord;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

public class AboutSettings extends VerticalLayout implements Window.CloseListener {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	AboutSettings() {
		addStyleName("aboutTab");
		setSizeFull();
		setSpacing(true);
		setMargin(true);

		AboutRecord aboutRecord = VaadinSession.getCurrent().getAttribute(AboutRecord.class);

		Label title = new Label("<h3>Welcome to MariaDB Manager<h3/>", ContentMode.HTML);
		title.setSizeUndefined();
		addComponent(title);
		setComponentAlignment(title, Alignment.MIDDLE_CENTER);

		addComponent(new Label("GUI Version: " + aboutRecord.getVersionGUI()));
		addComponent(new Label("API Version: " + aboutRecord.getVersionAPI()));
		addComponent(new Label("Monitor Version: " + aboutRecord.getVersionMonitor()));
		addComponent(new Label(""));
		addComponent(new Label(""));

		Label copyright = new Label("\u00A9 SkySQL Corporation Ab, 2013.");
		copyright.setSizeUndefined();
		addComponent(copyright);
		setComponentAlignment(copyright, Alignment.MIDDLE_CENTER);

		Link link = new Link("MariaDB is a trademark of SkySQL Corporation Ab.", new ExternalResource("http://www.mariadb.com/about/legal/trademarks"));
		link.setTargetName("_blank");
		addComponent(link);
		setComponentAlignment(link, Alignment.MIDDLE_CENTER);

	}

	public void windowClose(CloseEvent e) {

	}

}
