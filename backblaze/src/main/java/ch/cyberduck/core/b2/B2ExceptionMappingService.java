package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.QuotaException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import java.time.Duration;

import synapticloop.b2.exception.B2ApiException;

public class B2ExceptionMappingService extends AbstractExceptionMappingService<B2ApiException> {
    private static final Logger log = Logger.getLogger(B2ExceptionMappingService.class);

    private final B2Session session;

    public B2ExceptionMappingService(final B2Session session) {
        this.session = session;
    }

    @Override
    public BackgroundException map(final B2ApiException e) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getMessage());
        switch(e.getStatus()) {
            case HttpStatus.SC_UNAUTHORIZED:
                // 401 Unauthorized.
                switch(e.getCode()) {
                    case "expired_auth_token":
                        try {
                            session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
                            return new RetriableAccessDeniedException(buffer.toString());
                        }
                        catch(BackgroundException f) {
                            log.warn(String.format("Attempt to renew expired auth token failed. %s", f.getDetail()));
                        }
                        break;
                }
                return new LoginFailureException(buffer.toString(), e);
            case HttpStatus.SC_FORBIDDEN:
                switch(e.getCode()) {
                    case "cap_exceeded":
                    case "storage_cap_exceeded":
                    case "transaction_cap_exceeded":
                        // Reached the storage cap that you set
                        return new QuotaException(buffer.toString(), e);
                }
                return new AccessDeniedException(buffer.toString(), e);
            case HttpStatus.SC_NOT_FOUND:
                return new NotfoundException(buffer.toString(), e);
            case HttpStatus.SC_INSUFFICIENT_SPACE_ON_RESOURCE:
                return new QuotaException(buffer.toString(), e);
            case HttpStatus.SC_INSUFFICIENT_STORAGE:
                return new QuotaException(buffer.toString(), e);
            case HttpStatus.SC_PAYMENT_REQUIRED:
                return new QuotaException(buffer.toString(), e);
            case HttpStatus.SC_BAD_REQUEST:
                switch(e.getCode()) {
                    case "file_not_present":
                        return new NotfoundException(buffer.toString(), e);
                    case "cap_exceeded":
                        // Reached the storage cap that you set
                        return new QuotaException(buffer.toString(), e);
                    case "bad_request":
                        switch(e.getMessage()) {
                            case "sha1 did not match data received":
                                return new ChecksumException(buffer.toString(), e);
                        }
                }
                return new InteroperabilityException(buffer.toString(), e);
            case HttpStatus.SC_METHOD_NOT_ALLOWED:
                return new InteroperabilityException(buffer.toString(), e);
            case HttpStatus.SC_NOT_IMPLEMENTED:
                return new InteroperabilityException(buffer.toString(), e);
            case HttpStatus.SC_SERVICE_UNAVAILABLE:
                return new ConnectionRefusedException(buffer.toString(), e);
            default:
                if(e.getRetry() != null) {
                    // Too Many Requests (429)
                    return new RetriableAccessDeniedException(buffer.toString(), Duration.ofSeconds(e.getRetry()));
                }
                return new InteroperabilityException(buffer.toString(), e);
        }
    }
}
