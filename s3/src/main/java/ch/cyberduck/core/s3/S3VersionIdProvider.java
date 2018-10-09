package ch.cyberduck.core.s3;

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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.IdProvider;

import org.apache.commons.lang3.StringUtils;

public class S3VersionIdProvider implements IdProvider {

    private final S3Session session;

    public S3VersionIdProvider(final S3Session session) {
        this.session = session;
    }

    @Override
    public String getFileid(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            return file.attributes().getVersionId();
        }
        if(file.isRoot()) {
            return null;
        }
        return new S3AttributesFinderFeature(session).find(file, new DisabledListProgressListener()).getVersionId();
    }

    @Override
    public IdProvider withCache(final Cache<Path> cache) {
        return this;
    }
}
