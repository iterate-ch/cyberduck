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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.sardine.impl.SardineException;

public class DAVExceptionMappingService extends HttpResponseExceptionMappingService<SardineException> {
    private static final Logger log = LogManager.getLogger(DAVExceptionMappingService.class);

    @Override
    public BackgroundException map(final SardineException failure) {
        log.warn("Map failure {}", failure.toString());
        final StringBuilder buffer = new StringBuilder();
        switch(failure.getStatusCode()) {
            case HttpStatus.SC_OK:
            case HttpStatus.SC_MULTI_STATUS:
                // HTTP method status
                this.append(buffer, failure.getMessage());
                // Failure unmarshalling XML response
                return new InteroperabilityException(buffer.toString(), failure);
        }
        this.append(buffer, String.format("%s (%d %s)", failure.getReasonPhrase(), failure.getStatusCode(), failure.getResponsePhrase()));
        return super.map(failure, buffer, failure.getStatusCode());
    }
}
