package ch.cyberduck.core.http;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionRefusedException;

import org.apache.http.ConnectionClosedException;

import java.io.IOException;

public class HttpExceptionMappingService extends DefaultIOExceptionMappingService {

    @Override
    public BackgroundException map(final IOException failure) {
        if(failure instanceof ConnectionClosedException) {
            final StringBuilder buffer = new StringBuilder();
            this.append(buffer, failure.getMessage());
            return new ConnectionRefusedException(buffer.toString(), failure);
        }
        return super.map(failure);
    }
}
