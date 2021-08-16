package ch.cyberduck.core.brick;/*
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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

public class BrickThresholdUploadFeature implements Upload<Void> {

    private final BrickSession session;

    private Write<Void> writer;

    public BrickThresholdUploadFeature(final BrickSession session) {
        this.session = session;
    }

    @Override
    public Void upload(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        if(status.getLength() > 0) {
            return new BrickUploadFeature(session, new BrickWriteFeature(session)).withWriter(writer).upload(file, local, throttle, listener, status, callback);
        }
        else {
            new BrickTouchFeature(session).touch(file, status);
            return null;
        }
    }

    @Override
    public Write.Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return writer.append(file, status);
    }

    @Override
    public Upload<Void> withWriter(final Write<Void> writer) {
        this.writer = writer;
        return this;
    }
}
