package ch.cyberduck.core.cloudfront;

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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;

import org.apache.http.HttpStatus;
import org.jets3t.service.CloudFrontServiceException;

public class CloudFrontServiceExceptionMappingService extends AbstractExceptionMappingService<CloudFrontServiceException> {

    @Override
    public BackgroundException map(final CloudFrontServiceException e) {
        if(e.getCause() instanceof BackgroundException) {
            return (BackgroundException) e.getCause();
        }
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getErrorMessage());
        this.append(buffer, e.getErrorDetail());
        if(e.getResponseCode() == HttpStatus.SC_FORBIDDEN) {
            return new LoginFailureException(buffer.toString(), e);
        }
        if(e.getResponseCode() == HttpStatus.SC_NOT_FOUND) {
            return new NotfoundException(buffer.toString(), e);
        }
        if(e.getResponseCode() == HttpStatus.SC_BAD_REQUEST) {
            if(e.getErrorCode().equals("InvalidHttpAuthHeader")) {
                return new LoginFailureException(buffer.toString(), e);
            }
            return new InteroperabilityException(buffer.toString(), e);
        }
        if(e.getResponseCode() == HttpStatus.SC_METHOD_NOT_ALLOWED) {
            return new InteroperabilityException(buffer.toString(), e);
        }
        return this.wrap(e, buffer);
    }
}
