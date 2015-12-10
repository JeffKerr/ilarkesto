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
package ilarkesto.net.httpclient;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HttpSession {

	static HttpSession defaultSession = new HttpSession();

	private Map<String, HttpCookie> cookies = new HashMap<String, HttpCookie>();

	public void addCookie(HttpCookie cookie) {
		cookies.put(cookie.getName(), cookie);
	}

	public Collection<HttpCookie> getCookies() {
		return cookies.values();
	}

	public HttpRequest request(String url) {
		return new HttpRequest(url).setSession(this);
	}

	public String getCookieValue(String name) {
		HttpCookie cookie = getCookie(name);
		if (cookie == null) return null;
		return cookie.getValue();
	}

	public HttpCookie getCookie(String name) {
		return cookies.get(name);
	}

}
