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
 * You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package ilarkesto.persistence;

import ilarkesto.core.base.Args;
import ilarkesto.core.base.Str;
import ilarkesto.core.logging.Log;
import ilarkesto.core.persistance.AEntityQuery;
import ilarkesto.core.persistance.AllByTypeQuery;
import ilarkesto.core.persistance.Entity;
import ilarkesto.core.persistance.EntityDoesNotExistException;
import ilarkesto.core.persistance.Transient;
import ilarkesto.io.IO;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class FileEntityStore implements EntityStore {

	private static final Log log = Log.get(FileEntityStore.class);

	private boolean versionSaved;
	private boolean versionChecked;
	private boolean locked;

	private Map<Class, String> aliases = new HashMap<Class, String>();
	private Map<Class<AEntity>, Map<String, AEntity>> entitiesByIdByType = new HashMap<Class<AEntity>, Map<String, AEntity>>();

	// --- dependencies ---

	private long version;

	@Override
	public void setVersion(long version) {
		this.version = version;
	}

	private Serializer beanSerializer;

	public void setBeanSerializer(Serializer beanSerializer) {
		this.beanSerializer = beanSerializer;
	}

	private EntityfilePreparator entityfilePreparator;

	public void setEntityfilePreparator(EntityfilePreparator entityfilePreparator) {
		this.entityfilePreparator = entityfilePreparator;
	}

	private String dir;

	public void setDir(String dir) {
		this.dir = dir;
	}

	private boolean unitTestMode;

	public void setUnitTestMode(boolean unitTestMode) {
		this.unitTestMode = unitTestMode;
	}

	// --- ---

	public FileEntityStore() {}

	@Override
	public synchronized void lock() {
		if (locked) return;
		locked = true;
		log.info("File entity store locked.");
	}

	@Override
	public void update(Collection<AEntity> modified, Collection<String> deletedIds,
			Map<String, Map<String, String>> modifiedPropertiesByEntityId, Runnable callback, String transactionText) {
		if (locked) throw new RuntimeException("Can not persist entity changes. EntityStore already locked.");

		if (!versionSaved) saveVersion();

		// create operations
		List<Operation> operations = new ArrayList<FileEntityStore.Operation>(modified.size() + deletedIds.size());
		for (AEntity entity : new HashSet<AEntity>(modified)) {
			if (entity instanceof Transient) continue;
			if (deletedIds.contains(entity.getId())) continue;
			operations.add(new SaveOperation(entity));
		}

		for (String deletedId : deletedIds) {
			AEntity entity;
			try {
				entity = getById(deletedId);
			} catch (EntityDoesNotExistException ex) {
				continue;
			}
			operations.add(new DeleteOperation(entity));
		}

		// prepare operations
		for (Operation operation : operations) {
			operation.prepare();
		}

		// complete operations (critical)
		for (Operation operation : operations) {
			operation.complete();
		}

		StringBuilder sb = new StringBuilder();
		for (Operation operation : operations) {
			sb.append("\n    ").append(operation.toString());
		}
		log.debug("Entity changes persisted.", sb.toString());

		if (callback != null) callback.run();
	}

	private Map<String, AEntity> getDao(Class<? extends AEntity> type) {
		Map<String, AEntity> dao = entitiesByIdByType.get(type);
		if (dao == null) { throw new RuntimeException("Unknown entity type: " + type); }
		return dao;
	}

	@Override
	public boolean containsWithId(String id) {
		for (Map<String, AEntity> entitiesById : entitiesByIdByType.values()) {
			if (entitiesById.containsKey(id)) return true;
		}
		return false;
	}

	@Override
	public <C extends Collection<AEntity>> C getAll(C resultCollection) {
		for (Map<String, AEntity> entitiesById : entitiesByIdByType.values()) {
			resultCollection.addAll(entitiesById.values());
		}
		return resultCollection;
	}

	@Override
	public Set<AEntity> getAllAsSet() {
		return getAll(new HashSet<AEntity>());
	}

	@Override
	public List<AEntity> getAllAsList() {
		return getAll(new ArrayList<AEntity>());
	}

	@Override
	public AEntity findFirst(AEntityQuery<AEntity> query) {
		for (Map.Entry<Class<AEntity>, Map<String, AEntity>> daoEntry : entitiesByIdByType.entrySet()) {
			if (!query.testType(daoEntry.getKey())) continue;

			for (AEntity entity : daoEntry.getValue().values()) {
				if (query.test(entity)) return entity;
			}
		}
		return null;
	}

	@Override
	public <C extends Collection<AEntity>> C find(AEntityQuery<AEntity> query, C resultCollection) {
		for (Map.Entry<Class<AEntity>, Map<String, AEntity>> entry : entitiesByIdByType.entrySet()) {
			if (!query.testType(entry.getKey())) continue;
			if (query.getClass().equals(AllByTypeQuery.class)) {
				resultCollection.addAll(entry.getValue().values());
			} else {
				for (AEntity entity : entry.getValue().values()) {
					if (query.test(entity)) resultCollection.add(entity);
				}
			}
		}
		return resultCollection;
	}

	@Override
	public List<AEntity> getByIdsAsList(Collection<String> ids) {
		return getByIds(ids, new ArrayList<AEntity>(ids.size()));
	}

	@Override
	public Set<AEntity> getByIdsAsSet(Collection<String> ids) {
		return getByIds(ids, new HashSet<AEntity>(ids.size()));
	}

	@Override
	public AEntity getById(String id) {
		for (Map.Entry<Class<AEntity>, Map<String, AEntity>> daoEntry : entitiesByIdByType.entrySet()) {
			AEntity entity = daoEntry.getValue().get(id);
			if (entity != null) return entity;
		}
		throw new EntityDoesNotExistException(id);
	}

	@Override
	public <C extends Collection<AEntity>> C getByIds(Collection<String> ids, C resultContainer) {
		for (String id : ids) {
			resultContainer.add(getById(id));
		}
		return resultContainer;
	}

	@Override
	public void setAlias(String alias, Class cls) {
		aliases.put(cls, alias);
		beanSerializer.setAlias(alias, cls);
	}

	@Override
	public void load(Class<? extends AEntity> cls, String alias, boolean deleteOnFailure) {
		if (!versionChecked) checkVersion();

		aliases.put(cls, alias);

		Map<String, AEntity> entities = new HashMap<String, AEntity>();
		entitiesByIdByType.put((Class<AEntity>) cls, entities);

		beanSerializer.setAlias(Str.lowercaseFirstLetter(alias), cls);
		beanSerializer.setAlias(alias, cls);

		if (unitTestMode) return;

		File entitiesDir = new File(dir + "/" + Str.lowercaseFirstLetter(alias));

		File[] files = entitiesDir.listFiles();
		int count = files == null ? 0 : files.length;
		log.info("Loading", count, "entitiy files:", alias);
		if (count > 0) {
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				String filename = file.getName();

				if (!filename.endsWith(".xml")) {
					log.warn("Unsupported file. Skipping:", filename);
					continue;
				}

				try {
					loadObject(file, entities, cls, alias);
				} catch (Exception ex) {
					if (!deleteOnFailure) throw new RuntimeException("Loading object from " + file + " failed", ex);
					log.warn("Loading object from file failed:", file, ex);
					file.delete();
				}
			}
		}
	}

	private void loadObject(File file, Map<String, AEntity> container, Class type, String alias) {
		if (entityfilePreparator != null) entityfilePreparator.prepareEntityfile(file, type, alias);

		BufferedInputStream in;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException ex) {
			throw new RuntimeException(ex);
		}
		AEntity entity = (AEntity) beanSerializer.deserialize(in);
		container.put(entity.getId(), entity);
		try {
			in.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	private synchronized void checkVersion() {
		versionChecked = true;
		if (version <= 0) return;
		File propertiesFile = getPropertiesFile();
		if (!propertiesFile.exists()) return;
		Properties properties = IO.loadProperties(propertiesFile, IO.UTF_8);
		String s = properties.getProperty("version");
		if (Str.isBlank(s)) return;
		long dataVersion = Long.parseLong(s);
		if (dataVersion > version) throw new IllegalStateException(
				"Data stored in " + dir + " was created by a newer version of the application. "
						+ "You have probably downgraded. Since data formats changed, this is not possible. "
						+ "Application version is " + version + ", data version is " + dataVersion + ".");
	}

	private synchronized void saveVersion() {
		versionSaved = true;
		if (version <= 0) return;
		File propertiesFile = getPropertiesFile();
		Properties properties = propertiesFile.exists() ? IO.loadProperties(propertiesFile, IO.UTF_8)
				: new Properties();
		properties.setProperty("version", String.valueOf(version));
		IO.saveProperties(properties, getClass().getName(), propertiesFile);
	}

	private File getPropertiesFile() {
		return new File(dir + "/store.properties");
	}

	@Override
	public String createInfo() {
		return getClass().getName();
	}

	@Override
	public String loadOutsourcedString(Entity entity, String propertyName) {
		throw new RuntimeException(getClass().getName() + ".loadOutsourcedString() is not implemented");
	}

	@Override
	public void saveOutsourcedString(Entity entity, String propertyName, String value) {
		throw new RuntimeException(getClass().getName() + ".saveOutsourcedString() is not implemented");
	}

	abstract class Operation {

		protected abstract void prepare();

		protected abstract void complete();

		protected AEntity entity;

		public Operation(AEntity entity) {
			Args.assertNotNull(entity, "entity");
			this.entity = entity;
		}

	}

	class SaveOperation extends Operation {

		private File tmpFile;
		private File file;

		public SaveOperation(AEntity entity) {
			super(entity);
		}

		@Override
		protected void prepare() {
			// tmpFile = new File(dir + "/tmp/" + entity.getId() + ".xml");
			file = new File(dir + "/" + Str.lowercaseFirstLetter(entity.getDao().getEntityName()) + "/" + entity.getId()
					+ ".xml");
			tmpFile = new File(file.getPath() + ".tmp");

			wirteTemporaryFile();
		}

		@Override
		protected void complete() {
			if (!unitTestMode) IO.move(tmpFile, file, true);
			getDao(entity.getClass()).put(entity.getId(), entity);
		}

		public void wirteTemporaryFile() {
			IO.createDirectory(tmpFile.getParentFile());
			BufferedOutputStream out;
			try {
				out = new BufferedOutputStream(new FileOutputStream(tmpFile));
			} catch (IOException ex) {
				throw new RuntimeException("Writing entity file for " + entity.getClass().getSimpleName() + ":"
						+ entity.getId() + " failed: " + tmpFile.getPath(), ex);
			}
			beanSerializer.serialize(entity, out);
			try {
				out.flush();
			} catch (IOException ex) {
				throw new RuntimeException("Writing entity file for " + entity.getClass().getSimpleName() + ":"
						+ entity.getId() + " failed: " + tmpFile.getPath(), ex);
			}
			IO.close(out);

			if (!tmpFile.exists())
				throw new RuntimeException("Writing entity file for " + entity.getClass().getSimpleName() + ":"
						+ entity.getId() + " failed. File does not exist after serialization: " + tmpFile.getPath()
						+ " | Entity: " + entity);

			if (tmpFile.length() < 1)
				throw new RuntimeException("Writing entity file caused empty file: " + tmpFile.getPath());
		}

		@Override
		public String toString() {
			return "SAVED " + file.getPath() + " -> " + entity;
		}

	}

	class DeleteOperation extends Operation {

		private File file;

		public DeleteOperation(AEntity entity) {
			super(entity);
		}

		@Override
		protected void prepare() {
			file = new File(dir + "/" + Str.lowercaseFirstLetter(entity.getDao().getEntityName()) + "/" + entity.getId()
					+ ".xml");
		}

		@Override
		protected void complete() {
			if (!unitTestMode) IO.delete(file);
			getDao(entity.getClass()).remove(entity.getId());
		}

		@Override
		public String toString() {
			return "DELETED " + file.getPath();
		}

	}

}
