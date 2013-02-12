package com.skysql.consolev.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


import com.skysql.consolev.BackupRecord;
import com.skysql.consolev.ExecutorFactory;
import com.skysql.consolev.SessionData;
import com.skysql.consolev.StepRecord;
import com.skysql.consolev.TaskRecord;
import com.skysql.consolev.api.BackupStates;
import com.skysql.consolev.api.Backups;
import com.skysql.consolev.api.CommandSteps;
import com.skysql.consolev.api.Commands;
import com.skysql.consolev.api.NodeInfo;
import com.skysql.consolev.api.Steps;
import com.skysql.consolev.api.TaskInfo;
import com.skysql.consolev.api.TaskRun;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;


public final class RunningTask {

	private static final String NOT_AVAILABLE = "n/a";
	private static final String CMD_BACKUP = "7";
	private static final String CMD_RESTORE = "8";
	private VerticalLayout containerLayout, scriptingProgressLayout, scriptingControlsLayout, scriptingResultLayout;
	private HorizontalLayout scriptingLayout, progressIconsLayout;
	private Label scriptLabel, progressLabel, resultLabel;
	
	  /** If invocations might overlap, you can specify more than a single thread.*/ 
	private static final int NUM_THREADS = 6;
	private static final boolean DONT_INTERRUPT_IF_RUNNING = false;
	private ScheduledExecutorService fScheduler = ExecutorFactory.getScheduler(NUM_THREADS);
	private long fInitialDelay;
	private long fDelayBetweenRuns;
	private long fShutdownAfter;
	private ScheduledFuture<?> runTimerFuture, cancelTimerFuture;

	private long startTime, runningTime;
	private Embedded[] taskImages;
	private LinkedHashMap<String, NativeButton> ctrlButtons = new LinkedHashMap<String, NativeButton>();
	private String controls[], primitives[];
	private int lastIndex = -1, lastProgressIndex = 0;
	private String command, params;
	private NodeInfo nodeInfo;
	private TaskRecord taskRecord;
	private boolean observerMode;
	private boolean paramSelected;
	private GridLayout backupInfoGrid;
	private Link backupLogLink;
	private SessionData session;
	private ListSelect commandSelect;
	
	RunningTask(String command, NodeInfo nodeInfo, SessionData session, ListSelect commandSelect) {
		this.command = command;
		this.nodeInfo = nodeInfo;
		this.session = session;
		this.commandSelect = commandSelect;
		
		if (command == null) {
			observerMode = true;
			TaskInfo taskInfo = new TaskInfo(nodeInfo.getTask(), null, null, null);
			taskRecord = taskInfo.getTasksList().get(0);
			command = taskRecord.getCommand();
		}

		if (command.equalsIgnoreCase(CMD_BACKUP) || command.equalsIgnoreCase(CMD_RESTORE)) {
			nodeInfo.setBackupTask(this);
		} else {
			nodeInfo.setCommandTask(this);			
		}

		containerLayout = new VerticalLayout();
		containerLayout.addStyleName("containerLayout");
		containerLayout.setSizeFull();
		
        scriptingLayout = new HorizontalLayout();
        scriptingLayout.setSpacing(true);
        scriptingLayout.setSizeFull();
        containerLayout.addComponent(scriptingLayout);
        
		if (command.equalsIgnoreCase(CMD_BACKUP) || command.equalsIgnoreCase(CMD_RESTORE)) {

			// add PARAMETER layout
			final VerticalLayout parameterLayout = new VerticalLayout();
			parameterLayout.setSizeFull();
			parameterLayout.setSpacing(true);
			parameterLayout.setMargin(true);
			scriptingLayout.addComponent(parameterLayout);
			scriptingLayout.setComponentAlignment(parameterLayout, Alignment.MIDDLE_LEFT);
	
	        Label commandsLabel = new Label("", Label.CONTENT_PREFORMATTED);
	        commandsLabel.addStyleName("instructions");
	        parameterLayout.addComponent(commandsLabel);
	        parameterLayout.setComponentAlignment(commandsLabel, Alignment.TOP_CENTER);
			
			// COLUMN 1. PARAMETERS
			if (command.equalsIgnoreCase(CMD_BACKUP)) {
	            OptionGroup group = new OptionGroup("Backup Level");
	            group.setImmediate(true);
	            group.addItem("Full");
	            group.addItem("Incremental");
	            parameterLayout.addComponent(group);
	            
	            //*** temporary
	            group.setValue("Full");
	            group.setEnabled(false);
	            
	            /***  temporary
	            group.addListener(new ValueChangeListener() {
	                public void valueChange(ValueChangeEvent event) {
	                    selectParameter((String)event.getProperty().getValue());
	                }
	            });
	            ***/
	            
	        } else if (command.equalsIgnoreCase(CMD_RESTORE)) {
	        	final HorizontalLayout restoreLayout = new HorizontalLayout();
	        	parameterLayout.addComponent(restoreLayout);
	        	
	        	String firstItem = null;
	        	ListSelect select = new ListSelect("Backups");
	        	select.setImmediate(true);
	        	final Backups backups = new Backups(nodeInfo.getSystemID(), null); 
	        	final LinkedHashMap<String, BackupRecord> backupsList = backups.getBackupsList();
	        	if (backupsList != null) {
					Collection<BackupRecord> set = backupsList.values();
					Iterator<BackupRecord> iter = set.iterator();
					while(iter.hasNext()) {
						BackupRecord backupRecord = iter.next();
						String started = backupRecord.getStarted();
						select.addItem(started);
						if (firstItem == null) {
							firstItem = started;
						}
					} 
			        select.setNullSelectionAllowed(false);
					select.setRows(8);   // Show a few items and a scrollbar if there are more
					restoreLayout.addComponent(select);
		            
					final VerticalLayout backupInfoLayout = new VerticalLayout();
					backupInfoLayout.setMargin(true);
					restoreLayout.addComponent(backupInfoLayout);
					
					select.addListener(new ValueChangeListener() {
						private static final long serialVersionUID = 0x4C656F6E6172646FL;
		                public void valueChange(ValueChangeEvent event) {
		        			String date = (String)event.getProperty().getValue();
		                	Collection<BackupRecord> set = backupsList.values();
		        			Iterator<BackupRecord> iter = set.iterator();
		        			while(iter.hasNext()) {
		        				BackupRecord backupRecord = iter.next();
		        				String started = backupRecord.getStarted();
		        				if (date.equalsIgnoreCase(started)) {
		        					displayBackupInfo(backupInfoLayout, backupRecord);
		        					selectParameter(backupRecord.getID());
		        					break;
		        				}
		        			}
		
		                }
		            });
					
					select.select(firstItem);
					
					//final DisplayBackupRecord displayRecord = new DisplayBackupRecord(parameterLayout);
	        	} else {
	                VerticalLayout messageLayout = new VerticalLayout();
	                messageLayout.addStyleName("placeholderLayout");
	                messageLayout.setSizeUndefined();
	
	                Label placeholderLabel = new Label("No Backups are available for Restore");
	                placeholderLabel.addStyleName("placeholder");
	                messageLayout.addComponent(placeholderLabel);
	
	                scriptingLayout.addComponent(messageLayout);
	                scriptingLayout.setComponentAlignment(messageLayout, Alignment.MIDDLE_CENTER);
	           	}
	
	        }
		}

		// COLUMN 2. CONTROLS
        scriptingControlsLayout = new VerticalLayout();
        scriptingControlsLayout.addStyleName("scriptingControlsLayout");
        scriptingControlsLayout.setSizeFull();
        scriptingControlsLayout.setSpacing(true);
        scriptingControlsLayout.setMargin(true);
        scriptingLayout.addComponent(scriptingControlsLayout);
        scriptingLayout.setComponentAlignment(scriptingControlsLayout, Alignment.MIDDLE_LEFT);

        {
        Label label = new Label("");
        scriptingControlsLayout.addComponent(label);
		scriptingControlsLayout.setComponentAlignment(label, Alignment.TOP_CENTER);
        }


		// COLUMN 3. PROGRESS
        scriptingProgressLayout = new VerticalLayout();
        scriptingProgressLayout.addStyleName("scriptingProgressLayout");
        scriptingProgressLayout.setSizeFull();
        scriptingProgressLayout.setSpacing(true);
        scriptingProgressLayout.setMargin(true);        
        scriptingLayout.addComponent(scriptingProgressLayout);
        scriptingLayout.setComponentAlignment(scriptingProgressLayout, Alignment.MIDDLE_LEFT);

        scriptLabel = new Label("");
        scriptLabel.addStyleName("instructions");
        scriptingProgressLayout.addComponent(scriptLabel);
        scriptingProgressLayout.setComponentAlignment(scriptLabel, Alignment.TOP_CENTER);
        
        progressIconsLayout = new HorizontalLayout();
        progressIconsLayout.addStyleName("progressIconsLayout");
        scriptingProgressLayout.addComponent(progressIconsLayout);
       
        progressLabel = new Label("");
        progressLabel.setImmediate(true);
        scriptingProgressLayout.addComponent(progressLabel);
        scriptingProgressLayout.setComponentAlignment(progressLabel, Alignment.MIDDLE_CENTER);


		// COLUMN 4. RESULT
        scriptingResultLayout = new VerticalLayout();
        scriptingResultLayout.addStyleName("scriptingResultsLayout");
        scriptingResultLayout.setSizeFull();
        scriptingResultLayout.setSpacing(true);
        scriptingResultLayout.setMargin(true);        
        scriptingLayout.addComponent(scriptingResultLayout);
        scriptingLayout.setComponentAlignment(scriptingResultLayout, Alignment.MIDDLE_LEFT);

        {
        Label label = new Label("");
        label.addStyleName("instructions");
        scriptingResultLayout.addComponent(label);
        scriptingResultLayout.setComponentAlignment(label, Alignment.TOP_CENTER);
        }

        resultLabel = new Label("Has not run yet", Label.CONTENT_RAW);
        resultLabel.addStyleName("instructions");
        resultLabel.setImmediate(true);
        scriptingResultLayout.addComponent(resultLabel);
        scriptingResultLayout.setComponentAlignment(resultLabel, Alignment.TOP_CENTER);
        
        
        //******* BUILD COLUMN 2 - CONTROL
        String commandName = Commands.getNames().get(command);
        
		// observer mode
		if (observerMode) {
			//String userName = Users.getUserNames().get(taskRecord.getUser());
			String userName = taskRecord.getUser();
			String started = taskRecord.getStart();
			
			final Label label = new Label("The " + commandName + " command<br>was started at " + started + "<br>by " + userName, Label.CONTENT_RAW);
			label.addStyleName("instructions");
			label.setImmediate(true);
			scriptingControlsLayout.addComponent(label);
			scriptingControlsLayout.setComponentAlignment(label, Alignment.TOP_CENTER);
			
		} else {
			// add task controls
			// controls = taskRun.getControls();   this is for when they are server-side driven
			controls = new String[] {"run"};
		
			for (String control : controls) {
				final NativeButton button = new NativeButton();
				button.addStyleName(control);
				button.setImmediate(true);
				button.setDescription(control); // this should be a proper description
				button.setData(control);
				scriptingControlsLayout.addComponent(button);
				scriptingControlsLayout.setComponentAlignment(button, Alignment.TOP_CENTER);
				ctrlButtons.put(control, button);
				button.addListener(new Button.ClickListener() {
					private static final long serialVersionUID = 0x4C656F6E6172646FL;
					public void buttonClick(ClickEvent event) {
						event.getButton().setEnabled(false);
						scriptCommand((String)event.getButton().getData());
					}
				});
			}
		}
		
		//*********  BUILD COLUMN 3 - PROGRESS
		
		scriptLabel.setValue(commandName);
		/***
        {
		Embedded image = new Embedded(commandName, new ThemeResource("img/scripting/script_small.png"));
		image.addStyleName("stepIcons");
		image.setImmediate(true);
		image.setAlternateText(commandName);
		image.setDescription(Commands.getDescriptions().get(command));
		progressIconsLayout.addComponent(image);
		progressIconsLayout.setComponentAlignment(image, Alignment.TOP_CENTER);
        }
        ***/
		
        Steps steps = new Steps("bogus");  // bogus param not to call empty constructor
		LinkedHashMap<String, StepRecord> stepRecords = steps.getStepsList();
		
		LinkedHashMap<String, String[]> stepsMap = CommandSteps.getStepsMap();
		String[] stepsIDs = stepsMap.get(command);
		
		primitives = new String[stepsIDs.length];
		taskImages = new Embedded[stepsIDs.length+1];  // allow for one more for the "done" icon
		// add steps icons
		for (int index = 0; index < stepsIDs.length; index++) {
			StepRecord stepRecord = stepRecords.get(stepsIDs[index]);
			String stepName = stepRecord.getScript(); // this should really be another column!
			String iconName = stepRecord.getIcon();
			String description = stepRecord.getDescription();

			Embedded image = new Embedded(null, new ThemeResource("img/scripting/pending/" + iconName + ".png"));
			image.addStyleName("stepIcons");
			image.setImmediate(true);
			image.setAlternateText(stepName);
			image.setDescription(description);
			progressIconsLayout.addComponent(image);
			primitives[index] = iconName;
			taskImages[index] = image;

		}
		Embedded image = new Embedded(null, new ThemeResource("img/scripting/pending/done.png"));
		image.addStyleName("stepIcons");
		image.setImmediate(true);
		image.setAlternateText("Done");
		image.setDescription("Done");
		progressIconsLayout.addComponent(image);
		taskImages[stepsIDs.length] = image;
		
		//*** Temporary
		if (command.equalsIgnoreCase(CMD_BACKUP)) {
			selectParameter("Full");
		}

		        	
	}

	public VerticalLayout getLayout() {
		return containerLayout;
	}

	public String getCommand() {
		return command;
	}
	
	private String backupLabels[] = {
            "Node",
			"Level",
            "State",
            "Size",
            "Updated",
            "Restored"
    };

	final public void displayBackupInfo(VerticalLayout layout, BackupRecord record) {
		String value;
		String values[] = {
			(value = record.getID()) != null ? value : NOT_AVAILABLE,
			(value = record.getLevel()) != null ? value : NOT_AVAILABLE,
			((value = record.getStatus()) != null) && (value = BackupStates.getDescriptions().get(value)) != null ? value : "Invalid",
			(value = record.getSize()) != null ? value : NOT_AVAILABLE,
			(value = record.getUpdated()) != null ? value : NOT_AVAILABLE,
			(value = record.getRestored()) != null ? value : "" 
		};
		
		GridLayout newBackupInfoGrid = new GridLayout(2, backupLabels.length);
        for (int i=0; i < backupLabels.length; i++) {
        	newBackupInfoGrid.addComponent(new Label(backupLabels[i]), 0, i);
        	newBackupInfoGrid.addComponent(new Label(values[i]), 1, i);
        }
        
        if (backupInfoGrid == null) {
        	layout.addComponent(newBackupInfoGrid);
        	backupInfoGrid = newBackupInfoGrid;
        } else {
        	layout.replaceComponent(backupInfoGrid, newBackupInfoGrid);
        	backupInfoGrid = newBackupInfoGrid;
        }	
		
        LinkedHashMap<String,String> sysProperties = session.getSystemProperties().getProperties();
        String EIP = sysProperties.get("EIP");
    	if (EIP != null) {
    		String url = "http://" + EIP + "/consoleAPI/" + record.getLog();
    		Link newBackupLogLink = new Link("Backup Log", new ExternalResource(url));
    		newBackupLogLink.setTargetName("_blank");
    		newBackupLogLink.setDescription("Open backup log in a new window");
    		newBackupLogLink.setIcon(new ThemeResource("img/externalLink.png"));
    		newBackupLogLink.addStyleName("icon-after-caption");
    		if (backupLogLink == null) {
    			layout.addComponent(newBackupLogLink);
    			backupLogLink = newBackupLogLink;
    		} else {
            	layout.replaceComponent(backupLogLink, newBackupLogLink);
            	backupLogLink = newBackupLogLink;
    		}
    	}
	}
	
	
	public void selectParameter(String parameter) {
		params = parameter;
		
		if (!paramSelected) {
			paramSelected = true;
			  
		    // update enable/disabled state of control buttons
			// String controls[] = taskRun.getControls();   this is for when they are server-side driven
			String controls[] = new String[] {"run"};
			for (String key : ctrlButtons.keySet()) {
				  NativeButton button = ctrlButtons.get(key);
				  button.setEnabled(Arrays.asList(controls).contains(key) ? true : false);
			}
			
		}
	}

	private void scriptCommand(String cmd) {
		if (cmd.equalsIgnoreCase("run")) {
			start();
		} else if (cmd.equalsIgnoreCase("pause")) {
			pause();
		} else if (cmd.equalsIgnoreCase("stop")) {
			stop();
		}
	}
	
	void start() {
		commandSelect.setEnabled(false);  // disable command selection immediately
		
		startTime = System.currentTimeMillis();
		resultLabel.setValue("Running...");

		TaskRun taskRun = new TaskRun(nodeInfo.getSystemID(), nodeInfo.getNodeID(), session.getUserLogin().getUserID(), command, params);
		nodeInfo.setTask(taskRun.getTask());
		
		activateTimer();
		/*
		To start the timer at a specific date in the future, the initial delay
		needs to be calculated relative to the current time, as in : 
		Date futureDate = ...
		long startTime = futureDate.getTime() - System.currentTimeMillis();
		*/	
	}
	
	void stop() {
		
	}
	
	void pause() {
		
	}
	
	public void close() {
		// make sure timers get stopped
		if (runTimerFuture != null)
			runTimerFuture.cancel(DONT_INTERRUPT_IF_RUNNING);
  	  
		if(cancelTimerFuture != null)
			cancelTimerFuture.cancel(DONT_INTERRUPT_IF_RUNNING);
		
	}
	
	public void activateTimer() {
		// if timer not running yet
		if (runTimerFuture == null) {
		    log(nodeInfo.getTask() + " - activateTimer");

			fInitialDelay = 0;
			fDelayBetweenRuns = 3;
			Runnable runTimerTask = new RunTimerTask();
		    runTimerFuture = fScheduler.scheduleWithFixedDelay(runTimerTask, fInitialDelay, fDelayBetweenRuns, TimeUnit.SECONDS);
			fShutdownAfter = 60*10;
		    Runnable stopTimer = new StopTimerTask(runTimerFuture);
		    cancelTimerFuture = fScheduler.schedule(stopTimer, fShutdownAfter, TimeUnit.SECONDS);
		} else {
			// cancel StopTimerTask and restart with full timeout

		}
		
	}
	
	private final class RunTimerTask implements Runnable {
		private int fCount;
	    public void run() {
	      ++fCount;
	      log(nodeInfo.getTask() + " - " + fCount);
	      
	      TaskInfo taskInfo = new TaskInfo(nodeInfo.getTask(), null, null, null);
	      TaskRecord taskRecord = taskInfo.getTasksList().get(0);
	      int index = Integer.parseInt(taskRecord.getIndex()) - 1;
		  int status = Integer.parseInt(taskRecord.getStatus());

	      if (scriptingProgressLayout.getWindow() != null) {
	    	  while (lastProgressIndex < index) {
	    		  log(nodeInfo.getTask() + " - updating last position");

	    		  taskImages[lastProgressIndex].setSource(new ThemeResource("img/scripting/done/" + primitives[lastProgressIndex] + ".png"));
	    		  lastProgressIndex++;
		      }
	      } else {
	    	  log(nodeInfo.getTask() + " - cannot update display");
	      }

	      if ((status == 2) && (index != lastIndex)) {
	    	  if (scriptingProgressLayout.getWindow() != null) {
	    		  log(nodeInfo.getTask() + " - updating running position");

	    		  taskImages[index].setSource(new ThemeResource("img/scripting/active/" + primitives[index] + ".png"));
	    		  progressLabel.setValue(taskImages[index].getDescription());
	    		  //progressLabel.setValue(primitives[index]);
	    	  } else {
		    	  log(nodeInfo.getTask() + " - cannot update display");
		      }
	    	  lastIndex = index;
	    	  
	      } else if (status == 5) {
	    	  runningTime = System.currentTimeMillis() - startTime;
	    	  if (scriptingProgressLayout.getWindow() != null) {
	    		  log(nodeInfo.getTask() + " - updating done position");

	    		  taskImages[index].setSource(new ThemeResource("img/scripting/done/" + primitives[index] + ".png"));
		    	  taskImages[index+1].setSource(new ThemeResource("img/scripting/done/done.png"));
	    		  String time = String.format("%d min, %d sec", 
	    				    TimeUnit.MILLISECONDS.toMinutes(runningTime),
	    				    TimeUnit.MILLISECONDS.toSeconds(runningTime) - 
	    				    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(runningTime))
	    				);
	    		  DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    		  Date date = new Date();
	    		  progressLabel.setValue("Done");
	    		  resultLabel.setValue("Completed successfully<br><br>on " + dateFormat.format(date) + "<br><br>in " + time);
	    		  
	    	  } else {
		    	  log(nodeInfo.getTask() + " - cannot update display");
		      }
	    	  
	    	  log(nodeInfo.getTask() + " - Canceling Timer (done)");
	    	  runTimerFuture.cancel(DONT_INTERRUPT_IF_RUNNING);
	    	  cancelTimerFuture.cancel(DONT_INTERRUPT_IF_RUNNING);
	    	  lastIndex = -1; lastProgressIndex = 0;
	    	  
	      } else if (status == 6) {
	    	  if (scriptingProgressLayout.getWindow() != null) {
	    		  log(nodeInfo.getTask() + " - updating error position");

	    		  taskImages[taskImages.length-1].setSource(new ThemeResource("img/scripting/error.png"));
	    		  progressLabel.setValue("Error!");
	    		  String time = String.format("%d min, %d sec", 
	    				    TimeUnit.MILLISECONDS.toMinutes(runningTime),
	    				    TimeUnit.MILLISECONDS.toSeconds(runningTime) - 
	    				    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(runningTime))
	    				);
	    		  DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    		  Date date = new Date();
	    		  resultLabel.setValue("Command failed<br><br>on " + dateFormat.format(date) + "<br><br>after " + time);
	    	  } else {
		    	  log(nodeInfo.getTask() + " - cannot update display");
		      }
	    	  	    	  
	    	  log(nodeInfo.getTask() + " - Canceling Timer (canceled, error)");
	    	  runTimerFuture.cancel(DONT_INTERRUPT_IF_RUNNING);
	    	  cancelTimerFuture.cancel(DONT_INTERRUPT_IF_RUNNING);
	    	  //lastIndex = 0; lastProgressIndex = 0;
	      }

	      // update enable/disabled state of control buttons
	      /***
    		  if (scriptingControlsLayout.getWindow() != null) {
				  String controls[] = taskRecord.getControls();
				  for (String key : ctrlButtons.keySet()) {
					  NativeButton button = ctrlButtons.get(key);
					  button.setEnabled(Arrays.asList(controls).contains(key) ? true : false);
				  }
			  }
	       ***/
    		  
	      // Push the changes
	      session.getICEPush().push();
  
	    } 

	}
	 
	private final class StopTimerTask implements Runnable {
	    StopTimerTask(ScheduledFuture<?> aSchedFuture){
	      fSchedFuture = aSchedFuture;
	    }
	    public void run() {
	      log(nodeInfo.getTask() + " - Stopping Timer.");
	      fSchedFuture.cancel(DONT_INTERRUPT_IF_RUNNING);
	      
	      // cleanup, by asking the scheduler to shutdown gracefully. 
	      //fScheduler.shutdown();
	      
	      lastIndex = -1;
	      
	    }
	    private ScheduledFuture<?> fSchedFuture;
	 }
	 
	 private static void log(String aMsg){
	    //System.out.println(aMsg);
	 }		
}
