package ch.cyberduck.core.exception;

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

import org.apache.http.HttpStatus;

import com.googlecode.sardine.impl.SardineException;

/**
 * @version $Id$
 */
public class SardineExceptionMappingService extends AbstractIOExceptionMappingService<SardineException> {

    @Override
    public BackgroundException map(final SardineException e) {
        final StringBuilder buffer = new StringBuilder();
        // HTTP method status
        this.append(buffer, e.getResponsePhrase());
        if(e.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
            return new LoginFailureException(buffer.toString(), e);
        }
        if(e.getStatusCode() == HttpStatus.SC_FORBIDDEN) {
            return new LoginFailureException(buffer.toString(), e);
        }
        if(e.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
            return new NotfoundException(buffer.toString(), e);
        }
        return this.wrap(e, buffer);
    }
}
