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
import com.vaadin.data.Validator.EmptyValueException;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Form;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("deprecation")
public class NodeForm extends VerticalLayout {
	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	final TextField id = new TextField("ID");
	final TextField name = new TextField("Name");
	final TextField hostname = new TextField("Hostname");
	final TextField instanceID = new TextField("Instance ID");
	final TextField publicIP = new TextField("Public IP");
	final TextField privateIP = new TextField("Private IP");
	final TextField username = new TextField("Username");
	final TextField password = new TextField("Password");
	final Form form = new Form();
	private NodeInfo node;

	NodeForm(final NodeInfo node, String description) {
		this.node = node;

		setMargin(new MarginInfo(true, true, false, true));
		setSpacing(false);

		addComponent(form);
		form.setImmediate(false);
		form.setFooter(null);
		form.setDescription(description);

		if (node.getID() == null) {
			form.addField("id", id);
			id.focus();
		}

		String value;
		if ((value = node.getName()) != null) {
			name.setValue(value);
		}
		form.addField("name", name);
		if (node.getID() != null) {
			name.focus();
		}

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
		publicIP.setRequired(true);
		publicIP.setRequiredError("Public IP is a required field");

		if ((value = node.getPrivateIP()) != null) {
			privateIP.setValue(value);
		}
		form.addField("privateIP", privateIP);
		privateIP.setRequired(true);
		privateIP.setRequiredError("Private IP is a required field");

		if ((value = node.getUsername()) != null) {
			username.setValue(value);
		}
		form.addField("username", username);

		if ((value = node.getPassword()) != null) {
			password.setValue(value);
		}
		form.addField("password", password);

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
			node.setUsername(username.getValue());
			node.setPassword(password.getValue());

			return true;

		} catch (EmptyValueException e) {
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}

	public void setDescription(String description) {
		form.setDescription(description);
	}
}
