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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Move;

import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.StorageObject;

import java.util.Map;

/**
 * @version $Id$
 */
public class S3MoveFeature implements Move {
    private static final Logger log = Logger.getLogger(S3MoveFeature.class);

    private PathContainerService containerService
            = new S3PathContainerService();

    private S3Session session;

    public S3MoveFeature(final S3Session session) {
        this.session = session;
    }

    @Override
    public void move(final Path file, final Path renamed, boolean exists, final ProgressListener listener) throws BackgroundException {
        try {
            if(file.isFile() || file.isPlaceholder()) {
                final StorageObject destination = new StorageObject(containerService.getKey(renamed));
                // Keep same storage class
                destination.setStorageClass(file.attributes().getStorageClass());
                // Keep encryption setting
                destination.setServerSideEncryptionAlgorithm(file.attributes().getEncryption());
                // Apply non standard ACL
                final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
                destination.setAcl(acl.convert(acl.getPermission(file)));
                // Moving the object retaining the metadata of the original.
                final Map<String, Object> headers = session.getClient().copyObject(
                        containerService.getContainer(file).getName(),
                        containerService.getKey(file),
                        containerService.getContainer(renamed).getName(),
                        destination, false);
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Received response headers for copy %s", headers));
                }
                session.getClient().deleteObject(containerService.getContainer(file).getName(), containerService.getKey(file));
            }
            else if(file.isDirectory()) {
                for(Path i : session.list(file, new DisabledListProgressListener())) {
                    this.move(i, new Path(renamed, i.getName(), i.getType()), false, listener);
                }
            }
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public boolean isSupported(final Path file) {
        return !containerService.isContainer(file);
    }
}