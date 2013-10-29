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
