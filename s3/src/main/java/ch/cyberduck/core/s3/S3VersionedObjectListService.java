package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.jets3t.service.ServiceException;
import org.jets3t.service.VersionOrDeleteMarkersChunk;
import org.jets3t.service.model.BaseVersionOrDeleteMarker;
import org.jets3t.service.model.S3Version;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class S3VersionedObjectListService implements ListService {

    private final Preferences preferences
            = PreferencesFactory.get();

    private final PathContainerService containerService
            = new S3PathContainerService();

    private final S3Session session;

    public S3VersionedObjectListService(final S3Session session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final S3ObjectListService list = new S3ObjectListService(session);
        try {
            final String prefix = list.createPrefix(directory);
            final Path bucket = containerService.getContainer(directory);
            final AttributedList<Path> children = new AttributedList<Path>();
            final Versioning versioning = session.getFeature(Versioning.class);
            if(versioning.getConfiguration(bucket).isEnabled()) {
                String priorLastKey = null;
                String priorLastVersionId = null;
                do {
                    final VersionOrDeleteMarkersChunk chunk = session.getClient().listVersionedObjectsChunked(
                            bucket.getName(), prefix, String.valueOf(Path.DELIMITER),
                            preferences.getInteger("s3.listing.chunksize"),
                            priorLastKey, priorLastVersionId, true);
                    // Amazon S3 returns object versions in the order in which they were
                    // stored, with the most recently stored returned first.
                    final List<BaseVersionOrDeleteMarker> items = Arrays.asList(chunk.getItems());
                    int i = 0;
                    for(BaseVersionOrDeleteMarker marker : items) {
                        if((marker.isDeleteMarker() && marker.isLatest()) || !marker.isLatest()) {
                            // Latest version already in default listing
                            final String key = PathNormalizer.normalize(marker.getKey());
                            if(new Path(bucket, key, EnumSet.of(Path.Type.directory)).equals(directory)) {
                                continue;
                            }
                            final Path p = new Path(directory, PathNormalizer.name(key), EnumSet.of(Path.Type.file));
                            // Versioning is enabled if non null.
                            p.attributes().setVersionId(marker.getVersionId());
                            p.attributes().setRevision(++i);
                            p.attributes().setDuplicate(true);
                            p.attributes().setModificationDate(marker.getLastModified().getTime());
                            p.attributes().setRegion(bucket.attributes().getRegion());
                            if(marker instanceof S3Version) {
                                p.attributes().setSize(((S3Version) marker).getSize());
                                p.attributes().setETag(((S3Version) marker).getEtag());
                                p.attributes().setStorageClass(((S3Version) marker).getStorageClass());
                            }
                            children.add(p);
                        }
                    }
                    priorLastKey = chunk.getNextKeyMarker();
                    priorLastVersionId = chunk.getNextVersionIdMarker();
                    listener.chunk(directory, children);
                }
                while(priorLastKey != null);
            }
            return children;
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }
}
