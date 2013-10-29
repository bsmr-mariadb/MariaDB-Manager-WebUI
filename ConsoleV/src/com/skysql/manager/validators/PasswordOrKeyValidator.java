package com.skysql.manager.validators;

import com.vaadin.data.Validator;
import com.vaadin.ui.PasswordField;

public class PasswordOrKeyValidator implements Validator {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private PasswordField passwordField;

	public PasswordOrKeyValidator(PasswordField passwordField) {
		super();
		this.passwordField = passwordField;
	}

	public boolean isValid(Object value) {
		if (value == null || !(value instanceof String)) {
			return false;
		} else {
			boolean isKey = !String.valueOf(value).isEmpty();
			boolean isPassword = !String.valueOf(passwordField.getValue()).isEmpty();
			return isKey || isPassword;
		}
	}

	// Upon failure, the validate() method throws an exception
	public void validate(Object value) throws InvalidValueException {
		if (!isValid(value)) {
			throw new InvalidValueException("Either Root Password or SSH Key must be provided.");
		}
	}
}
