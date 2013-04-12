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
 * Copyright SkySQL Ab
 */

package com.skysql.consolev.ui;

import java.util.ArrayList;

import com.skysql.consolev.api.UserChart;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class TimelineDialog {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	Window dialogWindow;

	public TimelineDialog(UserChart userChart) {

		dialogWindow = new TimelineWindow("Timeline");
		VerticalLayout windowLayout = (VerticalLayout) dialogWindow.getContent();
		windowLayout.setSizeFull();

		UI.getCurrent().addWindow(dialogWindow);

		ArrayList<String> monitorIDs = userChart.getMonitorIDs();
		TimelineLayout timelineLayout = new TimelineLayout(userChart.getName(), monitorIDs);
		windowLayout.addComponent(timelineLayout.getTimeLine());

	}

}

class TimelineWindow extends Window {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	public TimelineWindow(String caption) {
		setModal(true);
		setWidth("800px");
		setHeight("800px");
		center();
		setCaption(caption);
		VerticalLayout layout = new VerticalLayout();
		setContent(layout);
		layout.setSpacing(true);
		layout.setMargin(true);
	}
}
