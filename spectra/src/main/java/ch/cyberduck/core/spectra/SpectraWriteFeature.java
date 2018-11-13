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
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.s3.S3DisabledMultipartService;
import ch.cyberduck.core.s3.S3PathContainerService;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.jets3t.service.model.S3Object;

public class SpectraWriteFeature extends S3WriteFeature {

    private final PathContainerService containerService
        = new S3PathContainerService();

    public SpectraWriteFeature(final SpectraSession session) {
        this(session, new DefaultFindFeature(session), new DefaultAttributesFinderFeature(session));
    }

    public SpectraWriteFeature(final SpectraSession session, final Find finder, final AttributesFinder attributes) {
        super(session, new S3DisabledMultipartService(), finder, attributes);
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
        return object;
    }

    @Override
    public ChecksumCompute checksum(final Path file) {
        return ChecksumComputeFactory.get(HashAlgorithm.crc32);
    }
}
