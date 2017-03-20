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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;

import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.MultipartUpload;

import java.util.ArrayList;
import java.util.List;

public class S3DefaultDeleteFeature implements Delete {
    private static final Logger log = Logger.getLogger(S3DefaultDeleteFeature.class);

    private final S3Session session;

    private final PathContainerService containerService
            = new S3PathContainerService();

    private final S3MultipartService multipartService;

    public S3DefaultDeleteFeature(final S3Session session) {
        this(session, new S3DefaultMultipartService(session));
    }

    public S3DefaultDeleteFeature(final S3Session session, final S3MultipartService multipartService) {
        this.session = session;
        this.multipartService = multipartService;
    }

    public void delete(final List<Path> files, final LoginCallback prompt, final Callback callback) throws BackgroundException {
        final List<Path> containers = new ArrayList<Path>();
        for(Path file : files) {
            if(containerService.isContainer(file)) {
                containers.add(file);
                continue;
            }
            if(file.getType().contains(Path.Type.upload)) {
                callback.delete(file);
                // In-progress multipart upload
                try {
                    multipartService.delete(new MultipartUpload(file.attributes().getVersionId(),
                            containerService.getContainer(file).getName(), containerService.getKey(file)));
                }
                catch(NotfoundException ignored) {
                    log.warn(String.format("Ignore failure deleting multipart upload %s", file));
                }
                continue;
            }
            callback.delete(file);
            try {
                // Always returning 204 even if the key does not exist. Does not return 404 for non-existing keys
                session.getClient().deleteObject(containerService.getContainer(file).getName(), containerService.getKey(file));
            }
            catch(ServiceException e) {
                try {
                    throw new S3ExceptionMappingService().map("Cannot delete {0}", e, file);
                }
                catch(NotfoundException ignored) {
                    log.warn(String.format("Ignore missing placeholder object %s", file));
                }
            }
        }
        for(Path file : containers) {
            callback.delete(file);
            try {
                session.getClient().deleteBucket(containerService.getContainer(file).getName());
            }
            catch(ServiceException e) {
                throw new S3ExceptionMappingService().map("Cannot delete {0}", e, file);
            }
        }
    }

    @Override
    public boolean isRecursive() {
        return false;
    }
}
