package ch.cyberduck.core.b2;

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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.entity.AbstractHttpEntity;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2FileInfoResponse;
import synapticloop.b2.response.B2UploadPartResponse;

public class B2PartWriteFeature extends AbstractHttpWriteFeature<B2UploadPartResponse> implements Write {
    private static final Logger log = Logger.getLogger(B2WriteFeature.class);

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;

    public B2PartWriteFeature(final B2Session session) {
        super(session);
        this.session = session;
    }

    @Override
    public ResponseOutputStream<B2UploadPartResponse> write(final Path file, final TransferStatus status) throws BackgroundException {
        // Submit store call to background thread
        final DelayedHttpEntityCallable<B2UploadPartResponse> command = new DelayedHttpEntityCallable<B2UploadPartResponse>() {
            /**
             * @return The SHA-1 returned by the server for the uploaded object
             */
            @Override
            public B2UploadPartResponse call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    return session.getClient().uploadLargeFilePart(file.attributes().getVersionId(), status.getPart(), entity, status.getChecksum().toString());
                }
                catch(B2ApiException e) {
                    throw new B2ExceptionMappingService(session).map("Upload {0} failed", e, file);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
                }
            }

            @Override
            public long getContentLength() {
                return status.getLength();
            }
        };
        return this.write(file, status, command);
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
    public Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
        Long size = 0L;
        final B2LargeUploadPartService service = new B2LargeUploadPartService(session);
        final List<B2FileInfoResponse> uploads = service.find(file);
        if(uploads.isEmpty()) {
            return Write.notfound;
        }
        final List<B2UploadPartResponse> list = service.list(uploads.iterator().next().getFileId());
        for(B2UploadPartResponse part : list) {
            size += part.getContentLength();
        }
        return new Append(size);
    }
}
