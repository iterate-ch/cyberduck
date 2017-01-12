package ch.cyberduck.core.nio;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.AbstractExceptionMappingService;
import ch.cyberduck.core.exception.BackgroundException;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

public class LocalExceptionMappingService extends AbstractExceptionMappingService<IOException> {
    @Override
    public BackgroundException map(final IOException e) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, e.getMessage());
        if(e instanceof AccessDeniedException) {
            return new ch.cyberduck.core.exception.AccessDeniedException(buffer.toString(), e);
        }
        return this.wrap(e, buffer);
    }
}
