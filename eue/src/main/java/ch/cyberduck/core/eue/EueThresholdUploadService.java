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
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.EnumSet;

public class EueThresholdUploadService implements Upload<EueWriteFeature.Chunk> {

    private final EueSession session;
    private final Long threshold;
    private final EueResourceIdProvider fileid;

    public EueThresholdUploadService(final EueSession session, final EueResourceIdProvider fileid) {
        this(session, fileid, HostPreferencesFactory.get(session.getHost()).getLong("eue.upload.multipart.threshold"));
    }

    public EueThresholdUploadService(final EueSession session, final EueResourceIdProvider fileid, final Long threshold) {
        this.session = session;
        this.threshold = threshold;
        this.fileid = fileid;
    }

    @Override
    public EueWriteFeature.Chunk upload(final Write<EueWriteFeature.Chunk> write, final Path file, Local local, final BandwidthThrottle throttle, final ProgressListener progress, final StreamListener listener,
                                        final TransferStatus status, final ConnectionCallback prompt) throws BackgroundException {
        if(status.getLength() >= threshold) {
            // Only allow concurrent write of chunks when not uploading to vault. Write with default feature multiple 4MB chunks in parallel
            return new EueLargeUploadService(session, fileid).upload(write, file, local, throttle, progress, listener, status, prompt);
        }
        // Write single chunk smaller than threshold
        return new EueSingleUploadService(session, fileid).upload(write, file, local, throttle, progress, listener, status, prompt);
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        if(status.getLength() >= threshold) {

            return new EueLargeUploadService(session, fileid).checksum(file, status);
        }
        return new EueSingleUploadService(session, fileid).checksum(file, status);
    }

    @Override
    public EnumSet<Flags> features(final Path file, final TransferStatus status) {
        if(status.getLength() >= threshold) {
            return new EueLargeUploadService(session, fileid).features(file, status);
        }
        return new EueSingleUploadService(session, fileid).features(file, status);
    }
}
