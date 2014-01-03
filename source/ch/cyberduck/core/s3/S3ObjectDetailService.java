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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.StorageObject;

/**
 * @version $Id$
 */
public class S3ObjectDetailService {
    private static final Logger log = Logger.getLogger(S3ObjectDetailService.class);

    private S3Session session;

    private PathContainerService containerService
            = new PathContainerService();

    public S3ObjectDetailService(final S3Session session) {
        this.session = session;
    }

    /**
     * Retrieve and cache object details.
     *
     * @return Object details
     */
    public StorageObject getDetails(final Path file) throws BackgroundException {
        final String container = containerService.getContainer(file).getName();
        try {
            if(file.attributes().isDuplicate()) {
                return session.getClient().getVersionedObjectDetails(file.attributes().getVersionId(),
                        container, containerService.getKey(file));
            }
            else {
                return session.getClient().getObjectDetails(container, containerService.getKey(file));
            }
        }
        catch(ServiceException e) {
            try {
                throw new ServiceExceptionMappingService().map("Cannot read file attributes", e, file);
            }
            catch(AccessDeniedException l) {
                log.warn(String.format("Missing permission to read object details for %s %s", file, e.getMessage()));
                final StorageObject object = new StorageObject(containerService.getKey(file));
                object.setBucketName(container);
                return object;
            }
        }
    }
}
