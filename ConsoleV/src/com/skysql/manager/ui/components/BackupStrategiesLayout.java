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

package com.skysql.manager.ui.components;

import com.skysql.manager.ClusterComponent;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.api.SystemInfo;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class BackupStrategiesLayout extends VerticalLayout {

	private enum Strategies {
		Good, Better, Best;
	}

	private UpdaterThread updaterThread;
	private NativeSelect selectStrategy;

	public BackupStrategiesLayout() {

		addStyleName("strategiesLayout");
		setSpacing(true);
		setMargin(true);

		//		HorizontalLayout strategyLayout = new HorizontalLayout();
		//		addComponent(strategyLayout);
		//		strategyLayout.setSpacing(true);
		//		FormLayout strategyForm = new FormLayout();
		//		strategyLayout.addComponent(strategyForm);
		//
		//		selectStrategy = new NativeSelect("Current Strategy");
		//		strategyForm.addComponent(selectStrategy);
		//		selectStrategy.setImmediate(true);
		//		selectStrategy.setNullSelectionAllowed(false);
		//		for (Strategies strategy : Strategies.values()) {
		//			selectStrategy.addItem(strategy.name());
		//		}
		//
		//		final Label strategyInfo = new Label();
		//		strategyLayout.addComponent(strategyInfo);
		//		strategyLayout.setComponentAlignment(strategyInfo, Alignment.MIDDLE_LEFT);
		//
		//		selectStrategy.addValueChangeListener(new ValueChangeListener() {
		//			private static final long serialVersionUID = 0x4C656F6E6172646FL;
		//
		//			public void valueChange(ValueChangeEvent event) {
		//
		//				String info = null;
		//				switch (Strategies.valueOf((String) event.getProperty().getValue())) {
		//				case Good:
		//					info = "Full every Sunday at 02:00; Incremental every day at 03:00.";
		//					break;
		//				case Better:
		//					info = "Full every Sunday and Wednesday at 02:00; Incremental every day at 03:00.";
		//					break;
		//				case Best:
		//					info = "Full every day at 02:00; Incremental every hour.";
		//					break;
		//				}
		//				strategyInfo.setValue(" - " + info);
		//			}
		//		});
		//
		//		selectStrategy.select(Strategies.Good.name());

		final Label immediateLabel = new Label("(To run an immediate backup, select a node first then switch to the Control panel)");
		immediateLabel.setSizeUndefined();
		addComponent(immediateLabel);
		setComponentAlignment(immediateLabel, Alignment.MIDDLE_LEFT);

	}

	public void refresh() {

		ManagerUI.log(this.getClass().getName() + " refresh()");
		updaterThread = new UpdaterThread(updaterThread);
		updaterThread.start();

	}

	class UpdaterThread extends Thread {
		UpdaterThread oldUpdaterThread;
		volatile boolean flagged = false;

		UpdaterThread(UpdaterThread oldUpdaterThread) {
			this.oldUpdaterThread = oldUpdaterThread;
		}

		@Override
		public void run() {
			if (oldUpdaterThread != null && oldUpdaterThread.isAlive()) {
				ManagerUI.log(this.getClass().getName() + " - Old thread is alive: " + oldUpdaterThread);
				oldUpdaterThread.flagged = true;
				oldUpdaterThread.interrupt();
				try {
					ManagerUI.log(this.getClass().getName() + " - Before Join");
					oldUpdaterThread.join();
					ManagerUI.log(this.getClass().getName() + " - After Join");
				} catch (InterruptedException iex) {
					ManagerUI.log(this.getClass().getName() + " - Interrupted Exception");
					return;
				}

			}

			ManagerUI.log(this.getClass().getName() + " - UpdaterThread.this: " + this);
			asynchRefresh(this);
		}
	}

	private void asynchRefresh(final UpdaterThread updaterThread) {

		ManagerUI managerUI = getSession().getAttribute(ManagerUI.class);
		SystemInfo systemInfo = getSession().getAttribute(SystemInfo.class);

		/***
		LinkedHashMap<String, String> sysProperties = systemInfo.getCurrentSystem().getProperties();
		final String EIP = sysProperties.get(SystemInfo.PROPERTY_EIP);
		***/

		String systemID = systemInfo.getCurrentID();
		if (SystemInfo.SYSTEM_ROOT.equals(systemID)) {
			ClusterComponent clusterComponent = VaadinSession.getCurrent().getAttribute(ClusterComponent.class);
			systemID = clusterComponent.getID();
		}

		managerUI.access(new Runnable() {
			@Override
			public void run() {
				// Here the UI is locked and can be updated

				ManagerUI.log(this.getClass().getName() + " access.run(): ");

			}
		});

	}

}
