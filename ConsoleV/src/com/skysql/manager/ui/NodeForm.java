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

import com.skysql.manager.api.NodeInfo;
import com.skysql.manager.api.SystemTypes;
import com.skysql.manager.validators.Password2Validator;
import com.skysql.manager.validators.PasswordOrKeyValidator;
import com.skysql.manager.validators.UserNotRootValidator;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.EmptyValueException;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("deprecation")
public class NodeForm extends VerticalLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	final static String connectionInfo = "If this node is available now, a connection will be attempted using either the root password or ssh key provided by the user. If it is not, a node representation will be created in the system and when it becomes available, the user can run the connect command from the Control panel. In both cases, neither the password nor the key will be stored or retained beyond the attempt to establish the initial connection and will be discarded whether this is successful or not.";

	final TextField name = new TextField("Name");
	final TextField hostname = new TextField("Hostname");
	final TextField instanceID = new TextField("Instance ID");
	final TextField publicIP = new TextField("Public IP");
	final TextField privateIP = new TextField("Private IP");
	final TextField dbUsername = new TextField("Database Username");
	final PasswordField dbPassword = new PasswordField("Database Password");
	final PasswordField dbPassword2 = new PasswordField("Confirm Password");
	final TextField repUsername = new TextField("Replication Username");
	final PasswordField repPassword = new PasswordField("Replication Password");
	final PasswordField repPassword2 = new PasswordField("Confirm Password");
	final PasswordField connectPassword = new PasswordField("Root Password");
	final PasswordField connectPassword2 = new PasswordField("Confirm Password");
	final TextField connectKey = new TextField("SSH Key");
	final Validator connectValidator = new PasswordOrKeyValidator(connectPassword);

	final Form form = new Form();
	private NodeInfo node;
	public boolean runConnect = false;
	public boolean isGalera = false;

	NodeForm(final NodeInfo node, String description) {
		this.node = node;

		setMargin(new MarginInfo(true, true, false, true));
		setSpacing(false);

		addComponent(form);
		form.setImmediate(false);
		form.setFooter(null);
		form.setDescription(description);

		isGalera = node.getSystemType().equals(SystemTypes.Type.galera.name());

		String value;
		if ((value = node.getName()) != null) {
			name.setValue(value);
			name.focus();
		}
		form.addField("name", name);

		if ((value = node.getHostname()) != null) {
			hostname.setValue(value);
		}
		form.addField("hostname", hostname);

		if ((value = node.getInstanceID()) != null) {
			instanceID.setValue(value);
		}
		form.addField("instanceID", instanceID);

		if ((value = node.getPublicIP()) != null) {
			publicIP.setValue(value);
		}
		form.addField("publicIP", publicIP);

		if ((value = node.getPrivateIP()) != null) {
			privateIP.setValue(value);
		}
		form.addField("privateIP", privateIP);
		privateIP.setRequired(true);
		privateIP.setRequiredError("Private IP is a required field");

		if (!isGalera) {
			if ((value = node.getDBUsername()) != null) {
				dbUsername.setValue(value);
			}
			form.addField("dbusername", dbUsername);
			dbUsername.setRequired(true);
			dbUsername.setImmediate(false);
			dbUsername.setRequiredError("Database Username is a required field");
			dbUsername.addValidator(new UserNotRootValidator(dbUsername.getCaption()));

			if ((value = node.getDBPassword()) != null) {
				dbPassword.setValue(value);
			}
			form.addField("dbpassword", dbPassword);
			dbPassword.setRequired(true);
			dbPassword.setImmediate(false);
			dbPassword.setRequiredError("Database Password is a required field");

			if ((value = node.getDBPassword()) != null) {
				dbPassword2.setValue(value);
			}
			form.addField("dbpassword2", dbPassword2);
			dbPassword2.setRequired(true);
			dbPassword2.setImmediate(true);
			dbPassword2.setRequiredError("Confirm Password is a required field");
			dbPassword2.addValidator(new Password2Validator(dbPassword));
		}

		if (!isGalera) {
			if ((value = node.getRepUsername()) != null) {
				repUsername.setValue(value);
			}
			form.addField("repusername", repUsername);
			repUsername.setRequired(true);
			repUsername.setImmediate(false);
			repUsername.setRequiredError("Replication Username is a required field");
			repUsername.addValidator(new UserNotRootValidator(repUsername.getCaption()));

			if ((value = node.getRepPassword()) != null) {
				repPassword.setValue(value);
			}
			form.addField("reppassword", repPassword);
			repPassword.setRequired(true);
			repPassword.setImmediate(false);
			repPassword.setRequiredError("Replication Password is a required field");

			if ((value = node.getRepPassword()) != null) {
				repPassword2.setValue(value);
			}
			form.addField("reppassword2", repPassword2);
			repPassword2.setRequired(true);
			repPassword2.setImmediate(true);
			repPassword2.setRequiredError("Confirm Password is a required field");
			repPassword2.addValidator(new Password2Validator(repPassword));
		}

		if (node.getID() == null) {
			Layout layout = form.getLayout();

			HorizontalLayout optionLayout = new HorizontalLayout();
			optionLayout.addStyleName("formInfoLayout");
			optionLayout.setSpacing(true);
			layout.addComponent(optionLayout);

			Embedded info = new Embedded(null, new ThemeResource("img/info.png"));
			info.addStyleName("infoButton");
			info.setDescription(connectionInfo);
			optionLayout.addComponent(info);

			final OptionGroup option = new OptionGroup("Connection options");
			option.addItem(true);
			option.setItemCaption(true, "Node is available now, connect automatically.");
			option.addItem(false);
			option.setItemCaption(false, "Node is not available, user will run connect later.");
			option.setImmediate(true);
			option.addValueChangeListener(new Property.ValueChangeListener() {
				private static final long serialVersionUID = 0x4C656F6E6172646FL;

				@Override
				public void valueChange(ValueChangeEvent event) {
					runConnect = (Boolean) event.getProperty().getValue();
					connectPassword.setEnabled(runConnect);
					connectPassword2.setEnabled(runConnect);
					connectKey.setEnabled(runConnect);
					if (!runConnect) {
						connectKey.removeValidator(connectValidator);
					}
				}
			});
			optionLayout.addComponent(option);
			option.select(true);

			form.addField("connectPassword", connectPassword);
			connectPassword.setImmediate(false);

			form.addField("connectPassword2", connectPassword2);
			connectPassword2.setImmediate(true);
			connectPassword2.addValidator(new Password2Validator(connectPassword));

			form.addField("connectKey", connectKey);
			connectKey.setImmediate(true);
		}

	}

	public void setDescription(String description) {
		form.setDescription(description);
	}

	public boolean validateNode() {

		try {
			form.setComponentError(null);
			form.commit();

			node.setName(name.getValue());
			node.setHostname(hostname.getValue());
			node.setInstanceID(instanceID.getValue());
			node.setPublicIP(publicIP.getValue());
			node.setPrivateIP(privateIP.getValue());
			if (!isGalera) {
				node.setDBUsername(dbUsername.getValue());
				node.setDBPassword(dbPassword.getValue());
				node.setRepUsername(repUsername.getValue());
				node.setRepPassword(repPassword.getValue());

			}
			if (runConnect) {
				// get and validate password and/or key logic, or do nothing
				connectKey.addValidator(connectValidator);
				connectKey.validate();
			}

			return true;

		} catch (EmptyValueException e) {
			return false;
		} catch (InvalidValueException e) {
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

}
