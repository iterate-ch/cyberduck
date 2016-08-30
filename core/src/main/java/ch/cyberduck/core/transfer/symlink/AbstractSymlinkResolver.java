package ch.cyberduck.core.transfer.symlink;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;

import org.apache.commons.lang3.StringUtils;

public abstract class AbstractSymlinkResolver<T> implements SymlinkResolver<T> {

    @Override
    public String relativize(final String base, final String name) {
        final String parent = PathNormalizer.parent(base, Path.DELIMITER);
        if(name.startsWith(parent)) {
            return StringUtils.substring(name, parent.length() + 1);
        }
        else {
            return String.format("..%s%s", Path.DELIMITER, this.relativize(parent, name));
        }
    }
}