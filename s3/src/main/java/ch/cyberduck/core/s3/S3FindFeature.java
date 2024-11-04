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

import ch.cyberduck.core.CancellingListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ListCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.RetriableAccessDeniedException;
import ch.cyberduck.core.features.Find;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.ServiceException;

public class S3FindFeature implements Find {
    private static final Logger log = LogManager.getLogger(S3FindFeature.class);

    private final PathContainerService containerService;
    private final S3Session session;
    private final S3AccessControlListFeature acl;
    private final S3AttributesFinderFeature attributes;

    public S3FindFeature(final S3Session session, final S3AccessControlListFeature acl) {
        this.session = session;
        this.acl = acl;
        this.attributes = new S3AttributesFinderFeature(session, acl);
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public boolean find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return true;
        }
        try {
            if(containerService.isContainer(file)) {
                try {
                    log.debug("Test if bucket {} is accessible", file);
                    return session.getClient().isBucketAccessible(containerService.getContainer(file).getName());
                }
                catch(ServiceException e) {
                    throw new S3ExceptionMappingService().map("Failure to read attributes of {0}", e, file);
                }
            }
            if(file.isFile() || file.isPlaceholder()) {
                attributes.find(file, listener);
                return true;
            }
            else {
                log.debug("Search for common prefix {}", file);
                // Check for common prefix
                try {
                    new S3ObjectListService(session, acl).list(file, new CancellingListProgressListener(), String.valueOf(Path.DELIMITER), 1);
                    return true;
                }
                catch(ListCanceledException l) {
                    // Found common prefix
                    return true;
                }
                catch(NotfoundException e) {
                    throw e;
                }
            }
        }
        catch(NotfoundException e) {
            return false;
        }
        catch(RetriableAccessDeniedException e) {
            // Must fail with server error
            throw e;
        }
        catch(AccessDeniedException e) {
            // Object is inaccessible to current user, but does exist.
            return true;
        }
    }
}
