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

	final TextField id = new TextField("ID");
	final TextField name = new TextField("Name");
	final NativeSelect systemType = new NativeSelect("Type");

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
		form.addField("id", id);
		if ((value = system.getID()) == null) {
			id.setRequired(true);
			id.setRequiredError("ID is a required field");
			id.focus();
		} else {
			id.setValue(value);
			id.setVisible(false);
		}

		if ((value = system.getName()) != null) {
			name.setValue(value);
		}
		form.addField("name", name);
		if (system.getID() != null) {
			name.focus();
		}

		for (String systemType : SystemTypes.getList().keySet()) {
			this.systemType.addItem(systemType);
		}
		systemType.select(system.getSystemType() != null ? system.getSystemType() : SystemTypes.getList().keySet().toArray()[0]);
		systemType.setNullSelectionAllowed(false);
		form.addField("systemType", systemType);

	}

	public boolean validateSystem() {

		try {
			form.setComponentError(null);
			form.commit();

			if (id.getValue() != null) {
				system.setID(id.getValue());
			}
			system.setName(name.getValue());
			system.setSystemType((String) systemType.getValue());

			return true;

		} catch (EmptyValueException e) {
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
}
