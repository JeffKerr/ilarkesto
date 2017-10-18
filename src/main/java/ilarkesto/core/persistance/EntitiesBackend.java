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

public interface EntitiesBackend<E extends Entity, T extends ATransaction<E>> extends EntitiesProvider<E> {

	void update(Collection<E> modified, Collection<String> deletedIds,
			Map<String, Map<String, String>> modifiedPropertiesByEntityId, Runnable callback, String transactionText);

	String createInfo();

	String loadOutsourcedString(Entity entity, String propertyName);

	void saveOutsourcedString(Entity entity, String propertyName, String value);

}
