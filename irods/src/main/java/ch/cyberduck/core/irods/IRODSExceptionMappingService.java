package ch.cyberduck.core.irods;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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
import ch.cyberduck.core.exception.BackgroundException;

public class IRODSExceptionMappingService extends AbstractExceptionMappingService<Exception> {
    private static final Logger log = LogManager.getLogger(IRODSExceptionMappingService.class);

    @Override
    public BackgroundException map(final Exception e) {
    	//TODO: write a more complete exception mapping services
        log.warn("Map failure {}", e.toString());
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getMessage());
        return this.wrap(e, buffer);
    }
    
}
