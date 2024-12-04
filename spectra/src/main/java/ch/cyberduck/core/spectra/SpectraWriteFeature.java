/*
 * Copyright (c) 2015-2016 Spectra Logic Corporation. All rights reserved.
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

package ch.cyberduck.core.spectra;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3PathContainerService;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.jets3t.service.model.S3Object;

public class SpectraWriteFeature extends S3WriteFeature {

    private final PathContainerService containerService;

    public SpectraWriteFeature(final SpectraSession session) {
        super(session, new S3AccessControlListFeature(session));
        this.containerService = new S3PathContainerService(session.getHost());
    }

    /**
     * Add default metadata. Do not add checksum as object metadata must remain constant for all chunks.
     */
    protected S3Object getDetails(final Path file, final TransferStatus status) {
        final S3Object object = new S3Object(containerService.getKey(file));
        final String mime = status.getMime();
        if(StringUtils.isNotBlank(mime)) {
            object.setContentType(mime);
        }
        final Checksum checksum = status.getChecksum();
        if(Checksum.NONE != checksum) {
            switch(checksum.algorithm) {
                case md5:
                    // Set checksum on our own to avoid jets3t setting AWS metadata for MD5 as metadata must remain
                    // constant for all chunks
                    object.addMetadata("Content-MD5", checksum.base64);
                    break;
            }
        }
        return object;
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return ChecksumComputeFactory.get(HashAlgorithm.md5);
    }
}
