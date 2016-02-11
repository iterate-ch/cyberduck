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
import ch.cyberduck.core.exception.RedirectException;
import ch.cyberduck.core.s3.S3ReadFeature;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.io.InputStream;

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
        try {
            // Make sure file is available in cache
            bulk.query(Transfer.Type.download, file, status);
        }
        catch(RedirectException e) {
            log.warn(String.format("Node %s returned for is not equal connected host %s.", e.getTarget(), session.getHost()));
        }
        return super.read(file, status);
    }
}
