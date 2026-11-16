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
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.http.CustomServiceUnavailableRetryStrategy;
import ch.cyberduck.core.oauth.OAuth2AuthorizationCodeProvider;
import ch.cyberduck.core.oauth.OAuth2AuthorizationCodeProviderFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.junit.Test;
import org.mockito.MockedStatic;

import javax.ws.rs.core.GenericType;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.google.api.client.testing.http.MockHttpTransport;
import com.sun.net.httpserver.HttpServer;

import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.*;

public class BrickSessionTest {
    @Test
    public void testRequestTimeoutRemainsMappedToNetworkFailure() throws Exception {
        final SocketTimeoutException timeout = new SocketTimeoutException("Read timed out");
        try(CloseableHttpClient http = mock(CloseableHttpClient.class)) {
            when(http.execute(any(HttpHost.class), any(HttpRequest.class), any(HttpContext.class))).thenThrow(timeout);
            final BrickApiClient client = new BrickApiClient(this.host(), http);
            try {
                final ApiException failure = assertThrows(ApiException.class, () -> this.invoke(client, "GET"));
                assertTrue(new BrickExceptionMappingService().map(failure) instanceof ConnectionTimeoutException);
            }
            finally {
                client.getHttpClient().close();
            }
        }
    }

    @Test
    public void testSavedAndExplicitKeysDoNotRequireLocalHostnameResolution() throws Exception {
        for(boolean password : new boolean[]{false, true}) {
            final Host host = this.host();
            if(password) {
                host.getCredentials().setPassword("manual-key").setOauth(OAuthTokens.EMPTY);
            }
            final BrickSession session = new BrickSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager()) {
                @Override
                @SuppressWarnings("unchecked")
                public <T> T _getFeature(final Class<T> type) {
                    if(type == AttributesFinder.class) {
                        return (T) (AttributesFinder) (file, listener) -> PathAttributes.EMPTY;
                    }
                    return super._getFeature(type);
                }
            };
            try(MockedStatic<InetAddress> address = mockStatic(InetAddress.class, withSettings().mockMaker("mock-maker-inline"));
                MockedStatic<PasswordStoreFactory> passwords = mockStatic(PasswordStoreFactory.class, withSettings().mockMaker("mock-maker-inline"))) {
                address.when(InetAddress::getLocalHost).thenThrow(new UnknownHostException("Unresolvable local hostname"));
                passwords.when(PasswordStoreFactory::get).thenReturn(mock(HostPasswordStore.class));
                session.open(target -> Proxy.DIRECT, HostKeyCallback.noop, LoginCallback.noop, CancelCallback.noop);
                session.login(LoginCallback.noop, CancelCallback.noop);
                address.verify(InetAddress::getLocalHost, never());
            }
            finally {
                session.close();
            }
        }
    }

    @Test
    public void testRejectedExplicitKeyDoesNotStartAuthorization() throws Exception {
        final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        final List<String> keys = new ArrayList<>();
        server.createContext("/api/rest/v1/files", exchange -> {
            keys.add(exchange.getRequestHeaders().getFirst("X-FilesAPI-Key"));
            exchange.sendResponseHeaders(401, -1);
            exchange.close();
        });
        server.start();
        final Host host = this.httpHost(server);
        host.getCredentials().setPassword("manual-key").setOauth(OAuthTokens.EMPTY);
        final OAuth2AuthorizationCodeProvider provider = mock(OAuth2AuthorizationCodeProvider.class);
        final BrickSession session = new BrickSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
        try(MockedStatic<PasswordStoreFactory> passwords = mockStatic(PasswordStoreFactory.class, withSettings().mockMaker("mock-maker-inline"));
            MockedStatic<OAuth2AuthorizationCodeProviderFactory> codes = mockStatic(OAuth2AuthorizationCodeProviderFactory.class, withSettings().mockMaker("mock-maker-inline"))) {
            passwords.when(PasswordStoreFactory::get).thenReturn(mock(HostPasswordStore.class));
            codes.when(OAuth2AuthorizationCodeProviderFactory::get).thenReturn(provider);
            session.open(target -> Proxy.DIRECT, HostKeyCallback.noop, LoginCallback.noop, CancelCallback.noop);
            final AccessDeniedException failure = assertThrows(AccessDeniedException.class, () -> session.login(LoginCallback.noop, CancelCallback.noop));
            assertTrue(failure.getCause() instanceof LoginFailureException);
            assertEquals(failure.getCause().getMessage(), failure.getMessage());
            assertEquals(((LoginFailureException) failure.getCause()).getDetail(), failure.getDetail());
            assertEquals(Collections.singletonList("manual-key"), keys);
            verifyNoInteractions(provider);
        }
        finally {
            session.close();
            server.stop(0);
        }
    }

    @Test
    public void testHeaderIsolationAndExplicitPasswordPrecedence() throws Exception {
        final Host host = this.host();
        host.getCredentials().setOauth(new OAuthTokens("oauth-key", null, Long.MAX_VALUE));
        final BrickOAuth2AuthorizationService service = this.service(host);
        final HttpCoreContext rest = this.request(service, host, "/api/rest/v1/files");
        assertEquals("oauth-key", rest.getRequest().getFirstHeader("X-FilesAPI-Key").getValue());
        host.getCredentials().setPassword("manual-key").setOauth(OAuthTokens.EMPTY);
        final HttpCoreContext manual = this.request(service, host, "/api/rest/v1/files");
        assertEquals("manual-key", manual.getRequest().getFirstHeader("X-FilesAPI-Key").getValue());
        manual.getRequest().addHeader("Authorization", "caller-authorization");
        service.process(manual.getRequest(), manual);
        assertEquals("caller-authorization", manual.getRequest().getFirstHeader("Authorization").getValue());
        for(HttpHost target : Arrays.asList(new HttpHost("storage.example.com", 443, "https"),
                new HttpHost(host.getHostname(), 8443, "https"), new HttpHost(host.getHostname(), 80, "http"))) {
            manual.setTargetHost(target);
            service.process(manual.getRequest(), manual);
            assertNull(manual.getRequest().getFirstHeader("X-FilesAPI-Key"));
            assertEquals(Boolean.FALSE, manual.getAttribute(BrickOAuth2AuthorizationService.REST_ORIGIN));
        }
        final HttpCoreContext transfer = this.request(service, host, "/download");
        assertNull(transfer.getRequest().getFirstHeader("X-FilesAPI-Key"));
        verify(service, never()).authorize(any());
    }

    @Test
    public void testOnlyRestOAuthUnauthorizedResponsesRecover() throws Exception {
        final Host host = this.host();
        final BrickOAuth2AuthorizationService service = this.service(host);
        final BrickUnauthorizedRetryStrategy recovery = new BrickUnauthorizedRetryStrategy(host, service, CancelCallback.noop);
        final HttpCoreContext context = this.request(service, host, "/download");
        assertFalse(recovery.retryRequest(this.response(401), 1, context));
        final HttpCoreContext rest = this.request(service, host, "/api/rest/v1/files");
        for(int status : new int[]{403, 404, 500}) {
            assertFalse(recovery.retryRequest(this.response(status), 1, rest));
        }
        host.getCredentials().setPassword("manual-key");
        assertFalse(recovery.retryRequest(this.response(401), 1, rest));
        verify(service, never()).authorize(any());
    }

    @Test
    public void testLateUnauthorizedRetriesWithReplacementKey() throws Exception {
        final Host host = this.host();
        final BrickOAuth2AuthorizationService service = this.service(host);
        final HttpCoreContext context = this.request(service, host, "/api/rest/v1/files");
        host.getCredentials().setOauth(new OAuthTokens("replacement-key", null, Long.MAX_VALUE));
        host.setHostname("canonical.files.com");
        assertTrue(new BrickUnauthorizedRetryStrategy(host, service, CancelCallback.noop).retryRequest(this.response(401), 1, context));
        verify(service, never()).authorize(any());
    }

    @Test
    public void testConcurrentRecoveryDoesNotBlockTransfersOrRepeatAuthorization() throws Exception {
        final Host host = this.host();
        final BrickOAuth2AuthorizationService service = this.service(host);
        final CountDownLatch authorizing = new CountDownLatch(1);
        final CountDownLatch complete = new CountDownLatch(1);
        doAnswer(invocation -> {
            authorizing.countDown();
            assertTrue(complete.await(5, TimeUnit.SECONDS));
            return new OAuthTokens("replacement-key", null, Long.MAX_VALUE);
        }).when(service).authorize(any());
        final BrickUnauthorizedRetryStrategy recovery = new BrickUnauthorizedRetryStrategy(host, service, CancelCallback.noop);
        final HttpCoreContext first = this.request(service, host, "/api/rest/v1/files");
        final HttpCoreContext second = this.request(service, host, "/api/rest/v1/files");
        final ExecutorService workers = Executors.newFixedThreadPool(3);
        try {
            final Future<Boolean> recovered = workers.submit(() -> recovery.retryRequest(this.response(401), 1, first));
            assertTrue(authorizing.await(5, TimeUnit.SECONDS));
            assertFalse(recovery.retryRequest(this.response(401), 1, second));
            final Future<HttpCoreContext> transfer = workers.submit(() -> this.request(service, host, "/download"));
            assertNull(transfer.get(2, TimeUnit.SECONDS).getRequest().getFirstHeader("X-FilesAPI-Key"));
            complete.countDown();
            assertTrue(recovered.get(5, TimeUnit.SECONDS));
            assertTrue(recovery.retryRequest(this.response(401), 1, second));
            verify(service).authorize(any());
        }
        finally {
            complete.countDown();
            workers.shutdownNow();
        }
    }

    @Test
    public void testCancelledRequestDoesNotStartRecovery() throws Exception {
        final Host host = this.host();
        final BrickOAuth2AuthorizationService service = this.service(host);
        final CancelCallback cancel = () -> {
            throw new ConnectionCanceledException();
        };
        assertFalse(new BrickUnauthorizedRetryStrategy(host, service, cancel)
                .retryRequest(this.response(401), 1, this.request(service, host, "/api/rest/v1/files")));
        verify(service, never()).authorize(any());
    }

    @Test
    public void testCancelledRecoveryDoesNotPoisonLaterRecovery() throws Exception {
        final Host host = this.host();
        final BrickOAuth2AuthorizationService service = this.service(host);
        doThrow(new LoginCanceledException()).doReturn(new OAuthTokens("replacement-key", null, Long.MAX_VALUE)).when(service).authorize(any());
        final BrickUnauthorizedRetryStrategy recovery = new BrickUnauthorizedRetryStrategy(host, service, CancelCallback.noop);
        final HttpCoreContext context = this.request(service, host, "/api/rest/v1/files");
        assertFalse(recovery.retryRequest(this.response(401), 1, context));
        assertTrue(recovery.retryRequest(this.response(401), 1, context));
        assertEquals("replacement-key", host.getCredentials().getOauth().getAccessToken());
    }

    @Test
    public void testGetRecoversButPostAndPatchNeedCallerRetry() throws Exception {
        for(String method : new String[]{"GET", "POST", "PATCH"}) {
            final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            final List<String> keys = new ArrayList<>();
            final List<String> bodies = new ArrayList<>();
            server.createContext("/api/rest/v1/folders", exchange -> {
                final ByteArrayOutputStream body = new ByteArrayOutputStream();
                final byte[] buffer = new byte[1024];
                for(int count; (count = exchange.getRequestBody().read(buffer)) != -1;) {
                    body.write(buffer, 0, count);
                }
                bodies.add(body.toString(StandardCharsets.UTF_8.name()));
                keys.add(exchange.getRequestHeaders().getFirst("X-FilesAPI-Key"));
                final byte[] response = "{}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(keys.size() == 1 ? 401 : 200, response.length);
                exchange.getResponseBody().write(response);
                exchange.close();
            });
            server.start();
            final Host host = this.httpHost(server);
            final BrickOAuth2AuthorizationService service = this.service(host);
            try(CloseableHttpClient http = this.http(host, service)) {
                final BrickApiClient client = new BrickApiClient(host, http);
                try {
                    if(!"GET".equals(method)) {
                        assertEquals(401, assertThrows(ApiException.class, () -> this.invoke(client, method)).getCode());
                        assertEquals(1, keys.size());
                    }
                    this.invoke(client, method);
                    assertEquals(Arrays.asList("old-key", "replacement-key"), keys);
                    if(!"GET".equals(method)) {
                        assertTrue(bodies.get(0).contains("a path"));
                        assertEquals(bodies.get(0), bodies.get(1));
                    }
                    verify(service).authorize(any());
                }
                finally {
                    client.getHttpClient().close();
                }
            }
            finally {
                server.stop(0);
            }
        }
    }

    @Test
    public void testPermanentUnauthorizedUsesConfiguredRetryLimit() throws Exception {
        final Host host = this.host();
        final BrickOAuth2AuthorizationService service = this.service(host);
        final CustomServiceUnavailableRetryStrategy strategy = new CustomServiceUnavailableRetryStrategy(host,
                new BrickUnauthorizedRetryStrategy(host, service, CancelCallback.noop));
        assertTrue(strategy.retryRequest(this.response(401), 1, this.request(service, host, "/api/rest/v1/files")));
        assertFalse(strategy.retryRequest(this.response(401), 2, this.request(service, host, "/api/rest/v1/files")));
        verify(service).authorize(any());
    }

    @Test
    public void testRedirectedStorageUnauthorizedDoesNotLeakKeyOrRecover() throws Exception {
        final HttpServer api = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        final HttpServer storage = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        final AtomicReference<String> leakedKey = new AtomicReference<>();
        storage.createContext("/download", exchange -> {
            leakedKey.set(exchange.getRequestHeaders().getFirst("X-FilesAPI-Key"));
            exchange.sendResponseHeaders(401, -1);
            exchange.close();
        });
        api.createContext("/api/rest/v1/folders", exchange -> {
            exchange.getResponseHeaders().set("Location", "http://127.0.0.1:" + storage.getAddress().getPort() + "/download");
            exchange.sendResponseHeaders(302, -1);
            exchange.close();
        });
        api.start();
        storage.start();
        final Host host = this.httpHost(api);
        final BrickOAuth2AuthorizationService service = this.service(host);
        try(CloseableHttpClient http = this.http(host, service);
            CloseableHttpResponse response = http.execute(new HttpGet("http://127.0.0.1:" + api.getAddress().getPort() + "/api/rest/v1/folders"))) {
            assertEquals(401, response.getStatusLine().getStatusCode());
            assertNull(leakedKey.get());
            verify(service, never()).authorize(any());
        }
        finally {
            api.stop(0);
            storage.stop(0);
        }
    }

    @Test
    public void testLoginUsesSessionsProxyForTokenExchange() throws Exception {
        final HttpServer proxy = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        final AtomicReference<String> request = new AtomicReference<>();
        proxy.createContext("/", exchange -> {
            request.set(exchange.getRequestURI().toString());
            final byte[] response = ("{\"access_token\":\"unused\",\"token_type\":\"Bearer\",\"files_api_key\":{"
                    + "\"password\":\"api-key\",\"user_username\":\"jane\",\"server\":\"https://customer.files.com\"}}")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        proxy.start();
        final Host host = this.httpHost(proxy);
        host.setHostname("unresolvable.invalid");
        host.getCredentials().setOauth(OAuthTokens.EMPTY);
        final OAuth2AuthorizationCodeProvider provider = mock(OAuth2AuthorizationCodeProvider.class);
        when(provider.prompt(any(), any(), anyString(), anyString(), anyString())).thenReturn("code");
        final BrickSession session = new BrickSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager()) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == AttributesFinder.class) {
                    return (T) (AttributesFinder) (file, listener) -> PathAttributes.EMPTY;
                }
                return super._getFeature(type);
            }
        };
        try(MockedStatic<PasswordStoreFactory> passwords = mockStatic(PasswordStoreFactory.class, withSettings().mockMaker("mock-maker-inline"));
            MockedStatic<OAuth2AuthorizationCodeProviderFactory> codes = mockStatic(OAuth2AuthorizationCodeProviderFactory.class, withSettings().mockMaker("mock-maker-inline"))) {
            passwords.when(PasswordStoreFactory::get).thenReturn(mock(HostPasswordStore.class));
            codes.when(OAuth2AuthorizationCodeProviderFactory::get).thenReturn(provider);
            session.open(target -> new Proxy(Proxy.Type.HTTP, "127.0.0.1", proxy.getAddress().getPort()), HostKeyCallback.noop, LoginCallback.noop, CancelCallback.noop);
            session.login(LoginCallback.noop, CancelCallback.noop);
            assertTrue(request.get().contains("unresolvable.invalid"));
            assertTrue(request.get().endsWith("/auth/oauth/token"));
            assertEquals("api-key", host.getCredentials().getOauth().getAccessToken());
        }
        finally {
            session.close();
            proxy.stop(0);
        }
    }

    private Host host() throws Exception {
        final Profile profile = new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new BrickProtocol())))
                .read(this.getClass().getResourceAsStream("/Files.com.cyberduckprofile"));
        final Host host = new Host(profile, "customer.files.com", new Credentials("jane"));
        host.getCredentials().setOauth(new OAuthTokens("old-key", null, Long.MAX_VALUE));
        host.setProperty("connection.retry", "1");
        host.setProperty("connection.retry.delay", "0");
        return host;
    }

    private Host httpHost(final HttpServer server) throws Exception {
        final Host host = this.host();
        final Profile protocol = mock(Profile.class, delegatesTo(host.getProtocol()));
        doReturn(Scheme.http).when(protocol).getScheme();
        host.setProtocol(protocol);
        host.setHostname("127.0.0.1");
        host.setPort(server.getAddress().getPort());
        return host;
    }

    private BrickOAuth2AuthorizationService service(final Host host) throws Exception {
        try(MockedStatic<PasswordStoreFactory> factory = mockStatic(PasswordStoreFactory.class, withSettings().mockMaker("mock-maker-inline"))) {
            factory.when(PasswordStoreFactory::get).thenReturn(mock(HostPasswordStore.class));
            final BrickOAuth2AuthorizationService service = spy(new BrickOAuth2AuthorizationService(new MockHttpTransport(), host,
                    LoginCallback.noop, CancelCallback.noop, mock(OAuth2AuthorizationCodeProvider.class)));
            doReturn(new OAuthTokens("replacement-key", null, Long.MAX_VALUE)).when(service).authorize(any());
            return service;
        }
    }

    private HttpCoreContext request(final BrickOAuth2AuthorizationService service, final Host host, final String path) {
        final HttpRequest request = new BasicHttpRequest("GET", path);
        request.addHeader("X-FilesAPI-Key", "caller-key");
        final HttpCoreContext context = HttpCoreContext.create();
        context.setTargetHost(new HttpHost(host.getHostname(), host.getPort(), host.getProtocol().getScheme().name()));
        context.setAttribute(HttpCoreContext.HTTP_REQUEST, request);
        service.process(request, context);
        return context;
    }

    private HttpResponse response(final int status) {
        return new BasicHttpResponse(HttpVersion.HTTP_1_1, status, "Response");
    }

    private CloseableHttpClient http(final Host host, final BrickOAuth2AuthorizationService service) {
        return HttpClients.custom().disableAutomaticRetries().addInterceptorLast(service)
                .setServiceUnavailableRetryStrategy(new CustomServiceUnavailableRetryStrategy(host,
                        new BrickUnauthorizedRetryStrategy(host, service, CancelCallback.noop))).build();
    }

    private void invoke(final BrickApiClient client, final String method) throws ApiException {
        client.invokeAPI("/folders", method, Collections.emptyList(), "GET".equals(method) ? null : Collections.singletonMap("path", "a path"),
                Collections.emptyMap(), Collections.emptyMap(), "application/json", "application/json", new String[0], new GenericType<String>() { });
    }
}
