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
package ilarkesto.persistence.git;

import ilarkesto.base.Env;
import ilarkesto.core.base.Bytes;
import ilarkesto.core.base.Utl;
import ilarkesto.core.persistance.AEntity;
import ilarkesto.core.time.DateAndTime;
import ilarkesto.di.Context;
import ilarkesto.di.app.AApplication;
import ilarkesto.integration.git.Git;
import ilarkesto.integration.git.GitProject;
import ilarkesto.persistence.file.AJsonFilesEntitiesBackend;
import ilarkesto.webapp.AWebApplication;

import java.io.File;
import java.util.Collection;

public abstract class AJsonFilesWithGitEntitiesBackend extends AJsonFilesEntitiesBackend {

	private GitProject git;

	public AJsonFilesWithGitEntitiesBackend() {
		super(AApplication.get().getFileStorage().getSubStorage("entities"), AApplication.get().getFileStorage()
				.getSubStorage("transactions").getSubStorage(DateAndTime.now().formatLog()));

		git = new GitProject(new Git(), new File(AApplication.get().getApplicationDataDir()));
	}

	@Override
	protected void onEntityChangesSaved(Collection<AEntity> modified, Collection<String> deleted,
			Collection<AEntity> created) {
		if (git.isInitialized()) {
			try {
				git.addAll();
			} catch (Exception ex) {
				String message = Utl.getRootCauseMessage(ex);
				if (message != null && message.contains(".git/index.lock")) {
					git.deleteIndexLock();
					try {
						git.addAll();
					} catch (Exception ex2) {
						log.error("git add failed:", ex);
					}
				} else {
					log.error("git add failed:", ex);
				}
			}

			try {
				git.commit(Context.get().toString());
			} catch (Exception ex) {
				String message = Utl.getRootCauseMessage(ex);
				if (message != null && message.contains(".git/index.lock")) {
					git.deleteIndexLock();
					try {
						git.commit(Context.get().toString());
					} catch (Exception ex2) {
						log.error("git commit failed:", ex);
					}
				} else {
					log.error("git commit failed:", ex);
				}
			}
		}

		AWebApplication webApplication = AWebApplication.get();
		webApplication.deleteFromClients(deleted);
		webApplication.sendToAllIfTracking((Collection) modified);
		webApplication.sendToAll((Collection) created);
	}

	@Override
	public String createInfo() {
		StringBuilder sb = new StringBuilder();

		sb.append("\nEntities size on disk: ")
				.append(new Bytes(Env.get().getFileSize(storage.getFile(null))).toRoundedString()).append("\n");

		sb.append(super.createInfo());

		if (git.isInitialized()) sb.append("\nGit status: ").append(git.status()).append("\n");

		return sb.toString();
	}
}
