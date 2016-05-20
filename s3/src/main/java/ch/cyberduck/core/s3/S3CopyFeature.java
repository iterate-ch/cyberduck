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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Encryption;

import org.jets3t.service.ServiceException;
import org.jets3t.service.model.StorageObject;

public class S3CopyFeature implements Copy {

    private final S3Session session;

    private final PathContainerService containerService
            = new S3PathContainerService();

    private final S3AccessControlListFeature accessControlListFeature;

    public S3CopyFeature(final S3Session session) {
        this(session, (S3AccessControlListFeature) session.getFeature(AclPermission.class));
    }

    public S3CopyFeature(final S3Session session, final S3AccessControlListFeature accessControlListFeature) {
        this.session = session;
        this.accessControlListFeature = accessControlListFeature;
    }

    @Override
    public void copy(final Path source, final Path copy) throws BackgroundException {
        if(source.isFile()) {
            // Keep same storage class
            final String storageClass = source.attributes().getStorageClass();
            // Keep encryption setting
            final Encryption.Algorithm encryption = source.attributes().getEncryption();
            // Apply non standard ACL
            if(null == accessControlListFeature) {
                this.copy(source, copy, storageClass, encryption, Acl.EMPTY);
            }
            else {
                final Acl acl = accessControlListFeature.getPermission(source);
                this.copy(source, copy, storageClass, encryption, acl);
            }
        }
    }

    protected void copy(final Path source, final Path copy, final String storageClass, final Encryption.Algorithm encryption,
                        final Acl acl) throws BackgroundException {
        if(source.isFile() || source.isPlaceholder()) {
            final StorageObject destination = new StorageObject(containerService.getKey(copy));
            destination.setStorageClass(storageClass);
            destination.setServerSideEncryptionAlgorithm(encryption.algorithm);
            // Set custom key id stored in KMS
            destination.setServerSideEncryptionKmsKeyId(encryption.key);
            if(null == accessControlListFeature) {
                destination.setAcl(null);
            }
            else {
                destination.setAcl(accessControlListFeature.convert(acl));
            }
            try {
                // Copying object applying the metadata of the original
                session.getClient().copyObject(containerService.getContainer(source).getName(),
                        containerService.getKey(source),
                        containerService.getContainer(copy).getName(), destination, false);
            }
            catch(ServiceException e) {
                throw new ServiceExceptionMappingService().map("Cannot copy {0}", e, source);
            }
        }
    }
}
