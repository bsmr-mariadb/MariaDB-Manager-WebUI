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

import java.util.LinkedHashMap;

import com.skysql.manager.ManagerUI;
import com.skysql.manager.api.APIrestful;
import com.skysql.manager.api.Versions;
import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

/**
 * The Class AboutSettings is used for the About panel of the Settings dialog.
 */
public class AboutSettings extends VerticalLayout implements Window.CloseListener {

	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	/**
	 * Reads the AboutRecord from the session and populates the panel layout.
	 */
	AboutSettings(boolean extraInfo) {
		addStyleName("aboutTab");
		setSizeFull();
		setSpacing(true);
		setMargin(true);

		Label title = new Label("<h3>Welcome to MariaDB Manager<h3/>", ContentMode.HTML);
		title.setSizeUndefined();
		addComponent(title);
		setComponentAlignment(title, Alignment.MIDDLE_CENTER);

		StringBuffer str = new StringBuffer();
		str.append("<table border=0 cellspacing=3 cellpadding=3 summary=\"\">" + "<tr bgcolor=\"#ccccff\">" + "<th align=left>Component"
				+ "<th align=left>Release");
		if (extraInfo) {
			str.append("<th align=left>Version" + "<th align=left>Date");
		}
		LinkedHashMap<String, Versions> versionsList = Versions.getVersionsList();
		int i = 0;
		for (Versions component : versionsList.values()) {
			str.append(((i++ & 1) == 0) ? "<tr>" : "<tr bgcolor=\"#eeeeff\">");
			str.append("<td><code>" + component.getName() + "</code><td>" + component.getRelease());
			if (extraInfo) {
				str.append("<td>" + component.getVersion());
				str.append("<td>" + (component.getDate() == null ? " " : component.getDate()));
			}
		}
		str.append("</table>");
		ManagerUI.log(str.toString());
		Label versionsLabel = new Label(str.toString(), ContentMode.HTML);
		versionsLabel.setSizeUndefined();
		addComponent(versionsLabel);
		setComponentAlignment(versionsLabel, Alignment.MIDDLE_CENTER);

		if (extraInfo) {
			Label guiInfo = new Label("WebUI calling API " + APIrestful.apiVERSION + " @ " + APIrestful.getURI());
			guiInfo.setSizeUndefined();
			addComponent(guiInfo);
			setComponentAlignment(guiInfo, Alignment.MIDDLE_CENTER);
		}

		addComponent(new Label(""));
		addComponent(new Label(""));

		Label copyright = new Label("\u00A9 SkySQL Corporation Ab, 2014.");
		copyright.setSizeUndefined();
		addComponent(copyright);
		setComponentAlignment(copyright, Alignment.MIDDLE_CENTER);

		Link link = new Link("MariaDB is a trademark of SkySQL Corporation Ab.", new ExternalResource("http://www.mariadb.com/about/legal/trademarks"));
		link.setTargetName("_blank");
		addComponent(link);
		setComponentAlignment(link, Alignment.MIDDLE_CENTER);

	}

	/* (non-Javadoc)
	 * @see com.vaadin.ui.Window.CloseListener#windowClose(com.vaadin.ui.Window.CloseEvent)
	 */
	public void windowClose(CloseEvent e) {

	}

}
