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
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.EmptyValueException;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Form;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("deprecation")
public class SystemForm extends VerticalLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	final TextField name = new TextField("Name");
	final NativeSelect systemType = new NativeSelect("Type");
	final TextField dbUsername = new TextField("Database Username");
	final PasswordField dbPassword = new PasswordField("Database Password");
	final PasswordField dbPassword2 = new PasswordField("Confirm Password");
	final TextField repUsername = new TextField("Replication Username");
	final PasswordField repPassword = new PasswordField("Replication Password");
	final PasswordField repPassword2 = new PasswordField("Confirm Password");
	private SystemRecord system;

	final Form form = new Form();

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
		} else {
			dbUsername.setValue("skysql");
		}
		form.addField("dbusername", dbUsername);
		dbUsername.setRequired(true);
		dbUsername.setImmediate(false);
		dbUsername.setRequiredError("Database Username is a required field");
		dbUsername.addValidator(new UserNotRootValidator(dbUsername.getCaption()));

		if ((value = system.getDBPassword()) != null) {
			dbPassword.setValue(value);
		}
		form.addField("dbpassword", dbPassword);
		dbPassword.setRequired(true);
		dbPassword.setImmediate(false);
		dbPassword.setRequiredError("Database Password is a required field");

		if ((value = system.getDBPassword()) != null) {
			dbPassword2.setValue(value);
		}
		form.addField("dbpassword2", dbPassword2);
		dbPassword2.setRequired(true);
		dbPassword2.setImmediate(true);
		dbPassword2.setRequiredError("Confirm Password is a required field");
		dbPassword2.addValidator(new Password2Validator(dbPassword));

		if ((value = system.getRepUsername()) != null) {
			repUsername.setValue(value);
		} else {
			repUsername.setValue("repluser");
		}
		form.addField("repusername", repUsername);
		repUsername.setRequired(true);
		repUsername.setImmediate(true);
		repUsername.setRequiredError("Replication Username is a required field");
		repUsername.addValidator(new UserNotRootValidator(repUsername.getCaption()));
		repUsername.addValidator(new UserDifferentValidator(dbUsername));

		if ((value = system.getRepPassword()) != null) {
			repPassword.setValue(value);
		}
		form.addField("reppassword", repPassword);
		repPassword.setRequired(true);
		repPassword.setImmediate(false);
		repPassword.setRequiredError("Replication Password is a required field");

		if ((value = system.getRepPassword()) != null) {
			repPassword2.setValue(value);
		}
		form.addField("reppassword2", repPassword2);
		repPassword2.setRequired(true);
		repPassword2.setImmediate(true);
		repPassword2.setRequiredError("Confirm Password is a required field");
		repPassword2.addValidator(new Password2Validator(repPassword));
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
		} catch (InvalidValueException e) {
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	class Password2Validator implements Validator {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		private PasswordField otherPassword;

		public Password2Validator(PasswordField otherPassword) {
			super();
			this.otherPassword = otherPassword;
		}

		public boolean isValid(Object value) {
			if (value == null || !(value instanceof String)) {
				return false;
			} else {
				boolean equal = ((String) value).equals((String) otherPassword.getValue());
				return equal;
			}
		}

		// Upon failure, the validate() method throws an exception
		public void validate(Object value) throws InvalidValueException {
			if (!isValid(value)) {
				throw new InvalidValueException(otherPassword.getCaption() + " mismatch.");
			}
		}
	}

	class UserDifferentValidator implements Validator {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		private TextField otherUser;

		public UserDifferentValidator(TextField otherUser) {
			super();
			this.otherUser = otherUser;
		}

		public boolean isValid(Object value) {
			if (value == null || !(value instanceof String)) {
				return false;
			} else {
				boolean equal = ((String) value).equals((String) otherUser.getValue());
				return !equal;
			}
		}

		// Upon failure, the validate() method throws an exception
		public void validate(Object value) throws InvalidValueException {
			if (!isValid(value)) {
				throw new InvalidValueException("Database User and Replication User must be different.");
			}
		}
	}

	class UserNotRootValidator implements Validator {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		private String label;

		public UserNotRootValidator(String label) {
			super();
			this.label = label;
		}

		public boolean isValid(Object value) {
			if (value == null || !(value instanceof String)) {
				return false;
			} else {
				boolean isRoot = "root".equalsIgnoreCase((String) value);
				return !isRoot;
			}
		}

		// Upon failure, the validate() method throws an exception
		public void validate(Object value) throws InvalidValueException {
			if (!isValid(value)) {
				throw new InvalidValueException(label + " cannot be root.");
			}
		}
	}

}
