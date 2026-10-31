package ch.cyberduck.core.brick;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.KeychainLoginService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.jersey.HttpComponentsProvider;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.http.protocol.HttpContext;
import org.glassfish.jersey.client.ClientConfig;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BrickSessionTest {
    @Test
    public void testDisabledKeychainDoesNotLoadSavedApiKey() throws Exception {
        final String previous = PreferencesFactory.get().getProperty("connection.login.keychain");
        final String previousStore = PreferencesFactory.get().getProperty("factory.passwordstore.class");
        PreferencesFactory.get().setProperty("connection.login.keychain", true);
        PreferencesFactory.get().setProperty("factory.passwordstore.class", SavedApiKeyStore.class.getName());
        SavedApiKeyStore.lookups.set(0);
        try {
            for(String password : new String[]{"", "manual-api-key"}) {
                final Host host = new Host(new BrickProtocol(), "customer.files.com", new Credentials("", password));
                final AtomicInteger authorizations = new AtomicInteger();
                final BrickSession session = new BrickSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager()) {
                    @Override
                    public Credentials authorize(final LoginCallback prompt, final CancelCallback cancel) {
                        authorizations.incrementAndGet();
                        return host.getCredentials().setPassword("new-api-key").setSaved(false);
                    }
                };
                final CloseableHttpClient http = mock(CloseableHttpClient.class);
                when(http.execute(any(HttpHost.class), any(HttpRequest.class), any(HttpContext.class))).thenAnswer(invocation -> this.response(200));
                try {
                    new KeychainLoginService(PasswordStoreFactory.get()).validate(host, new DefaultX509KeyManager(), LoginCallback.noop,
                            new LoginOptions(host.getProtocol()).keychain(false));
                    this.open(session, http);
                    session.login(LoginCallback.noop, CancelCallback.noop);
                    assertEquals(password.isEmpty() ? 1 : 0, authorizations.get());
                    assertEquals(password.isEmpty() ? "new-api-key" : password, host.getCredentials().getPassword());
                    assertEquals(0, SavedApiKeyStore.lookups.get());
                }
                finally {
                    session.close();
                }
            }
        }
        finally {
            PreferencesFactory.get().setProperty("connection.login.keychain", previous);
            PreferencesFactory.get().setProperty("factory.passwordstore.class", previousStore);
        }
    }

    @Test
    public void testSavedApiKeyIsLoadedWithoutPasswordInput() throws Exception {
        final Host host = new Host(new BrickProtocol(), "customer.files.com", new Credentials("jane"));
        final HostPasswordStore store = mock(HostPasswordStore.class);
        when(store.findLoginPassword(host)).thenReturn("saved-api-key");
        final BrickSession session = new BrickSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager()) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == AttributesFinder.class) {
                    return (T) (AttributesFinder) (file, listener) -> PathAttributes.EMPTY;
                }
                return super._getFeature(type);
            }

            @Override
            public Credentials authorize(final LoginCallback prompt, final CancelCallback cancel) {
                fail("A saved API key should not open browser authentication");
                return null;
            }
        };
        try {
            new KeychainLoginService(store).validate(host, new DefaultX509KeyManager(), LoginCallback.noop, new LoginOptions(host.getProtocol()));
            session.open(target -> Proxy.DIRECT, HostKeyCallback.noop, LoginCallback.noop, CancelCallback.noop);
            session.login(LoginCallback.noop, CancelCallback.noop);
            assertEquals("saved-api-key", host.getCredentials().getPassword());
            assertFalse(host.getCredentials().isSaved());
            verify(store).findLoginPassword(host);
        }
        finally {
            session.close();
        }
    }

    @Test
    public void testApiKeyWithoutUsernameDoesNotOpenBrowser() throws Exception {
        final Host host = new Host(new BrickProtocol(), "customer.files.com", new Credentials("", "manual-api-key"));
        final AtomicInteger authorizations = new AtomicInteger();
        final BrickSession session = this.session(host, authorizations);
        final CloseableHttpClient http = mock(CloseableHttpClient.class);
        when(http.execute(any(HttpHost.class), any(HttpRequest.class), any(HttpContext.class))).thenAnswer(invocation -> this.response(200));
        try {
            this.open(session, http);
            session.login(LoginCallback.noop, CancelCallback.noop);
            assertEquals(0, authorizations.get());
            assertEquals("manual-api-key", host.getCredentials().getPassword());
            verify(http).execute(any(HttpHost.class), any(HttpRequest.class), any(HttpContext.class));
        }
        finally {
            session.close();
        }
    }

    @Test
    public void testMissingApiKeyAuthorizes() throws Exception {
        final Host host = new Host(new BrickProtocol(), "customer.files.com");
        final AtomicInteger authorizations = new AtomicInteger();
        final BrickSession session = this.session(host, authorizations);
        try {
            session.open(target -> Proxy.DIRECT, HostKeyCallback.noop, LoginCallback.noop, CancelCallback.noop);
            session.login(LoginCallback.noop, CancelCallback.noop);
            assertEquals(1, authorizations.get());
            assertEquals("new-api-key", host.getCredentials().getPassword());
        }
        finally {
            session.close();
        }
    }

    @Test
    public void testUnauthorizedUsesOAuthOnce() throws Exception {
        final Host host = new Host(new BrickProtocol(), "customer.files.com");
        final AtomicInteger authorizations = new AtomicInteger();
        final BrickUnauthorizedRetryStrategy strategy = new BrickUnauthorizedRetryStrategy(
                this.session(host, authorizations), LoginCallback.noop, CancelCallback.noop);
        strategy.setApiKey("old-api-key");
        final ApiException response = new ApiException(401, "Unauthorized");
        final HttpCoreContext context = HttpCoreContext.create();
        assertTrue(strategy.retryRequest(response, 1));
        assertFalse(strategy.retryRequest(response, 2));
        assertFalse(strategy.retryRequest(new ApiException(403, "Forbidden"), 1));
        assertEquals(1, authorizations.get());
        final BasicHttpRequest request = new BasicHttpRequest("GET", "/api/rest/v1/folders");
        strategy.process(request, context);
        assertEquals("new-api-key", request.getFirstHeader("X-FilesAPI-Key").getValue());
    }

    @Test
    public void testApiKeyIsNotSentToDownloadHost() throws Exception {
        final Host host = new Host(new BrickProtocol(), "customer.files.com");
        final BrickUnauthorizedRetryStrategy strategy = new BrickUnauthorizedRetryStrategy(
                this.session(host, new AtomicInteger()), LoginCallback.noop, CancelCallback.noop);
        strategy.setApiKey("api-key");
        final HttpCoreContext context = HttpCoreContext.create();
        context.setTargetHost(new HttpHost("download.example.com", 443, "https"));
        final BasicHttpRequest request = new BasicHttpRequest("GET", "/download");
        request.addHeader("X-FilesAPI-Key", "copied-by-redirect");
        strategy.process(request, context);
        assertNull(request.getFirstHeader("X-FilesAPI-Key"));
        context.setTargetHost(new HttpHost("customer.files.com", 443, "https"));
        strategy.process(request, context);
        assertEquals("api-key", request.getFirstHeader("X-FilesAPI-Key").getValue());
        context.setTargetHost(new HttpHost("customer.files.com", 80, "http"));
        strategy.process(request, context);
        assertNull(request.getFirstHeader("X-FilesAPI-Key"));
    }

    @Test
    public void testSavedBookmarkPreservesAuthorizationFailureAndCancellation() throws Exception {
        for(BackgroundException failure : new BackgroundException[]{
                new LoginFailureException("Sign out of Files.com in your browser, then sign in as jane."), new LoginCanceledException()}) {
            final Host host = new Host(new BrickProtocol(), "customer.files.com", new Credentials("jane", "old-api-key"));
            final BrickSession session = new BrickSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager()) {
                @Override
                public Credentials authorize(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
                    throw failure;
                }
            };
            final CloseableHttpClient http = mock(CloseableHttpClient.class);
            when(http.execute(any(HttpHost.class), any(HttpRequest.class), any(HttpContext.class))).thenAnswer(invocation -> this.response(401));
            try {
                this.open(session, http);
                assertSame(failure, assertThrows(BackgroundException.class, () -> session.login(LoginCallback.noop, CancelCallback.noop)));
                verify(http).execute(any(HttpHost.class), any(HttpRequest.class), any(HttpContext.class));
            }
            finally {
                session.close();
            }
        }
    }

    @Test
    public void testApiKeyReauthorizationForDifferentSiteDoesNotRetryOperation() throws Exception {
        final Host host = new Host(new BrickProtocol(), "customer.files.com", new Credentials("", "old-api-key"));
        final MockHttpTransport transport = new MockHttpTransport.Builder().setLowLevelHttpResponse(new MockLowLevelHttpResponse()
                .setContentType("application/json").setContent("{\"access_token\":\"oauth-token\",\"token_type\":\"Bearer\",\"files_api_key\":{"
                        + "\"password\":\"replacement-key\",\"user_username\":\"jane\",\"nickname\":\"Other site\",\"server\":\"https://other-site.files.com\"}}"))
                .build();
        final BrickOAuth2AuthorizationService service = new BrickOAuth2AuthorizationService(transport, host, "test-client", LoginCallback.noop);
        final AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(),
                transport, new GsonFactory(), new GenericUrl("https://customer.files.com/auth/oauth/token"),
                new ClientParametersAuthentication("test-client", null), "test-client", "https://customer.files.com/auth/oauth/authorize").build();
        final BrickSession session = new BrickSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager()) {
            @Override
            public Credentials authorize(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
                service.exchangeToken(flow, "authorization-code", "http://127.0.0.1:8765/oauth/callback");
                return host.getCredentials().setSaved(false);
            }
        };
        final CloseableHttpClient http = mock(CloseableHttpClient.class);
        when(http.execute(any(HttpHost.class), any(HttpRequest.class), any(HttpContext.class))).thenAnswer(invocation -> this.response(401));
        try {
            this.open(session, http);
            final LoginFailureException failure = assertThrows(LoginFailureException.class, () -> session.login(LoginCallback.noop, CancelCallback.noop));
            assertTrue(failure.getDetail().contains("customer.files.com"));
            assertEquals("customer.files.com", host.getHostname());
            assertEquals("old-api-key", host.getCredentials().getPassword());
            verify(http).execute(any(HttpHost.class), any(HttpRequest.class), any(HttpContext.class));
        }
        finally {
            session.close();
        }
    }

    @Test
    public void testLegacyBookmarkRetriesAtAuthorizedSite() throws Exception {
        final Host host = new Host(new BrickProtocol(), "app.files.com", new Credentials("jane", "old-api-key"));
        final AtomicInteger authorizations = new AtomicInteger();
        final BrickSession session = new BrickSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager()) {
            @Override
            public Credentials authorize(final LoginCallback prompt, final CancelCallback cancel) {
                authorizations.incrementAndGet();
                host.setHostname("customer.files.com");
                return host.getCredentials().setPassword("new-api-key").setSaved(false);
            }
        };
        final CloseableHttpClient http = mock(CloseableHttpClient.class);
        final AtomicInteger requests = new AtomicInteger();
        when(http.execute(any(HttpHost.class), any(HttpRequest.class), any(HttpContext.class))).thenAnswer(invocation -> {
            final HttpHost target = invocation.getArgument(0);
            if(requests.getAndIncrement() == 0) {
                assertEquals("app.files.com", target.getHostName());
                return this.response(401);
            }
            assertEquals("customer.files.com", target.getHostName());
            return this.response(200);
        });
        try {
            this.open(session, http);
            session.login(LoginCallback.noop, CancelCallback.noop);
            assertEquals(1, authorizations.get());
            assertEquals(2, requests.get());
            assertEquals("new-api-key", host.getCredentials().getPassword());
        }
        finally {
            session.close();
        }
    }

    @Test
    public void testRejectedReplacementKeyDoesNotAuthorizeAgain() throws Exception {
        final Host host = new Host(new BrickProtocol(), "customer.files.com", new Credentials("jane", "old-api-key"));
        final AtomicInteger authorizations = new AtomicInteger();
        final BrickSession session = this.session(host, authorizations);
        final CloseableHttpClient http = mock(CloseableHttpClient.class);
        when(http.execute(any(HttpHost.class), any(HttpRequest.class), any(HttpContext.class))).thenAnswer(invocation -> this.response(401));
        try {
            this.open(session, http);
            assertThrows(LoginFailureException.class, () -> session.login(LoginCallback.noop, CancelCallback.noop));
            assertEquals(1, authorizations.get());
            verify(http, times(2)).execute(any(HttpHost.class), any(HttpRequest.class), any(HttpContext.class));
        }
        finally {
            session.close();
        }
    }

    private void open(final BrickSession session, final CloseableHttpClient http) throws Exception {
        session.open(target -> Proxy.DIRECT, HostKeyCallback.noop, LoginCallback.noop, CancelCallback.noop);
        final ClientConfig configuration = new ClientConfig(session.getClient().getHttpClient().getConfiguration());
        session.getClient().getHttpClient().close();
        session.getClient().setHttpClient(ClientBuilder.newClient(configuration.connectorProvider(new HttpComponentsProvider(http))));
    }

    private CloseableHttpResponse response(final int status) {
        final CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, status, ""));
        when(response.getAllHeaders()).thenReturn(new Header[]{new BasicHeader("Content-Type", "application/json")});
        when(response.getEntity()).thenReturn(new StringEntity(status == 200 ? "{\"path\":\"\",\"type\":\"directory\"}" : "{\"error\":\"Unauthorized\"}", ContentType.APPLICATION_JSON));
        return response;
    }

    private BrickSession session(final Host host, final AtomicInteger authorizations) {
        return new BrickSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager()) {
            @Override
            public Credentials authorize(final LoginCallback prompt, final CancelCallback cancel) {
                authorizations.incrementAndGet();
                return host.getCredentials().setUsername("jane").setPassword("new-api-key").setSaved(false);
            }
        };
    }

    public static class SavedApiKeyStore extends DisabledPasswordStore {
        private static final AtomicInteger lookups = new AtomicInteger();

        @Override
        public String findLoginPassword(final Host bookmark) {
            lookups.incrementAndGet();
            return "saved-api-key";
        }
    }
}
