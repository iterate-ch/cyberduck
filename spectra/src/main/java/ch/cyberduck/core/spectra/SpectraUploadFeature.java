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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.jets3t.service.model.StorageObject;

import java.security.MessageDigest;
import java.util.EnumSet;
import java.util.List;

public class SpectraUploadFeature extends HttpUploadFeature<StorageObject> {

    private final SpectraBulkService bulk;

    public SpectraUploadFeature(final SpectraBulkService bulk) {
        this.bulk = bulk;
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return ChecksumComputeFactory.get(HashAlgorithm.md5);
    }

    @Override
    public EnumSet<Flags> features(final Path file, final TransferStatus status) {
        return EnumSet.of(Flags.checksumOptional);
    }

    @Override
    public StorageObject upload(final Write<StorageObject> write, final Path file, final Local local, final BandwidthThrottle throttle,
                                final ProgressListener progress, final StreamListener listener, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        // Make sure file is available in cache
        final List<TransferStatus> chunks = bulk.query(Transfer.Type.upload, file, status);
        for(TransferStatus chunk : chunks) {
            // The client-side checksum is passed to the BlackPearl gateway by supplying the applicable CRC HTTP header.
            // If this is done, the BlackPearl gateway verifies that the data received matches the checksum provided.
            // End-to-end data protection requires that the client provide the CRC when uploading the object and then
            // verify the CRC after downloading the object at a later time (see Get Object). The BlackPearl gateway also
            // verifies the CRC when reading from physical data stores so the gateway can identify problems before
            // transmitting data to the client.
            chunk.setChecksum(status.transform(this.checksum(file, status)).compute(local.getInputStream(), chunk));
            super.upload(write, file, local, throttle, progress, listener, chunk, callback);
        }
        return null;
    }

    /**
     * The ETag returned for a chunk is the checksum of the chunk and not of the whole file
     */
    @Override
    protected Checksum checksum(final MessageDigest digest) {
        return Checksum.NONE;
    }
}
