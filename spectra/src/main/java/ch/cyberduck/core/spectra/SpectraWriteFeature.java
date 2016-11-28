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

import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.s3.S3DisabledMultipartService;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.jets3t.service.model.S3Object;

public class SpectraWriteFeature extends S3WriteFeature {

    public SpectraWriteFeature(final SpectraSession session) {
        this(session, new DefaultFindFeature(session), new DefaultAttributesFinderFeature(session));
    }

    public SpectraWriteFeature(final SpectraSession session, final Find finder, final AttributesFinder attributes) {
        super(session, new S3DisabledMultipartService(), finder, attributes);
    }

    /**
     * Add default metadata
     */
    protected S3Object getDetails(final String key, final TransferStatus status) {
        final S3Object object = super.getDetails(key, status);
        final Checksum checksum = status.getChecksum();
        if(null != checksum) {
            switch(checksum.algorithm) {
                case crc32:
                    object.addMetadata("Content-CRC32", checksum.hash);
                    break;
            }
        }
        return object;
    }
}
