package ch.cyberduck.core.iam;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.http.HttpStatus;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

public class AmazonServiceExceptionMappingService extends AbstractExceptionMappingService<AmazonClientException> {

    @Override
    public BackgroundException map(final AmazonClientException e) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getMessage());
        if(e instanceof AmazonServiceException) {
            final AmazonServiceException failure = (AmazonServiceException) e;
            switch(failure.getStatusCode()) {
                case HttpStatus.SC_BAD_REQUEST:
                    switch(failure.getErrorCode()) {
                        case "UnrecognizedClientException":
                            return new LoginFailureException(buffer.toString(), e);
                    }
                    return new InteroperabilityException(buffer.toString(), e);
                case HttpStatus.SC_METHOD_NOT_ALLOWED:
                    return new InteroperabilityException(buffer.toString(), e);
                case HttpStatus.SC_FORBIDDEN:
                    switch(failure.getErrorCode()) {
                        case "SignatureDoesNotMatch":
                            return new LoginFailureException(buffer.toString(), e);
                        case "InvalidAccessKeyId":
                            return new LoginFailureException(buffer.toString(), e);
                        case "InvalidClientTokenId":
                            return new LoginFailureException(buffer.toString(), e);
                        case "InvalidSecurity":
                            return new LoginFailureException(buffer.toString(), e);
                        case "MissingClientTokenId":
                            return new LoginFailureException(buffer.toString(), e);
                        case "MissingAuthenticationToken":
                            return new LoginFailureException(buffer.toString(), e);
                    }
                    return new AccessDeniedException(buffer.toString(), e);
                case HttpStatus.SC_UNAUTHORIZED:
                    return new LoginFailureException(buffer.toString(), e);
                case HttpStatus.SC_NOT_FOUND:
                    return new NotfoundException(buffer.toString(), e);
                case HttpStatus.SC_SERVICE_UNAVAILABLE:
                    return new ConnectionRefusedException(buffer.toString(), e);
            }
        }
        return this.wrap(e, buffer);
    }
}
