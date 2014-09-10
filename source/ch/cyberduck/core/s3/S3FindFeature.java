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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;

import org.jets3t.service.ServiceException;

/**
 * @version $Id$
 */
public class S3FindFeature implements Find {

    private S3Session session;

    private PathContainerService containerService
            = new S3PathContainerService();

    private Cache cache;

    public S3FindFeature(final S3Session session) {
        this.session = session;
        this.cache = Cache.empty();
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return true;
        }
        final AttributedList<Path> list;
        if(cache.containsKey(file.getParent().getReference())) {
            list = cache.get(file.getParent().getReference());
        }
        else {
            list = new AttributedList<Path>();
            cache.put(file.getParent().getReference(), list);
        }
        if(list.contains(file.getReference())) {
            // Previously found
            return true;
        }
        if(cache.isHidden(file)) {
            // Previously not found
            return false;
        }
        try {
            final boolean found;
            if(file.isDirectory() && !file.isVolume()) {
                found = session.getClient().isObjectInBucket(containerService.getContainer(file).getName(),
                        containerService.getKey(file) + Path.DELIMITER);
            }
            else {
                found = session.getClient().isObjectInBucket(containerService.getContainer(file).getName(),
                        containerService.getKey(file));
            }
            if(found) {
                list.add(file);
            }
            else {
                list.attributes().addHidden(file);
            }
            return found;
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot read file attributes", e, file);
        }
    }

    @Override
    public Find withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }
}
