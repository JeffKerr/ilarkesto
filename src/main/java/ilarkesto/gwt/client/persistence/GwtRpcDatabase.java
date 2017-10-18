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
package ilarkesto.gwt.client.persistence;

import ilarkesto.core.persistance.ACachingEntitiesBackend;
import ilarkesto.core.persistance.AEntity;
import ilarkesto.core.persistance.Entity;
import ilarkesto.core.persistance.EntityDoesNotExistException;
import ilarkesto.core.persistance.Transaction;
import ilarkesto.gwt.client.AGwtApplication;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GwtRpcDatabase extends ACachingEntitiesBackend {

	private AGwtEntityFactory factory;

	public GwtRpcDatabase(AGwtEntityFactory factory) {
		super();
		this.factory = factory;
	}

	@Override
	protected void onUpdate(Collection<AEntity> modified, Collection<String> deleted,
			Map<String, Map<String, String>> modifiedProperties, Runnable callback, String transactionText) {
		AGwtApplication.get().sendChangesToServer(modified, deleted, modifiedProperties, callback);
	}

	public Set<AEntity> updateFromServer(Collection<Map<String, String>> entityDatas) {
		HashSet<AEntity> entities = new HashSet<AEntity>();
		Transaction t = Transaction.get();
		t.setIgnoreModificationEvents(true);
		try {
			for (Map<String, String> data : entityDatas) {
				String id = data.get("id");
				AEntity entity;
				try {
					entity = cache.getById(id);
				} catch (EntityDoesNotExistException ex) {
					String type = data.get("@type");
					entity = factory.createEntity(type, id);
					cache.add(entity);
				}
				entity.updateProperties(data);
				entities.add(entity);
			}
		} finally {
			t.setIgnoreModificationEvents(false);
		}
		return entities;
	}

	@Override
	public String loadOutsourcedString(Entity entity, String propertyName) {
		throw new RuntimeException(getClass().getName() + ".loadOutsourcedString() is not implemented");
	}

	@Override
	public void saveOutsourcedString(Entity entity, String propertyName, String value) {
		throw new RuntimeException(getClass().getName() + ".saveOutsourcedString() is not implemented");
	}

	public void onEntityDeletionsReceived(Set<String> entityIds) {
		cache.removeAll(entityIds);
	}

	@Override
	public boolean isPartial() {
		return true;
	}

}
