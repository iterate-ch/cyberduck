package ch.cyberduck.core.onedrive.features;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.BufferInputStream;
import ch.cyberduck.core.io.BufferOutputStream;
import ch.cyberduck.core.io.FileBuffer;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

public class GraphBufferWriteFeature implements MultipartWrite<Void> {
    private static final Logger log = Logger.getLogger(GraphBufferWriteFeature.class);

    private final GraphSession session;
    private final Find finder;
    private final AttributesFinder attributes;

    public GraphBufferWriteFeature(final GraphSession session) {
        this(session, new DefaultFindFeature(session), new DefaultAttributesFinderFeature(session));
    }

    public GraphBufferWriteFeature(final GraphSession session, final Find finder, final AttributesFinder attributes) {
        this.session = session;
        this.finder = finder;
        this.attributes = attributes;
    }

    @Override
    public HttpResponseOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final FileBuffer buffer = new FileBuffer();
        return new HttpResponseOutputStream<Void>(new BufferOutputStream(buffer) {
            @Override
            public void flush() throws IOException {
                //
            }

            @Override
            public void close() throws IOException {
                try {
                    // Reset offset in transfer status because data was already streamed
                    // through StreamCopier when writing to buffer
                    final TransferStatus range = new TransferStatus(status).length(buffer.length()).append(false);
                    if(0L == buffer.length()) {
                        new GraphTouchFeature(session).touch(file, new TransferStatus());
                    }
                    else {
                        final HttpResponseOutputStream<Void> out = new GraphWriteFeature(session).write(file,
                            range, callback);
                        IOUtils.copy(new BufferInputStream(buffer), out);
                        out.close();
                        log.info(String.format("Completed upload for %s with status %s", file, range));
                    }
                    super.close();
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

    @Override
    public Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        if(finder.withCache(cache).find(file)) {
            final PathAttributes attr = attributes.withCache(cache).find(file);
            return new Append(false, true).withSize(attr.getSize()).withChecksum(attr.getChecksum());
        }
        return Write.notfound;
    }

    @Override
    public boolean temporary() {
        return true;
    }

    @Override
    public boolean random() {
        return false;
    }
}
