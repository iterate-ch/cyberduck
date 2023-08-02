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

import org.apache.commons.lang3.StringUtils;

public class TildePathExpander {

    public static final String PREFIX
            = String.format("%s%s%s", Path.DELIMITER, Path.HOME, Path.DELIMITER);

    private final Path workdir;

    public TildePathExpander(final Path workdir) {
        this.workdir = workdir;
    }

    public String expand(final String remote) {
        return this.expand(remote, PREFIX);
    }

    /**
     * @param remote Path
     * @param prefix Prefix to replace in path
     * @return Absolute path with prefix replaced with working directory path
     */
    public String expand(final String remote, final String prefix) {
        if(remote.startsWith(prefix)) {
            return StringUtils.replaceOnce(remote, prefix, workdir.getAbsolute() + Path.DELIMITER);
        }
        return remote;
    }
}
