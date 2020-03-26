package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
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

import java.util.Objects;

public final class CaseInsensitivePathPredicate implements CacheReference<Path> {

    private static final UnicodeNormalizer normalizer = new NFCNormalizer();

    private final Path.Type type;
    private final String path;

    public CaseInsensitivePathPredicate(final Path file) {
        this.type = file.isSymbolicLink() ? Path.Type.symboliclink : file.isFile() ? Path.Type.file : Path.Type.directory;
        this.path = StringUtils.lowerCase(normalizer.normalize(file.getAbsolute()).toString());
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
        return Objects.hash(type, path);
    }

    @Override
    public boolean test(final Path test) {
        return this.equals(new CaseInsensitivePathPredicate(test));
    }

    @Override
    public String toString() {
        return "[" + type + "]" + "-" + path;
    }
}
