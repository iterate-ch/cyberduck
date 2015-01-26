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
public class
        DefaultPathReference implements CacheReference<Path> {

    private Path path;

    private PathContainerService containerService
            = new PathContainerService();

    public DefaultPathReference(final Path path) {
        this.path = path;
    }

    public String attributes() {
        String qualifier = StringUtils.EMPTY;
        if(StringUtils.isNotBlank(path.attributes().getRegion())) {
            if(containerService.isContainer(path)) {
                qualifier += path.attributes().getRegion();
            }
        }
        if(StringUtils.isNotBlank(path.attributes().getVersionId())) {
            qualifier += path.attributes().getVersionId();
        }
        return qualifier;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    /**
     * Obtain a string representation of the path that is unique for versioned files.
     *
     * @return The absolute path with version ID and checksum if any.
     */
    @Override
    public String toString() {
        return String.format("%s-%s%s",
                String.valueOf(path.getType()), this.attributes(), path.getAbsolute());
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
        if(other instanceof CacheReference) {
            return this.hashCode() == other.hashCode();
        }
        return false;
    }
}
