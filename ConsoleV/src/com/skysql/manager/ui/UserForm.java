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

import com.skysql.manager.api.UserObject;
import com.vaadin.data.Validator.EmptyValueException;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Form;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("deprecation")
public class UserForm extends VerticalLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	final TextField newUsername = new TextField("New Username");
	final TextField fullname = new TextField("Full Name");
	final PasswordField newPassword;
	final PasswordField newPassword2;
	final Form form = new Form();
	private UserObject user;

	UserForm(final UserObject user, String description) {
		this.user = user;

		setMargin(new MarginInfo(true, true, false, true));
		setSpacing(false);

		addComponent(form);
		form.setImmediate(false);
		form.setFooter(null);
		form.setDescription(description);

		form.addField("newUsername", newUsername);
		form.getField("newUsername").setRequired(true);
		form.getField("newUsername").setRequiredError("Username is missing");
		newUsername.focus();
		form.addField("fullname", fullname);
		newPassword = new PasswordField("New Password");
		newPassword2 = new PasswordField("Verify Password");
		form.addField("newPassword", newPassword);
		form.addField("newPassword2", newPassword2);
		newPassword2.setImmediate(true);
	}

	public boolean validateUser() {

		try {
			form.setComponentError(null);
			form.commit();

			user.setUserID(newUsername.getValue());
			user.setName(fullname.getValue());
			user.setPassword(newPassword.getValue());

			return true;

		} catch (EmptyValueException e) {
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

}
