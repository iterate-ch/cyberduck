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

import java.util.List;

public class S3DefaultDeleteFeature implements Delete {
    private static final Logger log = Logger.getLogger(S3DefaultDeleteFeature.class);

    private S3Session session;

    private PathContainerService containerService
            = new S3PathContainerService();

    public S3DefaultDeleteFeature(final S3Session session) {
        this.session = session;
    }

    public void delete(final List<Path> files, final LoginCallback prompt, final Callback callback) throws BackgroundException {
        for(Path file : files) {
            callback.delete(file);
            try {
                if(containerService.isContainer(file)) {
                    session.getClient().deleteBucket(containerService.getContainer(file).getName());
                }
                else {
                    // Always returning 204 even if the key does not exist. Does not return 404 for non-existing keys
                    session.getClient().deleteObject(containerService.getContainer(file).getName(), containerService.getKey(file));
                }
            }
            catch(ServiceException e) {
                try {
                    throw new ServiceExceptionMappingService().map("Cannot delete {0}", e, file);
                }
                catch(NotfoundException n) {
                    // Ignore
                }
            }
        }
    }
}
