package ch.cyberduck.core.transfer;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;

public class CachingListProgressListener extends DisabledListProgressListener {
    private final Cache<Path> cache;

    public CachingListProgressListener(final Cache<Path> cache) {
        this.cache = cache;
    }

    @Override
    public void chunk(final Path folder, final AttributedList<Path> list) {
        cache.put(folder, list);
    }
}
