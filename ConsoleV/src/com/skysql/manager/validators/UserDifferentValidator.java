package com.skysql.manager.validators;

import com.vaadin.data.Validator;
import com.vaadin.ui.TextField;

public class UserDifferentValidator implements Validator {
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
