package ch.cyberduck.core.googledrive;

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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.QuotaException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;

import org.apache.http.HttpStatus;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpResponseException;

public class DriveExceptionMappingService extends DefaultIOExceptionMappingService {

    @Override
    public BackgroundException map(final IOException failure) {
        final StringBuilder buffer = new StringBuilder();
        if(failure instanceof GoogleJsonResponseException) {
            final GoogleJsonResponseException error = (GoogleJsonResponseException) failure;
            this.append(buffer, error.getDetails().getMessage());
            switch(error.getDetails().getCode()) {
                case 403:
                    final List<GoogleJsonError.ErrorInfo> errors = error.getDetails().getErrors();
                    for(GoogleJsonError.ErrorInfo info : errors) {
                        if("usageLimits".equals(info.getDomain())) {
                            return new RetriableAccessDeniedException(buffer.toString(), Duration.ofSeconds(5), failure);
                        }
                    }
                    break;
            }
        }
        if(failure instanceof HttpResponseException) {
            final HttpResponseException response = (HttpResponseException) failure;
            this.append(buffer, response.getStatusMessage());
            switch(response.getStatusCode()) {
                case HttpStatus.SC_UNAUTHORIZED:
                    // Invalid Credentials. Refresh the access token using the long-lived refresh token
                    return new LoginFailureException(buffer.toString(), failure);
                case HttpStatus.SC_FORBIDDEN:
                    // 403: User Rate Limit Exceeded
                    return new AccessDeniedException(buffer.toString(), failure);
                case HttpStatus.SC_NOT_FOUND:
                    return new NotfoundException(buffer.toString(), failure);
                case HttpStatus.SC_INSUFFICIENT_SPACE_ON_RESOURCE:
                    return new QuotaException(buffer.toString(), failure);
                case HttpStatus.SC_INSUFFICIENT_STORAGE:
                    return new QuotaException(buffer.toString(), failure);
                case HttpStatus.SC_PAYMENT_REQUIRED:
                    return new QuotaException(buffer.toString(), failure);
                case HttpStatus.SC_BAD_REQUEST:
                    return new InteroperabilityException(buffer.toString(), failure);
                case HttpStatus.SC_METHOD_NOT_ALLOWED:
                    return new InteroperabilityException(buffer.toString(), failure);
                case HttpStatus.SC_NOT_IMPLEMENTED:
                    return new InteroperabilityException(buffer.toString(), failure);
                case HttpStatus.SC_INTERNAL_SERVER_ERROR:
                    return new InteroperabilityException(buffer.toString(), failure);
                case HttpStatus.SC_SERVICE_UNAVAILABLE:
                    return new ConnectionRefusedException(buffer.toString(), failure);
                case HttpStatus.SC_REQUEST_TIMEOUT:
                    return new ConnectionTimeoutException(buffer.toString(), failure);
            }
        }
        return super.map(failure);
    }
}