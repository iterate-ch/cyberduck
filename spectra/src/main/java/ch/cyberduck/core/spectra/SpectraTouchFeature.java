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
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Collections;

public class SpectraTouchFeature implements Touch {

    private final SpectraSession session;

    public SpectraTouchFeature(final SpectraSession session) {
        this.session = session;
    }

    @Override
    public void touch(final Path file, final TransferStatus transferStatus) throws BackgroundException {
        final SpectraBulkService bulk = new SpectraBulkService(session);
        final TransferStatus status = new TransferStatus();
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(file, status.length(0L)));
    }


    @Override
    public boolean isSupported(final Path workdir) {
        // Creating files is only possible inside a bucket.
        return !workdir.isRoot();
    }
}
