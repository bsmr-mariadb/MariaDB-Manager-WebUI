package com.skysql.consolev.ui;

import com.skysql.consolev.SessionData;
import com.skysql.consolev.api.NodeInfo;
import com.skysql.consolev.api.UserChart;
import com.skysql.consolev.ui.components.ChartsLayout;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class ChartPreviewLayout extends VerticalLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private static final Integer COUNT_15 = 15;
	private static final Integer COUNT_30 = 30;
	private static final Integer COUNT_45 = 45;
	private static final Integer COUNT_60 = 60;
	private static final Integer COUNT_75 = 75;
	private static final Integer COUNT_90 = 90;

	private String chartTime, chartInterval = "1800";
	private ChartsLayout chartLayout;
	private UserChart userChart;

	public ChartPreviewLayout(final UserChart userChart) {

		this.userChart = userChart;

		addStyleName("ChartPreviewLayout");
		setSpacing(true);
		setMargin(true);

		final Label monitorsLabel = new Label("Display as Chart");
		monitorsLabel.setStyleName("dialogLabel");
		addComponent(monitorsLabel);
		setComponentAlignment(monitorsLabel, Alignment.TOP_CENTER);

		HorizontalLayout chartInfo = new HorizontalLayout();
		chartInfo.setSpacing(true);
		addComponent(chartInfo);
		setComponentAlignment(chartInfo, Alignment.MIDDLE_CENTER);

		FormLayout formLayout = new FormLayout();
		chartInfo.addComponent(formLayout);

		TextField chartName = new TextField("Title");
		chartName.setImmediate(true);
		chartName.setValue(userChart.getName());
		formLayout.addComponent(chartName);
		chartName.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {

				String value = (String) (event.getProperty()).getValue();
				userChart.setName(value);
				refresh();

			}
		});

		TextField chartDescription = new TextField("Description");
		chartDescription.setWidth("25em");
		chartDescription.setImmediate(true);
		chartDescription.setValue(userChart.getDescription());
		formLayout.addComponent(chartDescription);
		chartDescription.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {

				String value = (String) (event.getProperty()).getValue();
				userChart.setDescription(value);
				refresh();

			}
		});

		TextField chartUnit = new TextField("Unit");
		chartUnit.setImmediate(true);
		chartUnit.setValue(userChart.getUnit());
		formLayout.addComponent(chartUnit);
		chartUnit.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {

				String value = (String) (event.getProperty()).getValue();
				userChart.setUnit(value);
				refresh();

			}
		});

		// formLayout = new FormLayout();
		// chartInfo.addComponent(formLayout);
		NativeSelect chartSelectType = new NativeSelect("Type");
		chartSelectType.setImmediate(true);
		chartSelectType.addItem(UserChart.LINECHART);
		chartSelectType.addItem(UserChart.AREACHART);
		chartSelectType.setValue(userChart.getType());
		formLayout.addComponent(chartSelectType);
		chartSelectType.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {

				String value = (String) (event.getProperty()).getValue();
				userChart.setType(value);
				refresh();

			}
		});

		NativeSelect selectCount = new NativeSelect("Points");
		selectCount.setImmediate(true);

		selectCount.addItem(COUNT_15);
		selectCount.addItem(COUNT_30);
		selectCount.addItem(COUNT_45);
		selectCount.addItem(COUNT_60);
		selectCount.addItem(COUNT_75);
		selectCount.addItem(COUNT_90);

		selectCount.setItemCaption(COUNT_15, "15");
		selectCount.setItemCaption(COUNT_30, "30");
		selectCount.setItemCaption(COUNT_45, "45");
		selectCount.setItemCaption(COUNT_60, "60");
		selectCount.setItemCaption(COUNT_75, "75");
		selectCount.setItemCaption(COUNT_90, "90");

		selectCount.select(COUNT_15);
		selectCount.setNullSelectionAllowed(false);
		selectCount.setValue(userChart.getType());
		formLayout.addComponent(selectCount);
		selectCount.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {

				int points = (Integer) (event.getProperty()).getValue();
				userChart.setPoints(points);
				refresh();

			}
		});

		chartLayout = drawChart();
		addComponent(chartLayout);

	}

	private ChartsLayout drawChart() {
		ChartsLayout newChartsLayout = new ChartsLayout(true);
		newChartsLayout.addStyleName("chartPreview");
		userChart.clearMonitorData();
		newChartsLayout.initializeChart(userChart);
		SessionData userData = VaadinSession.getCurrent().getAttribute(SessionData.class);
		NodeInfo nodeInfo = userData.getNodeInfo();
		newChartsLayout.refresh(nodeInfo, chartTime, chartInterval, String.valueOf(userChart.getPoints()));
		return newChartsLayout;
	}

	public void refresh() {
		ChartsLayout newChartLayout = drawChart();
		replaceComponent(chartLayout, newChartLayout);
		chartLayout = newChartLayout;
	}
}
