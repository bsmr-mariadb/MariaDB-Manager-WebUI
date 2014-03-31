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

import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * The Class ModalWindow for generic Modal Window support.
 */
public class ModalWindow extends Window {

	private static final long serialVersionUID = 0x4C656F6E6172646FL;

	/** Are we allowing the user to close the window? */
	private boolean closeAllowed = true;

	/**
	 * Instantiates a new modal window.
	 *
	 * @param caption the caption
	 * @param width the width
	 */
	public ModalWindow(String caption, String width) {
		setModal(true);
		if (width != null) {
			setWidth(width);
		}
		center();
		setCaption(caption);
		VerticalLayout layout = new VerticalLayout();
		setContent(layout);
		layout.setSpacing(true);
		layout.setMargin(true);
	}

	/* (non-Javadoc)
	 * @see com.vaadin.ui.Window#close()
	 */
	@Override
	public void close() {
		if (closeAllowed) {
			super.close();
		} else {

		}
	}

	/**
	 * Sets whether closing is allowed
	 *
	 * @param allowed the new closing allowed value
	 */
	public void setClose(boolean allowed) {
		closeAllowed = allowed;
	}
}
