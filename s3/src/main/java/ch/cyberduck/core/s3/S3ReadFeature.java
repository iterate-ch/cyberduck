package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3Object;

import java.io.InputStream;

public class S3ReadFeature implements Read {
    private static final Logger log = Logger.getLogger(S3ReadFeature.class);

    private final PathContainerService containerService
            = new S3PathContainerService();

    private final S3Session session;
    private final Versioning versioning;


    public S3ReadFeature(final S3Session session) {
        this(session, session.getFeature(Versioning.class));
    }

    public S3ReadFeature(final S3Session session, final Versioning versioning) {
        this.session = session;
        this.versioning = versioning;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final S3Object object;
            final HttpRange range = HttpRange.withStatus(status);
            final RequestEntityRestStorageService client = session.getClient();
            if(versioning != null && versioning.getConfiguration(containerService.getContainer(file)).isEnabled()) {
                object = client.getVersionedObject(
                        file.attributes().getVersionId(),
                        containerService.getContainer(file).getName(), containerService.getKey(file),
                        null, // ifModifiedSince
                        null, // ifUnmodifiedSince
                        null, // ifMatch
                        null, // ifNoneMatch
                        status.isAppend() ? range.getStart() : null,
                        status.isAppend() ? (range.getEnd() == -1 ? null : range.getEnd()) : null);
            }
            else {
                object = client.getObject(
                        containerService.getContainer(file).getName(),
                        containerService.getKey(file),
                        null, // ifModifiedSince
                        null, // ifUnmodifiedSince
                        null, // ifMatch
                        null, // ifNoneMatch
                        status.isAppend() ? range.getStart() : null,
                        status.isAppend() ? (range.getEnd() == -1 ? null : range.getEnd()) : null);
            }
            if(log.isDebugEnabled()) {
                log.debug(String.format("Reading stream with content length %d", object.getContentLength()));
            }
            return object.getDataInputStream();
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    @Override
    public boolean offset(final Path file) {
        return true;
    }
}
