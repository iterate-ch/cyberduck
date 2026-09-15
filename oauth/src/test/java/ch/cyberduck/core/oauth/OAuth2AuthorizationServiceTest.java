package ch.cyberduck.core.oauth;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.junit.Test;

import java.net.URI;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.openidconnect.IdTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.testing.http.MockHttpTransport;

import static org.junit.Assert.*;

public class OAuth2AuthorizationServiceTest {

    @Test
    public void testToRedirectUriLoopbackWithoutPort() throws Exception {
        final URI uri = URI.create(OAuth2AuthorizationService.toRedirectUri("http://localhost/"));
        assertEquals("http", uri.getScheme());
        assertEquals("localhost", uri.getHost());
        assertNotEquals(-1, uri.getPort());
        assertTrue(uri.getPort() > 0);
        assertEquals("/", uri.getPath());
    }

    @Test
    public void testToRedirectUriLoopbackAddressWithoutPort() throws Exception {
        final URI uri = URI.create(OAuth2AuthorizationService.toRedirectUri("http://127.0.0.1/oauth/callback?p=v"));
        assertEquals("127.0.0.1", uri.getHost());
        assertTrue(uri.getPort() > 0);
        assertEquals("/oauth/callback", uri.getPath());
        assertEquals("p=v", uri.getQuery());
    }

    @Test
    public void testToRedirectUriUnchanged() throws Exception {
        assertEquals("http://localhost:8765/", OAuth2AuthorizationService.toRedirectUri("http://localhost:8765/"));
        assertEquals(OAuth2AuthorizationService.OOB_REDIRECT_URI, OAuth2AuthorizationService.toRedirectUri(OAuth2AuthorizationService.OOB_REDIRECT_URI));
        assertEquals("x-cyberduck-action:oauth", OAuth2AuthorizationService.toRedirectUri("x-cyberduck-action:oauth"));
        assertEquals("http://192.0.2.1/", OAuth2AuthorizationService.toRedirectUri("http://192.0.2.1/"));
    }

    @Test
    public void testAuthorizeLoopbackRedirectUriWithoutPort() throws Exception {
        final String property = "factory.authorizationcodeprovider.class";
        final String previous = PreferencesFactory.get().getProperty(property);
        PreferencesFactory.get().setProperty(property, CapturingAuthorizationCodeProvider.class.getName());
        try {
            final AtomicReference<String> tokenRequestRedirectUri = new AtomicReference<>();
            final OAuth2AuthorizationService service = new OAuth2AuthorizationService(new MockHttpTransport(), new Host(new TestProtocol()),
                    "http://localhost/token", "http://localhost/authorize", "client", null, Collections.emptyList(), true,
                    new DisabledLoginCallback()) {
                @Override
                protected IdTokenResponse exchangeToken(final AuthorizationCodeFlow flow, final String authorizationCode, final String redirectUri) {
                    tokenRequestRedirectUri.set(redirectUri);
                    return new IdTokenResponse().setAccessToken("a").setRefreshToken("r").setExpiresInSeconds(3600L);
                }
            }.setRedirectUri("http://localhost/");
            final OAuthTokens tokens = service.authorize();
            assertEquals("a", tokens.getAccessToken());
            final String redirectUri = CapturingAuthorizationCodeProvider.redirectUri.get();
            assertNotNull(redirectUri);
            assertTrue(URI.create(redirectUri).getPort() > 0);
            assertEquals(redirectUri, new GenericUrl(CapturingAuthorizationCodeProvider.authorizationCodeUrl.get()).getFirst("redirect_uri"));
            assertEquals(redirectUri, tokenRequestRedirectUri.get());
            // Configuration is not modified
            assertEquals("http://localhost/", service.getRedirectUri());
        }
        finally {
            PreferencesFactory.get().setProperty(property, previous);
        }
    }

    public static final class CapturingAuthorizationCodeProvider implements OAuth2AuthorizationCodeProvider {
        private static final AtomicReference<String> authorizationCodeUrl = new AtomicReference<>();
        private static final AtomicReference<String> redirectUri = new AtomicReference<>();

        @Override
        public String prompt(final Host bookmark, final LoginCallback prompt, final String authorizationCodeRequestUrl, final String redirectUri, final String state) throws BackgroundException {
            CapturingAuthorizationCodeProvider.authorizationCodeUrl.set(authorizationCodeRequestUrl);
            CapturingAuthorizationCodeProvider.redirectUri.set(redirectUri);
            return "code";
        }
    }
}
