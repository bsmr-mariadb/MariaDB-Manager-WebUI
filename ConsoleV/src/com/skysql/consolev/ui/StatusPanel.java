package com.skysql.consolev.ui;

import com.vaadin.ui.HorizontalLayout;

public class StatusPanel {

	private static HorizontalLayout statusLayout;

    public static HorizontalLayout getStatusLayout() {
    	if (statusLayout == null) {
            statusLayout = new HorizontalLayout();
    	}
    	return statusLayout;
    }
    
}
