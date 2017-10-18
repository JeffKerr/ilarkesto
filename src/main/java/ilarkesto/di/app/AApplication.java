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
package ilarkesto.di.app;

import ilarkesto.base.Str;
import ilarkesto.base.Sys;
import ilarkesto.base.Tm;
import ilarkesto.base.Utl;
import ilarkesto.concurrent.ATask;
import ilarkesto.concurrent.DefaultSynchronizer;
import ilarkesto.concurrent.TaskManager;
import ilarkesto.core.logging.Log;
import ilarkesto.core.persistance.ATransactionManager;
import ilarkesto.core.persistance.EntitiesBackend;
import ilarkesto.core.persistance.EntityIntegrityEnsurer;
import ilarkesto.core.persistance.Persistence;
import ilarkesto.core.persistance.SingletonTransactionManager;
import ilarkesto.core.time.DateAndTime;
import ilarkesto.core.time.TimePeriod;
import ilarkesto.di.Context;
import ilarkesto.integration.xstream.XStreamSerializer;
import ilarkesto.io.AFileStorage;
import ilarkesto.io.ExclusiveFileLock;
import ilarkesto.io.ExclusiveFileLock.FileLockedException;
import ilarkesto.io.IO;
import ilarkesto.io.SimpleFileStorage;
import ilarkesto.io.Zip;
import ilarkesto.logging.DefaultLogRecordHandler;
import ilarkesto.persistence.DaoListener;
import ilarkesto.persistence.DaoService;
import ilarkesto.persistence.EntityStore;
import ilarkesto.persistence.FileEntityStore;
import ilarkesto.persistence.LegacySingletonTransactionManager;
import ilarkesto.persistence.LegacyThreadlocalTransactionManager;
import ilarkesto.persistence.Serializer;
import ilarkesto.persistence.ThreadlocalTransactionManager;
import ilarkesto.properties.FilePropertiesStore;

import java.io.File;
import java.io.FileFilter;
import java.util.Map;
import java.util.Set;

/**
 * Base class of a custom application
 *
 * @author Witoslaw Koczewski
 */
public abstract class AApplication {

	protected Log log = Log.get(getClass());

	private ExclusiveFileLock exclusiveFileLock;
	private boolean startupFailed;
	private boolean shuttingDown;
	private boolean shutdown;
	private boolean unitTestMode;

	private BuildProperties buildProperties;

	protected abstract void onStart();

	protected abstract void onShutdown();

	protected abstract void scheduleTasks(TaskManager tm);

	protected abstract EntitiesBackend createEntitiesBackend();

	protected Context context;
	private String[] arguments = new String[0];

	protected void initializePersistence() {
		EntitiesBackend backend = createEntitiesBackend();
		if (backend != null) {
			log.info("Entities backend:", backend.getClass().getSimpleName());
			ATransactionManager tm;
			if (unitTestMode) {
				tm = backend instanceof FileEntityStore ? new LegacySingletonTransactionManager()
						: new SingletonTransactionManager();
			} else {
				tm = backend instanceof FileEntityStore ? new LegacyThreadlocalTransactionManager()
						: new ThreadlocalTransactionManager();
			}
			Persistence.initialize(backend, tm);

			if (Persistence.backend != null) {

				Persistence.runInTransaction("persistence-init", new Runnable() {

					@Override
					public void run() {
						DaoService ds = getDaoService();
						if (ds != null) {
							ds.ensureIntegrity();
						} else {
							EntityIntegrityEnsurer.runForAll();
						}
					}

				});

			}

		} else {
			log.debug("No persistence backend");
		}
	}

	protected void ensureIntegrity() {}

	protected boolean isSingleton() {
		return true;
	}

	public final void start() {
		if (instance != null) { throw new RuntimeException("An Application already started: " + instance); }
		synchronized (getApplicationLock()) {

			instance = this;

			log.info("\n\n     DATA PATH:", getApplicationDataDir(), "\n\n");

			context = Context.createRootContext("app:" + getApplicationName());
			context.addBeanProvider(this);

			if (isSingleton()) {
				File lockFile = new File(getApplicationDataDir() + "/.lock");
				for (int i = 0; i < 10; i++) {
					try {
						exclusiveFileLock = new ExclusiveFileLock(lockFile);
						break;
					} catch (FileLockedException ex) {
						log.info("Application already running. File locked: " + lockFile.getAbsolutePath());
					}
					Utl.sleep(1000);
				}
				if (exclusiveFileLock == null) {
					log.fatal("Application startup failed. Another instance is running. Lock file: "
							+ lockFile.getAbsolutePath());
					shutdown();
					return;
				}
			}

			DefaultSynchronizer.install();

			try {
				getApplicationConfig();
			} catch (Throwable ex) {
				startupFailed = true;
				RuntimeException ret = new RuntimeException("Application startup failed. Loading configuration failed.",
						ex);
				onApplicationStartupFailed(ret);
				throw ret;
			}

			try {
				onPreStart();
			} catch (Throwable ex) {
				log.error("onPreStart() failed.", ex);
			}

			try {
				backupApplicationDataDir();
			} catch (Throwable ex) {
				log.error("Backing up application data directory failed.", ex);
			}

			log.info("Initializing persistence");
			try {
				initializePersistence();
			} catch (Throwable ex) {
				startupFailed = true;
				shutdown(false);
				RuntimeException ret = new RuntimeException(
						"Application startup failed. Initializing persistence failed.", ex);
				onApplicationStartupFailed(ret);
				throw ret;
			}

			log.info("Ensuring application integrity");
			try {
				Persistence.runInTransaction("start.ensureIntegrity", new Runnable() {

					@Override
					public void run() {
						ensureIntegrity();
					}
				});
			} catch (Throwable ex) {
				startupFailed = true;
				shutdown(false);
				RuntimeException ret = new RuntimeException(
						"Application startup failed. Data integrity check or repair failed.", ex);
				onApplicationStartupFailed(ret);
				throw ret;
			}

			try {
				Persistence.runInTransaction("start.onStart", new Runnable() {

					@Override
					public void run() {
						onStart();
					}
				});

			} catch (Throwable ex) {
				startupFailed = true;
				shutdown(false);
				RuntimeException ret = new RuntimeException("Application startup failed.", ex);
				onApplicationStartupFailed(ret);
				throw ret;
			}

			try {
				scheduleTasks(getTaskManager());
			} catch (Throwable ex) {
				startupFailed = true;
				shutdown(true);
				RuntimeException ret = new RuntimeException("Application startup failed.", ex);
				onApplicationStartupFailed(ret);
				throw ret;
			}

			if (isPreventProcessEnd()) {
				Thread thread = new Thread() {

					@Override
					public void run() {
						while (!isShutdown()) {
							Utl.sleep(1000);
						}
					};

				};
				thread.setDaemon(false);
				thread.start();
			}
		}
	}

	protected void onApplicationStartupFailed(RuntimeException ret) {}

	protected boolean isPreventProcessEnd() {
		return false;
	}

	protected void onPreStart() {}

	public final void shutdown() {
		shutdown(true);
	}

	private final void shutdown(final boolean runOnShutdown) {
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized (getApplicationLock()) {
					if (instance == null) throw new RuntimeException("Application not started yet.");
					log.info("Shutdown initiated:", getApplicationName());

					if (runOnShutdown) {

						try {
							Persistence.runInTransaction("shutdown", new Runnable() {

								@Override
								public void run() {
									onShutdown();
								}
							});
						} catch (Exception ex) {
							onShutdown();
						}

					}
					getTaskManager().shutdown(10000);
					Set<ATask> tasks = getTaskManager().getRunningTasks();
					if (!tasks.isEmpty()) {
						log.warn("Aborting tasks on shutdown failed:", tasks);
					}
					if (entityStore != null) entityStore.lock();
					shutdown = true;

					if (context != null) context.destroy(true);

					if (exclusiveFileLock != null) exclusiveFileLock.release();
					Log.flush();
					DefaultLogRecordHandler.stopLogging();

					try {
						Thread.sleep(500);
					} catch (InterruptedException ex) {}
					for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
						StackTraceElement[] elements = entry.getValue();
						for (StackTraceElement element : elements) {
							Thread t = entry.getKey();
							if (!t.isAlive()) continue;
							if (t.isDaemon()) continue;
							System.out.println("Non-Daemon thread still running: -> " + t.getName() + " -> "
									+ t.isDaemon() + "," + t.isAlive() + " > " + element.getClassName() + "."
									+ element.getMethodName() + " > " + element.toString());
						}
					}
				}
			}

		});
		thread.setDaemon(true);
		thread.setName(getApplicationName() + "-shutdown");
		shuttingDown = true;
		thread.start();
	}

	public void backupApplicationDataDir() {
		final File dataDir = new File(getApplicationDataDir());
		File backupFile = new File(dataDir.getPath() + "/backups/" + getApplicationName() + "-data_"
				+ DateAndTime.now().formatLog() + ".zip");
		log.info("Backing up application data dir:", dataDir.getAbsolutePath(), "into", backupFile);
		long starttime = Tm.getCurrentTimeMillis();
		Object lock = entityStore == null ? this : entityStore;
		synchronized (lock) {
			Zip.zip(backupFile, new File[] { dataDir }, new FileFilter() {

				@Override
				public boolean accept(File file) {
					return acceptBackupFile(file);
				}
			});
		}
		long runtime = Tm.getCurrentTimeMillis() - starttime;
		log.info("  Backup completed in", new TimePeriod(runtime).toShortestString());
		deleteOldApplicationDataDirBackups();
	}

	protected boolean acceptBackupFile(File file) {
		File dataDir = new File(getApplicationDataDir());
		File dir = file.getParentFile();
		if (dir.equals(dataDir)) {
			// base dir
			String name = file.getName();
			if (name.equals(".lock")) return false;
			if (name.equals("backups")) return false;
			if (name.equals("entities-rescue")) return false;
			if (name.equals("Caches")) return false;
			if (name.equals("Temp")) return false;
			if (name.equals("tmp")) return false;
			if (name.startsWith("gwt-")) return false;
			if (file.isDirectory()) log.info("    Zipping", file.getPath());
		}
		return true;
	}

	private void deleteOldApplicationDataDirBackups() {
		File backupDir = new File(getApplicationDataDir() + "/backups");
		File[] files = backupDir.listFiles();
		if (files == null || files.length == 0) return;

		log.info("Deleting old backup files from", backupDir);
		final long deadline = Tm.getCurrentTimeMillis() - Tm.DAY * 7;

		for (File file : files) {
			if (!file.getName().startsWith(getApplicationName())) continue;
			if (file.lastModified() >= deadline && !file.getName().endsWith(".zip~")) continue;
			log.debug("    Deleting", file);
			IO.delete(file);
		}
	}

	public final <T> T autowire(T bean) {
		return context.autowire(bean);
	}

	public void setArguments(String[] arguments) {
		this.arguments = arguments;
	}

	public String[] getArguments() {
		return arguments;
	}

	private static AApplication instance;

	public static AApplication get() {
		if (instance == null) throw new RuntimeException("No application started yet");
		return instance;
	}

	public static boolean isStarted() {
		return instance != null;
	}

	private static String APPLICATION_LOCK = "APPLICATION_LOCK";

	public Object getApplicationLock() {
		return APPLICATION_LOCK;
	}

	public final String getApplicationPackageName() {
		return getClass().getPackage().getName();
	}

	public String getApplicationLabel() {
		return getApplicationName();
	}

	public AApplication getApplication() {
		return this;
	}

	private String applicationName;

	public String getApplicationName() {
		if (applicationName == null) {
			applicationName = getClass().getSimpleName();
			applicationName = Str.lowercaseFirstLetter(applicationName);
			applicationName = Str.removeSuffix(applicationName, "Application");
		}
		return applicationName;
	}

	private SimpleFileStorage fileStorage;

	public AFileStorage getFileStorage() {
		if (fileStorage == null) fileStorage = new SimpleFileStorage(new File(getApplicationDataDir()));
		return fileStorage;
	}

	private String applicationDataDir;

	public String getApplicationDataDir() {
		if (applicationDataDir == null) {
			if (isDevelopmentMode()) {
				String path = "runtimedata";
				applicationDataDir = new File(path).getAbsolutePath();
			} else {
				applicationDataDir = getProductionModeApplicationDataDir();
			}
		}
		return applicationDataDir;
	}

	protected String getProductionModeApplicationDataDir() {
		return Sys.getUsersHomePath() + "/." + getApplicationName();
	}

	private String applicationTempDir;

	public String getApplicationTempDir() {
		if (applicationTempDir == null) {
			applicationTempDir = getApplicationDataDir() + "/tmp";
		}
		return applicationTempDir;
	}

	private FilePropertiesStore applicationConfig;

	public FilePropertiesStore getApplicationConfig() {
		if (applicationConfig == null) {
			applicationConfig = new FilePropertiesStore(getApplicationDataDir() + "/config.properties", false);
		}
		return applicationConfig;
	}

	public BuildProperties getBuildProperties() {
		if (buildProperties == null) buildProperties = new BuildProperties(getClass());
		return buildProperties;
	}

	public final boolean isDevelopmentMode() {
		return Sys.isDevelopmentMode();
	}

	public final boolean isProductionMode() {
		return !isDevelopmentMode();
	}

	public boolean isStartupFailed() {
		return startupFailed;
	}

	public boolean isShuttingDown() {
		return shuttingDown;
	}

	public boolean isShutdown() {
		return shutdown;
	}

	@Override
	public final String toString() {
		return getApplicationName();
	}

	// --- beans / services ---

	private TaskManager taskManager;

	public TaskManager getTaskManager() {
		if (taskManager == null) taskManager = Context.get().autowire(new TaskManager());
		return taskManager;
	}

	private EntityStore entityStore;

	public final EntityStore getEntityStore() {
		if (entityStore == null) entityStore = createEntityStore();
		return entityStore;
	}

	protected EntityStore createEntityStore() {
		FileEntityStore store = new FileEntityStore();
		if (unitTestMode) store.setUnitTestMode(true);
		store.setDir(getApplicationDataDir() + "/entities");
		store.setVersion(getDataVersion());
		Context.get().autowire(store);
		return store;
	}

	protected int getDataVersion() {
		return -1;
	}

	private XStreamSerializer beanSerializer;

	public final Serializer getBeanSerializer() {
		if (beanSerializer == null) {
			beanSerializer = new XStreamSerializer();
			Context.get().autowire(beanSerializer);
		}
		return beanSerializer;
	}

	private DaoService daoService;

	public DaoService getDaoService() {
		if (daoService == null) {
			daoService = new DaoService();
			Context.get().autowire(daoService);
			daoService.initialize(context);
			for (DaoListener listener : Context.get().getBeansByType(DaoListener.class))
				daoService.addListener(listener);
		}
		return daoService;
	}

	public void setUnitTestMode(boolean unitTestMode) {
		this.unitTestMode = unitTestMode;
	}

	public boolean isUnitTestMode() {
		return unitTestMode;
	}

}
