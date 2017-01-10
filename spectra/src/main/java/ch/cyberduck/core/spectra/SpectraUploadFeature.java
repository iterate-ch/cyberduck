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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.jets3t.service.model.StorageObject;

import java.security.MessageDigest;
import java.util.List;

public class SpectraUploadFeature extends HttpUploadFeature<StorageObject, MessageDigest> {

    private final Write<StorageObject> writer;
    private final SpectraBulkService bulk;

    public SpectraUploadFeature(final SpectraSession session, final Write<StorageObject> writer, final SpectraBulkService bulk) {
        super(writer);
        this.writer = writer;
        this.bulk = bulk;
    }

    @Override
    public StorageObject upload(final Path file, final Local local, final BandwidthThrottle throttle,
                                final StreamListener listener, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        // The client-side checksum is passed to the BlackPearl gateway by supplying the applicable CRC HTTP header.
        // If this is done, the BlackPearl gateway verifies that the data received matches the checksum provided.
        // End-to-end data protection requires that the client provide the CRC when uploading the object and then
        // verify the CRC after downloading the object at a later time (see Get Object). The BlackPearl gateway also
        // verifies the CRC when reading from physical data stores so the gateway can identify problems before
        // transmitting data to the client.
        status.setChecksum(writer.checksum().compute(file, local.getInputStream(), status));
        // Make sure file is available in cache
        final List<TransferStatus> chunks = bulk.query(Transfer.Type.upload, file, status);
        StorageObject stored = null;
        for(TransferStatus chunk : chunks) {
            stored = super.upload(file, local, throttle, listener, chunk, callback);
        }
        return stored;
    }
}
