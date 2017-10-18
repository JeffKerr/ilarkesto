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
package ilarkesto.core.persistance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ACachingEntitiesBackend extends AEntitiesBackend {

	protected EntitiesCache<AEntity> cache = new EntitiesCache<AEntity>();

	protected abstract void onUpdate(Collection<AEntity> entities, Collection<String> entityIds,
			Map<String, Map<String, String>> modifiedPropertiesByEntityId, Runnable callback, String transactionText);

	@Override
	public synchronized void update(Collection<AEntity> modified, Collection<String> deletedIds,
			Map<String, Map<String, String>> modifiedPropertiesByEntityId, Runnable callback, String transactionText) {
		onUpdate(modified, deletedIds, modifiedPropertiesByEntityId, callback, transactionText);
		cache.addAll(modified);
		cache.removeAll(deletedIds);
	}

	@Override
	public synchronized AEntity getById(String id) throws EntityDoesNotExistException {
		return cache.getById(id);
	}

	@Override
	public synchronized boolean containsWithId(String id) {
		return cache.containsWithId(id);
	}

	@Override
	public synchronized Set<AEntity> getByIdsAsSet(Collection<String> ids) throws EntityDoesNotExistException {
		return cache.getByIdsAsSet(ids);
	}

	@Override
	public List<AEntity> getByIdsAsList(Collection<String> ids) throws EntityDoesNotExistException {
		return cache.getByIdsAsList(ids);
	}

	@Override
	public <C extends Collection<AEntity>> C getByIds(Collection<String> ids, C resultContainer)
			throws EntityDoesNotExistException {
		return cache.getByIds(ids, resultContainer);
	}

	@Override
	public AEntity findFirst(AEntityQuery query) {
		return cache.findFirst(query);
	}

	@Override
	public Set<AEntity> findAllAsSet(AEntityQuery query) {
		return cache.findAllAsSet(query);
	}

	@Override
	public <C extends Collection<AEntity>> C find(AEntityQuery<AEntity> query, C resultCollection) {
		return cache.find(query, resultCollection);
	}

	@Override
	public <C extends Collection<AEntity>> C getAll(C resultCollection) {
		return cache.getAll(resultCollection);
	}

	@Override
	public Set<AEntity> getAllAsSet() {
		return getAll(new HashSet<AEntity>());
	}

	@Override
	public List<AEntity> getAllAsList() {
		return getAll(new ArrayList<AEntity>());
	}

}
