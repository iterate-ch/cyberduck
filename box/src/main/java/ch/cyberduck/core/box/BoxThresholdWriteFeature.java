package ch.cyberduck.core.box;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.box.io.swagger.client.model.File;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

public class BoxThresholdWriteFeature implements Write<File> {

    private final BoxSession session;
    private final BoxFileidProvider fileid;

    public BoxThresholdWriteFeature(final BoxSession session, final BoxFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public StatusOutputStream<File> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        if(status.isSegment()) {
            return new BoxChunkedWriteFeature(session, fileid).write(file, status, callback);
        }
        return new BoxWriteFeature(session, fileid).write(file, status, callback);
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        if(status.isSegment()) {
            return new BoxChunkedWriteFeature(session, fileid).checksum(file, status);
        }
        return new BoxWriteFeature(session, fileid).checksum(file, status);
    }

    @Override
    public Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return new Append(false).withStatus(status);
    }

    @Override
    public boolean timestamp() {
        return true;
    }
}
