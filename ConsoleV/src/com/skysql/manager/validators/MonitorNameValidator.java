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

package com.skysql.manager.validators;

import com.skysql.manager.api.Monitors;
import com.vaadin.data.Validator;

public class MonitorNameValidator implements Validator {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private String ourName;

	public MonitorNameValidator(String ourName) {
		super();
		this.ourName = ourName;
	}

	public boolean isValid(Object value) {
		if (value == null || !(value instanceof String)) {
			return false;
		}
		return (true);
	}

	// Upon failure, the validate() method throws an exception
	public void validate(Object value) throws InvalidValueException {
		if (!isValid(value)) {
			throw new InvalidValueException("Name is invalid");
		} else {
			if (!Monitors.isNameUnique((String) value) && !value.equals(ourName)) {
				throw new InvalidValueException("Name already exists");
			}
		}
	}
}