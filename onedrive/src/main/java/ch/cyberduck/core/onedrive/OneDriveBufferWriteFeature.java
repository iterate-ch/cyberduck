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
import ch.cyberduck.core.io.Buffer;
import ch.cyberduck.core.io.BufferInputStream;
import ch.cyberduck.core.io.BufferSegmentingOutputStream;
import ch.cyberduck.core.io.FileBuffer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

import java.io.IOException;

public class OneDriveBufferWriteFeature extends OneDriveWriteFeature implements MultipartWrite<Void> {

    private final OneDriveSession session;

    public OneDriveBufferWriteFeature(final OneDriveSession session) {
        super(session);
        this.session = session;
    }

    public OneDriveBufferWriteFeature(final OneDriveSession session, final Find finder, final AttributesFinder attributes) {
        super(session, finder, attributes);
        this.session = session;
    }

    @Override
    public HttpResponseOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final Buffer buffer = new FileBuffer();
        return new HttpResponseOutputStream<Void>(new BufferSegmentingOutputStream(new NullOutputStream(), Long.MAX_VALUE, buffer) {
            @Override
            public void flush() throws IOException {
                //
            }

            @Override
            public void close() throws IOException {
                try {
                    if(0L == buffer.length()) {
                        new OneDriveTouchFeature(session).touch(file, status);
                    }
                    else {
                        final HttpResponseOutputStream<Void> proxy = OneDriveBufferWriteFeature.super.write(file, status.length(buffer.length()), callback);
                        IOUtils.copy(new BufferInputStream(buffer), proxy);
                        // Re-use buffer
                        buffer.truncate(0L);
                    }
                }
                catch(BackgroundException e) {
                    throw new IOException(e);
                }
            }
        }) {
            @Override
            public Void getStatus() throws BackgroundException {
                return null;
            }
        };
    }
}
