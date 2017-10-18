/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>, Artjom Kochtchi
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
package ilarkesto.core.base;

import ilarkesto.core.localization.GermanComparator;
import ilarkesto.core.localization.Localizer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Utl {

	public static final BigDecimal BD_HUNDRED = new BigDecimal(100);
	public static final Set<String> EMPTY_STRING_SET = Collections.emptySet();

	public static <T> List<T> join(T[] elements, T... newElements) {
		ArrayList<T> ret = new ArrayList<T>();
		for (T element : elements) {
			ret.add(element);
		}
		for (T element : newElements) {
			ret.add(element);
		}
		return ret;
	}

	public static <T> T[] array(T[] emptyArrayWhichReturns, T[] elementsAtStart, T... elementsAtEnd) {
		System.arraycopy(elementsAtStart, 0, emptyArrayWhichReturns, 0, elementsAtStart.length);
		System.arraycopy(elementsAtEnd, 0, emptyArrayWhichReturns, emptyArrayWhichReturns.length - elementsAtEnd.length,
			elementsAtEnd.length);
		return emptyArrayWhichReturns;
	}

	public static <T> List<List<T>> splitByCount(Collection<T> collection, int countPerGroup) {
		if (countPerGroup < 2) throw new IllegalArgumentException("countPerGroup < 2");
		ArrayList<List<T>> ret = new ArrayList<List<T>>();
		ArrayList<T> group = null;
		for (T item : collection) {
			if (group == null) {
				group = new ArrayList<T>();
				ret.add(group);
			}
			group.add(item);
			if (group.size() == countPerGroup) group = null;
		}
		return ret;
	}

	public static int getOptimalScale(BigDecimal bd) {
		bd = bd.stripTrailingZeros();
		String s = bd.toPlainString();
		int idx = s.indexOf('.');
		if (idx < 0) return 0;
		return s.length() - idx - 1;
	}

	public static BigDecimal multiply(BigDecimal fak1, BigDecimal fak2) {
		if (fak1 == null || fak2 == null) return null;
		return fak1.multiply(fak2);
	}

	public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
		BigDecimal ret = divide(dividend, divisor, 42);
		if (ret == null) return null;
		return ret.stripTrailingZeros();
	}

	public static BigDecimal divide(BigDecimal dividend, BigDecimal divisor, int scale) {
		if (dividend == null) return null;
		if (isZeroOrNull(divisor)) return null;
		return dividend.divide(divisor, scale, BigDecimal.ROUND_HALF_UP);
	}

	public static boolean isZeroOrNull(BigDecimal number) {
		if (number == null) return true;
		if (number.compareTo(BigDecimal.ZERO) == 0) return true;
		return false;
	}

	public static Long biggest(Long... values) {
		Long biggest = null;
		for (Long value : values) {
			if (value == null) continue;
			if (biggest == null || value.intValue() > biggest.intValue()) biggest = value;
		}
		return biggest;
	}

	public static Long biggest(Iterable<Long> values) {
		Long biggest = null;
		for (Long value : values) {
			if (value == null) continue;
			if (biggest == null || value.longValue() > biggest.longValue()) biggest = value;
		}
		return biggest;
	}

	public static <T> boolean containsAny(Collection<T> container, Collection<T> elements) {
		if (container == null || container.isEmpty()) return false;
		if (elements == null || elements.isEmpty()) return false;
		for (T element : elements) {
			if (container.contains(element)) return true;
		}
		return false;
	}

	public static boolean contains(int[] array, int value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) return true;
		}
		return false;
	}

	public static boolean contains(String[] array, String value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(value)) return true;
		}
		return false;
	}

	public static Integer toInteger(Long value) {
		if (value == null) return null;
		return Integer.valueOf(value.intValue());
	}

	public static <T> HashSet<T> mergeIntoHashSet(Collection<T>... collections) {
		HashSet<T> ret = new HashSet<T>();
		if (collections == null) return ret;
		for (Collection<T> collection : collections) {
			if (collection == null) continue;
			ret.addAll(collection);
		}
		return ret;
	}

	public static <T> boolean addIfNotNull(Collection<T> collection, T element) {
		if (element == null) return false;
		return collection.add(element);
	}

	public static <T> ArrayList<T> arrayList(T... elements) {
		ArrayList<T> ret = new ArrayList<T>();
		for (T element : elements) {
			ret.add(element);
		}
		return ret;
	}

	public static <T> boolean isElementFirst(List<T> list, T element) {
		return list.indexOf(element) == 0;
	}

	public static <T> boolean isElementLast(List<T> list, T element) {
		if (list.isEmpty()) return false;
		return list.indexOf(element) == list.size() - 1;
	}

	/**
	 * Moves te given element up in the list. Adds it to the list if it doesn't exist.
	 */
	public static <T> List<T> moveElementUp(List<T> list, T element) {
		if (!list.contains(element)) list.add(element);
		int idx = list.indexOf(element);
		if (idx == 0) return list; // already at the top
		list.remove(element);
		list.add(idx - 1, element);
		return list;
	}

	/**
	 * Moves te given element down in the list. Adds it to the list if it doesn't exist.
	 */
	public static <T> List<T> moveElementDown(List<T> list, T element) {
		if (!list.contains(element)) list.add(element);
		int idx = list.indexOf(element);
		if (idx == list.size() - 1) return list; // already at the bottom
		list.remove(element);
		list.add(idx + 1, element);
		return list;
	}

	/**
	 * Moves the given element to the top of the list. Adds it to the list if it doesn't exist.
	 */
	public static <T> List<T> moveElementTop(List<T> list, T element) {
		list.remove(element);
		list.add(0, element);
		return list;
	}

	/**
	 * Moves the given element to the top of the list. Adds it to the list if it doesn't exist.
	 */
	public static <T> List<T> moveElementBottom(List<T> list, T element) {
		list.remove(element);
		list.add(element);
		return list;
	}

	public static <T> T getFirstNotNull(T... objects) {
		for (T object : objects) {
			if (object != null) return object;
		}
		return null;
	}

	public static String getRootCauseMessage(Throwable ex) {
		Throwable cause = getRootCause(ex);
		String message = cause.getMessage();
		if (message == null) message = cause.getClass().getName();
		return message;
	}

	public static boolean containsCauseWithSimpleName(Throwable ex, String simpleName) {
		if (ex == null) return false;
		if (Str.getSimpleName(ex.getClass()).equals(simpleName)) return true;
		return containsCauseWithSimpleName(ex.getCause(), simpleName);
	}

	public static boolean isRootCause(Class<? extends Throwable> exceptionType, Throwable ex) {
		Throwable cause = getRootCause(ex);
		return cause.getClass().equals(exceptionType);
	}

	public static Throwable getRootCause(Throwable ex) {
		Throwable cause = ex.getCause();
		return cause == null ? ex : getRootCause(cause);
	}

	public static String[] concat(String[]... arrays) {
		int len = 0;
		for (String[] array : arrays) {
			len += array.length;
		}
		String[] ret = new String[len];
		int offset = 0;
		for (String[] array : arrays) {
			System.arraycopy(array, 0, ret, offset, array.length);
			offset += array.length;
		}
		return ret;
	}

	public static <K extends Comparable, V> LinkedHashMap<K, V> sort(Map<K, V> map) {
		LinkedHashMap<K, V> ret = new LinkedHashMap<K, V>();
		List<K> keys = sort(map.keySet());
		for (K key : keys) {
			ret.put(key, map.get(key));
		}
		return ret;
	}

	public static <T extends Comparable> List<T> sort(Collection<T> collection) {
		List<T> result = new ArrayList<T>(collection);
		Collections.sort(result);
		return result;
	}

	public static <T> List<T> sort(Collection<T> collection, Comparator<T> comparator) {
		List<T> result = new ArrayList<T>(collection);
		Collections.sort(result, comparator);
		return result;
	}

	public static <T> List<T> sortReverse(Collection<T> collection, Comparator<T> comparator) {
		List<T> result = new ArrayList<T>(collection);
		Collections.sort(result, reverseComparator(comparator));
		return result;
	}

	public static int hashCode(Object... objects) {
		int hashCode = 23;
		for (Object object : objects) {
			if (object == null) continue;
			if (object instanceof Object[]) {
				hashCode = hashCode * 37 + hashCode((Object[]) object);
			} else {
				hashCode = hashCode * 37 + object.hashCode();
			}
		}
		return hashCode;
	}

	@Deprecated
	public static String getLanguage() {
		return Localizer.get().getLanguage();
	}

	public static void removeDuplicates(Collection collection) {
		Set set = new HashSet(collection.size());
		Iterator iterator = collection.iterator();
		while (iterator.hasNext()) {
			Object object = iterator.next();
			if (!set.add(object)) iterator.remove();
		}
	}

	public static <T> T getFirstElement(Collection<T> collection) {
		if (collection == null) return null;
		if (collection.isEmpty()) return null;
		return collection.iterator().next();
	}

	public static <T> T getFirstElementIfOnlyOneExists(Collection<T> collection) {
		if (collection == null) return null;
		if (collection.size() != 1) return null;
		return collection.iterator().next();
	}

	public static <T> List<T> toList(Iterable<T> iterable) {
		if (iterable == null) return null;
		List<T> ret = new ArrayList<T>();
		for (T item : iterable) {
			ret.add(item);
		}
		return ret;
	}

	public static <T> List<T> toList(T... elements) {
		if (elements == null) return null;
		List<T> ret = new ArrayList<T>(elements.length);
		for (T element : elements) {
			if (element == null) continue;
			ret.add(element);
		}
		return ret;
	}

	public static int[] toArrayOfInt(Collection<Integer> list) {
		int[] ret = new int[list.size()];
		int i = 0;
		for (Integer value : list) {
			ret[i] = value == null ? 0 : value;
			i++;
		}
		return ret;
	}

	public static float[] toArrayOfFloat(Collection<Float> list) {
		float[] ret = new float[list.size()];
		int i = 0;
		for (Float value : list) {
			ret[i] = value == null ? 0 : value;
			i++;
		}
		return ret;
	}

	public static <T> T[] toArray(Collection<T> elements, T[] a) {
		int i = 0;
		for (T element : elements) {
			if (i >= a.length) break;
			a[i] = element;
			i++;
		}
		return a;
	}

	public static <T> Set<T> toSet(T... elements) {
		Set<T> ret = new HashSet<T>();
		for (T element : elements) {
			if (element == null) continue;
			ret.add(element);
		}
		return ret;
	}

	/**
	 * Check if the first given parameter equals at least one of the other parameters
	 */
	public static boolean equalsAny(Object o, Object... others) {
		for (Object other : others) {
			if (o.equals(other)) return true;
		}
		return false;
	}

	public static boolean equals(Object a, Object b) {
		if (a == null && b == null) return true;
		if (a == null || b == null) return false;
		if (a instanceof Object[] && b instanceof Object[]) {
			Object[] aa = (Object[]) a;
			Object[] ba = (Object[]) b;
			if (aa.length != ba.length) return false;
			for (int i = 0; i < aa.length; i++) {
				if (!equals(aa[i], ba[i])) return false;
			}
			return true;
		}
		return a.equals(b);
	}

	public static int compare(int a, int b) {
		if (a > b) return 1;
		if (a < b) return -1;
		return 0;
	}

	public static int compare(long a, long b) {
		if (a > b) return 1;
		if (a < b) return -1;
		return 0;
	}

	public static int compare(String a, String b) {
		if (a == null && b == null) return 0;
		if (a == null && b != null) return -1;
		if (a != null && b == null) return 1;
		return (GermanComparator.INSTANCE).compare(a, b);
	}

	public static int compare(Object a, Object b) {
		if (a == null && b == null) return 0;
		if (a == null && b != null) return -1;
		if (a != null && b == null) return 1;
		if (a instanceof String && b instanceof String) return (compare((String) a, (String) b));
		if (a instanceof Comparable && b instanceof Comparable && a.getClass().equals(b.getClass()))
			return ((Comparable) a).compareTo(b);
		return compare(a.toString(), b.toString());
	}

	public static int compareReverse(Object a, Object b) {
		return compare(a, b) * -1;
	}

	public static <A> Comparator<A> reverseComparator(final Comparator<A> comparator) {
		return new Comparator<A>() {

			@Override
			public int compare(A o1, A o2) {
				return comparator.compare(o2, o1);
			}
		};
	}

	public static int parseHex(String hex) {
		return Integer.parseInt(hex, 16);
	}

	public static String getSimpleName(Class type) {
		String name = type.getName();
		int idx = name.lastIndexOf('.');
		if (idx > 0) {
			name = name.substring(idx + 1);
		}
		return name;
	}

	public static <T> List<T> toList(Enumeration<T> e) {
		if (e == null) return null;
		List<T> ret = new ArrayList<T>();
		while (e.hasMoreElements()) {
			ret.add(e.nextElement());
		}
		return ret;
	}

	public static <T> void removeFirstElements(List<T> list, int count) {
		for (int i = 0; i < count; i++) {
			list.remove(0);
		}
	}

	public static final <T extends Comparable> List<T> sortReverse(List<T> list) {
		Collections.sort(list, REVERSE_COMPARATOR);
		return list;
	}

	public static final Comparator REVERSE_COMPARATOR = new Comparator<Comparable>() {

		@Override
		public int compare(Comparable a, Comparable b) {
			return b.compareTo(a);
		}
	};

	public static boolean isBetween(BigDecimal prozent, int from, int to) {
		if (prozent.compareTo(new BigDecimal(from)) < 0) return false;
		if (prozent.compareTo(new BigDecimal(to)) > 0) return false;
		return true;
	}

	public static <T> Map<T, T> toMapInOrder(Iterable<T> items) {
		LinkedHashMap<T, T> ret = new LinkedHashMap<T, T>();
		if (items == null) return ret;
		for (T item : items) {
			ret.put(item, item);
		}
		return ret;
	}

	public static <E extends Enum> List<String> getEnumNames(E[] enumValues) {
		ArrayList<String> ret = new ArrayList<String>(enumValues.length);
		for (E value : enumValues) {
			ret.add(value.name());
		}
		return ret;
	}

	public static boolean isLong(String s) {
		if (s == null) return false;
		try {
			Long.parseLong(s);
		} catch (NumberFormatException ex) {
			return false;
		}
		return true;
	}

	public static String toStringOrNull(Object o) {
		return o == null ? null : o.toString();
	}

	public static boolean isNullOrEmptyCollection(Object value) {
		if (value == null) return true;
		if (value instanceof Collection) return ((Collection) value).isEmpty();
		return false;
	}

	public static <T> List<T> getFirstElements(Collection<T> elements, int count) {
		ArrayList<T> ret = new ArrayList<T>(count);
		int i = 0;
		for (T element : elements) {
			if (i >= count) return ret;
			ret.add(element);
			i++;
		}
		return ret;
	}

	public static <T> boolean containsExactly(Collection<T> collection, T... items) {
		if (collection.size() != items.length) return false;
		for (T item : items) {
			if (!collection.contains(item)) return false;
		}
		return true;
	}

	public static <T> boolean containsAny(Collection<T> collection, T... items) {
		for (T item : items) {
			if (collection.contains(item)) return true;
		}
		return false;
	}

	public static <T> boolean containsOther(Collection<T> collection, T... items) {
		Set<T> test = new HashSet<T>(collection);
		for (T item : items) {
			test.remove(item);
		}
		return !test.isEmpty();
	}

	public static <T> boolean containsNoOtherExceptAny(Collection<T> collection, T... ofTheseItems) {
		if (!containsAny(collection, ofTheseItems)) return false;
		Set<T> itemsSet = new HashSet<T>();
		for (T item : ofTheseItems) {
			itemsSet.add(item);
		}
		return itemsSet.containsAll(collection);
	}

	public static <_> List<_> conj(List<_> list, _ element) {
		list.add(element);
		return list;
	}

}
