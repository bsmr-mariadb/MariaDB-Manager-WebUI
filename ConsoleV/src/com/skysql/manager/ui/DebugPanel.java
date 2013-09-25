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

import com.vaadin.server.VaadinSession;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;

public class DebugPanel extends VerticalLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private Label beatLabel;
	private TextArea logArea;
	private StringBuffer logBuffer;

	public DebugPanel() {
		setSizeFull();
		setSpacing(true);
		setMargin(true);
		addStyleName("debugPanel");

		// Control Layout
		HorizontalLayout controlLayout = new HorizontalLayout();
		controlLayout.setSpacing(true);
		addComponent(controlLayout);

		beatLabel = new Label();
		beatLabel.setSizeUndefined();
		controlLayout.addComponent(beatLabel);
		controlLayout.setExpandRatio(beatLabel, 1f);

		/***
		 * // Create an opener extension BrowserWindowOpener opener = new
		 * BrowserWindowOpener(DebugWindow.class);
		 * opener.setFeatures("height=200,width=300,resizable");
		 * 
		 * Button openLog = new Button("Open Log");
		 * controlLayout.addComponent(openLog); opener.extend(openLog);
		 ***/

		/**
		 * Link newBackupLogLink = new Link("Catalina Log", new FileResource(new
		 * File("/usr/local/tomcat/logs/catalina.out")));
		 * newBackupLogLink.setTargetName("_blank");
		 * newBackupLogLink.setDescription("Open Catalina log in a new window");
		 * newBackupLogLink.setIcon(new ThemeResource("img/externalLink.png"));
		 * newBackupLogLink.addStyleName("icon-after-caption");
		 * controlLayout.addComponent(newBackupLogLink);
		 * 
		 * // Find the application directory String basepath =
		 * VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
		 * ManagerUI.log("pathname: " + basepath); // Image as a file resource
		 * //FileResource resource = new FileResource(new File(basepath +
		 * "/catalina.out"));
		 ***/

		// Log
		VerticalLayout logLayout = new VerticalLayout();
		logLayout.setSizeFull();

		logArea = new TextArea("API Log");
		logArea.setSizeFull();
		logArea.setWordwrap(false);
		logLayout.addComponent(logArea);

		logBuffer = new StringBuffer();

		addComponent(logLayout);
		setExpandRatio(logLayout, 1f);

	}

	public void setBeat(long beat) {
		beatLabel.setValue("Heartbeat: " + String.valueOf(beat));
	}

	public void setLog(String log) {
		logBuffer.append(log + "\n");
		VaadinSession session = getSession();
		if (session != null) {
			session.lock();
			try {
				logArea.setValue(logBuffer.toString());
			} finally {
				session.unlock();
			}
		} else {
			logArea.setValue(logBuffer.toString());
		}

	}

}
