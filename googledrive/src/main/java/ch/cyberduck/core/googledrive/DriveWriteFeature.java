package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.NullInputStream;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

public class DriveWriteFeature extends AbstractHttpWriteFeature<File> {
    private static final Logger log = Logger.getLogger(DriveWriteFeature.class);

    private DriveSession session;

    public DriveWriteFeature(final DriveSession session) {
        super(session);
        this.session = session;
    }

    @Override
    public Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
        return Write.notfound;
    }

    @Override
    public boolean temporary() {
        return false;
    }

    @Override
    public boolean random() {
        return false;
    }

    @Override
    public ResponseOutputStream<File> write(final Path file, final TransferStatus status) throws BackgroundException {
        final File body = new File();
        body.setName(file.getName());
        body.setMimeType(status.getMime());
        final DelayedHttpEntityCallable<File> command = new DelayedHttpEntityCallable<File>() {
            @Override
            public File call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    final Drive.Files.Create insert = session.getClient().files().create(body,
                            new AbstractInputStreamContent(status.getMime()) {
                                @Override
                                public long getLength() throws IOException {
                                    return status.getLength();
                                }

                                @Override
                                public boolean retrySupported() {
                                    return false;
                                }

                                @Override
                                public InputStream getInputStream() throws IOException {
                                    return new NullInputStream(status.getLength());
                                }

                                @Override
                                public void writeTo(final OutputStream out) throws IOException {
                                    entity.writeTo(out);
                                }
                            });
                    return insert.execute();
                }
                catch(IOException e) {
                    throw new DriveExceptionMappingService().map("Upload failed", e, file);
                }
            }

            @Override
            public long getContentLength() {
                return status.getLength();
            }
        };
        return this.write(file, status, command);
    }
}
