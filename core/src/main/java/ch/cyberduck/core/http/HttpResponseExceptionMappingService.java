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

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;

public abstract class HttpResponseExceptionMappingService<E extends HttpResponseException> extends AbstractExceptionMappingService<E> {

    @Override
    public BackgroundException map(final E failure) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, failure.getReasonPhrase());
        final int statusCode = failure.getStatusCode();
        return this.map(failure, buffer, statusCode);
    }

    public BackgroundException map(final Throwable failure, final StringBuilder buffer, final int statusCode) {
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
            case HttpStatus.SC_SERVICE_UNAVAILABLE:
                return new ConnectionRefusedException(buffer.toString(), failure);
            case HttpStatus.SC_INTERNAL_SERVER_ERROR:
            case HttpStatus.SC_TOO_MANY_REQUESTS:
                // Too Many Requests. Rate limiting
            case 509:
                // Bandwidth Limit Exceeded
                return new RetriableAccessDeniedException(buffer.toString(), failure);
            default:
                return new InteroperabilityException(buffer.toString(), failure);
        }
    }
}
