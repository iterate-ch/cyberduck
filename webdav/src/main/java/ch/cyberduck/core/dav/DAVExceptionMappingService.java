package ch.cyberduck.core.dav;

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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.http.HttpResponseExceptionMappingService;

import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;

public class DAVExceptionMappingService extends HttpResponseExceptionMappingService {

    @Override
    public BackgroundException map(final HttpResponseException failure) {
        final StringBuilder buffer = new StringBuilder();
        final int statusCode = failure.getStatusCode();
        if(statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_MULTI_STATUS) {
            // HTTP method status
            this.append(buffer, failure.getMessage());
            // Failure unmarshalling XML response
            return new InteroperabilityException(buffer.toString(), failure);
        }
        return super.map(failure);
    }
}
