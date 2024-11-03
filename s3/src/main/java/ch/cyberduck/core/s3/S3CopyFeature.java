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
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.Constants;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3Object;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class S3CopyFeature implements Copy {
    private static final Logger log = LogManager.getLogger(S3CopyFeature.class);

    private final S3Session session;
    private final PathContainerService containerService;
    private final S3AccessControlListFeature acl;

    public S3CopyFeature(final S3Session session, final S3AccessControlListFeature acl) {
        this.session = session;
        this.acl = acl;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback, final StreamListener listener) throws BackgroundException {
        if(null == status.getStorageClass()) {
            // Keep same storage class
            status.setStorageClass(new S3StorageClassFeature(session, acl).getClass(source));
        }
        if(Encryption.Algorithm.NONE == status.getEncryption()) {
            // Keep encryption setting
            status.setEncryption(new S3EncryptionFeature(session, acl).getEncryption(source));
        }
        if(Acl.EMPTY == status.getAcl()) {
            // Apply non-standard ACL
            try {
                // Verify target bucket allows ACLs
                if(acl.getPermission(containerService.getContainer(target)).isEditable()) {
                    status.setAcl(acl.getPermission(source));
                }
            }
            catch(AccessDeniedException | InteroperabilityException e) {
                log.warn("Ignore failure {}", e.getMessage());
            }
        }
        final S3Object destination = new S3WriteFeature(session, acl).getDetails(target, status);
        destination.setAcl(acl.toAcl(status.getAcl()));
        final Path bucket = containerService.getContainer(target);
        destination.setBucketName(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName());
        destination.replaceAllMetadata(new HashMap<>(new S3MetadataFeature(session, acl).getMetadata(source)));
        final String versionId = this.copy(source, destination, status, listener);
        return target.withAttributes(new PathAttributes(source.attributes()).withVersionId(
                new HostPreferences(session.getHost()).getBoolean("s3.listing.versioning.enable") ? versionId : null));
    }

    protected String copy(final Path source, final S3Object destination, final TransferStatus status, final StreamListener listener) throws BackgroundException {
        try {
            // Copying object applying the metadata of the original
            final Path bucket = containerService.getContainer(source);
            final Map<String, Object> stringObjectMap = session.getClient().copyVersionedObject(source.attributes().getVersionId(),
                    bucket.isRoot() ? RequestEntityRestStorageService.findBucketInHostname(session.getHost()) : bucket.getName(),
                    containerService.getKey(source),
                    destination.getBucketName(), destination, false);
            listener.sent(status.getLength());
            final Map complete = (Map) stringObjectMap.get(Constants.KEY_FOR_COMPLETE_METADATA);
            return (String) complete.get(Constants.AMZ_VERSION_ID);
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Cannot copy {0}", e, source);
        }
    }

    @Override
    public void preflight(final Path source, final Path target) throws BackgroundException {
        if(containerService.isContainer(source)) {
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot copy {0}", "Error"), source.getName())).withFile(source);
        }
        if(containerService.isContainer(target)) {
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot copy {0}", "Error"), source.getName())).withFile(target);
        }
    }
}
