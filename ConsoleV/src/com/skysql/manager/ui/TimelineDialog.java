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

import java.util.ArrayList;

import com.skysql.manager.UserChart;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class TimelineDialog {

	Window dialogWindow;

	public TimelineDialog(UserChart userChart) {

		dialogWindow = new ModalWindow("Timeline", "800px");
		dialogWindow.setHeight("800px");

		VerticalLayout windowLayout = (VerticalLayout) dialogWindow.getContent();
		windowLayout.setSizeFull();

		UI.getCurrent().addWindow(dialogWindow);

		ArrayList<String> monitorIDs = userChart.getMonitorIDs();
		TimelineLayout timelineLayout = new TimelineLayout(userChart.getName(), monitorIDs);
		windowLayout.addComponent(timelineLayout.getTimeLine());

	}

}
