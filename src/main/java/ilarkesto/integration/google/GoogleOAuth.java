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
package ilarkesto.integration.google;

import ilarkesto.core.auth.LoginData;
import ilarkesto.core.auth.LoginDataProvider;
import ilarkesto.core.logging.Log;
import ilarkesto.integration.oauth.OAuth2;
import ilarkesto.swing.LoginPanel;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.gdata.client.contacts.ContactsService;

public class GoogleOAuth extends OAuth2 {

	private static final Log log = Log.get(GoogleOAuth.class);

	public static void main(String[] args) {
		LoginData clientIdLogin = LoginPanel.showDialog(null, "Client ID",
			new File("runtimedata/google-oauth2.properties"));
		if (clientIdLogin == null) return;

		GoogleOAuth client = new GoogleOAuth(clientIdLogin.getLogin(), clientIdLogin.getPassword(), REDIRECT_OOB, null,
				SCOPE_USERINFO_EMAIL, SCOPE_CONTACTS, SCOPE_CALENDAR);
		log.warn(client.createUrlForAuthenticationRequest(true));

		LoginData authorizationCodeLogin = LoginPanel.showDialog(null, "Authorization Code",
			new File("runtimedata/google-oauth2-code.properties"));

		client.exchangeAuthorizationCodeForAccessToken(authorizationCodeLogin.getPassword());
		System.out.println("refresh-token: " + client.getRefreshToken());
		client.getRefreshToken();

		client.exchangeRefreshTokenForAccessToken();
	}

	public static final String SCOPE_USERINFO_EMAIL = "https://www.googleapis.com/auth/userinfo.email";
	public static final String SCOPE_USERINFO_PROFILE = "https://www.googleapis.com/auth/userinfo.profile";
	public static final String SCOPE_CONTACTS = "https://www.google.com/m8/feeds/";
	public static final String SCOPE_CALENDAR = CalendarScopes.CALENDAR;

	public GoogleOAuth(LoginDataProvider clientIdAndSecret, String redirectUri, String refreshToken, String... scopes) {
		this(clientIdAndSecret.getLoginData().getLogin(), clientIdAndSecret.getLoginData().getPassword(), redirectUri,
				refreshToken, scopes);
	}

	public GoogleOAuth(String clientId, String clientSecret, String redirectUri, String refreshToken,
			String... scopes) {
		super("https://accounts.google.com/o/oauth2/auth", "https://www.googleapis.com/oauth2/v3/token", clientId,
				clientSecret, redirectUri, refreshToken, scopes);
	}

	public ContactsService createContactsService() {
		ContactsService service = new ContactsService("<var>Ilarkesto</var>");
		service.getRequestFactory().setHeader("User-Agent", "Ilarkesto");
		service.useSsl();
		service.setHeader("GData-Version", "3.0");
		service.setPrivateHeader("Authorization", "Bearer " + getAccessToken(true));
		service.getRequestFactory().setHeader("User-Agent", "Ilarkesto");
		return service;
	}

	public Calendar createCalendarService() {
		NetHttpTransport httpTransport;
		try {
			httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		} catch (GeneralSecurityException ex) {
			throw new RuntimeException(ex);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
		JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

		Credential credential = new Credential.Builder(BearerToken.authorizationHeaderAccessMethod())
				.setJsonFactory(jsonFactory).setTransport(httpTransport).build();

		// credential.setRefreshToken(getRefreshToken());
		credential.setAccessToken(getAccessToken(true));

		return new Calendar.Builder(httpTransport, jsonFactory, credential).setApplicationName("Ilarkesto").build();
	}

	public static GoogleOAuth createTestOAuthClient() {
		LoginData clientIdLogin = LoginPanel.showDialog(null, "Client ID",
			new File("runtimedata/google-oauth2.properties"));
		if (clientIdLogin == null) return null;

		LoginData refreshTokenLogin = LoginPanel.showDialog(null, "Refresh Token",
			new File("runtimedata/google-oauth2-refresh.properties"));

		GoogleOAuth client = new GoogleOAuth(clientIdLogin.getLogin(), clientIdLogin.getPassword(),
				GoogleOAuth.REDIRECT_OOB, refreshTokenLogin.getPassword(), GoogleOAuth.SCOPE_USERINFO_EMAIL,
				GoogleOAuth.SCOPE_CONTACTS, GoogleOAuth.SCOPE_CALENDAR);
		return client;
	}

}
