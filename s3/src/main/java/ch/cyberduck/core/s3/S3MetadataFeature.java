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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Metadata;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.shared.MetadataSizeFilter;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.Constants;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.utils.RestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class S3MetadataFeature implements Metadata {
    private static final Logger log = LogManager.getLogger(S3MetadataFeature.class);

    /**
     * Maximum number of bytes of keys and values of all user-defined metadata
     */
    public static final int MAXIMUM_TOTAL_SIZE = 2048;

    private static final String METADATA_PREFIX = "x-amz-meta-";

    private final S3Session session;
    private final PathContainerService containerService;
    private final S3AccessControlListFeature acl;

    public S3MetadataFeature(final S3Session session, final S3AccessControlListFeature acl) {
        this.session = session;
        this.acl = acl;
        this.containerService = new S3PathContainerService(session.getHost());
    }

    @Override
    public Map<String, String> getDefault(final Path file) {
        final Map<String, String> metadata = HostPreferencesFactory.get(session.getHost()).getMap("s3.metadata.default");
        if(metadata.isEmpty()) {
            return Metadata.EMPTY;
        }
        return metadata;
    }

    @Override
    public Map<String, String> getMetadata(final Path file) throws BackgroundException {
        if(containerService.isContainer(file)) {
            return Collections.emptyMap();
        }
        return new S3AttributesFinderFeature(session, acl).find(file).getMetadata();
    }

    @Override
    public void setMetadata(final Path file, final TransferStatus status) throws BackgroundException {
        if(containerService.isContainer(file)) {
            return;
        }
        log.debug("Write metadata {} for file {}", status, file);
        try {
            final S3Object target = new S3Object(containerService.getKey(file));
            target.replaceAllMetadata(new HashMap<>(filter(file, status.getMetadata())));
            if(Acl.EMPTY == status.getAcl()) {
                status.setAcl(acl.getPermission(file));
            }
            // Apply non-standard ACL
            target.setAcl(S3AccessControlListFeature.toObjectAccessControlList(status.getAcl()));
            if(null == status.getStorageClass()) {
                status.setStorageClass(new S3StorageClassFeature(session, acl).getClass(file));
            }
            target.setStorageClass(status.getStorageClass());
            if(Encryption.Algorithm.NONE == status.getEncryption()) {
                status.setEncryption(new S3EncryptionFeature(session, acl).getEncryption(file));
            }
            target.setServerSideEncryptionAlgorithm(status.getEncryption().algorithm);
            // Set custom key id stored in KMS
            target.setServerSideEncryptionKmsKeyId(status.getEncryption().key);
            final Path bucket = containerService.getContainer(file);
            final Map<String, Object> result = session.getClient().updateObjectMetadata(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(), target);
            final PathAttributes attributes = new S3AttributesAdapter(session.getHost()).toAttributes(target);
            // HTTP header names are case-insensitive
            final Map<String, Object> complete = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            complete.putAll((Map<String, Object>) result.get(Constants.KEY_FOR_COMPLETE_METADATA));
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

    /**
     * @param file     File
     * @param metadata Metadata to write
     * @return Metadata with user-defined entries exceeding limits removed. Timestamps are always kept
     */
    public static Map<String, String> filter(final Path file, final Map<String, String> metadata) {
        return new MetadataSizeFilter()
                .withMaximumTotalSize(MAXIMUM_TOTAL_SIZE)
                .withExcluded(S3MetadataFeature::isHeader)
                .withRequired(key -> StringUtils.equalsAnyIgnoreCase(StringUtils.removeStartIgnoreCase(key, METADATA_PREFIX),
                        S3TimestampFeature.METADATA_MODIFICATION_DATE, S3TimestampFeature.METADATA_CREATION_DATE))
                .filter(file, metadata);
    }

    /**
     * @param key Metadata key
     * @return True for standard HTTP header or header with prefix x-amz- not counted as user-defined metadata
     */
    private static boolean isHeader(final String key) {
        final String name = StringUtils.lowerCase(key);
        if(RestUtils.HTTP_HEADER_METADATA_NAMES.contains(name)) {
            return true;
        }
        return StringUtils.startsWith(name, "x-amz-") && !StringUtils.startsWith(name, METADATA_PREFIX);
    }
}
