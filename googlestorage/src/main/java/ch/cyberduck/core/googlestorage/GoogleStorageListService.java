package ch.cyberduck.core.googlestorage;

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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.s3.S3BucketListService;
import ch.cyberduck.core.s3.S3LocationFeature;
import ch.cyberduck.core.s3.S3ObjectListService;

public class GoogleStorageListService implements ListService {
    private final GoogleStorageSession session;

    public GoogleStorageListService(final GoogleStorageSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(directory.isRoot()) {
            return new S3BucketListService(session, new S3LocationFeature.S3Region(session.getHost().getRegion())).list(directory, listener);
        }
        return new S3ObjectListService(session).list(directory, listener);
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        return this;
    }
}
