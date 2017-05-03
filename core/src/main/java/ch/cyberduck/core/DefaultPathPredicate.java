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

import java.util.EnumSet;

/**
 * Path predicate that takes the region and version id of the path into account for comparisons.
 */
public class DefaultPathPredicate implements CacheReference<Path> {

    private final Path file;

    private final PathContainerService containerService
            = new PathContainerService();

    public DefaultPathPredicate(final Path file) {
        this.file = file;
    }

    public String attributes() {
        String qualifier = StringUtils.EMPTY;
        if(StringUtils.isNotBlank(file.attributes().getRegion())) {
            if(containerService.isContainer(file)) {
                qualifier += file.attributes().getRegion();
            }
        }
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            qualifier += file.attributes().getVersionId();
        }
        return qualifier;
    }

    protected String type() {
        final EnumSet<Path.Type> types = EnumSet.copyOf(file.getType());
        types.remove(Path.Type.placeholder);
        types.remove(Path.Type.volume);
        types.remove(Path.Type.encrypted);
        types.remove(Path.Type.decrypted);
        types.remove(Path.Type.vault);
        return String.valueOf(types);
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
        return this.type() + "-" + this.attributes() + file.getAbsolute();
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

    @Override
    public boolean test(final Path file) {
        return this.hashCode() == new DefaultPathPredicate(file).hashCode();
    }
}
