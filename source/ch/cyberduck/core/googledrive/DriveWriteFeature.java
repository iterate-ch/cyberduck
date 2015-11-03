package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.entity.AbstractHttpEntity;

import java.io.IOException;

import com.google.api.client.http.InputStreamContent;
import com.google.api.services.drive.model.File;

/**
 * @version $Id:$
 */
public class DriveWriteFeature extends AbstractHttpWriteFeature<File> {

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
        body.setTitle(file.getName());
        body.setMimeType(status.getMime());
        final DelayedHttpEntityCallable<File> command = new DelayedHttpEntityCallable<File>() {
            @Override
            public File call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    return session.getClient().files().insert(body,
                            new InputStreamContent(status.getMime(), entity.getContent())).execute();
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
