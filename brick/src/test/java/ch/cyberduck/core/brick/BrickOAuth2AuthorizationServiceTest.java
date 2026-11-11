package ch.cyberduck.core.brick;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.oauth.OAuth2AuthorizationCodeProvider;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.openidconnect.IdTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.AdditionalAnswers.delegatesTo;

public class BrickOAuth2AuthorizationServiceTest {
    @Test
    public void testAuthorizationWithDedicatedClientAndPKCE() throws Exception {
        final Host host = this.establishedHost();
        final MockHttpTransport transport = this.transport(this.key());
        final OAuth2AuthorizationCodeProvider provider = mock(OAuth2AuthorizationCodeProvider.class);
        when(provider.prompt(eq(host), any(), anyString(), anyString(), anyString())).thenReturn("authorization-code");
        final BrickOAuth2AuthorizationService service = new BrickOAuth2AuthorizationService(
                transport, host, host.getProtocol().getOAuthClientId(), LoginCallback.noop) {
            @Override
            protected String promptForCode(final String url, final String redirect, final String state) throws BackgroundException {
                return provider.prompt(host, LoginCallback.noop, url, redirect, state);
            }
        };
        service.setParameter("computer", "test-mac").setParameter("platform", "Cyberduck");
        service.authorize(CancelCallback.noop);
        final ArgumentCaptor<String> url = ArgumentCaptor.forClass(String.class);
        verify(provider).prompt(eq(host), any(), url.capture(), anyString(), anyString());
        final GenericUrl authorize = new GenericUrl(url.getValue());
        assertEquals("cyberduck", authorize.getFirst("client_id"));
        assertEquals("desktop", authorize.getFirst("scope"));
        assertEquals("S256", authorize.getFirst("code_challenge_method"));
        assertEquals("jane", authorize.getFirst("login_hint"));
        assertEquals("test-mac", authorize.getFirst("computer"));
        assertEquals("Cyberduck", authorize.getFirst("platform"));
        assertNotNull(authorize.getFirst("state"));
        final GenericUrl token = new GenericUrl("http://localhost/?" + transport.getLowLevelHttpRequest().getContentAsString());
        assertEquals("cyberduck", token.getFirst("client_id"));
        assertNull(token.getFirst("client_secret"));
        assertEquals(authorize.getFirst("redirect_uri"), token.getFirst("redirect_uri"));
        final String verifier = (String) token.getFirst("code_verifier");
        assertEquals(authorize.getFirst("code_challenge"), Base64.getUrlEncoder().withoutPadding().encodeToString(
                MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII))));
        assertEquals("api-key", host.getCredentials().getPassword());
        assertFalse(host.getCredentials().isOAuthAuthentication());
    }

    @Test
    public void testInitialAuthorizationIgnoresImplicitCliUsername() throws Exception {
        final BrickProtocol protocol = new BrickProtocol();
        final Host host = new HostParser(new ProtocolFactory(Collections.singleton(protocol)), protocol).get("brick://customer.files.com");
        this.exchange(host, this.key());
        assertEquals("jane", host.getCredentials().getUsername());
        assertEquals("api-key", host.getCredentials().getPassword());
    }

    @Test
    public void testInitialAuthorizationAdoptsCanonicalHostname() throws Exception {
        final Host host = new Host(new BrickProtocol(), "custom.example.com");
        this.exchange(host, this.key());
        assertEquals("customer.files.com", host.getHostname());
        assertEquals("jane", host.getCredentials().getUsername());
    }

    @Test
    public void testEstablishedAccountRejectsDifferentUserOrOriginWithoutRevocation() throws Exception {
        for(String field : new String[]{"user_username", "server"}) {
            final Host host = this.establishedHost();
            final Map<String, Object> key = this.key();
            key.put(field, field.equals("server") ? "https://other.files.com" : "other-user");
            assertThrows(LoginFailureException.class, () -> this.exchange(host, key));
            assertEquals("customer.files.com", host.getHostname());
            assertEquals("jane", host.getCredentials().getUsername());
            assertEquals("old-api-key", host.getCredentials().getPassword());
        }
    }

    @Test
    public void testExistingKeyWithoutKnownAccountStillBindsOrigin() {
        final Host host = new Host(new BrickProtocol(), "customer.files.com", new Credentials("os-user", "manual-key"));
        final Map<String, Object> key = this.key();
        key.put("server", "https://other.files.com");
        assertThrows(LoginFailureException.class, () -> this.exchange(host, key));
    }

    @Test
    public void testDefaultHostnameComesFromProtocol() throws Exception {
        final Host host = new Host(new BrickProtocol() {
            @Override
            public String getDefaultHostname() {
                return "bootstrap.example.com";
            }
        }, "BOOTSTRAP.example.com", new Credentials("jane", "old-api-key"));
        host.getCredentials().setProperty(BrickOAuth2AuthorizationService.ACCOUNT, "jane");
        this.exchange(host, this.key());
        assertEquals("customer.files.com", host.getHostname());
    }

    @Test
    public void testMissingOrInvalidRequiredFields() {
        for(String field : new String[]{"password", "user_username", "server"}) {
            final Map<String, Object> key = this.key();
            key.remove(field);
            assertThrows(LoginFailureException.class, () -> this.exchange(new Host(new BrickProtocol()), key));
        }
        for(String server : new String[]{"http://customer.files.com", "not a URI", "https:///missing-host"}) {
            final Map<String, Object> key = this.key();
            key.put("server", server);
            assertThrows(LoginFailureException.class, () -> this.exchange(new Host(new BrickProtocol()), key));
        }
        assertThrows(LoginFailureException.class, () -> this.exchange(new Host(new BrickProtocol()), null));
    }

    @Test
    public void testOptOutDoesNotMarkIssuedKeyForSaving() throws Exception {
        final Host host = new Host(new BrickProtocol(), "app.files.com");
        final Preferences preferences = mock(Preferences.class, delegatesTo(PreferencesFactory.get()));
        doReturn(false).when(preferences).getBoolean("connection.login.keychain");
        try(MockedStatic<PreferencesFactory> factory = mockStatic(PreferencesFactory.class, withSettings().mockMaker("mock-maker-inline"))) {
            factory.when(PreferencesFactory::get).thenReturn(preferences);
            this.exchange(host, this.key());
            assertFalse(host.getCredentials().isSaved());
            assertFalse(host.getCredentials().isOAuthAuthentication());
        }
    }

    private Host establishedHost() {
        final Host host = new Host(new BrickProtocol(), "customer.files.com", new Credentials("jane", "old-api-key"));
        host.getCredentials().setProperty(BrickOAuth2AuthorizationService.ACCOUNT, "jane");
        return host;
    }

    private void exchange(final Host host, final Object key) throws Exception {
        final MockHttpTransport transport = this.transport(key);
        final AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(),
                transport, new GsonFactory(), new GenericUrl("https://customer.files.com/auth/oauth/token"),
                new ClientParametersAuthentication("cyberduck", null), "cyberduck", "https://customer.files.com/auth/oauth/authorize").build();
        new BrickOAuth2AuthorizationService(transport, host, "cyberduck", LoginCallback.noop) {
            void exchange() throws BackgroundException {
                this.exchangeToken(flow, "code", "http://127.0.0.1:1234/oauth/callback");
            }
        }.exchange();
    }

    private Map<String, Object> key() {
        final Map<String, Object> key = new LinkedHashMap<>();
        key.put("password", "api-key");
        key.put("user_username", "jane");
        key.put("server", "https://customer.files.com");
        key.put("nickname", "Customer");
        return key;
    }

    private MockHttpTransport transport(final Object key) throws Exception {
        final GenericJson response = new GenericJson().set("access_token", "oauth-token").set("refresh_token", "refresh-token")
                .set("token_type", "Bearer").set("expires_in", 300);
        if(key != null) {
            response.set("files_api_key", key);
        }
        return new MockHttpTransport.Builder().setLowLevelHttpResponse(new MockLowLevelHttpResponse()
                .setContentType("application/json").setContent(new GsonFactory().toString(response))).build();
    }
}
