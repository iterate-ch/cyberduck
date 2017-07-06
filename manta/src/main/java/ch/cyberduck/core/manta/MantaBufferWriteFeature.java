package ch.cyberduck.core.manta;

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
import ch.cyberduck.core.io.FileBufferSegmentingOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;

import java.io.IOException;

/**
 * TODO: not sure how what triggers usage of this class
 */
public class MantaBufferWriteFeature extends MantaWriteFeature implements MultipartWrite<Void> {

    MantaBufferWriteFeature(final MantaSession session) {
        super(session);
    }

    @Override
    public HttpResponseOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        return new HttpResponseOutputStream<Void>(new FileBufferSegmentingOutputStream(status.getLength()) {
            @Override
            protected void copy(final Buffer buffer) throws IOException {
                try {
                    final HttpResponseOutputStream<Void> proxy = MantaBufferWriteFeature.super.write(file,
                            new TransferStatus(status).length(buffer.length()), callback);
                    IOUtils.copy(new BufferInputStream(buffer), proxy);
                    // Re-use buffer
                    buffer.truncate(0L);
                    proxy.close();
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
