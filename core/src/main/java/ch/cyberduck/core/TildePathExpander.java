package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.exception.BackgroundException;

import org.apache.commons.lang3.StringUtils;

public class TildePathExpander {

    public static final String PREFIX
            = String.format("%s%s", Path.DELIMITER, Path.HOME);

    private final Path workdir;

    public TildePathExpander(final Path workdir) {
        this.workdir = workdir;
    }

    public Path expand(final Path remote) throws BackgroundException {
        return this.expand(remote, PREFIX);
    }

    protected Path expand(final Path remote, final String format) throws BackgroundException {
        if(remote.getAbsolute().startsWith(format)) {
            return new Path(StringUtils.replaceOnce(remote.getAbsolute(), format, workdir.getAbsolute()),
                    remote.getType());
        }
        return remote;
    }
}
