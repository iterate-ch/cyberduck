package ch.cyberduck.core.aws;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

public class AmazonServiceExceptionMappingService extends AbstractExceptionMappingService<AmazonClientException> {
    private static final Logger log = LogManager.getLogger(AmazonServiceExceptionMappingService.class);

    @Override
    public BackgroundException map(final AmazonClientException e) {
        log.warn("Map failure {}", e.toString());
        final StringBuilder buffer = new StringBuilder();
        if(e instanceof AmazonServiceException) {
            final AmazonServiceException failure = (AmazonServiceException) e;
            this.append(buffer, failure.getErrorMessage());
            if(null != failure.getErrorCode()) {
                switch(failure.getStatusCode()) {
                    case HttpStatus.SC_BAD_REQUEST:
                        switch(failure.getErrorCode()) {
                            case "Throttling":
                                return new RetriableAccessDeniedException(buffer.toString(), e);
                            case "AccessDeniedException":
                                return new AccessDeniedException(buffer.toString(), e);
                            case "UnrecognizedClientException":
                                return new LoginFailureException(buffer.toString(), e);
                        }
                    case HttpStatus.SC_FORBIDDEN:
                        switch(failure.getErrorCode()) {
                            case "SignatureDoesNotMatch":
                            case "InvalidAccessKeyId":
                            case "InvalidClientTokenId":
                            case "InvalidSecurity":
                            case "MissingClientTokenId":
                            case "MissingAuthenticationToken":
                                return new LoginFailureException(buffer.toString(), e);
                        }
                }
            }
            return new DefaultHttpResponseExceptionMappingService().map(new HttpResponseException(failure.getStatusCode(), buffer.toString()));
        }
        this.append(buffer, e.getMessage());
        return this.wrap(e, buffer);
    }
}
