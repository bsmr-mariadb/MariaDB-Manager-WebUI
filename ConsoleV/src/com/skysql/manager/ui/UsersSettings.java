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
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

@SuppressWarnings("deprecation")
public class UsersSettings extends HorizontalLayout implements Window.CloseListener {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private TextField userName = new TextField("Full Name");
	private String currentUserID, selectedUserID;
	private ListSelect select;
	private Button changePassword, removeUser;
	private Window secondaryDialog;
	private UserInfo userInfo;

	UsersSettings() {
		addStyleName("usersTab");
		setSpacing(true);

		UserObject userObject = VaadinSession.getCurrent().getAttribute(UserObject.class);
		currentUserID = userObject.getUserID();

		VerticalLayout usersLayout = new VerticalLayout();
		addComponent(usersLayout);
		usersLayout.setSpacing(true);
		usersLayout.setMargin(true);

		// make sure we're working with current info
		userInfo = new UserInfo(null);
		VaadinSession.getCurrent().setAttribute(UserInfo.class, userInfo);

		select = new ListSelect("Users");
		select.setImmediate(true);
		for (UserObject user : userInfo.getUsersList().values()) {
			String id = user.getUserID();
			select.addItem(id);
			if (id.equals(currentUserID)) {
				select.select(id);
				userName.setValue(user.getName());
				selectedUserID = id;
			}
		}
		select.setNullSelectionAllowed(false);
		select.setWidth("14em");
		select.setRows(7);
		usersLayout.addComponent(select);
		select.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {

				selectedUserID = (String) event.getProperty().getValue();

				if (selectedUserID == null || selectedUserID.equals(currentUserID)) {
					removeUser.setEnabled(false);
				} else {
					removeUser.setEnabled(true);
				}

				if (selectedUserID == null) {
					changePassword.setEnabled(false);
					userName.setEnabled(false);
					userName.setValue("");
				} else {
					changePassword.setEnabled(true);
					userName.setValue(userInfo.findNameByID(selectedUserID));
					userName.setEnabled(true);
				}
				userName.removeAllValidators();
			}
		});

		HorizontalLayout userButtonsLayout = new HorizontalLayout();
		userButtonsLayout.setSpacing(true);
		usersLayout.addComponent(userButtonsLayout);

		Button addUser = new Button("Add...");
		addUser.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				addUser(event);
			}
		});
		userButtonsLayout.addComponent(addUser);
		userButtonsLayout.setComponentAlignment(addUser, Alignment.MIDDLE_CENTER);

		removeUser = new Button("Remove");
		removeUser.setEnabled(false);
		removeUser.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				removeUser(event);
			}
		});
		userButtonsLayout.addComponent(removeUser);
		userButtonsLayout.setComponentAlignment(removeUser, Alignment.MIDDLE_CENTER);

		VerticalLayout userLayout = new VerticalLayout();
		addComponent(userLayout);
		userLayout.setSpacing(true);
		userLayout.setMargin(true);

		userLayout.addComponent(userName);
		userName.setWidth("14em");
		userName.setImmediate(true);
		userName.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {
				if (selectedUserID != null) {
					String newName = (String) event.getProperty().getValue();
					String currentName = userInfo.findNameByID(selectedUserID);
					if (!newName.equals(currentName)) {
						userInfo.setUser(selectedUserID, newName, null);
						TopPanel topPanel = getSession().getAttribute(TopPanel.class);
						topPanel.setUserName(newName);
					}
					select.focus();
				}
			}
		});

		changePassword = new Button("Change Password");
		changePassword.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				changePassword();
			}
		});
		userLayout.addComponent(changePassword);
		userLayout.setComponentAlignment(changePassword, Alignment.MIDDLE_CENTER);

	}

	public void windowClose(CloseEvent e) {
		// reload current UserInfo after possible adds and removes
		userInfo = new UserInfo(null);
		VaadinSession.getCurrent().setAttribute(UserInfo.class, userInfo);
	}

	public void addUser(Button.ClickEvent event) {
		final TextField newUsername = new TextField("New Username");
		final TextField fullname = new TextField("Full Name");
		final PasswordField newPassword;
		final PasswordField newPassword2;
		final Button okButton = new Button("Add User");

		secondaryDialog = new ModalWindow("Add User", null);
		UI.getCurrent().addWindow(secondaryDialog);
		secondaryDialog.addCloseListener(this);

		final VerticalLayout formContainer = new VerticalLayout();
		formContainer.setMargin(new MarginInfo(true, true, false, true));
		formContainer.setSpacing(false);

		final Form form = new Form();
		formContainer.addComponent(form);
		form.setFooter(null);
		form.setImmediate(false);
		form.setDescription("Add a new administrator user");

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
		newPassword2.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {
				okButton.focus();
			}
		});

		HorizontalLayout buttonsBar = new HorizontalLayout();
		buttonsBar.setStyleName("buttonsBar");
		buttonsBar.setSizeFull();
		buttonsBar.setSpacing(true);
		buttonsBar.setMargin(true);
		buttonsBar.setHeight("49px");

		Label filler = new Label();
		buttonsBar.addComponent(filler);
		buttonsBar.setExpandRatio(filler, 1.0f);

		Button cancelButton = new Button("Cancel");
		buttonsBar.addComponent(cancelButton);
		buttonsBar.setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT);

		cancelButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				form.discard();
				secondaryDialog.close();
			}
		});

		okButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				try {
					form.setComponentError(null);
					form.commit();
					String userID = (String) newUsername.getValue();
					String name = (String) fullname.getValue();
					String password = (String) newPassword.getValue();
					boolean success = userInfo.setUser(userID, name, password);
					if (success) {
						select.addItem(userID);
					} else {
						return;
					}
				} catch (EmptyValueException e) {
					return;
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				secondaryDialog.close();
			}
		});
		buttonsBar.addComponent(okButton);
		buttonsBar.setComponentAlignment(okButton, Alignment.MIDDLE_RIGHT);

		VerticalLayout windowLayout = (VerticalLayout) secondaryDialog.getContent();
		windowLayout.setSpacing(false);
		windowLayout.setMargin(false);
		windowLayout.addComponent(formContainer);
		windowLayout.addComponent(buttonsBar);

		// attach the validators
		Validator userNameValidator = new UserNameValidator();
		newUsername.addValidator(userNameValidator);

		Validator password2Validator = new Password2Validator(newPassword);
		newPassword2.addValidator(password2Validator);

	}

	public void removeUser(Button.ClickEvent event) {
		secondaryDialog = new ModalWindow("Remove User", null);
		UI.getCurrent().addWindow(secondaryDialog);
		secondaryDialog.addCloseListener(this);

		final VerticalLayout formContainer = new VerticalLayout();
		formContainer.setMargin(new MarginInfo(true, true, false, true));
		formContainer.setSpacing(false);

		final Form form = new Form();
		formContainer.addComponent(form);
		form.setFooter(null);
		form.setDescription("Remove user " + userInfo.completeNamesByID(selectedUserID) + " from the system");

		HorizontalLayout buttonsBar = new HorizontalLayout();
		buttonsBar.setStyleName("buttonsBar");
		buttonsBar.setSizeFull();
		buttonsBar.setSpacing(true);
		buttonsBar.setMargin(true);
		buttonsBar.setHeight("49px");

		Label filler = new Label();
		buttonsBar.addComponent(filler);
		buttonsBar.setExpandRatio(filler, 1.0f);

		Button cancelButton = new Button("Cancel");
		buttonsBar.addComponent(cancelButton);
		buttonsBar.setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT);

		cancelButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				form.discard();
				secondaryDialog.close();
			}
		});

		Button okButton = new Button("Remove User");
		okButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				try {
					form.setComponentError(null);
					form.commit();
					boolean success = userInfo.deleteUser(selectedUserID);
					if (success) {
						select.removeItem(selectedUserID);
					} else {
						return;
					}
				} catch (EmptyValueException e) {
					return;
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				secondaryDialog.close();
			}
		});
		buttonsBar.addComponent(okButton);
		buttonsBar.setComponentAlignment(okButton, Alignment.MIDDLE_RIGHT);

		VerticalLayout windowLayout = (VerticalLayout) secondaryDialog.getContent();
		windowLayout.setSpacing(false);
		windowLayout.setMargin(false);
		windowLayout.addComponent(formContainer);
		windowLayout.addComponent(buttonsBar);

	}

	public void changePassword() {
		final PasswordField newPassword;
		final PasswordField newPassword2;
		final Button okButton = new Button("Change Password");

		secondaryDialog = new ModalWindow("Change Password", null);
		UI.getCurrent().addWindow(secondaryDialog);
		secondaryDialog.addCloseListener(this);

		final VerticalLayout formContainer = new VerticalLayout();
		formContainer.setMargin(new MarginInfo(true, true, false, true));
		formContainer.setSpacing(false);

		final Form form = new Form();
		formContainer.addComponent(form);
		form.setFooter(null);
		form.setImmediate(false);
		form.setDescription("Change Password for user " + userInfo.completeNamesByID(selectedUserID));

		newPassword = new PasswordField("New Password");
		newPassword2 = new PasswordField("Verify Password");
		form.addField("newPassword", newPassword);
		form.addField("newPassword2", newPassword2);
		newPassword.focus();
		newPassword2.setImmediate(true);
		newPassword2.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {
				okButton.focus();
			}
		});

		HorizontalLayout buttonsBar = new HorizontalLayout();
		buttonsBar.setStyleName("buttonsBar");
		buttonsBar.setSizeFull();
		buttonsBar.setSpacing(true);
		buttonsBar.setMargin(true);
		buttonsBar.setHeight("49px");

		Label filler = new Label();
		buttonsBar.addComponent(filler);
		buttonsBar.setExpandRatio(filler, 1.0f);

		Button cancelButton = new Button("Cancel");
		buttonsBar.addComponent(cancelButton);
		buttonsBar.setComponentAlignment(cancelButton, Alignment.MIDDLE_RIGHT);

		cancelButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				form.discard();
				secondaryDialog.close();
			}
		});

		okButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				try {
					form.setComponentError(null);
					form.commit();
					String password = (String) newPassword.getValue();
					boolean success = userInfo.setUser(selectedUserID, userInfo.findNameByID(selectedUserID), password);
					if (!success) {
						return;
					}
				} catch (EmptyValueException e) {
					return;
				} catch (InvalidValueException e) {
					return;
				}
				secondaryDialog.close();
			}
		});
		buttonsBar.addComponent(okButton);
		buttonsBar.setComponentAlignment(okButton, Alignment.MIDDLE_RIGHT);

		VerticalLayout windowLayout = (VerticalLayout) secondaryDialog.getContent();
		windowLayout.setSpacing(false);
		windowLayout.setMargin(false);
		windowLayout.addComponent(formContainer);
		windowLayout.addComponent(buttonsBar);

		Validator password2Validator = new Password2Validator(newPassword);
		newPassword2.addValidator(password2Validator);

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
				} else if (userInfo.findIDByName((String) value) != null) {
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
				boolean match = ((String) value).matches((String) otherPassword.getValue());
				return match;
			}
		}

		// Upon failure, the validate() method throws an exception
		public void validate(Object value) throws InvalidValueException {
			if (!isValid(value)) {
				throw new InvalidValueException("Passwords do not match.");
			}
		}
	}
}
