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

import java.io.InputStream;

public class SpectraReadFeature extends S3ReadFeature {

    private final SpectraBulkService bulk;

    public SpectraReadFeature(final SpectraSession session) {
        super(session);
        this.bulk = new SpectraBulkService(session);
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
        bulk.query(Transfer.Type.download, file, status);
        return super.read(file, status);
    }
}
