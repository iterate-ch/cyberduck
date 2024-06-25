package ch.cyberduck.core.storegate;

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
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.storegate.io.swagger.client.model.File;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.EnumSet;

public class StoregateThresholdWriteFeature implements Write<File> {

    private final StoregateSession session;
    private final StoregateIdProvider fileid;
    private final Long threshold;

    public StoregateThresholdWriteFeature(final StoregateSession session, final StoregateIdProvider fileid) {
        this(session, fileid, new HostPreferences(session.getHost()).getLong("storegate.upload.multipart.threshold"));
    }

    public StoregateThresholdWriteFeature(final StoregateSession session, final StoregateIdProvider fileid, final Long threshold) {
        this.session = session;
        this.fileid = fileid;
        this.threshold = threshold;
    }

    @Override
    public StatusOutputStream<File> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        if(status.getLength() >= threshold) {
            return new StoregateMultipartWriteFeature(session, fileid).write(file, status, callback);
        }
        return new StoregateWriteFeature(session, fileid).write(file, status, callback);
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return EnumSet.of(Flags.timestamp);
    }
}
