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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.s3.S3PathContainerService;
import ch.cyberduck.core.s3.ServiceExceptionMappingService;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SpectraReadFeature implements Read {
    private static final Logger log = Logger.getLogger(SpectraReadFeature.class);

    private final PathContainerService containerService
            = new S3PathContainerService();

    private final SpectraSession session;

    public SpectraReadFeature(final SpectraSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
        final SpectraBulkService bulk = new SpectraBulkService(session);
        // Make sure file is available in cache
        final List<TransferStatus> chunks = bulk.query(Transfer.Type.download, file, status);
        final List<InputStream> streams = new ArrayList<InputStream>();
        try {
            for(TransferStatus chunk : chunks) {
                final InputStream in = session.getClient().getObjectImpl(
                        false,
                        containerService.getContainer(file).getName(),
                        containerService.getKey(file),
                        null, // ifModifiedSince
                        null, // ifUnmodifiedSince
                        null, // ifMatch
                        null, // ifNoneMatch
                        null,
                        null,
                        null,
                        new HashMap<String, Object>(),
                        chunk.getParameters())
                        .getDataInputStream();
                streams.add(in);
            }
            // Concatenate streams
            return new SequenceInputStream(Collections.enumeration(streams));
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    @Override
    public boolean offset(final Path file) {
        return false;
    }
}
