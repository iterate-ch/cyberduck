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
import ch.cyberduck.core.MappingMimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.entity.AbstractHttpEntity;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2FileResponse;

public class B2WriteFeature extends AbstractHttpWriteFeature<B2FileResponse> implements Write {
    private static final Logger log = Logger.getLogger(B2WriteFeature.class);

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;

    public B2WriteFeature(final B2Session session) {
        super(session);
        this.session = session;
    }

    public B2WriteFeature(final B2Session session, final Find finder, final Attributes attributes) {
        super(finder, attributes);
        this.session = session;
    }

    @Override
    public ResponseOutputStream<B2FileResponse> write(final Path file, final TransferStatus status) throws BackgroundException {
        // Submit store call to background thread
        final DelayedHttpEntityCallable<B2FileResponse> command = new DelayedHttpEntityCallable<B2FileResponse>() {
            /**
             * @return The SHA-1 returned by the server for the uploaded object
             */
            @Override
            public B2FileResponse call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    return session.getClient().uploadFile(
                            new B2FileidProvider(session).getFileid(containerService.getContainer(file)),
                            containerService.getKey(file),
                            entity, status.getChecksum().toString(),
                            new MappingMimeTypeService().getMime(file.getName()), Collections.emptyMap());
                }
                catch(B2ApiException e) {
                    throw new B2ExceptionMappingService().map("Upload {0} failed", e, file);
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
        return true;
    }

    @Override
    public boolean random() {
        return true;
    }
}
