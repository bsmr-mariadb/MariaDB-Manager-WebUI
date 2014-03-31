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

package com.skysql.manager.ui.components;

import java.util.LinkedHashMap;

import com.skysql.manager.ManagerUI;
import com.skysql.manager.ui.RunningTask;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.VerticalLayout;

/**
 * The Class ScriptingControlsLayout.
 */
@SuppressWarnings("serial")
public class ScriptingControlsLayout extends VerticalLayout {

	/**
	 * The Enum Controls.
	 */
	public enum Controls {
		Run, Schedule, Stop, Pause;
	}

	private LinkedHashMap<String, Button> ctrlButtons = new LinkedHashMap<String, Button>();
	private UpdaterThread updaterThread;

	/**
	 * Instantiates a new scripting controls layout.
	 *
	 * @param runningTask the running task
	 * @param controls the controls
	 */
	public ScriptingControlsLayout(final RunningTask runningTask, Controls[] controls) {

		addStyleName("scriptingControlsLayout");
		setSizeUndefined();
		setSpacing(true);
		setMargin(true);

		for (Controls control : controls) {
			final Button button = new Button(control.name());
			button.setImmediate(true);
			button.setEnabled(false);
			button.setData(control);
			addComponent(button);
			setComponentAlignment(button, Alignment.MIDDLE_CENTER);
			ctrlButtons.put(control.name(), button);
			button.addClickListener(new Button.ClickListener() {
				private static final long serialVersionUID = 0x4C656F6E6172646FL;

				public void buttonClick(ClickEvent event) {
					event.getButton().setEnabled(false);
					runningTask.controlClicked((Controls) event.getButton().getData());
				}
			});
		}

	}

	/**
	 * Enable controls.
	 *
	 * @param enable the enable
	 */
	public void enableControls(boolean enable) {
		enableControls(enable, ctrlButtons.keySet().toArray(new Controls[0]));
	}

	/**
	 * Enable controls.
	 *
	 * @param enable the enable
	 * @param control the control
	 */
	public void enableControls(boolean enable, Controls control) {
		String key = control.name();
		Button button = ctrlButtons.get(key);
		button.setEnabled(enable);
	}

	/**
	 * Enable controls.
	 *
	 * @param enable the enable
	 * @param controls the controls
	 */
	public void enableControls(boolean enable, Controls[] controls) {
		for (Controls control : controls) {
			enableControls(enable, control);
		}
	}

	/**
	 * Refresh.
	 */
	public void refresh() {

		ManagerUI.log("ScheduledLayout refresh()");
		updaterThread = new UpdaterThread(updaterThread);
		updaterThread.start();

	}

	/**
	 * The Class UpdaterThread.
	 */
	class UpdaterThread extends Thread {

		/** The old updater thread. */
		UpdaterThread oldUpdaterThread;

		/** The flagged. */
		volatile boolean flagged = false;

		/**
		 * Instantiates a new updater thread.
		 *
		 * @param oldUpdaterThread the old updater thread
		 */
		UpdaterThread(UpdaterThread oldUpdaterThread) {
			this.oldUpdaterThread = oldUpdaterThread;
		}

		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			if (oldUpdaterThread != null && oldUpdaterThread.isAlive()) {
				ManagerUI.log(this.getClass().getName() + " - Old thread is alive: " + oldUpdaterThread);
				oldUpdaterThread.flagged = true;
				oldUpdaterThread.interrupt();
				try {
					ManagerUI.log(this.getClass().getName() + " - Before Join");
					oldUpdaterThread.join();
					ManagerUI.log(this.getClass().getName() + " - After Join");
				} catch (InterruptedException iex) {
					ManagerUI.log(this.getClass().getName() + " - Interrupted Exception");
					return;
				}

			}

			ManagerUI.log(this.getClass().getName() + " - UpdaterThread.this: " + this);
			asynchRefresh(this);
		}
	}

	/**
	 * Asynch refresh.
	 *
	 * @param updaterThread the updater thread
	 */
	private void asynchRefresh(final UpdaterThread updaterThread) {

		ManagerUI managerUI = getSession().getAttribute(ManagerUI.class);

		managerUI.access(new Runnable() {
			@Override
			public void run() {
				// Here the UI is locked and can be updated

				ManagerUI.log(this.getClass().getName() + " access run(): ");

			}
		});

	}
}
