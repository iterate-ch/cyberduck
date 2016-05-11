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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Attributes;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.io.Checksum;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @version $Id$
 */
public class S3AttributesFeature implements Attributes {
    private static final Logger log = Logger.getLogger(S3AttributesFeature.class);

    private S3Session session;

    private PathContainerService containerService
            = new S3PathContainerService();

    public S3AttributesFeature(final S3Session session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }
        if(containerService.isContainer(file)) {
            return PathAttributes.EMPTY;
        }
        else {
            return this.convert(this.details(file));
        }
    }

    @Override
    public Attributes withCache(final PathCache cache) {
        return this;
    }

    protected StorageObject details(final Path file) throws BackgroundException {
        final String container = containerService.getContainer(file).getName();
        try {
            final Versioning feature = session.getFeature(Versioning.class);
            if(feature != null && feature.getConfiguration(containerService.getContainer(file)).isEnabled()) {
                return session.getClient().getVersionedObjectDetails(file.attributes().getVersionId(),
                        container, containerService.getKey(file));
            }
            else {
                return session.getClient().getObjectDetails(container, containerService.getKey(file));
            }
        }
        catch(ServiceException e) {
            if(new ServiceExceptionMappingService().map(e) instanceof AccessDeniedException) {
                log.warn(String.format("Missing permission to read object details for %s %s", file, e.getMessage()));
                final StorageObject object = new StorageObject(containerService.getKey(file));
                object.setBucketName(container);
                return object;
            }
            throw new ServiceExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    protected PathAttributes convert(final StorageObject object) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setSize(object.getContentLength());
        final Date lastmodified = object.getLastModifiedDate();
        if(lastmodified != null) {
            attributes.setModificationDate(lastmodified.getTime());
        }
        attributes.setStorageClass(object.getStorageClass());
        if(StringUtils.isNotBlank(object.getETag())) {
            attributes.setChecksum(Checksum.parse(object.getETag()));
            attributes.setETag(object.getETag());
        }
        if(object instanceof S3Object) {
            attributes.setVersionId(((S3Object) object).getVersionId());
        }
        if(object.containsMetadata("x-amz-server-side-encryption-aws-kms-key-id")) {
            attributes.setEncryption(new Encryption.Properties(object.getServerSideEncryptionAlgorithm(),
                    object.getMetadata("x-amz-server-side-encryption-aws-kms-key-id").toString()));
        }
        else {
            // AES256 or None
            attributes.setEncryption(new Encryption.Properties(object.getServerSideEncryptionAlgorithm(), null));
        }
        final HashMap<String, String> metadata = new HashMap<String, String>();
        final Map<String, Object> source = object.getModifiableMetadata();
        for(Map.Entry<String, Object> entry : source.entrySet()) {
            metadata.put(entry.getKey(), entry.getValue().toString());
        }
        attributes.setMetadata(metadata);
        return attributes;
    }
}
