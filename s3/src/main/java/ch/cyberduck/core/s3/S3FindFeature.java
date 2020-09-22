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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;

public class S3FindFeature implements Find {

    private final PathContainerService containerService
        = new S3PathContainerService();

    private final S3Session session;
    private final S3AttributesFinderFeature attributes;

    public S3FindFeature(final S3Session session) {
        this.session = session;
        this.attributes = new S3AttributesFinderFeature(session);
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return true;
        }
        try {
            if(file.isFile() || file.isPlaceholder()) {
                attributes.find(file);
                return true;
            }
            else {
                // Check for common prefix
                try {
                    new S3ObjectListService(session).list(file, new DisabledListProgressListener(), containerService.getKey(file));
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
        catch(AccessDeniedException e) {
            // Object is inaccessible to current user, but does exist.
            return true;
        }
    }

    @Override
    public Find withCache(final Cache<Path> cache) {
        attributes.withCache(cache);
        return this;
    }
}
