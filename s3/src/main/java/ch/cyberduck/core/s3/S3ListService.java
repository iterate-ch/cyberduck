package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.ResolveFailedException;
import ch.cyberduck.core.features.Versioning;

import org.apache.log4j.Logger;
import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.model.MultipartUpload;

import java.util.EnumSet;

public class S3ListService implements ListService {
    private static final Logger log = Logger.getLogger(S3ListService.class);

    private final S3Session session;

    private final PathContainerService containerService
        = new S3PathContainerService();

    public S3ListService(final S3Session session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(directory.isRoot()) {
            // List all buckets
            try {
                return new S3BucketListService(session, new S3LocationFeature.S3Region(session.getHost().getRegion())).list(directory, listener);
            }
            catch(InteroperabilityException e) {
                // Bucket set in hostname that leads to parser failure for XML reply
                log.warn(String.format("Ignore failure %s listing buckets.", e));
            }
        }
        try {
            return this.listObjects(directory, listener);
        }
        catch(ResolveFailedException e) {
            log.warn(String.format("Failure %s resolving bucket name. Disable use of DNS bucket names", e));
            final Jets3tProperties configuration = session.getClient().getJetS3tProperties();
            configuration.setProperty("s3service.disable-dns-buckets", String.valueOf(true));
            return this.listObjects(directory, listener);
        }
    }

    private AttributedList<Path> listObjects(final Path directory, final ListProgressListener listener) throws BackgroundException {
        AttributedList<Path> objects;
        final VersioningConfiguration versioning = null != session.getFeature(Versioning.class) ? session.getFeature(Versioning.class).getConfiguration(
            containerService.getContainer(directory)
        ) : VersioningConfiguration.empty();
        if(versioning.isEnabled()) {
            try {
                objects = new S3VersionedObjectListService(session).list(directory, listener);
            }
            catch(AccessDeniedException | InteroperabilityException e) {
                log.warn(String.format("Ignore failure listing versioned objects. %s", e));
                objects = new S3ObjectListService(session).list(directory, listener);
            }
        }
        else {
            objects = new S3ObjectListService(session).list(directory, listener);
        }
        try {
            for(MultipartUpload upload : new S3DefaultMultipartService(session).find(directory)) {
                final PathAttributes attributes = new PathAttributes();
                attributes.setDuplicate(true);
                attributes.setVersionId(upload.getUploadId());
                attributes.setModificationDate(upload.getInitiatedDate().getTime());
                objects.add(new Path(directory, upload.getObjectKey(), EnumSet.of(Path.Type.file, Path.Type.upload), attributes));
            }
        }
        catch(AccessDeniedException | InteroperabilityException e) {
            log.warn(String.format("Ignore failure listing incomplete multipart uploads. %s", e));
        }
        return objects;
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        return this;
    }
}
