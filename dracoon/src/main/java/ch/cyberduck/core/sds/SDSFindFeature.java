package ch.cyberduck.core.sds;

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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;

public class SDSFindFeature implements Find {

    private final SDSNodeIdProvider nodeid;

    public SDSFindFeature(final SDSNodeIdProvider nodeid) {
        this.nodeid = nodeid;
    }

    @Override
    public boolean find(final Path file, final ListProgressListener listener) throws BackgroundException {
        try {
            nodeid.getFileid(file, new DisabledListProgressListener());
            return true;
        }
        catch(NotfoundException e) {
            return false;
        }
    }

    @Override
    public Find withCache(final Cache<Path> cache) {
        nodeid.withCache(cache);
        return this;
    }
}
