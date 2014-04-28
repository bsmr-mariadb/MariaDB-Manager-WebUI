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

import com.skysql.manager.ManagerUI;
import com.skysql.manager.SystemRecord;
import com.skysql.manager.api.SystemTypes;
import com.skysql.manager.validators.Password2Validator;
import com.skysql.manager.validators.SystemNameValidator;
import com.skysql.manager.validators.UserDifferentValidator;
import com.skysql.manager.validators.UserNotRootValidator;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator.EmptyValueException;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * The Class SystemForm.
 */
@SuppressWarnings("deprecation")
public class SystemForm extends VerticalLayout {

	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	final TextField name = new TextField("Name");
	final NativeSelect systemType = new NativeSelect("Type");
	final TextField dbUsername = new TextField("Database Username");
	final PasswordField dbPassword = new PasswordField("Database Password");
	final PasswordField dbPassword2 = new PasswordField("Confirm Password");
	final TextField repUsername = new TextField("Replication Username");
	final PasswordField repPassword = new PasswordField("Replication Password");
	final PasswordField repPassword2 = new PasswordField("Confirm Password");
	final Form form = new Form();
	private SystemRecord system;

	/**
	 * Instantiates a new system form.
	 *
	 * @param system the system
	 * @param description the description
	 * @param commitButton the commit button
	 */
	SystemForm(final SystemRecord system, String description, final Button commitButton) {
		this.system = system;

		setMargin(new MarginInfo(true, true, false, true));
		setSpacing(false);

		HorizontalLayout formDescription = new HorizontalLayout();
		formDescription.setSpacing(true);

		Embedded info = new Embedded(null, new ThemeResource("img/info.png"));
		info.addStyleName("infoButton");
		String infoText = "<table border=0 cellspacing=3 cellpadding=0 summary=\"\">\n" + "     <tr bgcolor=\"#ccccff\">" + "         <th align=left>Field"
				+ "         <th align=left>Description" + "     <tr>" + "         <td><code>Name</code>" + "         <td>Name of the system"
				+ "     <tr bgcolor=\"#eeeeff\">" + "         <td><code>Type</code>" + "         <td>Type of the system e.g. aws or galera" + "     <tr>"
				+ "         <td><code>Database Username</code>" + "         <td>System default for database user name" + "     <tr bgcolor=\"#eeeeff\">"
				+ "         <td><code>Database Password</code>" + "         <td>System default for database password" + "     <tr>"
				+ "         <td><code>Replication Username</code>" + "         <td>System default for replication user name" + "     <tr bgcolor=\"#eeeeff\">"
				+ "         <td><code>Replication Password</code>" + "         <td>System default for replication password" + " </table>" + " </blockquote>";
		info.setDescription(infoText);

		formDescription.addComponent(info);
		Label labelDescription = new Label(description);
		formDescription.addComponent(labelDescription);
		formDescription.setComponentAlignment(labelDescription, Alignment.MIDDLE_LEFT);
		addComponent(formDescription);

		addComponent(form);
		form.setImmediate(false);
		form.setFooter(null);
		form.setDescription(null);

		String value;
		if ((value = system.getName()) != null) {
			name.setValue(value);
		}
		form.addField("name", name);
		name.focus();
		name.setImmediate(true);
		name.addValidator(new SystemNameValidator(system.getName()));

		for (String systemType : SystemTypes.getList().keySet()) {
			this.systemType.addItem(systemType);
		}
		systemType.select(system.getSystemType() != null ? system.getSystemType() : SystemTypes.DEFAULT_SYSTEMTYPE);
		systemType.setNullSelectionAllowed(false);
		systemType.setEnabled(false);
		form.addField("systemType", systemType);

		if ((value = system.getDBUsername()) != null) {
			dbUsername.setValue(value);
		} else {
			dbUsername.setValue("skysql");
		}
		form.addField("dbusername", dbUsername);
		dbUsername.setRequired(true);
		dbUsername.setImmediate(true);
		dbUsername.setRequiredError("Database Username is a required field");
		dbUsername.addValidator(new UserNotRootValidator(dbUsername.getCaption()));

		if ((value = system.getDBPassword()) != null) {
			dbPassword.setValue(value);
		}
		form.addField("dbpassword", dbPassword);
		dbPassword.setRequired(true);
		dbPassword.setImmediate(false);
		dbPassword.setRequiredError("Database Password is a required field");

		if ((value = system.getDBPassword()) != null) {
			dbPassword2.setValue(value);
		}
		form.addField("dbpassword2", dbPassword2);
		dbPassword2.setRequired(true);
		dbPassword2.setImmediate(true);
		dbPassword2.setRequiredError("Confirm Password is a required field");
		dbPassword2.addValidator(new Password2Validator(dbPassword));

		if ((value = system.getRepUsername()) != null) {
			repUsername.setValue(value);
		} else {
			repUsername.setValue("repluser");
		}
		form.addField("repusername", repUsername);
		repUsername.setRequired(true);
		repUsername.setImmediate(true);
		repUsername.setRequiredError("Replication Username is a required field");
		repUsername.addValidator(new UserNotRootValidator(repUsername.getCaption()));
		repUsername.addValidator(new UserDifferentValidator(dbUsername));

		if ((value = system.getRepPassword()) != null) {
			repPassword.setValue(value);
		}
		form.addField("reppassword", repPassword);
		repPassword.setRequired(true);
		repPassword.setImmediate(true);
		repPassword.setRequiredError("Replication Password is a required field");
		repPassword.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {
				commitButton.setClickShortcut(KeyCode.ENTER);
			}
		});

		if ((value = system.getRepPassword()) != null) {
			repPassword2.setValue(value);
		}
		form.addField("reppassword2", repPassword2);
		repPassword2.setRequired(true);
		repPassword2.setImmediate(true);
		repPassword2.setRequiredError("Confirm Password is a required field");
		repPassword2.addValidator(new Password2Validator(repPassword));
		repPassword2.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 0x4C656F6E6172646FL;

			public void valueChange(ValueChangeEvent event) {
				commitButton.focus();
			}
		});

	}

	/**
	 * Validate system.
	 *
	 * @return true, if successful
	 */
	public boolean validateSystem() {

		try {
			form.setComponentError(null);
			form.commit();

			system.setName(name.getValue());
			system.setSystemType((String) systemType.getValue());
			system.setDBUsername(dbUsername.getValue());
			system.setDBPassword(dbPassword.getValue());
			system.setRepUsername(repUsername.getValue());
			system.setRepPassword(repPassword.getValue());

			return true;

		} catch (EmptyValueException e) {
			return false;
		} catch (InvalidValueException e) {
			return false;
		} catch (Exception e) {
			ManagerUI.error(e.getMessage());
			return false;
		}

	}

}
