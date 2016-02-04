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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.s3.S3ReadFeature;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.io.InputStream;

import com.spectralogic.ds3client.models.bulk.Node;

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
        final Node node = bulk.query(Transfer.Type.download, file, status);
        if(null == node) {
            log.info(String.format("Failed to determine node for file %s", file));
        }
        else {
            log.info(String.format("Determined node %s for file %s", node, file));
        }
        return super.read(file, status);
    }
}
