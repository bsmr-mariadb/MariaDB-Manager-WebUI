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

package com.skysql.manager.ui.components;

import java.util.concurrent.TimeUnit;

import com.skysql.manager.DateConversion;
import com.skysql.manager.ManagerUI;
import com.skysql.manager.TaskRecord;
import com.skysql.manager.api.CommandStates;
import com.skysql.manager.api.Steps;
import com.skysql.manager.api.UserInfo;
import com.skysql.manager.ui.RunningTask;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class ScriptingProgressLayout extends HorizontalLayout {

	private UpdaterThread updaterThread;
	private Label scriptLabel, progressLabel, resultLabel;
	private HorizontalLayout progressIconsLayout;
	private VerticalLayout progressLayout, resultLayout;
	private boolean observerMode;
	private int lastIndex = -1, lastProgressIndex = 0;
	private Embedded[] taskImages;
	private String primitives[];
	private TaskRecord taskRecord;
	private long startTime, runningTime;
	private RunningTask runningTask;

	public ScriptingProgressLayout(final RunningTask runningTask, boolean observerMode) {
		this.runningTask = runningTask;
		this.observerMode = observerMode;

		addStyleName("scriptingProgressLayout");
		setSpacing(true);
		setMargin(true);

		progressLayout = new VerticalLayout();
		progressLayout.setSpacing(true);
		addComponent(progressLayout);

		scriptLabel = new Label("");
		scriptLabel.addStyleName("instructions");
		progressLayout.addComponent(scriptLabel);
		progressLayout.setComponentAlignment(scriptLabel, Alignment.TOP_CENTER);

		progressIconsLayout = new HorizontalLayout();
		progressIconsLayout.addStyleName("progressIconsLayout");
		progressLayout.addComponent(progressIconsLayout);
		progressLayout.setComponentAlignment(progressIconsLayout, Alignment.MIDDLE_CENTER);

		progressLabel = new Label("");
		progressLabel.setImmediate(true);
		progressLayout.addComponent(progressLabel);
		progressLayout.setComponentAlignment(progressLabel, Alignment.BOTTOM_CENTER);

		resultLayout = new VerticalLayout();
		resultLayout.addStyleName("scriptingResultsLayout");
		resultLayout.setSizeFull();
		resultLayout.setSpacing(true);
		resultLayout.setMargin(true);
		addComponent(resultLayout);
		//setComponentAlignment(resultLayout, Alignment.MIDDLE_LEFT);

		resultLabel = new Label(observerMode ? "Running" : "Has not run yet", ContentMode.HTML);
		resultLabel.addStyleName("instructions");
		resultLabel.setImmediate(true);
		resultLayout.addComponent(resultLabel);
		resultLayout.setComponentAlignment(resultLabel, Alignment.MIDDLE_CENTER);

	}

	public void setTitle(String value) {
		scriptLabel.setValue(value);
	}

	public void setProgress(String value) {
		progressLabel.setValue(value);
	}

	public void setResult(String value) {
		resultLabel.setValue(value);
	}

	public void start() {
		startTime = System.currentTimeMillis();
		setResult("Launching");
	}

	public void buildProgress(TaskRecord taskRecord, String command, String steps) {

		if (observerMode) {
			// String userName = Users.getUserNames().get(taskRecord.getUser());
			String userID = taskRecord.getUserID();
			UserInfo userInfo = (UserInfo) VaadinSession.getCurrent().getAttribute(UserInfo.class);
			setTitle(command + " was started on " + taskRecord.getStart() + " by " + userInfo.findNameByID(userID));
		} else {
			setTitle(command);
		}

		String[] stepIDs = steps.split(",");
		primitives = new String[stepIDs.length];
		taskImages = new Embedded[stepIDs.length];

		// add steps icons
		progressIconsLayout.removeAllComponents();
		for (int index = 0; index < stepIDs.length; index++) {
			String stepID = stepIDs[index].trim();
			String description = Steps.getDescription(stepID);

			VerticalLayout stepLayout = new VerticalLayout();
			progressIconsLayout.addComponent(stepLayout);
			stepLayout.addStyleName("stepIcons");
			Label name = new Label(stepID);
			stepLayout.addComponent(name);
			stepLayout.setComponentAlignment(name, Alignment.MIDDLE_CENTER);
			Embedded image = new Embedded(null, new ThemeResource("img/scripting/pending.png"));
			image.setImmediate(true);
			image.setDescription(description);
			stepLayout.addComponent(image);
			primitives[index] = stepID;
			taskImages[index] = image;

		}

		setProgress("");

	}

	public void refresh(TaskRecord taskRecord) {
		this.taskRecord = taskRecord;

		ManagerUI.log("ScriptingProgressLayout refresh()");
		updaterThread = new UpdaterThread(updaterThread);
		updaterThread.start();

	}

	class UpdaterThread extends Thread {
		UpdaterThread oldUpdaterThread;
		volatile boolean flagged = false;

		UpdaterThread(UpdaterThread oldUpdaterThread) {
			this.oldUpdaterThread = oldUpdaterThread;
		}

		@Override
		public void run() {
			if (oldUpdaterThread != null && oldUpdaterThread.isAlive()) {
				ManagerUI.log("ScriptingProgressLayout - Old thread is alive: " + oldUpdaterThread);
				oldUpdaterThread.flagged = true;
				oldUpdaterThread.interrupt();
				try {
					ManagerUI.log("ScriptingProgressLayout - Before Join");
					oldUpdaterThread.join();
					ManagerUI.log("ScriptingProgressLayout - After Join");
				} catch (InterruptedException iex) {
					ManagerUI.log("ScriptingProgressLayout - Interrupted Exception");
					return;
				}

			}

			ManagerUI.log("ScriptingProgressLayout - UpdaterThread.this: " + this);
			asynchRefresh(this);
		}
	}

	private void asynchRefresh(final UpdaterThread updaterThread) {

		ManagerUI managerUI = getSession().getAttribute(ManagerUI.class);

		managerUI.access(new Runnable() {
			@Override
			public void run() {
				// Here the UI is locked and can be updated

				ManagerUI.log("ScriptingProgressLayout access run(): ");

				String stateString;
				if ((stateString = taskRecord.getState()) == null) {
					return; // we're waiting for something to happen
				}
				CommandStates.States state = CommandStates.States.valueOf(stateString);
				setResult(CommandStates.getDescriptions().get(state.name()));

				String indexString;
				if ((indexString = taskRecord.getIndex()) == null) {
					return; // we're waiting for something to happen
				}
				int index = Integer.parseInt(indexString) - 1;

				while (lastProgressIndex < index) {
					taskImages[lastProgressIndex].setSource(new ThemeResource("img/scripting/past.png"));
					lastProgressIndex++;
				}

				switch (state) {
				case running:
					if (index != lastIndex) {
						setResult("Running");
						taskImages[index].setSource(new ThemeResource("img/scripting/active.png"));
						setProgress(taskImages[index].getDescription());
						lastIndex = index;
					}
					break;
				case done:
					taskImages[index].setSource(new ThemeResource("img/scripting/past.png/"));
					setProgress("Done");
					setResult("Completed successfully<br/><br/>on " + DateConversion.adjust(taskRecord.getEnd()) + "<br/><br/>in " + getRunningTime());
					runningTask.close();
					break;
				case error:
				case missing:
					taskImages[taskImages.length - 1].setSource(new ThemeResource("img/scripting/error.png"));
					setProgress("Error");
					setResult("Command failed<br/><br/>on " + DateConversion.adjust(taskRecord.getEnd()) + "<br/><br/>after " + getRunningTime()
							+ "<br/><br/>with error: " + taskRecord.getError());
					runningTask.close();
					break;
				case cancelled:
				case stopped:
					setProgress("Cancelled");
					setResult("Command cancelled<br/><br/>on " + DateConversion.adjust(taskRecord.getEnd()) + "<br/><br/>after " + getRunningTime());
					runningTask.close();
					break;
				}
			}
		});

	}

	private String getRunningTime() {
		runningTime = System.currentTimeMillis() - startTime;
		String time = String.format("%d min, %d sec", TimeUnit.MILLISECONDS.toMinutes(runningTime), TimeUnit.MILLISECONDS.toSeconds(runningTime)
				- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(runningTime)));
		return time;
	}
}
