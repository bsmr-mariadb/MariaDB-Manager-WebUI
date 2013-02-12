package com.skysql.consolev;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class ExecutorFactory {
	private static ScheduledExecutorService fScheduler;

	public static ScheduledExecutorService getScheduler(int numThreads) {
		if (fScheduler == null) {
			fScheduler = Executors.newScheduledThreadPool(numThreads);    
		}
		return fScheduler;
	}

}
