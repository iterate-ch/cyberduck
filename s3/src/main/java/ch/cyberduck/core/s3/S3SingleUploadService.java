package ch.cyberduck.core.s3;

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
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.transfer.TransferStatus;

import org.jets3t.service.model.StorageObject;

import java.util.EnumSet;

public class S3SingleUploadService extends HttpUploadFeature<StorageObject> {

    private final S3ObjectLockService objectlock;
    private final PathContainerService containerService;

    public S3SingleUploadService(S3Session session) {
        this.objectlock = new S3ObjectLockService(session);
        this.containerService = new S3PathContainerService(session.getHost());
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return ChecksumComputeFactory.get(HashAlgorithm.md5);
    }

    @Override
    public EnumSet<Flags> features(final Path file, final TransferStatus status) throws BackgroundException {
        if(objectlock.isObjectLockEnabled(containerService.getContainer(file))) {
            return EnumSet.of(Flags.checksumRequired, Flags.checksumResponse);
        }
        return EnumSet.of(Flags.checksumOptional, Flags.checksumResponse);
    }
}
