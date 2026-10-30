package ch.cyberduck.core.http;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LockedException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.ProxyLoginFailureException;
import ch.cyberduck.core.exception.QuotaException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class HttpResponseExceptionMappingService<E extends HttpResponseException> extends AbstractExceptionMappingService<E> {
    private static final Logger log = LogManager.getLogger(HttpResponseExceptionMappingService.class);

    @Override
    public BackgroundException map(final E failure) {
        log.warn("Map failure {}", failure.toString());
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, failure.getReasonPhrase());
        final int statusCode = failure.getStatusCode();
        return this.map(failure, buffer, statusCode);
    }

    public BackgroundException map(final Throwable failure, final StringBuilder buffer, final int statusCode) {
        return this.map(failure, buffer, statusCode, Collections.emptyMap());
    }

    /**
     * @param headers Response headers
     * @return Delay requested with Retry-After header either in seconds or as HTTP-date
     */
    public static Optional<Duration> retryAfter(final Map<String, List<String>> headers) {
        return retryAfter(headers, Clock.systemUTC());
    }

    public static Optional<Duration> retryAfter(final Map<String, List<String>> headers, final Clock clock) {
        if(null == headers) {
            return Optional.empty();
        }
        return headers.entrySet().stream()
                .filter(e -> HttpHeaders.RETRY_AFTER.equalsIgnoreCase(e.getKey()))
                .flatMap(e -> e.getValue().stream())
                .map(String::trim)
                .map(v -> {
                    if(v.matches("\\d{1,9}")) {
                        return Optional.of(Duration.ofSeconds(Long.parseLong(v)));
                    }
                    try {
                        final Duration delay = Duration.between(clock.instant(), ZonedDateTime.parse(v, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant());
                        return Optional.of(delay.isNegative() ? Duration.ZERO : delay);
                    }
                    catch(DateTimeParseException e) {
                        return Optional.<Duration>empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public BackgroundException map(final Throwable failure, final StringBuilder buffer, final int statusCode, final Map<String, List<String>> headers) {
        switch(statusCode) {
            case HttpStatus.SC_UNAUTHORIZED:
                return new LoginFailureException(buffer.toString(), failure);
            case HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED:
                return new ProxyLoginFailureException(buffer.toString(), failure);
            case HttpStatus.SC_FORBIDDEN:
            case HttpStatus.SC_NOT_ACCEPTABLE:
                return new AccessDeniedException(buffer.toString(), failure);
            case HttpStatus.SC_CONFLICT:
            case HttpStatus.SC_METHOD_NOT_ALLOWED:
            case HttpStatus.SC_PRECONDITION_FAILED:
                return new ConflictException(buffer.toString(), failure);
            case HttpStatus.SC_NOT_FOUND:
            case HttpStatus.SC_GONE:
            case HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE:
                return new NotfoundException(buffer.toString(), failure);
            case HttpStatus.SC_INSUFFICIENT_SPACE_ON_RESOURCE:
            case HttpStatus.SC_INSUFFICIENT_STORAGE:
            case HttpStatus.SC_PAYMENT_REQUIRED:
                return new QuotaException(buffer.toString(), failure);
            case HttpStatus.SC_REQUEST_TIMEOUT:
            case HttpStatus.SC_GATEWAY_TIMEOUT:
                return new ConnectionTimeoutException(buffer.toString(), failure);
            case HttpStatus.SC_LOCKED:
                return new LockedException(buffer.toString(), failure);
            case HttpStatus.SC_BAD_GATEWAY:
                return new ConnectionRefusedException(buffer.toString(), failure);
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
            case HttpStatus.SC_SERVICE_UNAVAILABLE:
            case HttpStatus.SC_TOO_MANY_REQUESTS:
                // Too Many Requests. Rate limiting
            case 509:
                // Bandwidth Limit Exceeded
                return retryAfter(headers).map(delay -> new RetriableAccessDeniedException(buffer.toString(), delay, failure))
                        .orElseGet(() -> new RetriableAccessDeniedException(buffer.toString(), failure));
            default:
                return new InteroperabilityException(buffer.toString(), failure);
        }
    }
}
