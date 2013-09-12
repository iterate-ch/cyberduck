package ch.cyberduck.core;

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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import org.apache.commons.lang3.StringUtils;

/**
 * @version $Id$
 */
public class DefaultPathReference implements PathReference<String> {

    private Path path;

    public DefaultPathReference(final Path path) {
        this.path = path;
    }

    /**
     * Obtain a string representation of the path that is unique for versioned files.
     *
     * @return The absolute path with version ID and checksum if any.
     */
    @Override
    public String unique() {
        String qualifier = String.valueOf(path.attributes().getType());
        if(StringUtils.isNotBlank(path.attributes().getRegion())) {
            qualifier += path.attributes().getRegion();
        }
        if(StringUtils.isNotBlank(path.attributes().getVersionId())) {
            qualifier += path.attributes().getVersionId();
        }
        if(StringUtils.isNotBlank(qualifier)) {
            return String.format("%s-%s", path.getAbsolute(), qualifier);
        }
        return path.getAbsolute();
    }

    @Override
    public int hashCode() {
        return this.unique().hashCode();
    }

    @Override
    public String toString() {
        return this.unique();
    }

    /**
     * Comparing the hashcode.
     *
     * @see #hashCode()
     */
    @Override
    public boolean equals(Object other) {
        if(null == other) {
            return false;
        }
        if(other instanceof PathReference) {
            return this.hashCode() == other.hashCode();
        }
        return false;
    }
}
