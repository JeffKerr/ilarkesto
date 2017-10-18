/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <http://www.gnu.org/licenses/>.
 */
package ilarkesto.io;

import ilarkesto.concurrent.ALoopTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

public abstract class AFileChangeWatchTask extends ALoopTask {

	private File root;
	private WatchService watcher;
	private long sleepUntilChangeNotivication = 1000;

	boolean first = true;

	protected abstract void onChange();

	public AFileChangeWatchTask(File root) {
		this.root = root;
	}

	public boolean isOffline() {
		return watcher == null;
	}

	protected void onFirstChange() {
		onChange();
	}

	private void connect() {

		try {
			watcher = FileSystems.getDefault().newWatchService();
			NIO.registerDirectoryTreeForAllChanges(watcher, root);
		} catch (IOException ex) {
			watcher = null;
			log.info("Offline:", root);
			return;
		}

		log.info("Watching", root.getAbsolutePath());

		if (first) {
			onFirstChange();
			first = false;
		} else {
			onChange();
		}
	}

	@Override
	protected void iteration() throws InterruptedException {
		if (isOffline()) connect();
		if (isOffline()) {
			sleep(3000);
			return;
		}

		WatchKey key = watcher.take();
		reset(key);
		if (isOffline()) return;

		log.info("CHANGE");

		if (sleepUntilChangeNotivication > 0) {
			sleep(sleepUntilChangeNotivication);
			key = watcher.poll();
			if (key != null) {
				reset(key);
			}
		}

		if (isAbortRequested()) return;

		onChange();
	}

	private void reset(WatchKey key) {
		if (isOffline()) return;

		key.pollEvents();
		if (!key.reset()) {
			log.info("Offline:", root);
			watcher = null;
			return;
		}
		try {
			NIO.registerDirectoryTreeForAllChanges(watcher, root);
		} catch (IOException ex) {
			log.info("Offline:", root);
			watcher = null;
			return;
		}
	}

}
