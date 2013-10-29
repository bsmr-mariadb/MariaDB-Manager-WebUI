package com.skysql.manager.validators;

import com.vaadin.data.Validator;

public class UserNotRootValidator implements Validator {
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
