package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.io.Checksum;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import com.google.api.client.util.DateTime;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.StorageObject;

public class GoogleStorageAttributesFinderFeature implements AttributesFinder {

    private final PathContainerService containerService
        = new GoogleStoragePathContainerService();

    private final GoogleStorageSession session;

    public GoogleStorageAttributesFinderFeature(final GoogleStorageSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }
        try {
            if(containerService.isContainer(file)) {
                return this.toAttributes(session.getClient().buckets().get(
                    containerService.getContainer(file).getName()).execute());
            }
            else {
                return this.toAttributes(session.getClient().objects().get(
                    containerService.getContainer(file).getName(), containerService.getKey(file)).execute());
            }
        }
        catch(IOException e) {
            throw new GoogleStorageExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    protected PathAttributes toAttributes(final Bucket bucket) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setRegion(bucket.getLocation());
        attributes.setStorageClass(bucket.getStorageClass());
        attributes.setCreationDate(bucket.getTimeCreated().getValue());
        attributes.setETag(bucket.getEtag());
        if(bucket.getEncryption() != null) {
            attributes.setEncryption(new Encryption.Algorithm("AES256", bucket.getEncryption().getDefaultKmsKeyName()));
        }
        attributes.setRegion(bucket.getLocation());
        return attributes;
    }

    protected PathAttributes toAttributes(final StorageObject object) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setSize(object.getSize().longValue());
        final DateTime lastmodified = object.getTimeCreated();
        if(lastmodified != null) {
            attributes.setModificationDate(lastmodified.getValue());
        }
        attributes.setStorageClass(object.getStorageClass());
        if(StringUtils.isNotBlank(object.getEtag())) {
            attributes.setETag(object.getEtag());
        }
        // The content generation of this object. Used for object versioning.
        // attributes.setVersionId(String.valueOf(object.getGeneration()));
        // Archived versions of objects have a `timeDeleted` property.
        // attributes.setDuplicate(object.getTimeDeleted() != null);
        // if(object.getTimeDeleted() != null) {
        //     attributes.setCustom(Collections.singletonMap(KEY_DELETE_MARKER, Boolean.TRUE.toString()));
        // }
        if(object.getKmsKeyName() != null) {
            attributes.setEncryption(new Encryption.Algorithm("AES256",
                object.getKmsKeyName()) {
                @Override
                public String getDescription() {
                    return String.format("SSE-KMS (%s)", key);
                }
            });
        }
        attributes.setChecksum(Checksum.parse(object.getCrc32c()));
        if(object.getMetadata() != null) {
            attributes.setMetadata(object.getMetadata());
        }
        return attributes;
    }
}
