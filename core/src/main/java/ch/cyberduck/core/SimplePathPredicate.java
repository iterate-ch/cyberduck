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

import java.util.Objects;

public class SimplePathPredicate implements CacheReference<Path> {

    protected final Path file;

    private final Path.Type type;
    private final String path;

    public SimplePathPredicate(final Path file) {
        this.file = file;
        this.type = file.isSymbolicLink() ? Path.Type.symboliclink : file.isFile() ? Path.Type.file : Path.Type.directory;
        this.path = file.getAbsolute();
    }

    @Override
    public boolean equals(final Object o) {
        if(null == o) {
            return false;
        }
        if(o instanceof CacheReference) {
            return this.hashCode() == o.hashCode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, path);
    }

    @Override
    public boolean test(final Path test) {
        return this.hashCode() == new SimplePathPredicate(test).hashCode();
    }
}
