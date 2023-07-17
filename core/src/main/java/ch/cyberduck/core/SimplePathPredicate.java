package ch.cyberduck.core;

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

import ch.cyberduck.core.unicode.NFCNormalizer;
import ch.cyberduck.core.unicode.UnicodeNormalizer;

import org.apache.commons.lang3.StringUtils;

public class SimplePathPredicate implements CacheReference<Path> {

    private static final UnicodeNormalizer normalizer = new NFCNormalizer();

    private final Path.Type type;
    private final String path;

    public SimplePathPredicate(final Path file) {
        this(file.isSymbolicLink() ? Path.Type.symboliclink : file.isFile() ? Path.Type.file : Path.Type.directory, file.getAbsolute());
    }

    public SimplePathPredicate(final Path.Type type, final String path) {
        this.type = type;
        this.path = normalizer.normalize(path).toString();
    }

    @Override
    public boolean equals(final Object o) {
        if(null == o) {
            return false;
        }
        if(o instanceof CacheReference) {
            if(this.hashCode() == o.hashCode()) {
                return this.toString().equals(o.toString());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.toString().hashCode();
    }

    @Override
    public boolean test(final Path test) {
        return this.equals(new SimplePathPredicate(test));
    }

    /**
     * @param directory Parent directory
     * @return True if this is child of parameter based on path comparison
     */
    public boolean isChild(final SimplePathPredicate directory) {
        if(StringUtils.equals(PathNormalizer.parent(path, Path.DELIMITER),
                PathNormalizer.parent(directory.path, Path.DELIMITER))) {
            return false;
        }
        if(directory.path.equals(String.valueOf(Path.DELIMITER))) {
            return true;
        }
        return StringUtils.startsWith(path, directory.path + Path.DELIMITER);
    }

    @Override
    public String toString() {
        return "[" + type + "]" + "-" + path;
    }
}
