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

import ilarkesto.core.base.RuntimeTracker;
import ilarkesto.core.fp.FP;
import ilarkesto.core.logging.Log;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class EntityIntegrityEnsurer {

	private static Log log = Log.get(EntityIntegrityEnsurer.class);

	public static void runForAll() {
		run(ATransaction.get().getAllAsList());
	}

	public static void run(Collection<Entity> entities) {
		int count = entities.size();
		log.info("Ensuring integrity for all", count, "entities");
		RuntimeTracker rt = new RuntimeTracker();
		final Map<Class<? extends Entity>, List<Entity>> entitiesByType = FP.group(entities,
			new EntitiesByTypeGroupFunctionn());

		for (Map.Entry<Class<? extends Entity>, List<Entity>> entry : entitiesByType.entrySet()) {
			RuntimeTracker rt2 = new RuntimeTracker();
			Class<? extends Entity> type = entry.getKey();
			Collection<Entity> entitiesOfType = entry.getValue();
			log.info("   ", type.getSimpleName(), entitiesOfType.size());
			ensureIntegrity(entitiesOfType);
			if (rt2.getRuntime() > 1000)
				log.info("      ->", rt2.getRuntimeFormated(), "|", rt2.getRuntime() / count, "ms/entity");
		}

		log.info("Integrity for all", count, "entities ensured in", rt.getRuntimeFormated());
	}

	protected static void ensureIntegrity(Collection<Entity> entities) {
		for (Entity entity : entities) {
			try {
				entity.ensureIntegrity();
			} catch (Exception ex) {
				throw new RuntimeException(
						"Ensuring integrity failed for entity: " + Persistence.toStringWithTypeAndId(entity), ex);
			}
		}
	}

}
