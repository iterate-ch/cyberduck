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
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.oauth.OAuth2AuthorizationCodeProvider;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.CancelCallback;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;

import static org.junit.Assert.*;

public class BrickOAuth2AuthorizationServiceTest {
    @Test
    public void testAuthorizationWithDedicatedClientAndPKCE() throws Exception {
        final String property = "factory.authorizationcodeprovider.class";
        final String previous = PreferencesFactory.get().getProperty(property);
        PreferencesFactory.get().setProperty(property, CapturingCodeProvider.class.getName());
        try {
            final Host host = new Host(new BrickProtocol(), "customer.files.com");
            final MockHttpTransport transport = this.transport(this.key());
            final BrickOAuth2AuthorizationService service = new BrickOAuth2AuthorizationService(
                    transport, host, host.getProtocol().getOAuthClientId(), LoginCallback.noop);
            service.setParameter("computer", "test-mac").setParameter("platform", "Cyberduck");
            service.authorize(CancelCallback.noop);
            final GenericUrl authorize = new GenericUrl(CapturingCodeProvider.url);
            assertEquals("cyberduck", authorize.getFirst("client_id"));
            assertEquals("desktop", authorize.getFirst("scope"));
            assertEquals("S256", authorize.getFirst("code_challenge_method"));
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
        }
        finally {
            PreferencesFactory.get().setProperty(property, previous);
        }
    }

    @Test
    public void testExchangeApiKey() throws Exception {
        final Host host = new Host(new BrickProtocol(), "app.files.com");
        this.exchange(host, this.key());
        assertEquals("customer.files.com", host.getHostname());
        assertEquals(443, host.getPort());
        assertEquals("jane", host.getCredentials().getUsername());
        assertEquals("api-key", host.getCredentials().getPassword());
        assertFalse(host.getCredentials().isOAuthAuthentication());
    }

    @Test
    public void testReauthorizationPreservesAccount() throws Exception {
        final Host host = new Host(new BrickProtocol(), "customer.files.com", new Credentials("jane", "old-api-key"));
        final Map<String, Object> key = this.key();
        key.put("server", "https://CUSTOMER.files.com:443");
        this.exchange(host, key);
        assertEquals("jane", host.getCredentials().getUsername());
        assertEquals("api-key", host.getCredentials().getPassword());
        assertEquals(443, host.getPort());
    }

    @Test
    public void testLegacyDefaultHostnameAdoptsAuthorizedSiteForSameUser() throws Exception {
        final Host host = new Host(new BrickProtocol(), "APP.files.com", new Credentials("jane", "old-api-key"));
        this.exchange(host, this.key());
        assertEquals("customer.files.com", host.getHostname());
        assertEquals(443, host.getPort());
        assertEquals("jane", host.getCredentials().getUsername());
        assertEquals("api-key", host.getCredentials().getPassword());
    }

    @Test
    public void testLegacyDefaultHostnameStillRejectsDifferentUserOrNonstandardPort() {
        for(Host host : new Host[]{
                new Host(new BrickProtocol(), "app.files.com", new Credentials("other-user", "old-api-key")),
                new Host(new BrickProtocol(), "app.files.com", 444, new Credentials("jane", "old-api-key"))}) {
            assertThrows(LoginFailureException.class, () -> this.exchange(host, this.key()));
            assertEquals("app.files.com", host.getHostname());
            assertEquals("old-api-key", host.getCredentials().getPassword());
        }
    }

    @Test
    public void testReauthorizationRejectsDifferentAccount() {
        for(String[] account : new String[][]{
                {"other-user", "https://customer.files.com"},
                {"jane", "https://other-site.files.com"},
                {"jane", "https://customer.files.com:444"}}) {
            final Host host = new Host(new BrickProtocol(), "customer.files.com", new Credentials("jane", "old-api-key"));
            host.setNickname("Saved account");
            host.getCredentials().setSaved(false);
            final Map<String, Object> key = this.key();
            key.put("user_username", account[0]);
            key.put("server", account[1]);
            assertThrows(LoginFailureException.class, () -> this.exchange(host, key));
            assertEquals("customer.files.com", host.getHostname());
            assertEquals(443, host.getPort());
            assertEquals("jane", host.getCredentials().getUsername());
            assertEquals("old-api-key", host.getCredentials().getPassword());
            assertEquals("Saved account", host.getNickname());
            assertFalse(host.getCredentials().isSaved());
        }
    }

    @Test
    public void testRejectedCredentialsAreRevokedAtTokenServer() throws Exception {
        final Host host = new Host(new BrickProtocol(), "customer.files.com", new Credentials("original-user", "old-api-key"));
        final MockHttpTransport tokenTransport = this.transport(this.key());
        final MockLowLevelHttpRequest deletion = new MockLowLevelHttpRequest().setResponse(new MockLowLevelHttpResponse().setStatusCode(204));
        final MockHttpTransport transport = new MockHttpTransport() {
            @Override
            public LowLevelHttpRequest buildRequest(final String method, final String url) throws IOException {
                if("DELETE".equals(method)) {
                    assertEquals("https://app.files.com/api/rest/v1/api_key", url);
                    return deletion;
                }
                return tokenTransport.buildRequest(method, url);
            }
        };
        assertThrows(LoginFailureException.class, () -> this.exchangeWithTransport(host, transport));
        assertEquals("api-key", deletion.getFirstHeaderValue("X-FilesAPI-Key"));
        assertEquals("old-api-key", host.getCredentials().getPassword());
    }

    @Test
    public void testRevocationFailurePreservesAccountMismatch() throws Exception {
        final Host host = new Host(new BrickProtocol(), "customer.files.com", new Credentials("original-user", "old-api-key"));
        final MockHttpTransport tokenTransport = this.transport(this.key());
        final MockHttpTransport transport = new MockHttpTransport() {
            @Override
            public LowLevelHttpRequest buildRequest(final String method, final String url) throws IOException {
                if("DELETE".equals(method)) {
                    throw new IOException("Revocation unavailable");
                }
                return tokenTransport.buildRequest(method, url);
            }
        };
        final LoginFailureException failure = assertThrows(LoginFailureException.class, () -> this.exchangeWithTransport(host, transport));
        assertTrue(failure.getDetail().contains("original-user"));
        assertEquals("original-user", host.getCredentials().getUsername());
        assertEquals("old-api-key", host.getCredentials().getPassword());
    }

    @Test
    public void testMissingApiKey() {
        final Host host = new Host(new BrickProtocol(), "app.files.com");
        assertThrows(LoginFailureException.class, () -> this.exchange(host, null));
        assertEquals("app.files.com", host.getHostname());
    }

    @Test
    public void testIncompleteApiKey() {
        for(String field : new String[]{"password", "user_username", "server"}) {
            final Host host = new Host(new BrickProtocol(), "app.files.com");
            final Map<String, Object> key = this.key();
            key.remove(field);
            assertThrows(field, LoginFailureException.class, () -> this.exchange(host, key));
            assertEquals("app.files.com", host.getHostname());
            assertFalse(host.getCredentials().isPasswordAuthentication());
        }
    }

    @Test
    public void testInvalidServer() {
        for(String server : new String[]{"http://customer.files.com", "https://user@customer.files.com",
                "https://customer.files.com/path", "https://customer.files.com?query", "https://customer.files.com#fragment", "[invalid"}) {
            final Host host = new Host(new BrickProtocol(), "app.files.com");
            final Map<String, Object> key = this.key();
            key.put("server", server);
            assertThrows(server, LoginFailureException.class, () -> this.exchange(host, key));
            assertEquals("app.files.com", host.getHostname());
            assertFalse(host.getCredentials().isPasswordAuthentication());
        }
    }

    private Map<String, Object> key() {
        final Map<String, Object> key = new LinkedHashMap<>();
        key.put("password", "api-key");
        key.put("user_username", "jane");
        key.put("nickname", "jane @ customer.files.com");
        key.put("server", "https://customer.files.com");
        return key;
    }

    private void exchange(final Host host, final Object key) throws Exception {
        this.exchangeWithTransport(host, this.transport(key));
    }

    private void exchangeWithTransport(final Host host, final MockHttpTransport transport) throws Exception {
        final GsonFactory json = new GsonFactory();
        final BrickOAuth2AuthorizationService service = new BrickOAuth2AuthorizationService(transport, host, "test-client", LoginCallback.noop);
        final AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(),
                transport, json, new GenericUrl("https://app.files.com/auth/oauth/token"),
                new ClientParametersAuthentication("test-client", null), "test-client", "https://app.files.com/auth/oauth/authorize")
                .setScopes(Collections.emptyList()).build();
        service.exchangeToken(flow, "authorization-code", "http://127.0.0.1:8765/oauth/callback");
    }

    private MockHttpTransport transport(final Object key) throws Exception {
        final GenericJson response = new GenericJson().set("access_token", "oauth-token").set("token_type", "Bearer").set("expires_in", 300);
        if(key != null) {
            response.set("files_api_key", key);
        }
        final GsonFactory json = new GsonFactory();
        return new MockHttpTransport.Builder().setLowLevelHttpResponse(
                new MockLowLevelHttpResponse().setContentType("application/json").setContent(json.toString(response))).build();
    }

    public static class CapturingCodeProvider implements OAuth2AuthorizationCodeProvider {
        private static String url;

        @Override
        public String prompt(final Host host, final LoginCallback prompt, final String authorizationCodeUrl, final String redirectUri, final String state) {
            url = authorizationCodeUrl;
            return "authorization-code";
        }
    }
}
