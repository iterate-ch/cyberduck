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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpHeaders;
import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3Object;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public class S3ReadFeature implements Read {
    private static final Logger log = Logger.getLogger(S3ReadFeature.class);

    private PathContainerService containerService
            = new S3PathContainerService();

    private S3Session session;

    private Map<Path, VersioningConfiguration> versioning
            = new HashMap<Path, VersioningConfiguration>();

    public S3ReadFeature(final S3Session session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final S3Object object;
            if(session.getFeature(Versioning.class) != null
                    && session.getFeature(Versioning.class).withCache(versioning).getConfiguration(containerService.getContainer(file)).isEnabled()) {
                object = session.getClient().getVersionedObject(
                        file.attributes().getVersionId(),
                        containerService.getContainer(file).getName(), containerService.getKey(file),
                        null, // ifModifiedSince
                        null, // ifUnmodifiedSince
                        null, // ifMatch
                        null, // ifNoneMatch
                        status.isAppend() ? status.getCurrent() : null, null);
                return object.getDataInputStream();
            }
            else {
                object = session.getClient().getObject(
                        containerService.getContainer(file).getName(),
                        containerService.getKey(file),
                        null, // ifModifiedSince
                        null, // ifUnmodifiedSince
                        null, // ifMatch
                        null, // ifNoneMatch
                        status.isAppend() ? status.getCurrent() : null, null);
            }
            if(log.isDebugEnabled()) {
                log.debug(String.format("Reading stream with content length %d", object.getContentLength()));
            }
            // Update content length
            final Object contentLength = object.getMetadata(HttpHeaders.CONTENT_LENGTH);
            if(contentLength != null) {
                status.setLength(Long.parseLong(contentLength.toString()));
            }
            else {
                log.warn(String.format("Unknown content length for %s", file));
            }
            return object.getDataInputStream();
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Download failed", e, file);
        }
    }

    @Override
    public boolean append(final Path file) {
        return true;
    }
}
