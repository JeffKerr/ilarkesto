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

import ilarkesto.core.base.Args;
import ilarkesto.core.base.Str;
import ilarkesto.core.logging.Log;
import ilarkesto.core.money.Money;
import ilarkesto.core.time.Date;
import ilarkesto.core.time.DateAndTime;
import ilarkesto.core.time.DateRange;
import ilarkesto.core.time.DayAndMonth;
import ilarkesto.core.time.Time;
import ilarkesto.core.time.TimePeriod;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Persistence {

	private static final Log log = Log.get(Persistence.class);

	public static EntitiesBackend backend;
	public static ATransactionManager transactionManager;
	public static Map<String, ValuesCache> valuesCachesById = new HashMap<String, ValuesCache>();

	public static void initialize(EntitiesBackend backend, ATransactionManager transactionManager) {
		Args.assertNotNull(backend, "backend");
		Args.assertNotNull(transactionManager, "transactionManager");
		Persistence.backend = backend;
		Persistence.transactionManager = transactionManager;
	}

	public static String loadOutsourcedString(Entity entity, String propertyName) {
		return backend.loadOutsourcedString(entity, propertyName);
	}

	public static void saveOutsourcedString(Entity entity, String propertyName, String value) {
		backend.saveOutsourcedString(entity, propertyName, value);
	}

	public static void runInTransaction(String name, Runnable runnable) {
		runInTransaction(name, runnable, null);
	}

	public static void runInTransaction(String name, Runnable runnable, Runnable runAfterCommited) {
		// log.debug("runInTransaction()", name, "->", runnable.getClass());

		if (transactionManager == null) {
			runnable.run();
			if (runAfterCommited != null) runAfterCommited.run();
			return;
		}

		final ATransaction transaction = transactionManager.createWriteTransaction(name);
		boolean autoCommit = transaction.isAutoCommit();
		transaction.setAutoCommit(false);
		transaction.runAfterCommited(runAfterCommited);
		try {
			runnable.run();
		} catch (Exception ex) {
			transaction.rollback();
			throw new RunInTransactionException(runnable, transaction, ex);
		} finally {
			transaction.setAutoCommit(autoCommit);
		}
		transaction.commit();
	}

	static ValuesCache getValuesCache(String id) {
		ValuesCache cache = valuesCachesById.get(id);
		if (cache == null) {
			cache = new ValuesCache();
			valuesCachesById.put(id, cache);
		}
		return cache;
	}

	public static void clearCaches() {
		valuesCachesById.clear();
	}

	public static int parsePropertyint(String value) {
		if (value == null) return 0;
		return Integer.parseInt(value);
	}

	public static boolean parsePropertyboolean(String value) {
		return Str.isTrue(value);
	}

	public static List<String> parsePropertyStringCollection(String value) {
		return parsePropertyStringList(value);
	}

	public static List<String> parsePropertyStringList(String value) {
		return parseStringList(value, new ArrayList<String>());
	}

	public static Set<String> parsePropertyStringSet(String value) {
		return parseStringList(value, new HashSet<String>());
	}

	public static Set<String> parsePropertyReferenceSet(String value) {
		return parseStringList(value, new HashSet<String>());
	}

	public static List<String> parsePropertyReferenceList(String value) {
		return parseStringList(value, new ArrayList<String>());
	}

	private static <T extends Collection<String>> T parseStringList(String value, T ret) {
		if (value == null) return ret;
		while (true) {
			value = value.trim();
			if (value.isEmpty()) break;
			int idx = value.indexOf(",");
			if (idx >= 0) {
				String id = value.substring(0, idx);
				ret.add(id.trim());
				value = value.substring(idx + 1);
			} else {
				ret.add(value);
				break;
			}
		}
		return ret;
	}

	public static List<Date> parsePropertyDateCollection(String value) {
		List<Date> ret = new ArrayList<Date>();
		if (value == null) return ret;
		while (true) {
			value = value.trim();
			if (value.isEmpty()) break;
			int idx = value.indexOf(",");
			if (idx >= 0) {
				String s = value.substring(0, idx);
				ret.add(new Date(s.trim()));
				value = value.substring(idx + 1);
			} else {
				ret.add(new Date(value));
				break;
			}
		}
		return ret;
	}

	public static List<Integer> parsePropertyIntegerCollection(String value) {
		List<Integer> ret = new ArrayList<Integer>();
		if (value == null) return ret;
		while (true) {
			value = value.trim();
			if (value.isEmpty()) break;
			int idx = value.indexOf(",");
			if (idx >= 0) {
				String s = value.substring(0, idx);
				ret.add(Integer.parseInt(s.trim()));
				value = value.substring(idx + 1);
			} else {
				ret.add(Integer.parseInt(value));
				break;
			}
		}
		return ret;
	}

	public static Money parsePropertyMoney(String value) {
		return value == null ? null : new Money(value);
	}

	public static BigDecimal parsePropertyBigDecimal(String value) {
		return value == null ? null : new BigDecimal(value);
	}

	public static String parsePropertyReference(String value) {
		return value;
	}

	public static Double parsePropertyDouble(String value) {
		return value == null ? null : Double.parseDouble(value);
	}

	public static Float parsePropertyFloat(String value) {
		return value == null ? null : Float.parseFloat(value);
	}

	public static Long parsePropertyLong(String value) {
		return value == null ? null : Long.parseLong(value);
	}

	public static Integer parsePropertyInteger(String value) {
		return value == null ? null : Integer.parseInt(value);
	}

	public static Date parsePropertyDate(String value) {
		return value == null ? null : new Date(value);
	}

	public static Time parsePropertyTime(String value) {
		return value == null ? null : new Time(value);
	}

	public static TimePeriod parsePropertyTimePeriod(String value) {
		return value == null ? null : new TimePeriod(value);
	}

	public static DateAndTime parsePropertyDateAndTime(String value) {
		return value == null ? null : new DateAndTime(value);
	}

	public static DateRange parsePropertyDateRange(String value) {
		return value == null ? null : new DateRange(value);
	}

	public static DayAndMonth parsePropertyDayAndMonth(String value) {
		return value == null ? null : new DayAndMonth(value);
	}

	public static String parsePropertyString(String value) {
		return value;
	}

	public static String propertyAsString(String value) {
		return value;
	}

	public static String propertyAsString(Object value) {
		return value == null ? null : value.toString();
	}

	public static String propertyAsString(Number value) {
		if (value == null) return null;
		return String.valueOf(value);
	}

	public static String propertyAsString(Date value) {
		return value == null ? null : value.toString();
	}

	public static String propertyAsString(Time value) {
		return value == null ? null : value.toString();
	}

	public static String propertyAsString(DateAndTime value) {
		return value == null ? null : value.toString();
	}

	public static String propertyAsString(DateRange value) {
		return value == null ? null : value.toString();
	}

	public static String propertyAsString(Money value) {
		return value == null ? null : value.toString();
	}

	public static String propertyAsString(DayAndMonth value) {
		return value == null ? null : value.toString();
	}

	public static String propertyAsString(Boolean value) {
		return value == null ? null : value.toString();
	}

	public static String propertyAsString(Collection<String> value) {
		return value == null ? null : Str.concat(value, ", ");
	}

	public static Set<String> getIdsAsSet(Collection<? extends Entity> entities) {
		Set<String> result = new HashSet<String>(entities.size());
		for (Entity entity : entities)
			result.add(entity.getId());
		return result;
	}

	public static List<String> getIdsAsList(Collection<? extends Entity> entities) {
		List<String> result = new ArrayList<String>();
		for (Entity entity : entities)
			result.add(entity.getId());
		return result;
	}

	public static String toStringWithTypeAndId(Entity entity) {
		if (entity == null) return null;
		String s;
		try {
			s = entity.toString();
		} catch (Exception ex) {
			s = "toString()-ERROR: " + Str.formatException(ex);
		}
		return getTypeAndId(entity) + " " + s;
	}

	public static String getTypeAndId(Entity entity) {
		if (entity == null) return null;
		return Str.getSimpleName(entity.getClass()) + ":" + entity.getId();
	}

	public static void deleteAll(Iterable<? extends Entity> entities) {
		for (Entity entity : entities) {
			entity.delete();
		}
	}

}
