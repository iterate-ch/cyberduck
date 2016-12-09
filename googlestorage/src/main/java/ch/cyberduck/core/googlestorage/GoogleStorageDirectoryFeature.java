package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.s3.S3DirectoryFeature;
import ch.cyberduck.core.s3.S3PathContainerService;
import ch.cyberduck.core.transfer.TransferStatus;

public class GoogleStorageDirectoryFeature extends S3DirectoryFeature {

    private final GoogleStorageSession session;

    private final PathContainerService containerService
            = new S3PathContainerService();

    public GoogleStorageDirectoryFeature(final GoogleStorageSession session) {
        super(session);
        this.session = session;
    }

    @Override
    public void mkdir(final Path file) throws BackgroundException {
        this.mkdir(file, null, null);
    }

    @Override
    public void mkdir(final Path file, final String region, final TransferStatus status) throws BackgroundException {
        if(containerService.isContainer(file)) {
            final GoogleStorageBucketCreateService service = new GoogleStorageBucketCreateService(session);
            service.create(file, region);
        }
        else {
            super.mkdir(file, region, status);
        }
    }
}
