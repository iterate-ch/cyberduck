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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.Constants;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3Object;

import java.util.HashMap;
import java.util.Map;

public class S3MetadataFeature implements Headers {
    private static final Logger log = LogManager.getLogger(S3MetadataFeature.class);

    private final S3Session session;
    private final PathContainerService containerService;
    private final S3AccessControlListFeature acl;

    public S3MetadataFeature(final S3Session session, final S3AccessControlListFeature acl) {
        this.session = session;
        this.acl = acl;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public Map<String, String> getDefault(final Local local) {
        return new HostPreferences(session.getHost()).getMap("s3.metadata.default");
    }

    @Override
    public Map<String, String> getMetadata(final Path file) throws BackgroundException {
        return new S3AttributesFinderFeature(session, acl).find(file).getMetadata();
    }

    @Override
    public void setMetadata(final Path file, final TransferStatus status) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Write metadata %s for file %s", status, file));
        }
        try {
            final S3Object target = new S3Object(containerService.getKey(file));
            target.replaceAllMetadata(new HashMap<>(status.getMetadata()));
            try {
                // Apply non-standard ACL
                final Acl list = acl.getPermission(file);
                if(list.isEditable()) {
                    target.setAcl(acl.toAcl(list));
                }
            }
            catch(AccessDeniedException | InteroperabilityException e) {
                log.warn(String.format("Ignore failure %s", e));
            }
            final Redundancy storageClassFeature = session.getFeature(Redundancy.class);
            if(storageClassFeature != null) {
                target.setStorageClass(storageClassFeature.getClass(file));
            }
            final Encryption encryptionFeature = session.getFeature(Encryption.class);
            if(encryptionFeature != null) {
                final Encryption.Algorithm encryption = encryptionFeature.getEncryption(file);
                target.setServerSideEncryptionAlgorithm(encryption.algorithm);
                // Set custom key id stored in KMS
                target.setServerSideEncryptionKmsKeyId(encryption.key);
            }
            final Path bucket = containerService.getContainer(file);
            final Map<String, Object> result = session.getClient().updateObjectMetadata(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(), target);
            final PathAttributes attributes = new S3AttributesAdapter(session.getHost()).toAttributes(target);
            final Map complete = (Map) result.get(Constants.KEY_FOR_COMPLETE_METADATA);
            attributes.setVersionId((String) complete.get(Constants.AMZ_VERSION_ID));
            status.setResponse(attributes);
        }
        catch(ServiceException e) {
            final BackgroundException failure = new S3ExceptionMappingService().map("Failure to write attributes of {0}", e, file);
            if(file.isDirectory()) {
                if(failure instanceof NotfoundException) {
                    // No placeholder file may exist but we just have a common prefix
                    return;
                }
            }
            throw failure;
        }
    }
}
