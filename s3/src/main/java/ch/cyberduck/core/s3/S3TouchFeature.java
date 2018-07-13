package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.DefaultStreamCloser;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;

public class S3TouchFeature implements Touch<StorageObject> {

    private final S3Session session;
    private Write<StorageObject> writer;

    public S3TouchFeature(final S3Session session) {
        this.session = session;
        this.writer = new S3WriteFeature(session, new S3DisabledMultipartService());
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        if(Checksum.NONE == status.getChecksum()) {
            status.setChecksum(writer.checksum(file).compute(new NullInputStream(0L), status));
        }
        status.setLength(0L);
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
    public Touch<StorageObject> withWriter(final Write<StorageObject> writer) {
        this.writer = writer;
        return this;
    }
}
