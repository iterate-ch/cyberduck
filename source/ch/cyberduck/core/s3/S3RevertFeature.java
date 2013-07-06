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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ServiceExceptionMappingService;
import ch.cyberduck.core.features.Revert;

import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3Object;

/**
 * @version $Id:$
 */
public class S3RevertFeature implements Revert {

    private S3Session session;

    public S3RevertFeature(final S3Session session) {
        this.session = session;
    }

    /**
     * Versioning support. Copy a previous version of the object into the same bucket.
     * The copied object becomes the latest version of that object and all object versions are preserved.
     */
    @Override
    public void revert(final Path file) throws BackgroundException {
        if(file.attributes().isFile()) {
            try {
                final S3Object destination = new S3Object(file.getKey());
                // Keep same storage class
                destination.setStorageClass(file.attributes().getStorageClass());
                // Keep encryption setting
                destination.setServerSideEncryptionAlgorithm(file.attributes().getEncryption());
                // Apply non standard ACL
                final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
                destination.setAcl(acl.convert(acl.read(file)));
                session.getClient().copyVersionedObject(file.attributes().getVersionId(),
                        file.getContainer().getName(), file.getKey(), file.getContainer().getName(), destination, false);
            }
            catch(ServiceException e) {
                throw new ServiceExceptionMappingService().map("Cannot revert file", e, file);
            }
        }
    }
}
