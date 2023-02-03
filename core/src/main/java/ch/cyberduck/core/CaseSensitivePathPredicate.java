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

public final class CaseSensitivePathPredicate extends SimplePathPredicate {

    public CaseSensitivePathPredicate(final Path file) {
        super(file);
    }

    public CaseSensitivePathPredicate(final Path.Type type, final String path) {
        super(type, path);
    }

    @Override
    public boolean test(final Path test) {
        return this.equals(new CaseSensitivePathPredicate(test));
    }
}
