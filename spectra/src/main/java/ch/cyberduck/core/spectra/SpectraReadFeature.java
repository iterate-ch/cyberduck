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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.s3.S3ReadFeature;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpectraReadFeature extends S3ReadFeature {
    private static final Logger log = Logger.getLogger(SpectraReadFeature.class);

    private final SpectraSession session;

    public SpectraReadFeature(final SpectraSession session) {
        super(session);
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
        final SpectraBulkService bulk = new SpectraBulkService(session);
        // Make sure file is available in cache
        final List<TransferStatus> chunks = bulk.query(Transfer.Type.download, file, status);
        final List<InputStream> streams = new ArrayList<InputStream>();
        for(TransferStatus chunk : chunks) {
            streams.add(super.read(file, chunk));
        }
        // Concatenate streams
        return new SequenceInputStream(Collections.enumeration(streams));
    }
}
