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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Redundancy;

import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.StorageObject;

import java.util.Collections;
import java.util.Map;

public class S3MoveFeature implements Move {
    private static final Logger log = Logger.getLogger(S3MoveFeature.class);

    private final PathContainerService containerService
            = new S3PathContainerService();

    private final S3Session session;
    private final S3AccessControlListFeature accessControlListFeature;

    private Delete delete;

    public S3MoveFeature(final S3Session session, final S3AccessControlListFeature accessControlListFeature) {
        this.session = session;
        this.accessControlListFeature = accessControlListFeature;
        this.delete = new S3DefaultDeleteFeature(session);
    }

    @Override
    public void move(final Path source, final Path renamed, boolean exists, final Delete.Callback callback) throws BackgroundException {
        try {
            if(source.isFile() || source.isPlaceholder()) {
                final StorageObject destination = new StorageObject(containerService.getKey(renamed));
                // Keep same storage class
                final Redundancy storageClassFeature = session.getFeature(Redundancy.class);
                if(storageClassFeature != null) {
                    destination.setStorageClass(storageClassFeature.getClass(source));
                }
                // Keep encryption setting
                final Encryption encryptionFeature = session.getFeature(Encryption.class);
                if(encryptionFeature != null) {
                    final Encryption.Algorithm encryption = encryptionFeature.getEncryption(source);
                    destination.setServerSideEncryptionAlgorithm(encryption.algorithm);
                    // Set custom key id stored in KMS
                    destination.setServerSideEncryptionKmsKeyId(encryption.key);
                }
                // Apply non standard ACL
                if(accessControlListFeature != null) {
                    Acl acl = Acl.EMPTY;
                    try {
                        acl = accessControlListFeature.getPermission(source);
                    }
                    catch(AccessDeniedException | InteroperabilityException e) {
                        log.warn(String.format("Ignore failure %s", e.getDetail()));
                    }
                    destination.setAcl(accessControlListFeature.convert(acl));
                }
                // Moving the object retaining the metadata of the original.
                final Map<String, Object> headers = session.getClient().copyObject(
                        containerService.getContainer(source).getName(),
                        containerService.getKey(source),
                        containerService.getContainer(renamed).getName(),
                        destination, false);
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Received response headers for copy %s", headers));
                }
                delete.delete(Collections.singletonList(source), new DisabledLoginCallback(), new Delete.DisabledCallback());
            }
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Cannot rename {0}", e, source);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return false;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return !containerService.isContainer(source);
    }

    @Override
    public Move withDelete(final Delete delete) {
        this.delete = delete;
        return this;
    }

}