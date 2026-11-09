package ch.cyberduck.core.http;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.junit.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HttpResponseExceptionMappingServiceTest {

    @Test
    public void testTooManyRequests() {
        final BackgroundException failure = new DefaultHttpResponseExceptionMappingService().map(new HttpResponseException(HttpStatus.SC_TOO_MANY_REQUESTS, "Too Many Requests"));
        assertTrue(failure instanceof RetriableAccessDeniedException);
        assertEquals(Duration.ofSeconds(PreferencesFactory.get().getLong("connection.retry.delay")), ((RetriableAccessDeniedException) failure).getDelay());
    }

    @Test
    public void testTooManyRequestsRetryAfter() {
        final BackgroundException failure = new DefaultHttpResponseExceptionMappingService().map(
                new HttpResponseException(HttpStatus.SC_TOO_MANY_REQUESTS, "Too Many Requests"), new StringBuilder("Too Many Requests"),
                HttpStatus.SC_TOO_MANY_REQUESTS, Collections.singletonMap("retry-after", Collections.singletonList(" 7 ")));
        assertTrue(failure instanceof RetriableAccessDeniedException);
        assertEquals(7L, ((RetriableAccessDeniedException) failure).getDelay().getSeconds());
    }

    @Test
    public void testServiceUnavailableRetryAfter() {
        final BackgroundException failure = new DefaultHttpResponseExceptionMappingService().map(
                new HttpResponseException(HttpStatus.SC_SERVICE_UNAVAILABLE, "Service Unavailable"), new StringBuilder("Service Unavailable"),
                HttpStatus.SC_SERVICE_UNAVAILABLE, Collections.singletonMap("Retry-After", Collections.singletonList("11")));
        assertTrue(failure instanceof RetriableAccessDeniedException);
        assertEquals(11L, ((RetriableAccessDeniedException) failure).getDelay().getSeconds());
    }

    @Test
    public void testServiceUnavailable() {
        final BackgroundException failure = new DefaultHttpResponseExceptionMappingService().map(new HttpResponseException(HttpStatus.SC_SERVICE_UNAVAILABLE, "Service Unavailable"));
        assertTrue(failure instanceof RetriableAccessDeniedException);
        assertEquals(Duration.ofSeconds(PreferencesFactory.get().getLong("connection.retry.delay")), ((RetriableAccessDeniedException) failure).getDelay());
    }

    @Test
    public void testTooManyRequestsRetryAfterHttpDate() {
        final Clock clock = Clock.fixed(Instant.parse("2026-10-21T07:27:30Z"), ZoneOffset.UTC);
        assertEquals(Optional.of(Duration.ofSeconds(30)), HttpResponseExceptionMappingService.retryAfter(
                Collections.singletonMap("Retry-After", Collections.singletonList("Wed, 21 Oct 2026 07:28:00 GMT")), clock));
        // Date in the past
        assertEquals(Optional.of(Duration.ZERO), HttpResponseExceptionMappingService.retryAfter(
                Collections.singletonMap("Retry-After", Collections.singletonList("Wed, 21 Oct 2026 07:00:00 GMT")), clock));
        assertEquals(Optional.empty(), HttpResponseExceptionMappingService.retryAfter(
                Collections.singletonMap("Retry-After", Collections.singletonList("tomorrow")), clock));
    }

    @Test
    public void testServerErrorRetriable() {
        final BackgroundException failure = new DefaultHttpResponseExceptionMappingService().map(new HttpResponseException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "f"));
        assertTrue(failure instanceof RetriableAccessDeniedException);
    }

    @Test
    public void testRetryAfter() {
        assertEquals(Optional.of(Duration.ofSeconds(5)), HttpResponseExceptionMappingService.retryAfter(Collections.singletonMap("Retry-After", Collections.singletonList("5"))));
        assertEquals(Optional.empty(), HttpResponseExceptionMappingService.retryAfter(Collections.emptyMap()));
        assertEquals(Optional.empty(), HttpResponseExceptionMappingService.retryAfter(null));
        assertEquals(Optional.empty(), HttpResponseExceptionMappingService.retryAfter(Collections.singletonMap("Retry-After", Collections.singletonList("-1"))));
    }
}
