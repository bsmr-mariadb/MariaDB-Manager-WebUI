package com.skysql.consolev;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ExecutorFactory {

	private static final int NUM_THREADS = 6;
	private static ScheduledExecutorService fScheduler;
	private static final boolean MAY_INTERRUPT_IF_RUNNING = true;
	private static int runningTimers = 0;
	private LinkedHashMap<Runnable, Object> myTimers = new LinkedHashMap<Runnable, Object>();

	public static ScheduledFuture<?> addTimer(Runnable timerTask, long fDelayBetweenRuns) {

		if (runningTimers == 0) {
			fScheduler = Executors.newScheduledThreadPool(NUM_THREADS);
		}

		log("timer added - " + runningTimers);

		ScheduledFuture<?> newTimerHandle = fScheduler.scheduleWithFixedDelay(timerTask, fDelayBetweenRuns, fDelayBetweenRuns, TimeUnit.SECONDS);
		if (newTimerHandle != null) {
			runningTimers++;
		}

		return newTimerHandle;

	}

	public static void removeTimer(ScheduledFuture<?> timerHandle) {
		if (timerHandle != null) {
			System.out.println("timer cancel");
			timerHandle.cancel(MAY_INTERRUPT_IF_RUNNING);
			runningTimers--;
		}

		if (runningTimers == 0) {
			List<Runnable> pendingTasks = fScheduler.shutdownNow();
			fScheduler = null;
			log("timer shutdown - pending: " + pendingTasks.size());
		}
	}

	private static void log(String aMsg) {
		// System.out.println(aMsg);
	}

}
