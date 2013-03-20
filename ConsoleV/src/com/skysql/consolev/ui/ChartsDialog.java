package com.skysql.consolev.ui;

import java.util.ArrayList;

import com.skysql.consolev.MonitorRecord;
import com.skysql.consolev.api.UserChart;
import com.skysql.consolev.ui.components.ChartsLayout;
import com.vaadin.addon.charts.Chart;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

public class ChartsDialog implements Window.CloseListener {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	Window dialogWindow;
	Button openButton;
	Button closebutton;
	UserChart newUserChart;
	Chart chart;
	ChartsLayout chartsLayout;

	public ChartsDialog(ChartsLayout chartsLayout, Chart chart) {

		this.chart = chart;
		this.chartsLayout = chartsLayout;

		dialogWindow = new ChartWindow("Monitors to Chart mapping");
		dialogWindow.addCloseListener(this);
		HorizontalLayout windowLayout = new HorizontalLayout();
		dialogWindow.setContent(windowLayout);

		UI.getCurrent().addWindow(dialogWindow);

		UserChart originalUserChart = (UserChart) chart.getData();
		newUserChart = new UserChart(originalUserChart);

		ArrayList<MonitorRecord> monitors = newUserChart.getMonitors();
		MonitorsLayout monitorsLayout = new MonitorsLayout(monitors);
		windowLayout.addComponent(monitorsLayout);

		VerticalLayout separator = new VerticalLayout();
		separator.setSizeFull();
		Embedded rightArrow = new Embedded(null, new ThemeResource("img/right_arrow.png"));
		separator.addComponent(rightArrow);
		separator.setComponentAlignment(rightArrow, Alignment.MIDDLE_CENTER);
		windowLayout.addComponent(separator);

		ChartPreviewLayout chartPreviewLayout = new ChartPreviewLayout(newUserChart);
		windowLayout.addComponent(chartPreviewLayout);
		monitorsLayout.addChartPreview(chartPreviewLayout);
	}

	/** Save data when window is closed */
	public void windowClose(CloseEvent e) {
		chartsLayout.replaceChart(chart, newUserChart);
		chartsLayout.refresh();
		dialogWindow.close();
	}
}

class ChartWindow extends Window {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	public ChartWindow(String caption) {
		setModal(true);
		setWidth("775px");
		center();
		setCaption(caption);
		VerticalLayout layout = new VerticalLayout();
		setContent(layout);
		layout.setSpacing(true);
		layout.setMargin(true);
	}
}
