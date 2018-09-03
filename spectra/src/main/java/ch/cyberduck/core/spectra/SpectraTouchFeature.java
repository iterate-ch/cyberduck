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

import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.DefaultStreamCloser;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.s3.S3AttributesFinderFeature;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;

import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;

import java.util.Collections;

public class SpectraTouchFeature implements Touch {

    private final SpectraSession session;
    private final S3WriteFeature writer;

    public SpectraTouchFeature(final SpectraSession session) {
        this.session = session;
        this.writer = new SpectraWriteFeature(session);
    }

    @Override
    public Path touch(final Path file, final TransferStatus transferStatus) throws BackgroundException {
        final SpectraBulkService bulk = new SpectraBulkService(session);
        final TransferStatus status = new TransferStatus();
        bulk.pre(Transfer.Type.upload, Collections.singletonMap(new TransferItem(file), status.length(0L)), new DisabledConnectionCallback());
        final StatusOutputStream<StorageObject> out = writer.write(file, status, new DisabledConnectionCallback());
        new DefaultStreamCloser().close(out);
        final S3Object metadata = (S3Object) out.getStatus();
        return new Path(file.getParent(), file.getName(), file.getType(),
            new S3AttributesFinderFeature(session).toAttributes(metadata));
    }


    @Override
    public boolean isSupported(final Path workdir) {
        // Creating files is only possible inside a bucket.
        return !workdir.isRoot();
    }

    @Override
    public SpectraTouchFeature withWriter(final Write writer) {
        return this;
    }
}
