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

package com.skysql.manager.ui;

import com.skysql.manager.api.UserInfo;
import com.skysql.manager.api.UserObject;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.EmptyValueException;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;

/**
 * The Class UsersSettings.
 */
@SuppressWarnings("deprecation")
public class UsersSettings extends VerticalLayout implements Window.CloseListener {

	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	private Label userName = new Label("Full Name");
	private String currentUserID, selectedUserID;
	private ListSelect select;
	private Button editUser, removeUser;
	private Window secondaryDialog;
	private UserInfo userInfo;
	private FormLayout userLayout;
	private final UsersSettings thisObject = this;

	/**
	 * Instantiates a new users settings.
	 */
	UsersSettings() {
		addStyleName("usersTab");
		setSizeFull();
		setSpacing(true);
		setMargin(true);

		UserObject currentUser = VaadinSession.getCurrent().getAttribute(UserObject.class);
		currentUserID = currentUser.getUserID();

		HorizontalLayout usersLayout = new HorizontalLayout();
		addComponent(usersLayout);
		usersLayout.setSizeFull();
		usersLayout.setSpacing(true);

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
					editUser.setEnabled(false);
					userName.setEnabled(false);
					userName.setValue("");
				} else {
					editUser.setEnabled(true);
					userName.setValue(userInfo.findNameByID(selectedUserID));
					userName.setEnabled(true);
				}
			}
		});

		usersLayout.addLayoutClickListener(new LayoutClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void layoutClick(LayoutClickEvent event) {

				Component child;
				if (event.isDoubleClick() && (child = event.getChildComponent()) != null && (child instanceof ListSelect)) {
					// Get the child component which was double-clicked
					ListSelect select = (ListSelect) child;
					String userID = (String) select.getValue();
					new UserDialog(userInfo, userInfo.getUsersList().get(selectedUserID), thisObject);
				}
			}
		});

		userLayout = new FormLayout();
		usersLayout.addComponent(userLayout);
		usersLayout.setExpandRatio(userLayout, 1.0f);
		userLayout.setSpacing(false);

		userName.setCaption("Full Name:");
		userLayout.addComponent(userName);

		HorizontalLayout userButtonsLayout = new HorizontalLayout();
		userButtonsLayout.setSpacing(true);
		addComponent(userButtonsLayout);

		Button addUser = new Button("Add...");
		addUser.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				new UserDialog(userInfo, null, thisObject);
			}
		});
		userButtonsLayout.addComponent(addUser);
		userButtonsLayout.setComponentAlignment(addUser, Alignment.MIDDLE_LEFT);

		removeUser = new Button("Delete");
		removeUser.setEnabled(false);
		removeUser.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				removeUser(event);
			}
		});
		userButtonsLayout.addComponent(removeUser);
		userButtonsLayout.setComponentAlignment(removeUser, Alignment.MIDDLE_LEFT);

		editUser = new Button("Edit...");
		editUser.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void buttonClick(ClickEvent event) {
				new UserDialog(userInfo, userInfo.getUsersList().get(selectedUserID), thisObject);
			}
		});
		userButtonsLayout.addComponent(editUser);
		userButtonsLayout.setComponentAlignment(editUser, Alignment.MIDDLE_CENTER);

	}

	/**
	 * Adds a user to the list.
	 *
	 * @param userID the user id
	 */
	public void addToSelect(String userID) {
		select.addItem(userID);
		select.select(userID);
		updateUserName(userID);
	}

	/**
	 * Update user name.
	 *
	 * @param userID the user id
	 */
	public void updateUserName(String userID) {
		String name = userInfo.findAnyNameByID(userID);
		userName.setValue(name);
		if (userID.equals(currentUserID)) {
			TopPanel topPanel = getSession().getAttribute(TopPanel.class);
			topPanel.setUserName(name);
		}
	}

	/* (non-Javadoc)
	 * @see com.vaadin.ui.Window.CloseListener#windowClose(com.vaadin.ui.Window.CloseEvent)
	 */
	public void windowClose(CloseEvent e) {
		// reload current UserInfo after possible adds and removes
		userInfo = new UserInfo(null);
		VaadinSession.getCurrent().setAttribute(UserInfo.class, userInfo);
	}

	/**
	 * Removes the user.
	 *
	 * @param event the event
	 */
	public void removeUser(Button.ClickEvent event) {
		secondaryDialog = new ModalWindow("Delete User", null);
		UI.getCurrent().addWindow(secondaryDialog);
		secondaryDialog.addCloseListener(this);

		final VerticalLayout formContainer = new VerticalLayout();
		formContainer.setMargin(new MarginInfo(true, true, false, true));
		formContainer.setSpacing(false);

		final Form form = new Form();
		formContainer.addComponent(form);
		form.setFooter(null);
		form.setDescription("Delete user " + userInfo.completeNamesByID(selectedUserID) + " from the system");

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

		Button okButton = new Button("Delete User");
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

}
