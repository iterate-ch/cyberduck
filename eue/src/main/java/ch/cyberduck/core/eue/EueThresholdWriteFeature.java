package ch.cyberduck.core.eue;

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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

public class EueThresholdWriteFeature implements Write<EueWriteFeature.Chunk> {

    private final EueSession session;
    private final EueResourceIdProvider fileid;
    private final Long threshold;

    public EueThresholdWriteFeature(final EueSession session, final EueResourceIdProvider fileid) {
        this(session, fileid, new HostPreferences(session.getHost()).getLong("eue.upload.multipart.threshold"));
    }

    public EueThresholdWriteFeature(final EueSession session, final EueResourceIdProvider fileid, final Long threshold) {
        this.session = session;
        this.fileid = fileid;
        this.threshold = threshold;
    }

    @Override
    public StatusOutputStream<EueWriteFeature.Chunk> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        if(status.getLength() >= threshold) {
            return new EueMultipartWriteFeature(session, fileid).write(file, status, callback);
        }
        return new EueWriteFeature(session, fileid).write(file, status, callback);
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return new SHA256ChecksumCompute();
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
