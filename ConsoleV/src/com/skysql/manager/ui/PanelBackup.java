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

import com.skysql.manager.ManagerUI;
import com.skysql.manager.ui.components.BackupScheduledLayout;
import com.skysql.manager.ui.components.BackupSetsLayout;
import com.skysql.manager.ui.components.BackupStrategiesLayout;
import com.vaadin.ui.VerticalLayout;

/**
 * The Class PanelBackup.
 */
public class PanelBackup extends VerticalLayout {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	/** The backup strategies layout. */
	private BackupStrategiesLayout backupStrategiesLayout;

	/** The backup scheduled layout. */
	private BackupScheduledLayout backupScheduledLayout;

	/** The backup sets layout. */
	private BackupSetsLayout backupSetsLayout;

	/**
	 * Instantiates a new panel backup.
	 */
	PanelBackup() {

		setSizeFull();
		addStyleName("backupTab");

		//		backupStrategiesLayout = new BackupStrategiesLayout();
		//		addComponent(backupStrategiesLayout);
		backupScheduledLayout = new BackupScheduledLayout();
		addComponent(backupScheduledLayout);
		backupSetsLayout = new BackupSetsLayout();
		addComponent(backupSetsLayout);
		setExpandRatio(backupSetsLayout, 1.0f);

	}

	/**
	 * Refresh.
	 */
	public void refresh() {

		ManagerUI.log("PanelBackup refresh()");
		//		backupStrategiesLayout.refresh();
		backupScheduledLayout.refresh();
		backupSetsLayout.refresh();
	}

}
