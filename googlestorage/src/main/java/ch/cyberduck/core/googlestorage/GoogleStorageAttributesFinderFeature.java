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

import ch.cyberduck.core.CancellingListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ListCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.io.Checksum;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;

import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.StorageObject;

public class GoogleStorageAttributesFinderFeature implements AttributesFinder, AttributesAdapter<StorageObject> {
    private static final Logger log = LogManager.getLogger(GoogleStorageAttributesFinderFeature.class);

    private final PathContainerService containerService;
    private final GoogleStorageSession session;

    public static final String KEY_REQUESTER_PAYS = "requester_pays";

    public GoogleStorageAttributesFinderFeature(final GoogleStorageSession session) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }
        try {
            if(containerService.isContainer(file)) {
                final Storage.Buckets.Get request = session.getClient().buckets().get(containerService.getContainer(file).getName());
                if(containerService.getContainer(file).attributes().getCustom().containsKey(GoogleStorageAttributesFinderFeature.KEY_REQUESTER_PAYS)) {
                    request.setUserProject(session.getHost().getCredentials().getUsername());
                }
                return this.toAttributes(request.execute());
            }
            else {
                final Storage.Objects.Get get = session.getClient().objects().get(
                        containerService.getContainer(file).getName(), containerService.getKey(file));
                if(containerService.getContainer(file).attributes().getCustom().containsKey(GoogleStorageAttributesFinderFeature.KEY_REQUESTER_PAYS)) {
                    get.setUserProject(session.getHost().getCredentials().getUsername());
                }
                final VersioningConfiguration versioning = null != session.getFeature(Versioning.class) ? session.getFeature(Versioning.class).getConfiguration(
                        containerService.getContainer(file)
                ) : VersioningConfiguration.empty();
                if(versioning.isEnabled()) {
                    if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
                        get.setGeneration(Long.parseLong(file.attributes().getVersionId()));
                    }
                }
                final PathAttributes attributes;
                try {
                    attributes = this.toAttributes(get.execute());
                }
                catch(IOException e) {
                    if(file.isDirectory()) {
                        final BackgroundException failure = new GoogleStorageExceptionMappingService().map("Failure to read attributes of {0}", e, file);
                        if(failure instanceof NotfoundException) {
                            if(log.isDebugEnabled()) {
                                log.debug("Search for common prefix {}", file);
                            }
                            // File may be marked as placeholder but no placeholder file exists. Check for common prefix returned.
                            try {
                                new GoogleStorageObjectListService(session).list(file, new CancellingListProgressListener(), String.valueOf(Path.DELIMITER), 1, VersioningConfiguration.empty());
                            }
                            catch(ListCanceledException l) {
                                // Found common prefix
                                return PathAttributes.EMPTY;
                            }
                            catch(NotfoundException n) {
                                throw e;
                            }
                            // Found common prefix
                            return PathAttributes.EMPTY;
                        }
                    }
                    throw e;
                }
                if(versioning.isEnabled()) {
                    // Determine if latest version
                    try {
                        // Duplicate if not latest version
                        final Storage.Objects.Get request = session.getClient().objects().get(
                                containerService.getContainer(file).getName(), containerService.getKey(file));
                        if(containerService.getContainer(file).attributes().getCustom().containsKey(GoogleStorageAttributesFinderFeature.KEY_REQUESTER_PAYS)) {
                            request.setUserProject(session.getHost().getCredentials().getUsername());
                        }
                        final String latest = this.toAttributes(request.execute()).getVersionId();
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
        if(bucket.getBilling() != null) {
            attributes.setCustom(Collections.singletonMap(GoogleStorageAttributesFinderFeature.KEY_REQUESTER_PAYS, String.valueOf(true)));
        }
        return attributes;
    }

    @Override
    public PathAttributes toAttributes(final StorageObject object) {
        final PathAttributes attributes = new PathAttributes();
        if(object.getSize() != null) {
            attributes.setSize(object.getSize().longValue());
        }
        if(object.getTimeCreated() != null) {
            attributes.setCreationDate(object.getTimeCreated().getValue());
        }
        if(object.getUpdated() != null) {
            attributes.setModificationDate(object.getUpdated().getValue());
        }
        if(object.getCustomTime() != null) {
            attributes.setModificationDate(object.getCustomTime().getValue());
        }
        attributes.setStorageClass(object.getStorageClass());
        if(StringUtils.isNotBlank(object.getEtag())) {
            attributes.setETag(object.getEtag());
        }
        // The content generation of this object. Used for object versioning.
        attributes.setVersionId(String.valueOf(object.getGeneration()));
        // Noncurrent versions of objects have a timeDeleted property.
        attributes.setDuplicate(object.getTimeDeleted() != null);
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
