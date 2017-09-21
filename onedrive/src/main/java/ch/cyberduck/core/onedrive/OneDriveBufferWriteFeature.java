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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.BufferSegmentingOutputStream;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.DisabledChecksumCompute;
import ch.cyberduck.core.io.FileBuffer;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveFile;
import org.nuxeo.onedrive.client.OneDriveUploadSession;

import java.io.IOException;

public class OneDriveBufferWriteFeature implements MultipartWrite<Void> {
    private static final Logger log = Logger.getLogger(OneDriveBufferWriteFeature.class);

    private final OneDriveSession session;
    private final Find finder;
    private final AttributesFinder attributes;

    public OneDriveBufferWriteFeature(final OneDriveSession session) {
        this(session, new DefaultFindFeature(session), new DefaultAttributesFinderFeature(session));
    }

    public OneDriveBufferWriteFeature(final OneDriveSession session, final Find finder, final AttributesFinder attributes) {
        this.session = session;
        this.finder = finder;
        this.attributes = attributes;
    }

    @Override
    public HttpResponseOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        return new HttpResponseOutputStream<Void>(new BufferSegmentingOutputStream(bytes, Long.MAX_VALUE, new FileBuffer()) {
            @Override
            public void close() throws IOException {
                try {
                    this.flush();
                    final byte[] content = bytes.toByteArray();
                    if(0L == content.length) {
                        new OneDriveTouchFeature(session).touch(file, status);
                    }
                    else {
                        final TransferStatus backed = new TransferStatus(status);
                        backed.setLength(content.length);
                        new OneDriveWriteFeature(session).write(file, backed, callback).write(content);
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
            final PathAttributes attributes = this.attributes.withCache(cache).find(file);
            return new Append(false, true).withSize(attributes.getSize()).withChecksum(attributes.getChecksum());
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

    @Override
    public ChecksumCompute checksum(final Path file) {
        return new DisabledChecksumCompute();
    }
}
