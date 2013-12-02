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
 
package com.skysql.manager.validators;

import com.vaadin.data.Validator;
import com.vaadin.ui.PasswordField;

public class Password2Validator implements Validator {
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
