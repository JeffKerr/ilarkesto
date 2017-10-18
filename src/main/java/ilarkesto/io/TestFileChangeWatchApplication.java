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

import ilarkesto.concurrent.TaskManager;
import ilarkesto.core.persistance.EntitiesBackend;
import ilarkesto.di.app.AApplication;
import ilarkesto.di.app.ApplicationStarter;

import java.io.File;

public class TestFileChangeWatchApplication extends AApplication {

	public static void main(String[] args) throws InterruptedException {
		ApplicationStarter.startApplication(TestFileChangeWatchApplication.class, "/media/witek/WITEK-FAT32/test");
	}

	@Override
	protected boolean isPreventProcessEnd() {
		return true;
	}

	@Override
	protected void onStart() {}

	@Override
	protected void onShutdown() {}

	@Override
	protected void scheduleTasks(TaskManager tm) {
		tm.start(new Task(getArguments()[0]));
	}

	@Override
	protected EntitiesBackend createEntitiesBackend() {
		return null;
	}

	class Task extends AFileChangeWatchTask {

		public Task(String path) {
			super(new File(path));
		}

		@Override
		protected void onChange() {
			System.out.println("CHANGE!!! ");
		}
	};

}
