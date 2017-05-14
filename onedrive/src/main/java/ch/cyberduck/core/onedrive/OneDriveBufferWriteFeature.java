package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.FileBufferSegmentingOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

public class OneDriveBufferWriteFeature extends OneDriveWriteFeature implements MultipartWrite<Void> {

    public OneDriveBufferWriteFeature(final OneDriveSession session) {
        super(session);
    }

    public OneDriveBufferWriteFeature(final OneDriveSession session, final Find finder, final AttributesFinder attributes) {
        super(session, finder, attributes);
    }

    @Override
    public HttpResponseOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final HttpResponseOutputStream<Void> proxy = super.write(file, status, callback);
        return new HttpResponseOutputStream<Void>(new FileBufferSegmentingOutputStream(proxy, status.getLength())) {
            @Override
            public Void getStatus() throws BackgroundException {
                return proxy.getStatus();
            }
        };
    }
}
