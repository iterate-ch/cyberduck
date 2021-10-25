package ch.cyberduck.core.eue;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

public class EueThresholdUploadService implements Upload<EueWriteFeature.Chunk> {

    private final EueSession session;
    private final Long threshold;
    private final EueResourceIdProvider fileid;

    private Write<EueWriteFeature.Chunk> writer;

    public EueThresholdUploadService(final EueSession session, final EueResourceIdProvider fileid) {
        this(session, fileid, new HostPreferences(session.getHost()).getLong("eue.upload.multipart.threshold"));
    }

    public EueThresholdUploadService(final EueSession session, final EueResourceIdProvider fileid, final Long threshold) {
        this.session = session;
        this.threshold = threshold;
        this.fileid = fileid;
        this.writer = new EueWriteFeature(session, fileid);
    }

    @Override
    public Write.Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return writer.append(file, status);
    }

    @Override
    public EueWriteFeature.Chunk upload(final Path file, Local local, final BandwidthThrottle throttle, final StreamListener listener,
                                        final TransferStatus status, final ConnectionCallback prompt) throws BackgroundException {
        if(status.getLength() >= threshold) {
            return new EueSequentialLargeUploadService(session, fileid, new EueMultipartWriteFeature(session, fileid)).upload(file, local, throttle, listener, status, prompt);
        }
        return new EueSingleUploadService(session, fileid, writer).upload(file, local, throttle, listener, status, prompt);
    }

    @Override
    public Upload<EueWriteFeature.Chunk> withWriter(final Write<EueWriteFeature.Chunk> writer) {
        this.writer = writer;
        return this;
    }
}
