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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.threading.AbstractBackgroundAction;
import ch.cyberduck.core.threading.BackgroundActionRegistry;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.protocol.HttpCoreContext;
import org.junit.Test;

import javax.ws.rs.core.GenericType;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BrickSessionTest {
    @Test
    public void testUpgradeRestoresSavedKeyWithoutOAuth() throws Exception {
        final Host host = this.host("", "jane");
        final HostPasswordStore store = mock(HostPasswordStore.class);
        when(store.findLoginPassword(host)).thenReturn("legacy-api-key");
        final AtomicInteger authorizations = new AtomicInteger();
        final BrickSession session = this.session(host, store, authorizations);
        try {
            this.open(session);
            session.login(LoginCallback.noop, CancelCallback.noop);
            session.login(LoginCallback.noop, CancelCallback.noop);
            assertEquals("legacy-api-key", host.getCredentials().getPassword());
            assertEquals("jane", host.getCredentials().getProperty(BrickOAuth2AuthorizationService.ACCOUNT));
            assertFalse(host.getCredentials().isOAuthAuthentication());
            assertFalse(host.getCredentials().isSaved());
            assertEquals(0, authorizations.get());
            verify(store).findLoginPassword(host);
        }
        finally {
            session.close();
        }
    }

    @Test
    public void testExistingKeyIgnoresOAuthExpiry() throws Exception {
        final Host host = this.host("manual-api-key", "os-user");
        host.getCredentials().setOauth(new OAuthTokens("unused", "unused", 1L));
        final HostPasswordStore store = mock(HostPasswordStore.class);
        final AtomicInteger authorizations = new AtomicInteger();
        final BrickSession session = this.session(host, store, authorizations);
        try {
            this.open(session);
            session.login(LoginCallback.noop, CancelCallback.noop);
            assertEquals("manual-api-key", host.getCredentials().getPassword());
            assertFalse(host.getCredentials().isOAuthAuthentication());
            assertNull(host.getCredentials().getProperty(BrickOAuth2AuthorizationService.ACCOUNT));
            assertEquals(0, authorizations.get());
            verifyNoInteractions(store);
        }
        finally {
            session.close();
        }
    }

    @Test
    public void testRecoveryUsesNewerSavedKey() throws Exception {
        final Host host = this.host("old-api-key", "jane");
        final HostPasswordStore store = mock(HostPasswordStore.class);
        when(store.findLoginPassword(host)).thenReturn("another-session-key");
        final AtomicInteger authorizations = new AtomicInteger();
        assertTrue(this.recovery(this.session(host, store, authorizations), store)
                .retryRequest(new ApiException(401, "Unauthorized"), 1, "old-api-key", 0));
        assertEquals("another-session-key", host.getCredentials().getPassword());
        assertEquals(0, authorizations.get());
        verify(store).findLoginPassword(host);
        verify(store, never()).save(any());
    }

    @Test
    public void testUnrelatedFailuresKeepKey() throws Exception {
        final Host host = this.host("old-api-key", "jane");
        final HostPasswordStore store = mock(HostPasswordStore.class);
        final AtomicInteger authorizations = new AtomicInteger();
        final BrickUnauthorizedRetryStrategy recovery = this.recovery(this.session(host, store, authorizations), store);
        for(int status : new int[]{0, 403, 500}) {
            assertFalse(recovery.retryRequest(new ApiException(status, "Failure"), 1, "old-api-key", 0));
            assertEquals("old-api-key", host.getCredentials().getPassword());
        }
        assertEquals(0, authorizations.get());
        verifyNoInteractions(store);
    }

    @Test
    public void testRecoveryFailureOnlyClearsKeyForDefinitiveAuthenticationFailure() throws Exception {
        for(BackgroundException failure : new BackgroundException[]{new ConnectionTimeoutException("Timed out"),
                new BackgroundException("Connection failed", "Service unavailable"), new LoginFailureException("Wrong account")}) {
            final Host host = this.host("old-api-key", "jane");
            host.getCredentials().setSaved(true);
            final HostPasswordStore store = mock(HostPasswordStore.class);
            final BrickSession session = new BrickSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager(), store) {
                @Override
                public Credentials authorize(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
                    throw failure;
                }
            };
            assertSame(failure, assertThrows(BackgroundException.class, () -> this.recovery(session, store)
                    .retryRequest(new ApiException(401, "Unauthorized"), 1, "old-api-key", 0)));
            final boolean definitive = failure instanceof LoginFailureException;
            assertEquals(definitive ? "" : "old-api-key", host.getCredentials().getPassword());
            assertEquals(!definitive, host.getCredentials().isSaved());
            verify(store, never()).save(any());
        }
    }

    @Test
    public void testCancellationClearsRejectedKeyWithoutRestoringOrSavingIt() throws Exception {
        final Host host = this.host("", "jane");
        final HostPasswordStore store = mock(HostPasswordStore.class);
        when(store.findLoginPassword(host)).thenReturn("old-api-key");
        final AtomicReference<BackgroundException> failure = new AtomicReference<>();
        final BrickSession session = new BrickSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager(), store) {
            @Override
            public Credentials authorize(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
                if(failure.get() != null) {
                    throw failure.get();
                }
                return host.getCredentials().setPassword("new-api-key").setSaved(false);
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == AttributesFinder.class) {
                    return (T) (AttributesFinder) (file, listener) -> PathAttributes.EMPTY;
                }
                return super._getFeature(type);
            }
        };
        try {
            this.open(session);
            session.login(LoginCallback.noop, CancelCallback.noop);
            final LoginCanceledException cancelled = new LoginCanceledException();
            failure.set(cancelled);
            assertSame(cancelled, assertThrows(LoginCanceledException.class, () -> this.recovery(session, store)
                    .retryRequest(new ApiException(401, "Unauthorized"), 1, "old-api-key", 0)));
            assertEquals("", host.getCredentials().getPassword());
            assertFalse(host.getCredentials().isSaved());
            failure.set(null);
            session.login(LoginCallback.noop, CancelCallback.noop);
            assertEquals("new-api-key", host.getCredentials().getPassword());
            verify(store, times(2)).findLoginPassword(host);
            verify(store, never()).save(any());
            verify(store, never()).deleteLoginPassword(any());
        }
        finally {
            session.close();
        }
    }

    @Test
    public void testConcurrentRecoverySharesFailuresEvenWithoutAKey() throws Exception {
        for(BackgroundException failure : new BackgroundException[]{new LoginCanceledException(), new LoginFailureException("Wrong account")}) {
            final Host host = this.host("old-api-key", "jane");
            final HostPasswordStore store = mock(HostPasswordStore.class);
            when(store.findLoginPassword(host)).thenReturn("old-api-key");
            final AtomicInteger authorizations = new AtomicInteger();
            final BrickSession session = new BrickSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager(), store) {
                @Override
                public Credentials authorize(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
                    if(authorizations.incrementAndGet() <= 2) {
                        throw failure;
                    }
                    return host.getCredentials().setPassword("new-api-key").setSaved(false);
                }
            };
            final BrickUnauthorizedRetryStrategy recovery = this.recovery(session, store);
            final ExecutorService workers = Executors.newFixedThreadPool(3);
            try {
                for(int round = 0; round < 2; round++) {
                    final CountDownLatch sent = new CountDownLatch(3);
                    final List<Future<BackgroundException>> results = new ArrayList<>();
                    for(int i = 0; i < 3; i++) {
                        results.add(workers.submit(() -> {
                            final long count = recovery.getRecoveryCount();
                            final String key = host.getCredentials().getPassword();
                            sent.countDown();
                            assertTrue(sent.await(5, TimeUnit.SECONDS));
                            return assertThrows(BackgroundException.class, () -> recovery.retryRequest(new ApiException(401, "Unauthorized"), 1, key, count));
                        }));
                    }
                    for(Future<BackgroundException> result : results) {
                        assertSame(failure, result.get(5, TimeUnit.SECONDS));
                    }
                    assertEquals(round + 1, authorizations.get());
                    assertEquals(round + 1, recovery.getRecoveryCount());
                    assertEquals("", host.getCredentials().getPassword());
                }
                final long count = recovery.getRecoveryCount();
                assertTrue(recovery.retryRequest(new ApiException(401, "Unauthorized"), 1, "", count));
                assertTrue(recovery.retryRequest(new ApiException(401, "Unauthorized"), 1, "", count));
                assertEquals(3, authorizations.get());
                assertEquals(3, recovery.getRecoveryCount());
                assertEquals("new-api-key", host.getCredentials().getPassword());
                verify(store, never()).save(any());
            }
            finally {
                workers.shutdownNow();
            }
        }
    }

    @Test
    public void testWaitingRequestCanCancelBeforeSignInCompletes() throws Exception {
        final Host host = this.host("old-api-key", "jane");
        final HostPasswordStore store = mock(HostPasswordStore.class);
        final BrickSession session = mock(BrickSession.class);
        when(session.getHost()).thenReturn(host);
        final CountDownLatch authorizing = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        when(session.authorize(any(), any())).thenAnswer(invocation -> {
            authorizing.countDown();
            assertTrue(release.await(5, TimeUnit.SECONDS));
            return host.getCredentials().setPassword("new-api-key");
        });
        final BrickUnauthorizedRetryStrategy recovery = this.recovery(session, store);
        final CountDownLatch waiting = new CountDownLatch(1);
        final AbstractBackgroundAction<ConnectionCanceledException> action = new AbstractBackgroundAction<ConnectionCanceledException>() {
            @Override
            public ConnectionCanceledException run() {
                waiting.countDown();
                return assertThrows(ConnectionCanceledException.class, () -> recovery.retryRequest(new ApiException(401, "Unauthorized"), 1, "old-api-key", 0));
            }
        };
        final BackgroundActionRegistry registry = BackgroundActionRegistry.global();
        registry.add(action);
        final ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            final Future<Boolean> first = workers.submit(() -> recovery.retryRequest(new ApiException(401, "Unauthorized"), 1, "old-api-key", 0));
            assertTrue(authorizing.await(5, TimeUnit.SECONDS));
            final Future<ConnectionCanceledException> second = workers.submit(() -> {
                action.prepare();
                return action.call();
            });
            assertTrue(waiting.await(5, TimeUnit.SECONDS));
            action.cancel();
            assertNotNull(second.get(2, TimeUnit.SECONDS));
            assertEquals(1, release.getCount());
            action.finish();
            release.countDown();
            assertTrue(first.get(5, TimeUnit.SECONDS));
            assertEquals("new-api-key", host.getCredentials().getPassword());
            assertEquals(1, recovery.getRecoveryCount());
            verify(session).authorize(any(), any());
        }
        finally {
            action.finish();
            release.countDown();
            workers.shutdownNow();
        }
    }

    @Test
    public void testCallerAuthorizationHeaderIsPreserved() throws Exception {
        final Host host = this.host("api-key", "jane");
        final HostPasswordStore store = mock(HostPasswordStore.class);
        final BrickUnauthorizedRetryStrategy handler = this.recovery(this.session(host, store, new AtomicInteger()), store);
        final HttpCoreContext context = HttpCoreContext.create();
        context.setTargetHost(new HttpHost("storage.example.com", 443, "https"));
        final BasicHttpRequest request = new BasicHttpRequest("GET", "/download");
        request.addHeader("Authorization", "caller-credential");
        handler.process(request, context);
        assertEquals("caller-credential", request.getFirstHeader("Authorization").getValue());
        assertNull(request.getFirstHeader("X-FilesAPI-Key"));
    }

    @Test
    public void testHeaderIsolationIncludesRedirectsAndSameHostTransferPaths() throws Exception {
        final Host host = this.host("api-key", "jane");
        final BrickUnauthorizedRetryStrategy handler = this.recovery(this.session(host, mock(HostPasswordStore.class), new AtomicInteger()),
                mock(HostPasswordStore.class));
        final HttpCoreContext context = HttpCoreContext.create();
        for(String destination : new String[]{"https://customer.files.com/api/rest/v1/folders", "https://storage.example.com/api/rest/v1/folders",
                "https://customer.files.com/download", "http://customer.files.com/api/rest/v1/folders"}) {
            final java.net.URI uri = java.net.URI.create(destination);
            context.setTargetHost(new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()));
            final BasicHttpRequest request = new BasicHttpRequest("GET", uri.getRawPath());
            request.addHeader("X-FilesAPI-Key", "copied-key");
            handler.process(request, context);
            assertNull(request.getFirstHeader("Authorization"));
            if(destination.equals("https://customer.files.com/api/rest/v1/folders")) {
                assertEquals("api-key", request.getFirstHeader("X-FilesAPI-Key").getValue());
            }
            else {
                assertNull(request.getFirstHeader("X-FilesAPI-Key"));
            }
        }
    }

    @Test
    public void testUnauthorizedPostAndPatchRebuildBody() throws Exception {
        for(String method : new String[]{"POST", "PATCH"}) {
            this.requestWithBody(method, false);
        }
    }

    @Test
    public void testPermanentUnauthorizedHasOneRecoveryAndClearsReplacement() throws Exception {
        this.requestWithBody("POST", true);
    }

    @Test
    public void testRejectedReplacementIsNotRestoredForNextUserRequest() throws Exception {
        final Host host = this.host("rejected-replacement", "jane");
        final HostPasswordStore store = mock(HostPasswordStore.class);
        when(store.findLoginPassword(host)).thenReturn("rejected-replacement");
        final AtomicInteger authorizations = new AtomicInteger();
        final BrickUnauthorizedRetryStrategy recovery = this.recovery(this.session(host, store, authorizations), store);
        assertFalse(recovery.retryRequest(new ApiException(401, "Unauthorized"), 2, "rejected-replacement", 0));
        assertEquals("", host.getCredentials().getPassword());
        assertTrue(recovery.retryRequest(new ApiException(401, "Unauthorized"), 1, "", recovery.getRecoveryCount()));
        assertEquals(1, authorizations.get());
        assertEquals("new-api-key", host.getCredentials().getPassword());
    }

    @Test
    public void testLaterConnectionsReuseStoredRecovery() throws Exception {
        final HostPasswordStore store = mock(HostPasswordStore.class);
        when(store.findLoginPassword(any())).thenReturn("old-api-key", "new-api-key", "new-api-key");
        final AtomicInteger authorizations = new AtomicInteger();
        final Host first = this.host("old-api-key", "jane");
        assertTrue(this.recovery(this.session(first, store, authorizations), store)
                .retryRequest(new ApiException(401, "Unauthorized"), 1, "old-api-key", 0));
        final ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            final List<Future<Boolean>> results = new ArrayList<>();
            for(int i = 0; i < 2; i++) {
                final Host next = this.host("old-api-key", "jane");
                final BrickUnauthorizedRetryStrategy recovery = this.recovery(this.session(next, store, authorizations), store);
                results.add(workers.submit(() -> recovery.retryRequest(new ApiException(401, "Unauthorized"), 1, "old-api-key", 0)));
            }
            for(Future<Boolean> result : results) {
                assertTrue(result.get(5, TimeUnit.SECONDS));
            }
            assertEquals(1, authorizations.get());
        }
        finally {
            workers.shutdownNow();
        }
    }

    @Test
    public void testSimultaneousRecoveryRemainsPerSessionLikePairing() throws Exception {
        final HostPasswordStore store = mock(HostPasswordStore.class);
        when(store.findLoginPassword(any())).thenReturn("old-api-key");
        final CountDownLatch authorizing = new CountDownLatch(2);
        final AtomicInteger authorizations = new AtomicInteger();
        final ExecutorService workers = Executors.newFixedThreadPool(2);
        try {
            final List<Future<Boolean>> results = new ArrayList<>();
            for(int i = 0; i < 2; i++) {
                final Host host = this.host("old-api-key", "jane");
                final BrickSession session = new BrickSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager(), store) {
                    @Override
                    public Credentials authorize(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
                        authorizations.incrementAndGet();
                        authorizing.countDown();
                        try {
                            assertTrue(authorizing.await(5, TimeUnit.SECONDS));
                        }
                        catch(InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new LoginCanceledException(e);
                        }
                        return host.getCredentials().setPassword("new-api-key").setSaved(false);
                    }
                };
                results.add(workers.submit(() -> this.recovery(session, store)
                        .retryRequest(new ApiException(401, "Unauthorized"), 1, "old-api-key", 0)));
            }
            for(Future<Boolean> result : results) {
                assertTrue(result.get(10, TimeUnit.SECONDS));
            }
            assertEquals(2, authorizations.get());
        }
        finally {
            workers.shutdownNow();
        }
    }

    @Test
    public void testRedirectedStorageUnauthorizedDoesNotRecover() throws Exception {
        final HttpServer api = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        final HttpServer storage = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        final AtomicInteger storageRequests = new AtomicInteger();
        final AtomicReference<String> leakedKey = new AtomicReference<>();
        final AtomicReference<String> leakedBearer = new AtomicReference<>();
        storage.createContext("/download", exchange -> {
            storageRequests.incrementAndGet();
            leakedKey.set(exchange.getRequestHeaders().getFirst("X-FilesAPI-Key"));
            leakedBearer.set(exchange.getRequestHeaders().getFirst("Authorization"));
            exchange.sendResponseHeaders(401, -1);
            exchange.close();
        });
        api.createContext("/api/rest/v1/folders", exchange -> {
            exchange.getResponseHeaders().set("Location", "http://127.0.0.1:" + storage.getAddress().getPort() + "/download");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        api.createContext("/transfer", exchange -> {
            exchange.getResponseHeaders().set("Location", "http://127.0.0.1:" + storage.getAddress().getPort() + "/download");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        api.start();
        storage.start();
        final Host host = new Host(new BrickProtocol() {
            @Override
            public Scheme getScheme() {
                return Scheme.http;
            }
        }, "127.0.0.1", api.getAddress().getPort(), new Credentials("jane", "api-key"));
        final HostPasswordStore store = mock(HostPasswordStore.class);
        final AtomicInteger authorizations = new AtomicInteger();
        final BrickUnauthorizedRetryStrategy handler = this.recovery(this.session(host, store, authorizations), store);
        try(CloseableHttpClient http = HttpClients.custom().disableAutomaticRetries()
                .addInterceptorLast((HttpRequestInterceptor) handler).addInterceptorLast((HttpResponseInterceptor) handler).build()) {
            final BrickApiClient client = new BrickApiClient(host, http, handler);
            try {
                assertThrows(ApiException.class, () -> this.invoke(client, "GET"));
                assertEquals(1, storageRequests.get());
                assertNull(leakedKey.get());
                assertNull(leakedBearer.get());
                assertEquals(0, authorizations.get());
                assertEquals("api-key", host.getCredentials().getPassword());
                verifyNoInteractions(store);
                try(CloseableHttpResponse response = http.execute(new HttpGet("http://127.0.0.1:" + storage.getAddress().getPort() + "/download"))) {
                    assertEquals(401, response.getStatusLine().getStatusCode());
                }
                try(CloseableHttpResponse response = http.execute(new HttpGet("http://127.0.0.1:" + api.getAddress().getPort() + "/transfer"))) {
                    assertEquals(401, response.getStatusLine().getStatusCode());
                }
                assertEquals(3, storageRequests.get());
                assertEquals(0, authorizations.get());
            }
            finally {
                client.getHttpClient().close();
            }
        }
        finally {
            api.stop(0);
            storage.stop(0);
        }
    }

    private void requestWithBody(final String method, final boolean permanentlyUnauthorized) throws Exception {
        final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        final List<String> bodies = new ArrayList<>();
        final List<String> keys = new ArrayList<>();
        server.createContext("/api/rest/v1/folders", exchange -> {
            final ByteArrayOutputStream body = new ByteArrayOutputStream();
            final byte[] buffer = new byte[1024];
            for(int read; (read = exchange.getRequestBody().read(buffer)) != -1;) {
                body.write(buffer, 0, read);
            }
            bodies.add(body.toString(StandardCharsets.UTF_8.name()));
            keys.add(exchange.getRequestHeaders().getFirst("X-FilesAPI-Key"));
            final int status = bodies.size() == 1 || permanentlyUnauthorized ? 401 : 200;
            final byte[] response = (status == 200 ? "{}" : "{\"error\":\"Unauthorized\"}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
        final Host host = new Host(new BrickProtocol() {
            @Override
            public Scheme getScheme() {
                return Scheme.http;
            }
        }, "127.0.0.1", server.getAddress().getPort(), new Credentials("jane", "old-api-key"));
        final HostPasswordStore store = mock(HostPasswordStore.class);
        final AtomicInteger authorizations = new AtomicInteger();
        final BrickSession session = this.session(host, store, authorizations);
        final BrickUnauthorizedRetryStrategy handler = this.recovery(session, store);
        try(CloseableHttpClient http = HttpClients.custom().disableAutomaticRetries()
                .addInterceptorLast((HttpRequestInterceptor) handler).addInterceptorLast((HttpResponseInterceptor) handler).build()) {
            final BrickApiClient client = new BrickApiClient(host, http, handler);
            try {
                if(permanentlyUnauthorized) {
                    assertEquals(401, assertThrows(ApiException.class, () -> this.invoke(client, method)).getCode());
                    assertEquals("", host.getCredentials().getPassword());
                    assertFalse(host.getCredentials().isSaved());
                }
                else {
                    this.invoke(client, method);
                    assertEquals("new-api-key", host.getCredentials().getPassword());
                }
                assertEquals(2, bodies.size());
                assertTrue(bodies.get(0).contains("a path"));
                assertEquals(bodies.get(0), bodies.get(1));
                assertEquals(java.util.Arrays.asList("old-api-key", "new-api-key"), keys);
                assertEquals(1, authorizations.get());
            }
            finally {
                client.getHttpClient().close();
            }
        }
        finally {
            server.stop(0);
        }
    }

    private void invoke(final BrickApiClient client, final String method) throws ApiException {
        client.invokeAPI("/folders", method, Collections.emptyList(), Collections.singletonMap("path", "a path"),
                Collections.emptyMap(), Collections.emptyMap(), "application/json", "application/json", new String[0], new GenericType<String>() { });
    }

    private Host host(final String password, final String username) {
        final Host host = new Host(new BrickProtocol(), "customer.files.com", new Credentials(username, password));
        return host;
    }

    private void open(final BrickSession session) throws Exception {
        session.open(target -> Proxy.DIRECT, HostKeyCallback.noop, LoginCallback.noop, CancelCallback.noop);
    }

    private BrickUnauthorizedRetryStrategy recovery(final BrickSession session, final HostPasswordStore store) {
        return new BrickUnauthorizedRetryStrategy(session, store, LoginCallback.noop, CancelCallback.noop);
    }

    private BrickSession session(final Host host, final HostPasswordStore store, final AtomicInteger authorizations) {
        return new BrickSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager(), store) {
            @Override
            public Credentials authorize(final LoginCallback prompt, final CancelCallback cancel) {
                authorizations.incrementAndGet();
                return host.getCredentials().setPassword("new-api-key").setSaved(false);
            }

            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == AttributesFinder.class) {
                    return (T) (AttributesFinder) (file, listener) -> PathAttributes.EMPTY;
                }
                return super._getFeature(type);
            }
        };
    }
}
