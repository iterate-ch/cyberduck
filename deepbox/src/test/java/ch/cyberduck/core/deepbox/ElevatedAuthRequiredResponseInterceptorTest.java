package ch.cyberduck.core.deepbox;

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
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.oauth.OAuth2RequestInterceptor;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class ElevatedAuthRequiredResponseInterceptorTest {

    private static final String ELEVATED_AUTH_REQUIRED = "{\"timestamp\":\"2026-09-25T16:43:41.605Z\",\"status\":403,\"error\":\"Forbidden\"," +
            "\"message\":\"Access denied. Elevated auth required.\",\"messageId\":\"elevated.auth.required\"}";

    @Test
    public void testRetryElevatedAuthRequired() throws Exception {
        final OAuthTokens tokens = new OAuthTokens("a", "r", Long.MAX_VALUE);
        final AtomicInteger authorize = new AtomicInteger();
        final AtomicReference<OAuthTokens> saved = new AtomicReference<>();
        final ElevatedAuthRequiredResponseInterceptor interceptor = new ElevatedAuthRequiredResponseInterceptor(new Host(new TestProtocol()),
                new MockOAuth2RequestInterceptor(authorize, saved, tokens), CancelCallback.noop);
        assertTrue(interceptor.retryRequest(response(HttpStatus.SC_FORBIDDEN, ELEVATED_AUTH_REQUIRED), 1, new BasicHttpContext()));
        assertEquals(1, authorize.get());
        assertSame(tokens, saved.get());
    }

    @Test
    public void testNoRetryOtherForbidden() throws Exception {
        final AtomicInteger authorize = new AtomicInteger();
        final AtomicReference<OAuthTokens> saved = new AtomicReference<>();
        final ElevatedAuthRequiredResponseInterceptor interceptor = new ElevatedAuthRequiredResponseInterceptor(new Host(new TestProtocol()),
                new MockOAuth2RequestInterceptor(authorize, saved, OAuthTokens.EMPTY), CancelCallback.noop);
        final String content = "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"Access denied.\",\"messageId\":\"access.denied\"}";
        final HttpResponse response = response(HttpStatus.SC_FORBIDDEN, content);
        assertFalse(interceptor.retryRequest(response, 1, new BasicHttpContext()));
        assertEquals(0, authorize.get());
        assertNull(saved.get());
        // Entity must still be readable for error mapping
        assertEquals(content, EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testNoRetryForbiddenInvalidJson() throws Exception {
        final AtomicInteger authorize = new AtomicInteger();
        final ElevatedAuthRequiredResponseInterceptor interceptor = new ElevatedAuthRequiredResponseInterceptor(new Host(new TestProtocol()),
                new MockOAuth2RequestInterceptor(authorize, new AtomicReference<>(), OAuthTokens.EMPTY), CancelCallback.noop);
        final HttpResponse response = response(HttpStatus.SC_FORBIDDEN, "<html>Forbidden</html>");
        assertFalse(interceptor.retryRequest(response, 1, new BasicHttpContext()));
        assertEquals(0, authorize.get());
        assertEquals("<html>Forbidden</html>", EntityUtils.toString(response.getEntity()));
    }

    @Test
    public void testNoRetryForbiddenNoEntity() throws Exception {
        final AtomicInteger authorize = new AtomicInteger();
        final ElevatedAuthRequiredResponseInterceptor interceptor = new ElevatedAuthRequiredResponseInterceptor(new Host(new TestProtocol()),
                new MockOAuth2RequestInterceptor(authorize, new AtomicReference<>(), OAuthTokens.EMPTY), CancelCallback.noop);
        assertFalse(interceptor.retryRequest(new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_FORBIDDEN, "Forbidden"), 1, new BasicHttpContext()));
        assertEquals(0, authorize.get());
    }

    @Test
    public void testNoRetryOtherStatus() throws Exception {
        final AtomicInteger authorize = new AtomicInteger();
        final ElevatedAuthRequiredResponseInterceptor interceptor = new ElevatedAuthRequiredResponseInterceptor(new Host(new TestProtocol()),
                new MockOAuth2RequestInterceptor(authorize, new AtomicReference<>(), OAuthTokens.EMPTY), CancelCallback.noop);
        assertFalse(interceptor.retryRequest(response(HttpStatus.SC_NOT_FOUND, ELEVATED_AUTH_REQUIRED), 1, new BasicHttpContext()));
        assertEquals(0, authorize.get());
    }

    @Test
    public void testNoRetryLoginCanceled() throws Exception {
        final AtomicReference<OAuthTokens> saved = new AtomicReference<>();
        final ElevatedAuthRequiredResponseInterceptor interceptor = new ElevatedAuthRequiredResponseInterceptor(new Host(new TestProtocol()),
                new MockOAuth2RequestInterceptor(new AtomicInteger(), saved, null), CancelCallback.noop);
        final HttpResponse response = response(HttpStatus.SC_FORBIDDEN, ELEVATED_AUTH_REQUIRED);
        assertFalse(interceptor.retryRequest(response, 1, new BasicHttpContext()));
        assertNull(saved.get());
        assertEquals(ELEVATED_AUTH_REQUIRED, EntityUtils.toString(response.getEntity()));
    }

    /**
     * @return Response with non-repeatable entity
     */
    private static HttpResponse response(final int status, final String content) {
        final BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, status, null);
        response.setEntity(new InputStreamEntity(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), ContentType.APPLICATION_JSON));
        return response;
    }

    private static final class MockOAuth2RequestInterceptor extends OAuth2RequestInterceptor {
        private final AtomicInteger authorize;
        private final AtomicReference<OAuthTokens> saved;
        private final OAuthTokens tokens;

        /**
         * @param tokens Tokens returned from authorization flow or null to cancel login
         */
        public MockOAuth2RequestInterceptor(final AtomicInteger authorize, final AtomicReference<OAuthTokens> saved, final OAuthTokens tokens) throws LoginCanceledException {
            super(HttpClientBuilder.create().build(), new Host(new TestProtocol()), "https://localhost/token", "https://localhost/auth",
                    "clientid", "clientsecret", Collections.emptyList(), false, new DisabledLoginCallback(), CancelCallback.noop);
            this.authorize = authorize;
            this.saved = saved;
            this.tokens = tokens;
        }

        @Override
        public OAuthTokens authorize(final CancelCallback cancel) throws BackgroundException {
            authorize.incrementAndGet();
            if(null == tokens) {
                throw new LoginCanceledException();
            }
            return tokens;
        }

        @Override
        public OAuthTokens save(final OAuthTokens tokens) {
            saved.set(tokens);
            return tokens;
        }
    }
}
