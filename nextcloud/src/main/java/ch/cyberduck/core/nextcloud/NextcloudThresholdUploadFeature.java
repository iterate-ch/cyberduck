package ch.cyberduck.core.nextcloud;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.EnumSet;

/**
 * Delegates to chunked upload for files of at least the threshold length and to a single request upload otherwise.
 */
public class NextcloudThresholdUploadFeature implements Upload<Void> {

    private final long threshold;
    private final Upload<Void> single;
    private final Upload<Void> chunked;

    public NextcloudThresholdUploadFeature(final DAVSession session) {
        this.threshold = HostPreferencesFactory.get(session.getHost()).getLong("nextcloud.upload.multipart.threshold");
        this.single = new HttpUploadFeature<>(EnumSet.of(Flags.checksumOptional), HashAlgorithm.sha1);
        this.chunked = new NextcloudChunkedUploadFeature(session);
    }

    @Override
    public Void upload(final Write<Void> write, final Path file, final Local local, final BandwidthThrottle throttle,
                       final ProgressListener progress, final StreamListener listener, final TransferStatus status,
                       final ConnectionCallback callback) throws BackgroundException {
        return this.delegate(status).upload(write, file, local, throttle, progress, listener, status, callback);
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return this.delegate(status).checksum(file, status);
    }

    @Override
    public EnumSet<Flags> features(final Path file, final TransferStatus status) throws BackgroundException {
        return this.delegate(status).features(file, status);
    }

    private Upload<Void> delegate(final TransferStatus status) {
        if(status.getLength() < threshold) {
            return single;
        }
        return chunked;
    }
}
