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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.StorageObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class S3MetadataFeature implements Headers {
    private static final Logger log = Logger.getLogger(S3MetadataFeature.class);

    private final S3Session session;

    private final PathContainerService containerService
            = new S3PathContainerService();

    private final S3AccessControlListFeature accessControlListFeature;

    public S3MetadataFeature(final S3Session session) {
        this(session, (S3AccessControlListFeature) session.getFeature(AclPermission.class));
    }

    public S3MetadataFeature(final S3Session session, final S3AccessControlListFeature accessControlListFeature) {
        this.session = session;
        this.accessControlListFeature = accessControlListFeature;
    }

    @Override
    public Map<String, String> getDefault() {
        return PreferencesFactory.get().getMap("s3.metadata.default");
    }

    @Override
    public Map<String, String> getMetadata(final Path file) throws BackgroundException {
        if(file.isFile() || file.isPlaceholder()) {
            return new S3AttributesFeature(session).find(file).getMetadata();
        }
        return Collections.emptyMap();
    }

    @Override
    public void setMetadata(final Path file, final Map<String, String> metadata) throws BackgroundException {
        if(file.isFile() || file.isPlaceholder()) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Write metadata %s for file %s", metadata, file));
            }
            try {
                // Make sure to copy existing attributes
                final StorageObject target = new S3AttributesFeature(session).details(file);
                target.replaceAllMetadata(new HashMap<String, Object>(metadata));
                // Apply non standard ACL
                if(accessControlListFeature != null) {
                    target.setAcl(accessControlListFeature.convert(accessControlListFeature.getPermission(file)));
                }
                final Redundancy storageClassFeature = session.getFeature(Redundancy.class);
                if(storageClassFeature != null) {
                    target.setStorageClass(storageClassFeature.getClass(file));
                }
                final Encryption encryptionFeature = session.getFeature(Encryption.class);
                if(encryptionFeature != null) {
                    final Encryption.Properties encryption = encryptionFeature.getEncryption(file);
                    target.setServerSideEncryptionAlgorithm(encryption.algorithm);
                    if(encryption.key != null) {
                        // Set custom key id stored in KMS
                        target.addMetadata("x-amz-server-side-encryption-aws-kms-key-id", encryption.key);
                    }
                }
                session.getClient().updateObjectMetadata(containerService.getContainer(file).getName(), target);
            }
            catch(ServiceException e) {
                throw new ServiceExceptionMappingService().map("Failure to write attributes of {0}", e, file);
            }
        }
    }
}
