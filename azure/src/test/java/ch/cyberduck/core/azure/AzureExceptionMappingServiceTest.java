package ch.cyberduck.core.azure;

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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;

import org.junit.Test;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collections;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AzureExceptionMappingServiceTest {

    @Test
    public void testMapStructuredMessage() {
        final BackgroundException e = new AzureExceptionMappingService().map(
                new HttpResponseException("Status code 403, empty body", new StubResponse(403), Collections.singletonMap("Message", "Server failed to authenticate the request")));
        assertTrue(e instanceof LoginFailureException);
        assertTrue(e.getDetail(false), e.getDetail(false).contains("Server failed to authenticate the request"));
        assertFalse(e.getDetail(false), e.getDetail(false).contains("empty body"));
    }

    @Test
    public void testMapLoginFailureWithoutStructuredMessage() {
        // Response from an intermediary with no storage error in the body
        final BackgroundException e = new AzureExceptionMappingService().map(
                new HttpResponseException("Status code 403, <html>Forbidden</html>", new StubResponse(403), null));
        assertTrue(e instanceof LoginFailureException);
        assertTrue(e.getDetail(false), e.getDetail(false).contains("Forbidden"));
    }

    @Test
    public void testMapWithoutStructuredMessage() {
        final BackgroundException e = new AzureExceptionMappingService().map(
                new HttpResponseException("Status code 404, <html>Not Found</html>", new StubResponse(404), null));
        assertTrue(e instanceof NotfoundException);
        assertTrue(e.getDetail(false), e.getDetail(false).contains("Not Found"));
    }

    private static final class StubResponse extends HttpResponse {
        private final int status;

        StubResponse(final int status) {
            super(new HttpRequest(HttpMethod.GET, "https://localhost/"));
            this.status = status;
        }

        @Override
        public int getStatusCode() {
            return status;
        }

        @Override
        public String getHeaderValue(final String name) {
            return null;
        }

        @Override
        public HttpHeaders getHeaders() {
            return new HttpHeaders();
        }

        @Override
        public Flux<ByteBuffer> getBody() {
            return Flux.empty();
        }

        @Override
        public Mono<byte[]> getBodyAsByteArray() {
            return Mono.empty();
        }

        @Override
        public Mono<String> getBodyAsString() {
            return Mono.empty();
        }

        @Override
        public Mono<String> getBodyAsString(final Charset charset) {
            return Mono.empty();
        }

        @Override
        public Mono<InputStream> getBodyAsInputStream() {
            return Mono.empty();
        }
    }
}
