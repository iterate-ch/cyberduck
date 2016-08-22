package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;

public final class BooleanRecursiveCallback<T> implements Worker.RecursiveCallback<T> {
    private final boolean recursive;

    public BooleanRecursiveCallback(final boolean recursive) {
        this.recursive = recursive;
    }

    @Override
    public boolean recurse(final Path directory, final T value) {
        return recursive;
    }
}
