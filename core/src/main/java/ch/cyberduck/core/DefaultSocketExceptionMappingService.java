package ch.cyberduck.core;

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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.ConnectionRefusedException;

import org.apache.commons.lang3.StringUtils;

import java.net.SocketException;

public class DefaultSocketExceptionMappingService extends AbstractExceptionMappingService<SocketException> {

    @Override
    public BackgroundException map(final SocketException failure) {
        final StringBuilder buffer = new StringBuilder();
        this.append(buffer, failure.getMessage());
        if(StringUtils.equals(failure.getMessage(), "Software caused connection abort")) {
            return new ConnectionCanceledException(buffer.toString(), failure);
        }
        if(StringUtils.equals(failure.getMessage(), "Socket closed")) {
            return new ConnectionCanceledException(buffer.toString(), failure);
        }
        return new ConnectionRefusedException(buffer.toString(), failure);
    }
}
