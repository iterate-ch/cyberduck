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
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3Object;

public class S3CopyFeature implements Copy {
    private static final Logger log = Logger.getLogger(S3CopyFeature.class);

    private final S3Session session;

    private final PathContainerService containerService
            = new S3PathContainerService();

    private final S3AccessControlListFeature accessControlListFeature;

    public S3CopyFeature(final S3Session session) {
        this(session, new S3AccessControlListFeature(session));
    }

    public S3CopyFeature(final S3Session session, final S3AccessControlListFeature accessControlListFeature) {
        this.session = session;
        this.accessControlListFeature = accessControlListFeature;
    }

    @Override
    public void copy(final Path source, final Path target, final TransferStatus status) throws BackgroundException {
        if(source.isFile() || source.isPlaceholder()) {
            // Keep same storage class
            status.setStorageClass(new S3StorageClassFeature(session).getClass(source));
            // Keep encryption setting
            status.setEncryption(new S3EncryptionFeature(session).getEncryption(source));
            // Apply non standard ACL
            try {
                status.setAcl(accessControlListFeature.getPermission(source));
            }
            catch(AccessDeniedException | InteroperabilityException e) {
                log.warn(String.format("Ignore failure %s", e.getDetail()));
            }
            final S3Object destination = new S3Object(containerService.getKey(target));
            destination.setStorageClass(status.getStorageClass());
            destination.setServerSideEncryptionAlgorithm(status.getEncryption().algorithm);
            // Set custom key id stored in KMS
            destination.setServerSideEncryptionKmsKeyId(status.getEncryption().key);
            destination.setAcl(accessControlListFeature.convert(status.getAcl()));
            destination.setBucketName(containerService.getContainer(target).getName());
            this.copy(source, destination, status);
        }
    }

    protected void copy(final Path source, final S3Object destination, final TransferStatus status) throws BackgroundException {
        try {
            // Copying object applying the metadata of the original
            session.getClient().copyObject(containerService.getContainer(source).getName(),
                    containerService.getKey(source),
                    destination.getBucketName(), destination, false);
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Cannot copy {0}", e, source);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return false;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return !containerService.isContainer(source) && !containerService.isContainer(target);
    }

    @Override
    public Copy withTarget(final Session<?> session) {
        return this;
    }
}
