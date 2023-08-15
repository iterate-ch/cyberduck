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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;

import java.util.Collections;

public class B2ListService implements ListService {

    private final B2Session session;
    private final B2VersionIdProvider fileid;

    private Path bucket;

    public B2ListService(final B2Session session, final B2VersionIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(directory.isRoot()) {
            if(bucket != null) {
                final AttributedList<Path> buckets = new AttributedList<>(Collections.singleton(bucket));
                listener.chunk(directory, buckets);
                return buckets;
            }
            return new B2BucketListService(session, fileid).list(directory, listener);
        }
        return new B2ObjectListService(session, fileid).list(directory, listener);
    }

    /**
     * @param bucket When present, access is restricted to one bucket.
     */
    public B2ListService withBucket(final Path bucket) {
        this.bucket = bucket;
        return this;
    }
}
