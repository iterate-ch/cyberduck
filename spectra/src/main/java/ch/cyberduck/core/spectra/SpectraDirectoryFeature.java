package ch.cyberduck.core.spectra;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.s3.S3DirectoryFeature;
import ch.cyberduck.core.s3.S3PathContainerService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;

public class SpectraDirectoryFeature extends S3DirectoryFeature {

    private final Preferences preferences
            = PreferencesFactory.get();

    private final PathContainerService containerService
            = new S3PathContainerService();

    private final SpectraSession session;

    public SpectraDirectoryFeature(final SpectraSession session) {
        super(session);
        this.session = session;
    }

    public SpectraDirectoryFeature(final SpectraSession session, final SpectraWriteFeature write) {
        super(session, write);
        this.session = session;
    }

    @Override
    public void mkdir(final Path file, final String region, TransferStatus status) throws BackgroundException {
        if(containerService.isContainer(file)) {
            super.mkdir(file, region, status);
        }
        else {
            if(null == status) {
                status = new TransferStatus();
            }
            if(preferences.getBoolean("spectra.upload.crc32")) {
                status.setChecksum(session.getFeature(ChecksumCompute.class, ChecksumComputeFactory.get(HashAlgorithm.crc32))
                        .compute(new NullInputStream(0L), status));
            }
            if(preferences.getBoolean("spectra.upload.md5")) {
                status.setChecksum(session.getFeature(ChecksumCompute.class, ChecksumComputeFactory.get(HashAlgorithm.md5))
                        .compute(new NullInputStream(0L), status));
            }
            super.mkdir(file, region, status);
        }
    }
}
