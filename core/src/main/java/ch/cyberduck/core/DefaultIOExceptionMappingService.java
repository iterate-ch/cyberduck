package ch.cyberduck.core;

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
import ch.cyberduck.core.ssl.SSLExceptionMappingService;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.net.ssl.SSLException;
import java.io.IOException;

public class DefaultIOExceptionMappingService extends AbstractExceptionMappingService<IOException> {

    public BackgroundException map(final IOException failure, final Path directory) {
        return super.map("Connection failed", failure, directory);
    }

    @Override
    public BackgroundException map(final IOException failure) {
        final Throwable[] stack = ExceptionUtils.getThrowables(failure);
        for(Throwable t : stack) {
            if(t instanceof BackgroundException) {
                return (BackgroundException) t;
            }
        }
        if(failure instanceof SSLException) {
            return new SSLExceptionMappingService().map((SSLException) failure);
        }
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, failure.getMessage());
        for(Throwable cause : ExceptionUtils.getThrowableList(failure)) {
            if(!StringUtils.contains(failure.getMessage(), cause.getMessage())) {
                this.append(buffer, cause.getMessage());
            }
        }
        return this.wrap(failure, buffer);
    }
}