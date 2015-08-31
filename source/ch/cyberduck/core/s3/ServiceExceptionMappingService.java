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
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.QuotaException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.jets3t.service.ServiceException;

import java.io.IOException;

/**
 * @version $Id$
 */
public class ServiceExceptionMappingService extends AbstractExceptionMappingService<ServiceException> {

    @Override
    public BackgroundException map(final ServiceException e) {
        if(e.getCause() instanceof ServiceException) {
            return this.map((ServiceException) e.getCause());
        }
        final StringBuilder buffer = new StringBuilder();
        final int code = e.getResponseCode();
        if(StringUtils.isNotBlank(e.getErrorMessage())) {
            // S3 protocol message parsed from XML
            this.append(buffer, e.getErrorMessage());
        }
        else {
            this.append(buffer, e.getResponseStatus());
            this.append(buffer, e.getMessage());
        }
        if(HttpStatus.SC_NOT_FOUND == code) {
            return new NotfoundException(buffer.toString(), e);
        }
        if(HttpStatus.SC_FORBIDDEN == code) {
            if(StringUtils.isNotBlank(e.getErrorCode())) {
                if(e.getErrorCode().equals("SignatureDoesNotMatch")) {
                    return new LoginFailureException(buffer.toString(), e);
                }
                if(e.getErrorCode().equals("InvalidAccessKeyId")) {
                    return new LoginFailureException(buffer.toString(), e);
                }
                if(e.getErrorCode().equals("InvalidClientTokenId")) {
                    return new LoginFailureException(buffer.toString(), e);
                }
                if(e.getErrorCode().equals("InvalidSecurity")) {
                    return new LoginFailureException(buffer.toString(), e);
                }
                if(e.getErrorCode().equals("MissingClientTokenId")) {
                    return new LoginFailureException(buffer.toString(), e);
                }
                if(e.getErrorCode().equals("MissingAuthenticationToken")) {
                    return new LoginFailureException(buffer.toString(), e);
                }
            }
            return new AccessDeniedException(buffer.toString(), e);
        }
        if(HttpStatus.SC_UNAUTHORIZED == code) {
            // Actually never returned by S3 but always 403
            return new LoginFailureException(buffer.toString(), e);
        }
        if(HttpStatus.SC_BAD_REQUEST == code) {
            return new InteroperabilityException(buffer.toString(), e);
        }
        if(HttpStatus.SC_NOT_IMPLEMENTED == code) {
            return new InteroperabilityException(buffer.toString(), e);
        }
        if(HttpStatus.SC_SERVICE_UNAVAILABLE == code) {
            return new InteroperabilityException(buffer.toString(), e);
        }
        if(HttpStatus.SC_METHOD_NOT_ALLOWED == code) {
            return new InteroperabilityException(buffer.toString(), e);
        }
        if(HttpStatus.SC_PAYMENT_REQUIRED == code) {
            return new QuotaException(buffer.toString(), e);
        }
        if(e.getCause() instanceof IOException) {
            return new DefaultIOExceptionMappingService().map((IOException) e.getCause());
        }
        return this.wrap(e, buffer);
    }
}