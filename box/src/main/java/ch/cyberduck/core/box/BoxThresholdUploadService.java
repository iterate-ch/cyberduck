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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.box.io.swagger.client.model.File;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultRegistry;

public class BoxThresholdUploadService implements Upload<File> {

    private final BoxSession session;
    private final BoxFileidProvider fileid;
    private final VaultRegistry registry;

    private Write<File> writer;

    public BoxThresholdUploadService(final BoxSession session, final BoxFileidProvider fileid, final VaultRegistry registry) {
        this.session = session;
        this.fileid = fileid;
        this.registry = registry;
        this.writer = new BoxWriteFeature(session, fileid);
    }

    @Override
    public File upload(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener,
                       final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        if(this.threshold(status.getLength())) {
            if(Vault.DISABLED == registry.find(session, file)) {
                return new BoxLargeUploadService(session, fileid, new BoxChunkedWriteFeature(session, fileid)).upload(file, local, throttle, listener, status, callback);
            }
            // Cannot comply with chunk size requirement from server
        }
        return new BoxSmallUploadService(session, fileid, writer).upload(file, local, throttle, listener, status, callback);
    }

    @Override
    public Upload<File> withWriter(final Write<File> writer) {
        this.writer = writer;
        return this;
    }

    protected boolean threshold(final Long length) {
        return length >= new HostPreferences(session.getHost()).getLong("box.upload.multipart.threshold");
    }
}
