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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.transfer.TransferStatus;

import org.jets3t.service.model.StorageObject;

import java.security.MessageDigest;

public class S3SingleUploadService extends HttpUploadFeature<StorageObject> {

    public S3SingleUploadService(final S3Session session) {
        super(new S3AttributesAdapter(session.getHost()));
    }

    /**
     * The checksum of the write feature is the SHA-256 for the request signature. Verify with the MD5 returned as ETag.
     */
    @Override
    public MessageDigest digest(final Write<StorageObject> write, final Path file, final TransferStatus status) throws BackgroundException {
        return ChecksumComputeFactory.get(HashAlgorithm.md5).digest();
    }
}
