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
 * Copyright 2012-2014 SkySQL Ab
 */

package com.skysql.manager.ui;

import com.skysql.java.Logging;
import com.skysql.manager.api.UserInfo;
import com.skysql.manager.api.UserObject;
import com.skysql.manager.validators.Password2Validator;
import com.skysql.manager.validators.UserNameValidator;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
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

	UserForm(final UserInfo userInfo, final UserObject user, String description, final Button commitButton) {
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
			userName.addValidator(new UserNameValidator(userInfo));
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
			user.setPassword(newPassword.getValue());

			return true;

		} catch (EmptyValueException e) {
			return false;
		} catch (InvalidValueException e) {
			return false;
		} catch (Exception e) {
//			e.printStackTrace();
			Logging.error(e.getMessage());
			return false;
		}

	}

}
