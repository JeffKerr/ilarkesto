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

import java.util.Collection;
import java.util.Map;

public final class InMemoryEntitiesBackend extends ACachingEntitiesBackend {

	@Override
	protected void onUpdate(Collection<AEntity> entities, Collection<String> entityIds,
			Map<String, Map<String, String>> modifiedPropertiesByEntityId, Runnable callback, String transactionText) {
		if (callback != null) callback.run();
	}

	@Override
	public String loadOutsourcedString(Entity entity, String propertyName) {
		throw new RuntimeException(getClass().getName() + ".loadOutsourcedString() is not implemented");
	}

	@Override
	public void saveOutsourcedString(Entity entity, String propertyName, String value) {
		throw new RuntimeException(getClass().getName() + ".saveOutsourcedString() is not implemented");
	}

}
