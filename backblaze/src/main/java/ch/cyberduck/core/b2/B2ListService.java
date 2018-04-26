package ch.cyberduck.core.b2;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;

public class B2ListService implements ListService {

    private final B2BucketListService buckets;
    private final B2ObjectListService objects;

    public B2ListService(final B2Session session, final B2FileidProvider fileid) {
        buckets = new B2BucketListService(session);
        objects = new B2ObjectListService(session, fileid);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(directory.isRoot()) {
            return buckets.list(directory, listener);
        }
        else {
            return objects.list(directory, listener);
        }
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        buckets.withCache(cache);
        objects.withCache(cache);
        return this;
    }
}
