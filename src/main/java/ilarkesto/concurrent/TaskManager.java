/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>, Artjom Kochtchi
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package ilarkesto.concurrent;

import ilarkesto.base.Utl;
import ilarkesto.core.logging.Log;
import ilarkesto.core.time.Tm;
import ilarkesto.di.Context;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskManager {

	private static final Log LOG = Log.get(TaskManager.class);

	private Set<ATask> runningTasks = Collections.synchronizedSet(new HashSet<ATask>());
	private Set<ATask> scheduledTasks = Collections.synchronizedSet(new HashSet<ATask>());
	private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5,
		new DeamonThreadFactory());
	private ExecutorService executorService = Executors.newCachedThreadPool(new DeamonThreadFactory());
	private boolean shutdownInProgress;

	public void waitForRunningTasks() {
		waitForRunningTasks(Long.MAX_VALUE);
	}

	public void waitForRunningTasks(long maxWaitTime) {
		long now = Tm.getCurrentTimeMillis();
		long tryUntilTime = now + maxWaitTime;
		if (tryUntilTime < now) tryUntilTime = Long.MAX_VALUE;
		Set<ATask> tasks;
		while ((!(tasks = getRunningTasks()).isEmpty()) && Tm.getCurrentTimeMillis() < tryUntilTime) {
			LOG.info("Waiting for running tasks:", tasks);
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				LOG.info("    Waiting for running tasks aborted by InterruptedException");
				return;
			}
		}
		LOG.info("All tasks finished");
	}

	public Set<ATask> getRunningTasks() {
		return Utl.toSet(runningTasks.toArray(new ATask[runningTasks.size()]));
	}

	public void abortAllRunningTasks() {
		for (ATask task : getRunningTasks()) {
			LOG.info("Aborting task:", task);
			task.abort();
		}
	}

	public void shutdown(long waitUntilKill) {
		shutdownInProgress = true;
		unscheduleAllTasks();
		scheduledExecutorService.shutdownNow();
		abortAllRunningTasks();
		waitForRunningTasks(waitUntilKill);
		executorService.shutdownNow();

		for (ATask task : getRunningTasks()) {
			Thread thread = task.getThread();
			if (thread == null) continue;
			thread.setDaemon(true);
			task.stopThread();
		}
	}

	public Set<ATask> getScheduledTasks() {
		return new HashSet<ATask>(scheduledTasks);
	}

	public void start(ATask task) {
		if (shutdownInProgress) {
			LOG.info("Task execution prevented, cause shutdown in progress:", task);
			return;
		}
		TaskRunner runner = new TaskRunner(task, false, Context.get());
		executorService.execute(runner);
	}

	public void scheduleWithFixedDelay(ATask task, long delay) {
		scheduleWithFixedDelay(task, delay, delay);
	}

	public void scheduleWithFixedDelay(ATask task, long initialDelay, long delay) {
		scheduledTasks.add(task);
		scheduledExecutorService.scheduleWithFixedDelay(new TaskRunner(task, true, Context.get()), initialDelay, delay,
			TimeUnit.MILLISECONDS);
		LOG.info("Scheduled task:", task);
	}

	public boolean unschedule(ATask task) {
		return scheduledTasks.remove(task);
	}

	public void unscheduleAllTasks() {
		if (!scheduledTasks.isEmpty()) LOG.info("Removing scheduled tasks:", scheduledTasks);
		scheduledTasks.clear();
	}

	class TaskRunner implements Runnable {

		private ATask task;
		private boolean repeating;
		private Context parentContext;

		public TaskRunner(ATask task, boolean repeating, Context parentContext) {
			this.task = task;
			this.repeating = repeating;
			this.parentContext = parentContext;
		}

		@Override
		public void run() {
			Context context = parentContext.createSubContext("task:" + task.toString());
			// Thread.currentThread().setName(task.toString());
			runningTasks.add(task);
			// LOG.debug("Task started:", task);
			try {
				task.run();
			} catch (Throwable ex) {
				if (shutdownInProgress && Utl.getRootCause(ex) instanceof InterruptedException) {
					LOG.info("Task interrupted while shutdown:", Utl.toStringWithType(task));
				} else {
					LOG.error(ex);
				}
			}
			// LOG.debug("Task finished:", task);
			runningTasks.remove(task);
			if (repeating) task.reset();
			context.destroy(false);
			synchronized (TaskManager.this) {
				TaskManager.this.notifyAll();
			}
		}

	}

}
