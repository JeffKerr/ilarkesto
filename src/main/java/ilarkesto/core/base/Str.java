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

import ilarkesto.core.localization.Localizer;
import ilarkesto.core.persistance.EntityDoesNotExistException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Str {

	public static final char interpunct = '·';

	public static final char ue = '\u00FC';
	public static final char UE = '\u00DC';
	public static final char oe = '\u00F6';
	public static final char OE = '\u00D6';
	public static final char ae = '\u00E4';
	public static final char AE = '\u00C4';
	public static final char sz = '\u00DF';

	public static final char EUR = '\u0080';

	public static String removeControlChars(String s) {
		if (s == null) return null;
		s = s.replace("\b", "");
		s = s.replace("\r", "");
		s = s.replace("\f", "");
		return s;
	}

	public static String escapeJavaString(String s) {
		if (s == null) return null;
		s = s.replace("\\", "\\\\");
		s = s.replace("\n", "\\\n");
		s = s.replace("\"", "\\\"");
		return s;
	}

	public static String textOneOrMany(int count, String textForOne, String textForMany) {
		return count == 1 ? textForOne : textForMany;
	}

	public static String prefix(String s, String prefix) {
		if (s == null || prefix == null) return s;
		return prefix + s;
	}

	public static boolean isLongerThan(String text, int maxLength) {
		if (text == null) return false;
		return text.length() > maxLength;
	}

	public static String insert(String into, int index, String s) {
		if (into == null) return null;
		if (s == null) return into;
		return into.substring(0, index) + s + into.substring(index);
	}

	public static String insert(String into, int index, char s) {
		if (into == null) return null;
		return into.substring(0, index) + s + into.substring(index);
	}

	public static boolean isWhitespace(char ch) {
		return ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t' || ch == '\f' || ch == '\u00A0' || ch == '\u2007'
				|| ch == '\u202F' || ch == '\u000B' || ch == '\u001C' || ch == '\u001D' || ch == '\u001E'
				|| ch == '\u001F';
	}

	public static String formatWithThousandsSeparator(long value, String separator) {
		return formatWithThousandsSeparator(String.valueOf(value), separator);
	}

	public static String formatWithThousandsSeparator(String s, String separator) {
		if (s == null) return null;
		if (separator == null || s.length() <= 3) return s;
		boolean negative = false;
		if (s.startsWith("-")) {
			negative = true;
			s = s.substring(1);
		}
		if (s.length() > 3) s = s.substring(0, s.length() - 3) + separator + s.substring(s.length() - 3);
		if (s.length() > 7) s = s.substring(0, s.length() - 7) + separator + s.substring(s.length() - 7);
		if (s.length() > 11) s = s.substring(0, s.length() - 11) + separator + s.substring(s.length() - 11);
		if (negative) s = '-' + s;
		return s;
	}

	public static String trimAndNull(String s) {
		if (s == null) return null;
		s = s.trim();
		if (s.length() == 0) return null;
		return s;
	}

	public static List<String> parseCommaSeparatedString(String s) {
		return parseCommaSeparatedString(s, true);
	}

	public static List<String> parseCommaSeparatedString(String s, boolean trim) {
		if (s == null) return Collections.emptyList();
		if (s.length() == 0) return Collections.emptyList();
		if (!trim) return Arrays.asList(s.split(","));

		if (isBlank(s)) return Collections.emptyList();
		List<String> ret = new ArrayList<String>();
		for (String token : s.split(",")) {
			ret.add(token.trim());
		}
		return ret;
	}

	public static String toFileCompatibleString(String s) {
		return toFileCompatibleString(s, null);
	}

	public static String toFileCompatibleString(String s, String spaceReplacement) {
		if (s == null) return "null";
		if (spaceReplacement != null) s = s.replace(" ", spaceReplacement);
		s = s.replace('/', '-');
		s = s.replace('\\', '-');

		s = s.replace(String.valueOf(ae), "ae");
		s = s.replace(String.valueOf(AE), "Ae");
		s = s.replace(String.valueOf(ue), "ue");
		s = s.replace(String.valueOf(UE), "Ue");
		s = s.replace(String.valueOf(oe), "oe");
		s = s.replace(String.valueOf(OE), "Oe");
		s = s.replace(String.valueOf(sz), "ss");
		s = s.replace(String.valueOf(EUR), "EUR");

		s = s.replace(',', '_');
		s = s.replace(':', '_');
		s = s.replace(';', '_');
		s = s.replace('&', '_');
		s = s.replace('?', '_');
		s = s.replace('=', '_');
		s = s.replace('*', '_');

		return s;
	}

	public static boolean containsDigit(String s) {
		if (s == null) return false;
		for (int i = 0; i < s.length(); i++) {
			if (Character.isDigit(s.charAt(i))) return true;
		}
		return false;
	}

	public static boolean containsLetter(String s) {
		if (s == null) return false;
		for (int i = 0; i < s.length(); i++) {
			if (Character.isLetter(s.charAt(i))) return true;
		}
		return false;
	}

	public static boolean containsNonLetterOrDigit(String s) {
		if (s == null) return false;
		for (int i = 0; i < s.length(); i++) {
			if (!Character.isLetterOrDigit(s.charAt(i))) return true;
		}
		return false;
	}

	public static String toUrl(String s) {
		if (s == null) return null;
		s = s.trim();
		if (s.contains("://")) return s;
		if (s.startsWith("mailto:")) return s;
		return "http://" + s;
	}

	public static String formatUrlAsLink(String url) {
		return formatUrlAsLink(url, 50);
	}

	public static String formatUrlAsLink(String url, int maxLength) {
		if (url == null) return null;
		url = removePrefix(url, "http://");
		url = removePrefix(url, "https://");
		url = removePrefix(url, "ftp://");
		url = removePrefix(url, "mailto://");
		url = removePrefix(url, "apt://");
		url = removePrefix(url, "file://");
		url = removePrefix(url, "www.");
		int paramsIdx = url.indexOf('?');
		if (paramsIdx > 0) url = url.substring(0, paramsIdx);
		url = removeSuffix(url, "/");
		int len = url.length();
		if (len > maxLength) {
			int cutlen = len - maxLength + 2;
			int fromIdx = url.indexOf('/') + 1;
			if (fromIdx > 4 && fromIdx + cutlen < len) {
				url = url.substring(0, fromIdx) + ".." + url.substring(fromIdx + cutlen);
			} else {
				url = cutRight(url, maxLength, "..");
			}
		}
		return url;
	}

	public static String uppercaseFirstLetter(String s) {
		if (s == null) return null;
		if (s.isEmpty()) return s;
		StringBuilder sb = new StringBuilder();
		sb.append(Character.toUpperCase(s.charAt(0)));
		sb.append(s.substring(1));
		return sb.toString();
	}

	public static String lowercaseFirstLetter(String s) {
		if (s == null) return null;
		if (s.length() == 0) return s;
		StringBuilder sb = new StringBuilder();
		sb.append(Character.toLowerCase(s.charAt(0)));
		sb.append(s.substring(1));
		return sb.toString();
	}

	public static String getFirstLine(String s) {
		return getFirstLine(s, Integer.MAX_VALUE, null);
	}

	public static String getFirstLine(String s, int cutAfterLength, String appendAfterCut) {
		return getLine(s, 0, cutAfterLength, appendAfterCut);
	}

	public static String getLine(String s, int index, int cutAfterLength, String appendAfterCut) {
		String ret = getLine(s, index);
		if (ret == null) return "";
		if (ret.length() > cutAfterLength) {
			ret = ret.substring(0, cutAfterLength);
			if (appendAfterCut != null) ret += appendAfterCut;
		}
		return ret;
	}

	public static String getLine(String s, int index) {
		if (s == null) return null;

		for (int i = 0; i < index; i++) {
			int idx = s.indexOf('\n');
			if (idx < 0) return null;
			s = s.substring(idx + 1);
		}

		int idx = indexOf(s, new String[] { "\r\n", "\n" }, 0);
		if (idx < 0) return s;
		return s.substring(0, idx);
	}

	public static String replaceIndexedParams(String s, Object... params) {
		if (s == null) return null;
		if (params == null || params.length == 0) return s;
		for (int i = 0; i < params.length; i++) {
			String param = format(params[i]);
			if (param == null) continue;
			s = s.replace("{" + i + "}", param);
		}
		return s;
	}

	public static String uppercase(CharSequence text) {
		if (text == null) return null;
		return String.valueOf(text).toUpperCase();
	}

	public static String fillUpLeft(String s, String filler, int minLength) {
		// TODO: optimize algorithm
		while (s.length() < minLength) {
			s = filler + s;
		}
		return s;
	}

	public static String cutLeft(String s, int maxlength, String fillerOnCut) {
		if (s == null) return null;
		if (s.length() > maxlength) {
			return fillerOnCut + s.substring(s.length() - maxlength + fillerOnCut.length());
		} else return s;
	}

	public static String cutRight(String s, int maxlength) {
		if (s == null) return null;
		if (s.length() > maxlength) {
			return s.substring(0, maxlength);
		} else return s;
	}

	public static String cutRight(String s, int maxlength, String fillerOnCut) {
		if (s == null) return null;
		if (s.length() > maxlength) {
			return s.substring(0, maxlength - fillerOnCut.length()) + fillerOnCut;
		} else return s;
	}

	public static String constructUrl(String base, Map parameters) {
		StringBuilder sb = new StringBuilder();
		sb.append(base);
		if (parameters != null && parameters.size() > 0) {
			boolean first = true;
			Set<Map.Entry> entries = parameters.entrySet();
			for (Map.Entry entry : entries) {
				if (first) {
					sb.append('?');
					first = false;
				} else {
					sb.append("&");
				}

				Object key = entry.getKey();
				Object value = entry.getValue();
				sb.append(key);
				sb.append("=");
				if (value != null) sb.append(encodeUrlParameter(value.toString()));
			}
		}
		return sb.toString();
	}

	/**
	 * @deprecated UrlEncoding.encode()
	 */
	@Deprecated
	public static String encodeUrlParameter(String s) {
		if (s == null) return "";
		StringBuilder sb = new StringBuilder();
		int len = s.length();
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			switch (c) {
				case '$':
					sb.append("%24");
					break;
				case '&':
					sb.append("%26");
					break;
				// case '+':
				// sb.append("%2B");
				// break;
				case ',':
					sb.append("%2C");
					break;
				// case '/':
				// sb.append("%2F");
				// break;
				case ':':
					sb.append("%3A");
					break;
				case ';':
					sb.append("%3B");
					break;
				// case '=':
				// sb.append("%3D");
				// break;
				case '?':
					sb.append("%3F");
					break;
				case '@':
					sb.append("%40");
					break;
				case ' ':
					sb.append("%20");
					break;
				case '"':
					sb.append("%22");
					break;
				case '<':
					sb.append("%3C");
					break;
				case '>':
					sb.append("%3E");
					break;
				case '#':
					sb.append("%23");
					break;
				case '%':
					sb.append("%25");
					break;
				case '{':
					sb.append("7B%");
					break;
				case '}':
					sb.append("7D%");
					break;
				case '|':
					sb.append("%7C");
					break;
				case '\\':
					sb.append("%5C");
					break;
				case '^':
					sb.append("%5E");
					break;
				case '~':
					sb.append("%7E");
					break;
				case '[':
					sb.append("%5B");
					break;
				case ']':
					sb.append("%5D");
					break;
				case '`':
					sb.append("%60");
					break;
				case '\n':
					sb.append("%0A");
					break;
				default:
					sb.append(c);
			}
		}
		return sb.toString();
	}

	public static String[] toStringArray(Collection<String> c) {
		return toStringArray(c.toArray());
	}

	public static String[] toStringArray(Object[] oa) {
		String[] sa = new String[oa.length];
		for (int i = 0; i < oa.length; i++) {
			sa[i] = oa[i] == null ? null : oa[i].toString();
		}
		return sa;
	}

	public static String toStringHelper(Object thiz, Object... properties) {
		return toStringHelper(getSimpleName(thiz.getClass()), properties);
	}

	public static String toStringHelper(String name, Object... properties) {
		return concat(name + "(", ")", ", ", properties);
	}

	public static String concat(String prefix, String suffix, String delimiter, Object... objects) {
		StringBuilder sb = new StringBuilder();
		if (prefix != null) sb.append(prefix);
		for (int i = 0; i < objects.length; i++) {
			sb.append(objects[i]);
			if (delimiter != null && i < objects.length - 1) {
				sb.append(delimiter);
			}
		}
		if (suffix != null) sb.append(suffix);
		return sb.toString();
	}

	public static String concat(int[] sa, String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sa.length; i++) {
			sb.append(sa[i]);
			if (i < sa.length - 1) {
				sb.append(delimiter);
			}
		}
		return sb.toString();
	}

	public static String concat(Object[] sa, String delimiter) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sa.length; i++) {
			sb.append(sa[i]);
			if (i < sa.length - 1) {
				sb.append(delimiter);
			}
		}
		return sb.toString();
	}

	public static String concat(Object[] sa) {
		return concat(sa, " ");
	}

	public static String concat(Iterable strings, String delimiter) {
		if (strings == null) return null;
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Object s : strings) {
			if (first) {
				first = false;
			} else {
				sb.append(delimiter);
			}
			sb.append(s);
		}
		return sb.toString();
	}

	public static String concat(Collection strings, String delimiter) {
		if (strings == null) return null;
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Object s : strings) {
			if (s == null) continue;
			if (first) {
				first = false;
			} else {
				sb.append(delimiter);
			}
			sb.append(format(s));
		}
		return sb.toString();
	}

	public static String concat(Collection items, String delimiter, String itemPrefix, String itemSuffix) {
		if (items == null) return null;
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Object s : items) {
			if (s == null) continue;
			if (first) {
				first = false;
			} else {
				sb.append(delimiter);
			}
			if (itemPrefix != null) sb.append(itemPrefix);
			sb.append(format(s));
			if (itemSuffix != null) sb.append(itemSuffix);
		}
		return sb.toString();
	}

	public static String concatNotBlank(Collection strings, String delimiter) {
		if (strings == null) return null;
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Object o : strings) {
			String s = format(o);
			if (Str.isBlank(s)) continue;
			if (first) {
				first = false;
			} else {
				sb.append(delimiter);
			}
			sb.append(s);
		}
		return sb.toString();
	}

	/**
	 * Removes a suffix from a string, if it exists.
	 */
	public static String removeSuffix(String s, String suffixToRemove) {
		if (s == null) return null;
		if (!s.endsWith(suffixToRemove)) return s;
		return s.substring(0, s.length() - suffixToRemove.length());
	}

	public static String removeSuffixStartingWith(String s, String suffixIndicator) {
		if (s == null) return null;
		int idx = s.indexOf(suffixIndicator);
		if (idx < 0) return s;
		return s.substring(0, idx);
	}

	public static String removeSuffixStartingWithLast(String s, String suffixIndicator) {
		if (s == null) return null;
		int idx = s.lastIndexOf(suffixIndicator);
		if (idx < 0) return s;
		return s.substring(0, idx);
	}

	public static String removePrefix(String s, String prefixToRemove) {
		if (s == null) return null;
		if (!s.startsWith(prefixToRemove)) return s;
		return s.substring(prefixToRemove.length());
	}

	public static String getSuffix(String s, String expectedPrefix) {
		if (s == null) return null;
		if (!s.startsWith(expectedPrefix)) return null;
		return s.substring(expectedPrefix.length());
	}

	public static String getFirstParagraph(String s) {
		return getFirstParagraph(s, "\n\n");
	}

	public static String getFirstParagraph(String s, String paragraphEndIndicator) {
		if (s == null) return null;
		int idx = s.indexOf(paragraphEndIndicator);
		if (idx <= 0) return s;
		return s.substring(0, idx);
	}

	public static String appendIfNotBlank(String s, String suffix) {
		return appendIfNotBlank(s, suffix, "");
	}

	public static String appendIfNotBlank(String s, String suffix, String separator) {
		if (isBlank(s)) return s;
		return s + separator + suffix;
	}

	public static String toHtml(String s) {
		return toHtml(s, true);
	}

	public static String toHtml(String s, boolean brForNl) {
		if (s == null) return null;
		s = s.replace("&", "&amp;");
		s = s.replace(String.valueOf(ae), "&auml;");
		s = s.replace(String.valueOf(ue), "&uuml;");
		s = s.replace(String.valueOf(oe), "&ouml;");
		s = s.replace(String.valueOf(AE), "&Auml;");
		s = s.replace(String.valueOf(UE), "&Uuml;");
		s = s.replace(String.valueOf(OE), "&Ouml;");
		s = s.replace(String.valueOf(sz), "&szlig;");
		s = s.replace(String.valueOf(EUR), "&euro;");
		s = s.replace("<", "&lt;");
		s = s.replace(">", "&gt;");
		s = s.replace("\"", "&quot;");
		if (brForNl) {
			s = s.replace("\n", "<br>");
		} else {
			s = s.replace("\n", "&#10;");
		}
		return s;
	}

	public static String getLeadingSpaces(String s) {
		StringBuilder sb = new StringBuilder();
		int len = s.length();
		for (int i = 0; i < len; i++) {
			if (s.charAt(i) != ' ') break;
			sb.append(' ');
		}
		return sb.toString();
	}

	public static String removeCenters(String s, String from, String to) {
		if (s == null) return null;
		while (true) {
			int previousLength = s.length();
			s = removeCenter(s, from, to);
			if (previousLength == s.length()) return s;
		}
	}

	public static String removeCenter(String s, String from, String to) {
		if (s == null) return null;
		int startIdx = s.indexOf(from);
		if (startIdx < 0) return s;
		int endIdx = s.indexOf(to, startIdx + from.length());
		if (endIdx < 0) return s;
		return s.substring(0, startIdx) + s.substring(endIdx + to.length());
	}

	public static String cutFromTo(String s, String from, String to) {
		if (s == null) return null;
		s = cutFrom(s, from);
		s = cutTo(s, to);
		return s;
	}

	public static String cutFrom(String s, String from) {
		if (s == null) return null;
		int fromIdx = s.indexOf(from);
		if (fromIdx < 0) return null;
		fromIdx += from.length();
		return s.substring(fromIdx);
	}

	public static String cutTo(String s, String to) {
		if (s == null) return null;
		int toIdx = s.indexOf(to);
		if (toIdx < 0) return null;
		return s.substring(0, toIdx);
	}

	public static String cutToIfContains(String s, String to) {
		if (s == null) return null;
		int toIdx = s.indexOf(to);
		if (toIdx < 0) return s;
		return s.substring(0, toIdx);
	}

	public static String toHtmlId(Object... objects) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Object object : objects) {
			if (first) {
				first = false;
			} else {
				sb.append("_");
			}
			if (object == null) {
				sb.append("null");
				continue;
			}
			sb.append(toHtmlId(object.toString()));
		}
		return sb.toString();
	}

	public static String toHtmlId(String s) {
		int len = s.length();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			char ch = s.charAt(i);
			if (Character.isLetter(ch) || (Character.isDigit(ch) && i > 0)) {
				sb.append(ch);
				continue;
			}
			sb.append('_');
		}
		return sb.toString();
	}

	// TODO rename
	public static String cutLeft(String s, int maxlength) {
		if (s.length() > maxlength) {
			return s.substring(s.length() - maxlength);
		} else return s;
	}

	// TODO rename
	public static String fillUpRight(String s, String filler, int minLength) {
		StringBuilder sb = new StringBuilder(s);
		while (sb.length() < minLength) {
			sb.append(filler);
		}
		return sb.toString();
	}

	public static boolean isLink(String s) {
		if (isBlank(s)) return false;
		if (s.contains(" ")) return false;
		if (s.contains("\n")) return false;
		if (s.startsWith("http://")) return true;
		if (s.startsWith("https://")) return true;
		if (s.startsWith("www.")) return true;
		if (s.startsWith("ftp://")) return true;
		return false;
	}

	public static boolean isEmail(String s) {
		if (isBlank(s)) return false;
		if (s.length() < 5) return false;
		s = s.toLowerCase();
		boolean at = false;
		boolean dot = false;
		int len = s.length();
		for (int i = 0; i < len; i++) {
			char c = s.charAt(i);
			if (c == '@') {
				if (at) return false;
				at = true;
				continue;
			}
			if (c == '.') {
				dot = true;
				continue;
			}
			if (Character.isLetterOrDigit(c) || c == '-' || c == '_' || c == '+' || c == 'ä' || c == 'ü' || c == 'ö')
				continue;
			return false;
		}
		if (!dot || !at) return false;
		return true;
	}

	public static boolean isTrue(String s) {
		if (s == null) return false;
		s = s.toLowerCase();
		if (s.equals("true")) return true;
		if (s.equals("yes")) return true;
		if (s.equals("y")) return true;
		if (s.equals("1")) return true;
		if (s.equals("ja")) return true;
		if (s.equals("j")) return true;
		return false;
	}

	public static int indexOf(String text, String[] toFind, int startIdx) {
		int firstIdx = -1;
		for (int i = 0; i < toFind.length; i++) {
			int idx = text.indexOf(toFind[i], startIdx);
			if (firstIdx < 0 || (idx >= 0 && idx < firstIdx)) {
				firstIdx = idx;
			}
		}
		return firstIdx;
	}

	public static String format(Object o) {
		if (o == null) return null;
		if (o instanceof Formatable) return ((Formatable) o).format();
		if (o instanceof Object[]) return formatObjectArray((Object[]) o);
		if (o instanceof Map) return formatMap((Map) o);
		if (o instanceof Collection) return formatCollection((Collection) o);
		if (o instanceof Enumeration) return formatEnumeration((Enumeration) o);
		if (o instanceof Throwable) return formatException((Throwable) o);
		if (o instanceof Number) return Localizer.get().format((Number) o, false);
		return o.toString();
	}

	public static String formatMessage(Object... messageParts) {
		StringBuilder sb = new StringBuilder();
		for (Object part : messageParts) {
			sb.append(' ');
			if (part instanceof Throwable) {
				Throwable ex = (Throwable) part;
				sb.append(formatException(ex));
				sb.append("\n").append(getStackTrace(ex));
			} else {
				try {
					sb.append(Str.format(part));
				} catch (Exception ex) {
					sb.append("[Message-ERROR: " + formatException(ex) + "]");
				}
			}
		}
		return sb.toString();
	}

	private static boolean isWrapperException(Throwable ex) {
		if (ex instanceof WrapperException) return true;
		if (getSimpleName(ex.getClass()).equals("RuntimeException")) return true;
		if (getSimpleName(ex.getClass()).equals("ExecutionException")) return true;
		if (getSimpleName(ex.getClass()).equals("UmbrellaException")) return true;
		return false;
	}

	public static String formatEnumeration(Enumeration e) {
		return formatCollection(Utl.toList(e));
	}

	public static String formatCollection(Collection c) {
		return formatObjectArray(c.toArray());
	}

	public static String formatMap(Map map) {
		StringBuilder sb = new StringBuilder();
		sb.append("map[");
		sb.append(map.size());
		sb.append("]={");
		boolean following = false;
		Set<Map.Entry> entries = map.entrySet();
		for (Map.Entry entry : entries) {
			Object key = entry.getKey();
			Object value = entry.getValue();
			if (following) {
				sb.append(',');
			}
			following = true;
			sb.append('"');
			sb.append(format(key));
			sb.append("\"=\"");
			sb.append(format(value));
			sb.append('"');
		}
		sb.append('}');
		return sb.toString();
	}

	public static String formatException(Throwable ex) {
		return formatException(ex, "\nCaused by ");
	}

	public static String formatException(Throwable ex, String causeSeparator) {
		StringBuilder sb = null;
		while (ex != null) {
			Throwable cause = ex.getCause();
			String message = ex.getMessage();
			if (ex instanceof EntityDoesNotExistException) {
				String callerInfo = ((EntityDoesNotExistException) ex).getCallerInfo();
				if (callerInfo != null) message += " @" + callerInfo;
				String entityType = ((EntityDoesNotExistException) ex).getEntityType();
				if (entityType != null) message += " (" + entityType + ")";
			}
			if (cause != null && message != null && message.startsWith(cause.getClass().getName())) message = null;
			while ((isWrapperException(ex) && cause != null)) {
				ex = cause;
				cause = ex.getCause();
				message = ex.getMessage();
				if (cause != null && message != null && message.startsWith(cause.getClass().getName())) message = null;
			}
			if (sb == null) {
				sb = new StringBuilder();
			} else {
				sb.append(causeSeparator);
			}
			if (!isWrapperException(ex)) {
				sb.append(getSimpleName(ex.getClass()));
				sb.append(": ");
			}
			sb.append(message);
			ex = cause;
		}
		return sb.toString();
	}

	public static String formatStackTrace(StackTraceElement[] trace) {
		StringBuilder sb = new StringBuilder();
		for (StackTraceElement element : trace)
			sb.append("    at ").append(element).append("\n");
		return sb.toString();
	}

	public static String formatMapAsKeyValueLines(Map<?, ?> map) {
		if (map == null) return "";
		StringBuilder sb = new StringBuilder();
		for (Map.Entry entry : map.entrySet()) {
			sb.append(format(entry.getKey()));
			sb.append(" = ");
			sb.append(format(entry.getValue()));
			sb.append("\n");
		}
		return sb.toString();
	}

	public static String getStackTrace(Throwable t) {
		StringBuilder sb = new StringBuilder();
		sb.append(t.toString()).append("\n");
		sb.append(formatStackTrace(t.getStackTrace()));

		Throwable cause = t.getCause();
		if (cause == null) return sb.toString();
		sb.append("Caused by: ").append(getStackTrace(cause));

		return sb.toString();
	}

	public static String formatObjectArray(Object[] oa) {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		for (int i = 0; i < oa.length; i++) {
			if (oa[i] != null) {
				sb.append('<');
				sb.append(format(oa[i]));
				sb.append('>');
			}
			if (i != oa.length - 1) {
				sb.append(',');
			}
		}
		sb.append('}');
		return sb.toString();
	}

	public static String getSimpleName(Class type) {
		String name = type.getName();
		int idx = name.lastIndexOf('.');
		if (idx > 0) {
			name = name.substring(idx + 1);
		}
		return name;
	}

	public static boolean isBlank(String s) {
		return s == null || s.isEmpty() || s.trim().isEmpty();
	}

	public static interface Formatable {

		String format();

	}

	public static boolean isAllDigits(String s) {
		if (s == null || s.isEmpty()) return false;
		int len = s.length();
		for (int i = 0; i < len; i++) {
			if (!Character.isDigit(s.charAt(i))) return false;
		}
		return true;
	}

	public static boolean isAllUppercase(String s) {
		if (s == null || s.isEmpty()) return false;
		int len = s.length();
		for (int i = 0; i < len; i++) {
			if (!Character.isUpperCase(s.charAt(i))) return false;
		}
		return true;
	}

	public static String trimAllWhitespace(String s) {
		String result = s.trim();
		result = s.replaceAll("\\s+", " ");
		return result;
	}

	public static boolean equalsTrimmedIgnoreCase(String a, String b) {
		if (a == null && b == null) return true;
		if (a == null || b == null) return false;
		return trimAllWhitespace(a).equalsIgnoreCase(trimAllWhitespace(b));
	}

	public static double getSimilarity(String a, String b) {
		if (a == null && b == null) return 1.0;
		if (a == null || b == null) return 0.0;
		a = Str.trimAllWhitespace(a).toUpperCase();
		b = Str.trimAllWhitespace(b).toUpperCase();
		int max = Math.max(a.length(), b.length());
		if (max == 0) return 1.0;
		int distance = getLevenshteinDistance(a, b);

		return 1.0d - (double) distance / (double) max;
	}

	private static int getLevenshteinDistance(String s, String t) {
		if (s == null || t == null) { throw new IllegalArgumentException("Strings must not be null"); }

		/*
		 * The difference between this impl. and the previous is that, rather than creating and retaining a
		 * matrix of size s.length()+1 by t.length()+1, we maintain two single-dimensional arrays of length
		 * s.length()+1. The first, d, is the 'current working' distance array that maintains the newest
		 * distance cost counts as we iterate through the characters of String s. Each time we increment the
		 * index of String t we are comparing, d is copied to p, the second int[]. Doing so allows us to
		 * retain the previous cost counts as required by the algorithm (taking the minimum of the cost count
		 * to the left, up one, and diagonally up and to the left of the current cost count being calculated).
		 * (Note that the arrays aren't really copied anymore, just switched...this is clearly much better
		 * than cloning an array or doing a System.arraycopy() each time through the outer loop.)
		 *
		 * Effectively, the difference between the two implementations is this one does not cause an out of
		 * memory condition when calculating the LD over two very large strings.
		 */

		int n = s.length(); // length of s
		int m = t.length(); // length of t

		if (n == 0) {
			return m;
		} else if (m == 0) { return n; }

		if (n > m) {
			// swap the input strings to consume less memory
			String tmp = s;
			s = t;
			t = tmp;
			n = m;
			m = t.length();
		}

		int p[] = new int[n + 1]; // 'previous' cost array, horizontally
		int d[] = new int[n + 1]; // cost array, horizontally
		int _d[]; // placeholder to assist in swapping p and d

		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t

		char t_j; // jth character of t

		int cost; // cost

		for (i = 0; i <= n; i++) {
			p[i] = i;
		}

		for (j = 1; j <= m; j++) {
			t_j = t.charAt(j - 1);
			d[0] = j;

			for (i = 1; i <= n; i++) {
				cost = s.charAt(i - 1) == t_j ? 0 : 1;
				// minimum of cell to the left+1, to the top+1, diagonally left and up +cost
				d[i] = Math.min(Math.min(d[i - 1] + 1, p[i] + 1), p[i - 1] + cost);
			}

			// copy current distance counts to 'previous row' distance counts
			_d = p;
			p = d;
			d = _d;
		}

		// our last action in the above loop was to switch d and p, so p now
		// actually has the most recent cost counts
		return p[n];
	}

	public static String securePath(String key) {
		if (key == null) return null;
		return key.replace("..", "__");
	}

	public static int getTotalLength(List<String> values) {
		if (values == null || values.isEmpty()) return 0;
		int len = 0;
		for (String s : values) {
			if (s == null) continue;
			len += s.length();
		}
		return len;
	}

	public static String concatOrNull(String seperator, String... ss) {
		if (ss == null) return null;
		int count = 0;
		StringBuilder sb = new StringBuilder();
		for (String s : ss) {
			if (s == null) continue;
			if (count > 0) sb.append(seperator);
			sb.append(s);
			count++;
		}
		return count == 0 ? null : sb.toString();
	}

}
