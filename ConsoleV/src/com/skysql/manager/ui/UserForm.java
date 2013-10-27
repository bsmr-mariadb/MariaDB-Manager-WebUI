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

import com.skysql.manager.api.UserInfo;
import com.skysql.manager.api.UserObject;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.EmptyValueException;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.Form;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("deprecation")
public class UserForm extends VerticalLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	final TextField userName = new TextField("Username");
	final TextField fullname = new TextField("Full Name");
	final PasswordField newPassword = new PasswordField("Password");
	final PasswordField newPassword2 = new PasswordField("Confirm Password");
	final Form form = new Form();
	private UserObject user;
	private UserInfo userInfo;

	UserForm(final UserInfo userInfo, final UserObject user, String description, final Button commitButton) {
		this.userInfo = userInfo;
		this.user = user;

		setMargin(new MarginInfo(true, true, false, true));
		setSpacing(false);

		addComponent(form);
		form.setImmediate(false);
		form.setFooter(null);
		form.setDescription(description);

		String value;
		if ((value = user.getUserID()) != null) {
			userName.setValue(value);
			userName.setEnabled(false);
		} else {
			userName.setRequired(true);
			userName.setRequiredError("Username is missing");
			userName.focus();
			userName.setImmediate(true);
			userName.addValidator(new UserNameValidator());
		}
		form.addField("userName", userName);

		if ((value = user.getName()) != null) {
			fullname.setValue(value);
		}
		form.addField("fullname", fullname);

		// we don't get the user password from the API - set input prompt so bullets are displayed
		if (user.getUserID() != null) {
			newPassword.setInputPrompt("placeholder");
			newPassword2.setInputPrompt("placeholder");
		} else {
			newPassword.setRequired(true);
			newPassword.setRequiredError("Password is a required field");
			newPassword2.setRequired(true);
			newPassword2.setRequiredError("Password is a required field");
		}

		newPassword.setImmediate(true);
		newPassword.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {
				commitButton.setClickShortcut(KeyCode.ENTER);
				newPassword2.focus();
			}
		});
		form.addField("newPassword", newPassword);

		newPassword2.setImmediate(true);
		newPassword2.addValidator(new Password2Validator(newPassword));
		newPassword2.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {
				commitButton.focus();
			}
		});
		form.addField("newPassword2", newPassword2);

	}

	public boolean validateUser() {

		try {
			form.setComponentError(null);
			form.commit();

			user.setUserID(userName.getValue());
			user.setName(fullname.getValue());
			String password = newPassword.getValue();
			user.setPassword(password);

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

	class UserNameValidator implements Validator {
		private static final long serialVersionUID = 0x4C656F6E6172646FL;

		public boolean isValid(Object value) {
			if (value == null || !(value instanceof String)) {
				return false;
			}
			return (true);
		}

		// Upon failure, the validate() method throws an exception
		public void validate(Object value) throws InvalidValueException {
			if (!isValid(value)) {
				throw new InvalidValueException("Username invalid");
			} else {
				String name = (String) value;
				if (name.contains(" ")) {
					throw new InvalidValueException("Username contains illegal characters");
				} else if (userInfo != null && userInfo.findIDByName((String) value) != null) {
					throw new InvalidValueException("Username already exists");
				}
			}
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
				boolean equals = ((String) value).equals((String) otherPassword.getValue());
				return equals;
			}
		}

		// Upon failure, the validate() method throws an exception
		public void validate(Object value) throws InvalidValueException {
			if (!isValid(value)) {
				newPassword2.focus();
				throw new InvalidValueException("Passwords do not match.");
			}
		}
	}

}
