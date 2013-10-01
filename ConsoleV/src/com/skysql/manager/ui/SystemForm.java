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

import com.skysql.manager.SystemRecord;
import com.skysql.manager.api.SystemTypes;
import com.vaadin.data.Validator.EmptyValueException;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Form;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("deprecation")
public class SystemForm extends VerticalLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	final TextField name = new TextField("Name");
	final NativeSelect systemType = new NativeSelect("Type");
	final TextField dbUsername = new TextField("Database Username");
	final TextField dbPassword = new TextField("Database Password");
	final TextField repUsername = new TextField("Replication Username");
	final TextField repPassword = new TextField("Replication Password");
	final Form form = new Form();
	private SystemRecord system;

	SystemForm(final SystemRecord system, String description) {
		this.system = system;

		setMargin(new MarginInfo(true, true, false, true));
		setSpacing(false);

		addComponent(form);
		form.setImmediate(false);
		form.setFooter(null);
		form.setDescription(description);

		String value;
		if ((value = system.getName()) != null) {
			name.setValue(value);
		}
		form.addField("name", name);
		name.focus();

		for (String systemType : SystemTypes.getList().keySet()) {
			this.systemType.addItem(systemType);
		}
		systemType.select(system.getSystemType() != null ? system.getSystemType() : SystemTypes.DEFAULT_SYSTEMTYPE);
		systemType.setNullSelectionAllowed(false);
		form.addField("systemType", systemType);

		if ((value = system.getDBUsername()) != null) {
			dbUsername.setValue(value);
		}
		form.addField("dbusername", dbUsername);

		dbPassword.setNullSettingAllowed(true);
		if ((value = system.getDBPassword()) != null) {
			dbPassword.setValue(value);
		}
		form.addField("dbpassword", dbPassword);

		if ((value = system.getRepUsername()) != null) {
			repUsername.setValue(value);
		}
		form.addField("repusername", repUsername);

		repPassword.setNullSettingAllowed(true);
		if ((value = system.getRepPassword()) != null) {
			repPassword.setValue(value);
		}
		form.addField("reppassword", repPassword);

	}

	public boolean validateSystem() {

		try {
			form.setComponentError(null);
			form.commit();

			system.setName(name.getValue());
			system.setSystemType((String) systemType.getValue());
			system.setDBUsername(dbUsername.getValue());
			system.setDBPassword(dbPassword.getValue());
			system.setRepUsername(repUsername.getValue());
			system.setRepPassword(repPassword.getValue());

			return true;

		} catch (EmptyValueException e) {
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
}
