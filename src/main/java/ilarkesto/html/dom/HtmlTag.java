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
package ilarkesto.html.dom;

import ilarkesto.core.base.Utl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HtmlTag extends AHtmlData implements HtmlDataContainer {

	private HtmlTag parent;
	private String name;
	private Map<String, String> attributes;
	private boolean closed;
	protected List<AHtmlData> contents;

	public HtmlTag(HtmlTag parent, String name, Map<String, String> attributes, boolean closed) {
		super();
		this.parent = parent;
		this.name = name.toLowerCase();
		this.attributes = attributes;
		this.closed = closed;
	}

	public boolean isRoot() {
		return parent == null;
	}

	@Override
	public void add(AHtmlData data) {
		if (closed) throw new IllegalStateException();
		if (contents == null) contents = new ArrayList<AHtmlData>();
		contents.add(data);
	}

	public boolean isClosed() {
		return closed;
	}

	public boolean isShort() {
		if (isClosed()) return true;
		if (name.equals("img")) return true;
		if (name.equals("br")) return true;
		if (name.equals("hr")) return true;
		if (name.equals("meta")) return true;
		return false;
	}

	public boolean isContentTextOnly() {
		if (name.equals("script")) return true;
		return false;
	}

	public String getName() {
		return name;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public List<AHtmlData> getContents() {
		return contents;
	}

	public HtmlTag getParent() {
		return parent;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<").append(name);

		if (attributes != null) {
			for (Map.Entry<String, String> entry : attributes.entrySet()) {
				String name = entry.getKey();
				String value = entry.getValue();
				sb.append(" ").append(name);
				if (value != null) sb.append("=\"").append(value).append("\"");
			}
		}

		if (closed) {
			sb.append("/>");
			return sb.toString();
		}
		sb.append(">");

		if (contents != null) {
			for (AHtmlData data : contents) {
				sb.append(data.toString());
			}
		}

		sb.append("</" + name + ">");

		return sb.toString();
	}

	public boolean isStyleClass(String classToCheck) {
		String styleClass = getStyleClass();
		if (styleClass == null) return false;
		return styleClass.contains(classToCheck);
	}

	public String getStyleClass() {
		return getAttribute("class");
	}

	public boolean isAttribute(String name, String valueToCheck) {
		return Utl.equals(valueToCheck, getAttribute(name));
	}

	public String getAttribute(String name) {
		if (attributes == null) return null;
		return attributes.get(name);
	}

	public HtmlTag getTagByName(String name) {
		if (contents == null) return null;
		for (AHtmlData data : contents) {
			if (!(data instanceof HtmlTag)) continue;
			HtmlTag tag = (HtmlTag) data;
			if (tag.getName().equals(name)) return tag;
			HtmlTag ret = tag.getTagByName(name);
			if (ret != null) return ret;
		}
		return null;
	}

	public HtmlTag getTagById(String id) {
		if (contents == null) return null;
		for (AHtmlData data : contents) {
			if (!(data instanceof HtmlTag)) continue;
			HtmlTag tag = (HtmlTag) data;
			if (tag.isAttribute("id", id)) return tag;
			HtmlTag ret = tag.getTagById(id);
			if (ret != null) return ret;
		}
		return null;
	}

	public HtmlTag getTagByStyleClass(String styleClass) {
		if (contents == null) return null;
		for (AHtmlData data : contents) {
			if (!(data instanceof HtmlTag)) continue;
			HtmlTag tag = (HtmlTag) data;
			if (tag.isStyleClass(styleClass)) return tag;
			HtmlTag ret = tag.getTagByStyleClass(styleClass);
			if (ret != null) return ret;
		}
		return null;
	}

	public void removeTagsByStyleClass(String styleClass, boolean recurse) {
		if (contents == null) return;
		for (AHtmlData data : new ArrayList<AHtmlData>(contents)) {
			if (!(data instanceof HtmlTag)) continue;
			HtmlTag tag = (HtmlTag) data;
			if (tag.isStyleClass(styleClass)) contents.remove(tag);
			if (recurse) tag.removeTagsByStyleClass(styleClass, recurse);
		}
	}

}
