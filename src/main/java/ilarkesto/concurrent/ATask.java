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
import ilarkesto.core.base.RunnableWithException;
import ilarkesto.core.logging.Log;
import ilarkesto.core.persistance.Persistence;
import ilarkesto.core.time.Tm;

public abstract class ATask {

	protected final Log log = Log.get(getClass());

	private volatile boolean finished;
	private long finishTime = -1;
	private volatile boolean started;
	private long startTime = -1;
	private volatile boolean abortRequested;
	private Thread thread;

	// --- dependencies ---

	private long maxSleepAtOnce = 1000;

	public final void setMaxSleepAtOnce(long maxSleepAtOnce) {
		this.maxSleepAtOnce = maxSleepAtOnce;
	}

	protected abstract void perform() throws InterruptedException;

	public float getProgress() {
		return isFinished() ? 1 : 0;
	}

	public String getProgressMessage() {
		return null;
	}

	// --- ---

	public final boolean isFinished() {
		return finished;
	}

	public final long getFinishTime() {
		return finishTime;
	}

	public final boolean isStarted() {
		return started;
	}

	public final long getStartTime() {
		return startTime;
	}

	public final boolean isAbortRequested() {
		return abortRequested;
	}

	public void abort() {
		this.abortRequested = true;
	}

	public final boolean isRunning() {
		return started && !finished;
	}

	public final long getRunTime() {
		if (startTime < 0) return -1;
		if (isFinished()) return finishTime - startTime;
		return Tm.getCurrentTimeMillis() - startTime;
	}

	public void reset() {
		if (isRunning()) {
			abort();
			try {
				waitForFinish();
			} catch (InterruptedException ex) {
				// nop
			}
		}
		started = false;
		finished = false;
		abortRequested = false;
		startTime = -1;
		finishTime = -1;
	}

	public final void run() {
		this.thread = Thread.currentThread();
		if (started) throw new IllegalStateException("Task already started: " + this);
		started = true;
		startTime = Tm.getCurrentTimeMillis();
		try {

			if (isRunInTransactionEnabled()) {

				Persistence.runInTransaction(getClass().getSimpleName(), new RunnableWithException() {

					@Override
					public void onRun() throws Exception {
						perform();
					}
				});

			} else {

				perform();

			}

		} catch (Exception ex) {
			Throwable rootCause = Utl.getRootCause(ex);
			if (rootCause instanceof InterruptedException) {
				// all right
			} else {
				log.error("Task execution failed:", this, ex);
				throw new TaskExcecutionFailedException(this, ex);
			}
		} finally {
			finished = true;
			finishTime = Tm.getCurrentTimeMillis();
			synchronized (this) {
				this.notifyAll();
			}
			thread = null;
		}
	}

	protected boolean isRunInTransactionEnabled() {
		return true;
	}

	public final void waitForFinish() throws InterruptedException {
		while (!isFinished()) {
			synchronized (this) {
				this.wait(1000);
			}
		}
	}

	public final void sleep(long millis) throws InterruptedException {
		while (!abortRequested && millis > 0) {
			long sleep = millis > maxSleepAtOnce ? maxSleepAtOnce : millis;
			Thread.sleep(sleep);
			millis -= sleep;
		}
	}

	public final Thread getThread() {
		return thread;
	}

	public final String getThreadName() {
		if (thread == null) return null;
		return thread.getName();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

	public Runnable createRunnable() {
		return new Runnable() {

			@Override
			public void run() {
				ATask.this.run();
			}
		};
	}

	public Thread createThread() {
		return new Thread(createRunnable());
	}

	public void stopThread() {
		if (thread == null) return;
		if (!thread.isAlive()) return;
		try {
			thread.interrupt();
			thread.stop();
		} catch (Throwable ex) {
			log.warn("Stopping thread failed: " + thread.getName());
		}
	}

}
