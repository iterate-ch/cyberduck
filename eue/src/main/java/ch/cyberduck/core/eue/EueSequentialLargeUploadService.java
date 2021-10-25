package ch.cyberduck.core.eue;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.security.MessageDigest;

public class EueSequentialLargeUploadService extends HttpUploadFeature<EueWriteFeature.Chunk, MessageDigest> {
    private static final Logger log = Logger.getLogger(EueSequentialLargeUploadService.class);

    public static final String RESOURCE_ID = "resourceId";

    private Write<EueWriteFeature.Chunk> writer;

    public EueSequentialLargeUploadService(final EueSession session, final EueResourceIdProvider fileid, final Write<EueWriteFeature.Chunk> writer) {
        super(writer);
    }

    @Override
    public EueWriteFeature.Chunk upload(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener,
                                        final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        return EueSequentialLargeUploadService.this.upload(file, local, throttle, listener, status, status, status, callback);
    }
}
