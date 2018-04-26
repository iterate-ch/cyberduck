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
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;

import org.apache.log4j.Logger;
import org.jets3t.service.model.MultipartUpload;

import java.util.EnumSet;

public class S3ListService implements ListService {
    private static final Logger log = Logger.getLogger(S3ListService.class);

    private final S3Session session;

    public S3ListService(final S3Session session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(directory.isRoot()) {
            // List all buckets
            return new S3BucketListService(session, new S3LocationFeature.S3Region(session.getHost().getRegion())).list(directory, listener);
        }
        else {
            AttributedList<Path> objects;
            try {
                objects = new S3VersionedObjectListService(session).list(directory, listener);
            }
            catch(AccessDeniedException | InteroperabilityException e) {
                log.warn(String.format("Ignore failure listing versioned objects. %s", e.getDetail()));
                objects = new S3ObjectListService(session).list(directory, listener);
            }
            try {
                for(MultipartUpload upload : new S3DefaultMultipartService(session).find(directory)) {
                    final PathAttributes attributes = new PathAttributes();
                    attributes.setVersionId(upload.getUploadId());
                    attributes.setModificationDate(upload.getInitiatedDate().getTime());
                    objects.add(new Path(directory, upload.getObjectKey(), EnumSet.of(Path.Type.file, Path.Type.upload), attributes));
                }
            }
            catch(AccessDeniedException | InteroperabilityException e) {
                log.warn(String.format("Ignore failure listing incomplete multipart uploads. %s", e.getDetail()));
            }
            return objects;
        }
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        return null;
    }
}
