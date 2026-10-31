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

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.bootstrap.HttpServer;
import org.apache.http.impl.bootstrap.ServerBootstrap;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

import java.net.InetAddress;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.api.client.http.apache.v2.ApacheHttpTransport;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;

import static org.junit.Assert.*;

public class BrickApiKeyRevocationServiceTest {
    @Test(timeout = 10000)
    public void testApacheTransportDoesNotFollowRedirects() throws Exception {
        final AtomicInteger redirectedRequests = new AtomicInteger();
        final AtomicInteger revocations = new AtomicInteger();
        final HttpServer destination = ServerBootstrap.bootstrap().setLocalAddress(InetAddress.getByName("127.0.0.1"))
                .registerHandler("*", (request, response, context) -> {
                    redirectedRequests.incrementAndGet();
                    response.setStatusCode(204);
                }).create();
        final HttpServer origin = ServerBootstrap.bootstrap().setLocalAddress(InetAddress.getByName("127.0.0.1"))
                .registerHandler("*", (request, response, context) -> {
                    revocations.incrementAndGet();
                    assertEquals("DELETE", request.getRequestLine().getMethod());
                    assertEquals("api-key", request.getFirstHeader("X-FilesAPI-Key").getValue());
                    response.setStatusCode(303);
                    response.setHeader("Location", String.format("http://127.0.0.1:%d/api_key", destination.getLocalPort()));
                }).create();
        try {
            destination.start();
            origin.start();
            try(CloseableHttpClient client = HttpClients.custom().setDefaultRequestConfig(RequestConfig.custom()
                    .setRedirectsEnabled(true).setConnectTimeout(3000).setSocketTimeout(3000).build()).build()) {
                final HttpResponseException failure = assertThrows(HttpResponseException.class, () ->
                        new BrickApiKeyRevocationService(new ApacheHttpTransport(client)).revoke(
                                URI.create(String.format("http://127.0.0.1:%d", origin.getLocalPort())), "api-key"));
                assertEquals(303, failure.getStatusCode());
                assertEquals(1, revocations.get());
                assertEquals(0, redirectedRequests.get());
            }
        }
        finally {
            origin.shutdown(0, TimeUnit.MILLISECONDS);
            destination.shutdown(0, TimeUnit.MILLISECONDS);
        }
    }

    @Test
    public void testDeletedAndAlreadyInvalidKeysSucceed() throws Exception {
        for(final int status : new int[]{200, 204, 401, 404}) {
            final MockHttpTransport transport = new MockHttpTransport.Builder().setLowLevelHttpResponse(
                    new MockLowLevelHttpResponse().setStatusCode(status)).build();
            new BrickApiKeyRevocationService(transport).revoke(URI.create("https://customer.files.com/auth/oauth/token"), "api-key");
            assertEquals("https://customer.files.com/api/rest/v1/api_key", transport.getLowLevelHttpRequest().getUrl());
            assertEquals("api-key", transport.getLowLevelHttpRequest().getFirstHeaderValue("X-FilesAPI-Key"));
        }
    }

    @Test
    public void testUnexpectedResponsesAndRedirectsFail() {
        for(final int status : new int[]{302, 403, 500}) {
            final MockHttpTransport transport = new MockHttpTransport.Builder().setLowLevelHttpResponse(
                    new MockLowLevelHttpResponse().setStatusCode(status).addHeader("Location", "https://other.files.com/api_key")).build();
            final HttpResponseException failure = assertThrows(HttpResponseException.class, () ->
                    new BrickApiKeyRevocationService(transport).revoke(URI.create("https://customer.files.com"), "api-key"));
            assertEquals(status, failure.getStatusCode());
        }
    }
}
