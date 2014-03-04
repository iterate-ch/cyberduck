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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Attributes;

import org.apache.commons.lang3.StringUtils;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;

/**
 * @version $Id$
 */
public class S3AttributesFeature implements Attributes {

    private S3Session session;

    private PathContainerService containerService
            = new PathContainerService();

    public S3AttributesFeature(S3Session session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(Path file) throws BackgroundException {
        if(containerService.isContainer(file)) {
            return new PathAttributes();
        }
        else {
            return this.find(new S3ObjectDetailService(session).getDetails(file));
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
        // Directory placeholders
        if(object.isDirectoryPlaceholder()) {
            attributes.setPlaceholder(true);
        }
        else if(0 == object.getContentLength()) {
            if("application/x-directory".equals(object.getContentType())) {
                attributes.setPlaceholder(true);
            }
        }
        final Object etag = object.getMetadataMap().get(StorageObject.METADATA_HEADER_ETAG);
        if(null != etag) {
            final String checksum = etag.toString().replaceAll("\"", StringUtils.EMPTY);
            attributes.setChecksum(checksum);
            if(checksum.equals("d66759af42f282e1ba19144df2d405d0")) {
                // Fix #5374 s3sync.rb interoperability
                attributes.setPlaceholder(true);
            }
        }
        if(object instanceof S3Object) {
            attributes.setVersionId(((S3Object) object).getVersionId());
        }
        return attributes;
    }
}
