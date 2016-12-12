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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.StreamProgress;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;
import org.jets3t.service.model.StorageObject;

import java.security.MessageDigest;
import java.util.List;

public class SpectraUploadFeature extends HttpUploadFeature<StorageObject, MessageDigest> {
    private static final Logger log = Logger.getLogger(SpectraUploadFeature.class);

    private final Preferences preferences = PreferencesFactory.get();

    private final SpectraSession session;
    private final SpectraBulkService bulk;

    public SpectraUploadFeature(final SpectraSession session, final SpectraWriteFeature writer) {
        this(session, writer, new SpectraBulkService(session));
    }

    public SpectraUploadFeature(final SpectraSession session, final SpectraWriteFeature writer, final SpectraBulkService bulk) {
        super(session, writer);
        this.session = session;
        this.bulk = bulk;
    }

    @Override
    public StorageObject upload(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener,
                                final TransferStatus status, final StreamCancelation cancel, final StreamProgress progress) throws BackgroundException {
        // The client-side checksum is passed to the BlackPearl gateway by supplying the applicable CRC HTTP header.
        // If this is done, the BlackPearl gateway verifies that the data received matches the checksum provided.
        // End-to-end data protection requires that the client provide the CRC when uploading the object and then
        // verify the CRC after downloading the object at a later time (see Get Object). The BlackPearl gateway also
        // verifies the CRC when reading from physical data stores so the gateway can identify problems before
        // transmitting data to the client.
        if(preferences.getBoolean("spectra.upload.crc32")) {
            status.setChecksum(session.getFeature(ChecksumCompute.class, ChecksumComputeFactory.get(HashAlgorithm.crc32))
                    .compute(local.getInputStream(), status)
            );
        }
        if(preferences.getBoolean("spectra.upload.md5")) {
            status.setChecksum(session.getFeature(ChecksumCompute.class, ChecksumComputeFactory.get(HashAlgorithm.md5))
                    .compute(local.getInputStream(), status)
            );
        }
        // Make sure file is available in cache
        final List<TransferStatus> chunks = bulk.query(Transfer.Type.upload, file, status);
        StorageObject stored = null;
        for(TransferStatus chunk : chunks) {
            stored = super.upload(file, local, throttle, listener, chunk, cancel, progress);
        }
        return stored;
    }
}
