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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.jets3t.service.ServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

public class SpectraReadFeature implements Read {

    private final PathContainerService containerService;
    private final SpectraSession session;
    private final SpectraBulkService bulk;

    public SpectraReadFeature(final SpectraSession session) {
        this(session, new SpectraBulkService(session));
    }

    public SpectraReadFeature(final SpectraSession session, final SpectraBulkService bulk) {
        this.session = session;
        this.bulk = bulk;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        // Make sure file is available in cache
        final List<TransferStatus> chunks = bulk.query(Transfer.Type.download, file, status);
        // Sort chunks by offset
        chunks.sort(Comparator.comparingLong(TransferStatus::getOffset));
        final List<LazyInputStream> streams = new ArrayList<>();
        for(TransferStatus chunk : chunks) {
            final LazyInputStream in = new LazyInputStream(new LazyInputStream.OpenCallback() {
                @Override
                public InputStream open() throws IOException {
                    try {
                        return session.getClient().getObjectImpl(
                            false,
                            containerService.getContainer(file).getName(),
                            containerService.getKey(file),
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            file.attributes().getVersionId(),
                            new HashMap<String, Object>(),
                            chunk.getParameters())
                            .getDataInputStream();
                    }
                    catch(ServiceException e) {
                        throw new IOException(e.getMessage(), e);
                    }
                }
            });
            streams.add(in);
        }
        // Concatenate streams
        return new SequenceInputStream(Collections.enumeration(streams));
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return EnumSet.noneOf(Flags.class);
    }
}
