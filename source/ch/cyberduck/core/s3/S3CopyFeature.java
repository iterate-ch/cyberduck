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
import ch.cyberduck.core.features.Copy;

import org.jets3t.service.ServiceException;
import org.jets3t.service.model.StorageObject;

/**
 * @version $Id:$
 */
public class S3CopyFeature implements Copy {

    private S3Session session;

    public S3CopyFeature(final S3Session session) {
        this.session = session;
    }

    @Override
    public void copy(final Path source, final Path copy) throws BackgroundException {
        try {
            if(source.attributes().isFile()) {
                final StorageObject destination = new StorageObject(copy.getKey());
                // Keep same storage class
                destination.setStorageClass(source.attributes().getStorageClass());
                // Keep encryption setting
                destination.setServerSideEncryptionAlgorithm(source.attributes().getEncryption());
                // Apply non standard ACL
                final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
                destination.setAcl(acl.convert(acl.read(source)));
                // Copying object applying the metadata of the original
                session.getClient().copyObject(source.getContainer().getName(), source.getKey(),
                        copy.getContainer().getName(), destination, false);
            }
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot copy {0}", e, source);
        }
    }
}
