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

import com.skysql.manager.ClusterComponent;
import com.skysql.manager.SystemRecord;
import com.skysql.manager.api.NodeInfo;
import com.skysql.manager.ui.components.ComponentButton;

/**
 * The Class ComponentDialog.
 */
public class ComponentDialog {

	/**
	 * Instantiates a new component dialog.
	 *
	 * @param componentInfo the component info
	 * @param button the button
	 */
	public ComponentDialog(ClusterComponent componentInfo, ComponentButton button) {

		switch (componentInfo.getType()) {
		case system:
			new SystemDialog((SystemRecord) componentInfo, button);
			break;

		case node:
			new NodeDialog((NodeInfo) componentInfo, button);
			break;
		}

	}

}
