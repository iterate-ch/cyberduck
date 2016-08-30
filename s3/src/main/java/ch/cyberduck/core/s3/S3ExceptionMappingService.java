package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.QuotaException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.jets3t.service.ServiceException;

import java.io.IOException;

public class S3ExceptionMappingService extends AbstractExceptionMappingService<ServiceException> {

    @Override
    public BackgroundException map(final ServiceException e) {
        if(e.getCause() instanceof ServiceException) {
            return this.map((ServiceException) e.getCause());
        }
        final StringBuilder buffer = new StringBuilder();
        if(StringUtils.isNotBlank(e.getErrorMessage())) {
            // S3 protocol message parsed from XML
            this.append(buffer, StringEscapeUtils.unescapeXml(e.getErrorMessage()));
        }
        else {
            this.append(buffer, e.getResponseStatus());
            this.append(buffer, e.getMessage());
        }
        switch(e.getResponseCode()) {
            case HttpStatus.SC_NOT_FOUND:
                return new NotfoundException(buffer.toString(), e);
            case HttpStatus.SC_CONFLICT:
                return new AccessDeniedException(buffer.toString(), e);
            case HttpStatus.SC_FORBIDDEN:
                if(StringUtils.isNotBlank(e.getErrorCode())) {
                    switch(e.getErrorCode()) {
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
                }
                return new AccessDeniedException(buffer.toString(), e);
            case HttpStatus.SC_UNAUTHORIZED:
                // Actually never returned by S3 but always 403
                return new LoginFailureException(buffer.toString(), e);
            case HttpStatus.SC_BAD_REQUEST:
            case HttpStatus.SC_NOT_IMPLEMENTED:
            case HttpStatus.SC_METHOD_NOT_ALLOWED:
                return new InteroperabilityException(buffer.toString(), e);
            case HttpStatus.SC_SERVICE_UNAVAILABLE:
                return new ConnectionRefusedException(buffer.toString(), e);
            case HttpStatus.SC_PAYMENT_REQUIRED:
                return new QuotaException(buffer.toString(), e);
        }
        if(e.getCause() instanceof IOException) {
            return new DefaultIOExceptionMappingService().map((IOException) e.getCause());
        }
        return this.wrap(e, buffer);
    }
}