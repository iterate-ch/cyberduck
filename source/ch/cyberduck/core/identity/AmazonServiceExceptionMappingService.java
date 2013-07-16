package ch.cyberduck.core.identity;

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

import ch.cyberduck.core.exception.AbstractIOExceptionMappingService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;

import org.apache.http.HttpStatus;

import com.amazonaws.AmazonServiceException;

/**
 * @version $Id$
 */
public class AmazonServiceExceptionMappingService extends AbstractIOExceptionMappingService<AmazonServiceException> {

    @Override
    public BackgroundException map(final AmazonServiceException e) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getMessage());
        this.append(buffer, e.getErrorCode());
        if(e.getStatusCode() == HttpStatus.SC_FORBIDDEN) {
            return new LoginFailureException(buffer.toString(), e);
        }
        if(e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            return new LoginFailureException(buffer.toString(), e);
        }
        return this.wrap(e, buffer);
    }
}
