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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3Object;

public class S3FindFeature implements Find {
    private static final Logger log = Logger.getLogger(S3AttributesFeature.class);

    private S3Session session;

    private PathContainerService containerService
            = new S3PathContainerService();

    private PathCache cache;

    public S3FindFeature(final S3Session session) {
        this.session = session;
        this.cache = PathCache.empty();
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return true;
        }
        final AttributedList<Path> list;
        if(cache.containsKey(file.getParent())) {
            list = cache.get(file.getParent());
        }
        else {
            list = new AttributedList<Path>();
            cache.put(file.getParent(), list);
        }
        if(list.contains(file)) {
            // Previously found
            return true;
        }
        if(cache.isHidden(file)) {
            // Previously not found
            return false;
        }
        try {
            if(session.getClient().isObjectInBucket(containerService.getContainer(file).getName(),
                    containerService.getKey(file))) {
                list.add(file);
                return true;
            }
            else {
                list.attributes().addHidden(file);
                return false;
            }
        }
        catch(ServiceException e) {
            switch(session.getSignatureVersion()) {
                case AWS4HMACSHA256:
                    if(new S3ExceptionMappingService().map(e) instanceof InteroperabilityException) {
                        log.warn("Workaround HEAD failure using GET because the expected AWS region cannot be determined " +
                                "from the HEAD error message if using AWS4-HMAC-SHA256 with the wrong region specifier " +
                                "in the authentication header.");
                        // Fallback to GET if HEAD fails with 400 response
                        try {
                            final S3Object object = session.getClient().getObject(containerService.getContainer(file).getName(),
                                    containerService.getKey(file), null, null, null, null, null, null);
                            IOUtils.closeQuietly(object.getDataInputStream());
                            list.add(file);
                            return true;
                        }
                        catch(ServiceException f) {
                            if(new S3ExceptionMappingService().map(f) instanceof NotfoundException) {
                                list.attributes().addHidden(file);
                                return false;
                            }
                            if(f.getResponseCode() == HttpStatus.SC_REQUESTED_RANGE_NOT_SATISFIABLE) {
                                // A 0 byte content length file does exist but will return 416
                                return true;
                            }
                        }
                    }
            }
            throw new S3ExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public Find withCache(final PathCache cache) {
        this.cache = cache;
        return this;
    }
}
