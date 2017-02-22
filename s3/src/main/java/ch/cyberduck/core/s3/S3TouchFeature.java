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

import ch.cyberduck.core.MappingMimeTypeService;
import ch.cyberduck.core.MimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.DefaultStreamCloser;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;
import org.jets3t.service.model.StorageObject;

public class S3TouchFeature implements Touch<StorageObject> {

    private final S3Session session;

    private final MimeTypeService mapping
            = new MappingMimeTypeService();

    private Write writer;

    public S3TouchFeature(final S3Session session) {
        this.session = session;
        this.writer = new S3WriteFeature(session, new S3DisabledMultipartService());
    }

    @Override
    public void touch(final Path file, final TransferStatus status) throws BackgroundException {
        status.setMime(mapping.getMime(file.getName()));
        if(Encryption.Algorithm.NONE == status.getEncryption()) {
            final Encryption encryption = session.getFeature(Encryption.class);
            if(encryption != null) {
                status.setEncryption(encryption.getDefault(file));
            }
        }
        if(null == status.getStorageClass()) {
            final Redundancy redundancy = session.getFeature(Redundancy.class);
            if(redundancy != null) {
                status.setStorageClass(redundancy.getDefault());
            }
        }
        if(Checksum.NONE == status.getChecksum()) {
            status.setChecksum(writer.checksum().compute(new NullInputStream(0L), status.length(0L)));
        }
        status.setLength(0L);
        new DefaultStreamCloser().close(writer.write(file, status));
    }

    @Override
    public boolean isSupported(final Path workdir) {
        // Creating files is only possible inside a bucket.
        return !workdir.isRoot();
    }

    @Override
    public Touch<StorageObject> withWriter(final Write writer) {
        this.writer = writer;
        return this;
    }
}
