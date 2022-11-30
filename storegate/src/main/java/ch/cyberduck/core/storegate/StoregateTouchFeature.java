package ch.cyberduck.core.storegate;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;

public class StoregateTouchFeature implements Touch<FileMetadata> {

    private Write<FileMetadata> writer;

    public StoregateTouchFeature(final StoregateSession session, final StoregateIdProvider fileid) {
        this.writer = new StoregateWriteFeature(session, fileid);
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final StatusOutputStream<FileMetadata> out = writer.write(file, status, new DisabledConnectionCallback());
            out.close();
            return file.withAttributes(status.getResponse());
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create {0}", e, file);
        }
    }

    @Override
    public Touch<FileMetadata> withWriter(final Write<FileMetadata> writer) {
        this.writer = writer;
        return this;
    }

    @Override
    public boolean isSupported(final Path workdir, final String filename) {
        return !workdir.isRoot();
    }
}
