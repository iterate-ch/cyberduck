package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Attributes;

import org.apache.log4j.Logger;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;

/**
 * @version $Id$
 */
public class S3AttributesFeature implements Attributes {
    private static final Logger log = Logger.getLogger(S3AttributesFeature.class);

    private S3Session session;

    private PathContainerService containerService
            = new S3PathContainerService();

    public S3AttributesFeature(final S3Session session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        if(containerService.isContainer(file)) {
            return new PathAttributes();
        }
        else {
            if(file.isFile() || file.isPlaceholder()) {
                return this.find(new S3ObjectDetailService(session).getDetails(file));
            }
            else {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Return blank attributes for directory delimiter %s", file));
                }
            }
            return new PathAttributes();
        }
    }

    @Override
    public Attributes withCache(final Cache cache) {
        return this;
    }

    protected PathAttributes find(final StorageObject object) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setSize(object.getContentLength());
        attributes.setModificationDate(object.getLastModifiedDate().getTime());
        attributes.setStorageClass(object.getStorageClass());
        attributes.setChecksum(object.getETag());
        if(object instanceof S3Object) {
            attributes.setVersionId(((S3Object) object).getVersionId());
        }
        return attributes;
    }
}
