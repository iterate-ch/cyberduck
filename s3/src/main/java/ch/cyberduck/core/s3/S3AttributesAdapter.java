package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.preferences.HostPreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

import static org.jets3t.service.model.StorageObject.METADATA_HEADER_SERVER_SIDE_ENCRYPTION_KMS_KEY_ID;

public class S3AttributesAdapter implements AttributesAdapter<StorageObject> {

    private final Host host;

    public S3AttributesAdapter(final Host host) {
        this.host = host;
    }

    @Override
    public PathAttributes toAttributes(final StorageObject object) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setSize(object.getContentLength());
        final Date lastmodified = object.getLastModifiedDate();
        if(lastmodified != null) {
            attributes.setModificationDate(lastmodified.getTime());
        }
        if(StringUtils.isNotBlank(object.getStorageClass())) {
            attributes.setStorageClass(object.getStorageClass());
        }
        else if(object.containsMetadata("storage-class")) {
            attributes.setStorageClass(object.getMetadata("storage-class").toString());
        }
        if(StringUtils.isNotBlank(object.getETag())) {
            attributes.setETag(StringUtils.remove(object.getETag(), '"'));
        }
        // The ETag will only be the MD5 of the object data when the object is stored as plaintext or encrypted
        // using SSE-S3. If the object is encrypted using another method (such as SSE-C or SSE-KMS) the ETag is
        // not the MD5 of the object data.
        attributes.setChecksum(Checksum.parse(object.getETag()));
        if(object instanceof S3Object) {
            if(HostPreferencesFactory.get(host).getBoolean("s3.listing.versioning.enable")) {
                attributes.setVersionId(((S3Object) object).getVersionId());
            }
        }
        if(object.containsMetadata(METADATA_HEADER_SERVER_SIDE_ENCRYPTION_KMS_KEY_ID)) {
            attributes.setEncryption(new Encryption.Algorithm(object.getServerSideEncryptionAlgorithm(),
                    object.getMetadata(METADATA_HEADER_SERVER_SIDE_ENCRYPTION_KMS_KEY_ID).toString()) {
                @Override
                public String getDescription() {
                    return String.format("SSE-KMS (%s)", key);
                }
            });
        }
        else {
            if(null != object.getServerSideEncryptionAlgorithm()) {
                // AES256
                attributes.setEncryption(new Encryption.Algorithm(object.getServerSideEncryptionAlgorithm(), null) {
                    @Override
                    public String getDescription() {
                        return "SSE-S3 (AES-256)";
                    }
                });
            }
        }
        final Map<String, String> metadata = metadata(object);
        if(!metadata.isEmpty()) {
            attributes.setMetadata(metadata);
        }
        final Long mtime = S3TimestampFeature.fromHeaders(S3TimestampFeature.METADATA_MODIFICATION_DATE,
                Maps.transformValues(object.getMetadataMap(), Object::toString));
        if(-1L != mtime) {
            attributes.setModificationDate(mtime);
        }
        final Long ctime = S3TimestampFeature.fromHeaders(S3TimestampFeature.METADATA_CREATION_DATE,
                Maps.transformValues(object.getMetadataMap(), Object::toString));
        if(-1L != ctime) {
            attributes.setCreationDate(ctime);
        }
        return attributes;
    }

    /**
     * @return Pruned metadata with user editable headers only
     */
    public static Map<String, String> metadata(final StorageObject object) {
        final Map<String, String> metadata = new HashMap<>(Maps.transformValues(object.getModifiableMetadata(), Object::toString));
        metadata.keySet().removeIf(header -> StringUtils.equalsIgnoreCase(header, "storage-class"));
        metadata.keySet().removeIf(header -> StringUtils.equalsIgnoreCase(header, "server-side-encryption"));
        metadata.keySet().removeIf(header -> StringUtils.equalsIgnoreCase(header, "expiration"));
        metadata.keySet().removeIf(header -> StringUtils.equalsIgnoreCase(header, "checksum-sha256"));
        metadata.keySet().removeIf(header -> StringUtils.equalsIgnoreCase(header, "version-id"));
        return metadata;
    }
}
