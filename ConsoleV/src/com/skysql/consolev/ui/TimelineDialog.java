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
