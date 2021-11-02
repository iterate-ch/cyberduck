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
import ch.cyberduck.core.box.io.swagger.client.model.Files;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.transfer.TransferStatus;

public class BoxThresholdUploadService implements Upload<Files> {

    private final BoxSession session;
    private final BoxFileidProvider fileid;

    private Write<Files> writer;

    public BoxThresholdUploadService(final BoxSession session, final BoxFileidProvider fileid) {
        super();
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Files upload(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener,
                        final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        if(this.threshold(status.getLength())) {
            final BoxLargeUploadService.BoxUploadResponse boxUploadResponse = new BoxLargeUploadService(session, fileid, new BoxChunkedWriteFeature(session)).upload(file, local, throttle, listener, status, callback);
            return boxUploadResponse.getFiles();
        }
        else {
            return new DefaultUploadFeature<>(writer).upload(file, local, throttle, listener, status, callback);
        }
    }

    @Override
    public Write.Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return new Write.Append(false).withStatus(status);
    }

    @Override
    public Upload<Files> withWriter(final Write<Files> writer) {
        this.writer = writer;
        return this;
    }

    protected boolean threshold(final Long length) {
        return length >= new HostPreferences(session.getHost()).getLong("box.upload.multipart.threshold");
    }
}
