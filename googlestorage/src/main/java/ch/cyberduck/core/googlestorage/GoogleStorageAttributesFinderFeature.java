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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultPathPredicate;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

import com.google.api.client.util.DateTime;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.StorageObject;

public class GoogleStorageAttributesFinderFeature implements AttributesFinder {
    private static final Logger log = Logger.getLogger(GoogleStorageAttributesFinderFeature.class);

    private final PathContainerService containerService;
    private final GoogleStorageSession session;
    /**
     * Lookup previous versions
     */
    private final boolean references;

    public GoogleStorageAttributesFinderFeature(final GoogleStorageSession session) {
        this(session, new HostPreferences(session.getHost()).getBoolean("googlestorage.versioning.references.enable"));
    }

    public GoogleStorageAttributesFinderFeature(final GoogleStorageSession session, final boolean references) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
        this.references = references;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }
        try {
            if(containerService.isContainer(file)) {
                return this.toAttributes(session.getClient().buckets().get(
                    containerService.getContainer(file).getName()).execute());
            }
            else {
                final Storage.Objects.Get request = session.getClient().objects().get(
                    containerService.getContainer(file).getName(), containerService.getKey(file));
                final VersioningConfiguration versioning = null != session.getFeature(Versioning.class) ? session.getFeature(Versioning.class).getConfiguration(
                    containerService.getContainer(file)
                ) : VersioningConfiguration.empty();
                if(versioning.isEnabled()) {
                    if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
                        request.setGeneration(Long.parseLong(file.attributes().getVersionId()));
                    }
                }
                final PathAttributes attributes = this.toAttributes(request.execute(), versioning);
                if(versioning.isEnabled()) {
                    if(references) {
                        // Add references to previous versions
                        final AttributedList<Path> list = new GoogleStorageObjectListService(session, true).list(file, new DisabledListProgressListener());
                        final Path versioned = list.find(new DefaultPathPredicate(file));
                        if(null != versioned) {
                            attributes.setDuplicate(versioned.attributes().isDuplicate());
                            attributes.setVersions(versioned.attributes().getVersions());
                        }
                    }
                    else {
                        // Determine if latest version
                        try {
                            // Duplicate if not latest version
                            final String latest = this.toAttributes(session.getClient().objects().get(
                                containerService.getContainer(file).getName(), containerService.getKey(file)).execute(), versioning).getVersionId();
                            if(null != latest) {
                                attributes.setDuplicate(!latest.equals(attributes.getVersionId()));
                            }
                        }
                        catch(IOException e) {
                            // Noncurrent versions only appear in requests that explicitly call for object versions to be included
                            final BackgroundException failure = new GoogleStorageExceptionMappingService().map("Failure to read attributes of {0}", e, file);
                            if(failure instanceof NotfoundException) {
                                // The latest version is a delete marker
                                attributes.setDuplicate(true);
                            }
                            else {
                                throw failure;
                            }
                        }
                    }
                }
                return attributes;
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

    protected PathAttributes toAttributes(final StorageObject object, final VersioningConfiguration versioning) {
        final PathAttributes attributes = new PathAttributes();
        if(object.getSize() != null) {
            attributes.setSize(object.getSize().longValue());
        }
        final DateTime lastmodified = object.getTimeCreated();
        if(lastmodified != null) {
            attributes.setModificationDate(lastmodified.getValue());
        }
        attributes.setStorageClass(object.getStorageClass());
        if(StringUtils.isNotBlank(object.getEtag())) {
            attributes.setETag(object.getEtag());
        }
        if(versioning.isEnabled()) {
            // The content generation of this object. Used for object versioning.
            attributes.setVersionId(String.valueOf(object.getGeneration()));
            // Noncurrent versions of objects have a timeDeleted property.
            attributes.setDuplicate(object.getTimeDeleted() != null);
        }
        if(object.getKmsKeyName() != null) {
            attributes.setEncryption(new Encryption.Algorithm("AES256",
                object.getKmsKeyName()) {
                @Override
                public String getDescription() {
                    return String.format("SSE-KMS (%s)", key);
                }
            });
        }
        attributes.setChecksum(Checksum.parse(object.getMd5Hash()));
        if(object.getMetadata() != null) {
            attributes.setMetadata(object.getMetadata());
        }
        return attributes;
    }
}
