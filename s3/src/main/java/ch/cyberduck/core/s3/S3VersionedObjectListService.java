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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.StorageObjectsChunk;
import org.jets3t.service.VersionOrDeleteMarkersChunk;
import org.jets3t.service.model.BaseVersionOrDeleteMarker;
import org.jets3t.service.model.S3Version;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.common.collect.ImmutableMap;

public class S3VersionedObjectListService extends S3AbstractListService implements ListService {
    private static final Logger log = Logger.getLogger(S3VersionedObjectListService.class);

    public static final String KEY_DELETE_MARKER = "delete_marker";

    private final Preferences preferences
        = PreferencesFactory.get();

    private final PathContainerService containerService
        = new S3PathContainerService();

    private final S3Session session;

    private final Integer concurrency;

    public S3VersionedObjectListService(final S3Session session) {
        this(session, PreferencesFactory.get().getInteger("s3.listing.concurrency"));
    }

    public S3VersionedObjectListService(final S3Session session, final Integer concurrency) {
        this.session = session;
        this.concurrency = concurrency;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final ThreadPool pool = ThreadPoolFactory.get("list", concurrency);
        try {
            final String prefix = this.createPrefix(directory);
            final Path bucket = containerService.getContainer(directory);
            final AttributedList<Path> children = new AttributedList<Path>();
            final List<Future<Path>> folders = new ArrayList<Future<Path>>();
            String priorLastKey = null;
            String priorLastVersionId = null;
            long revision = 0L;
            String lastKey = null;
            boolean placeholderFound = StringUtils.isEmpty(prefix);
            do {
                final VersionOrDeleteMarkersChunk chunk = session.getClient().listVersionedObjectsChunked(
                    bucket.getName(), prefix, String.valueOf(Path.DELIMITER),
                    preferences.getInteger("s3.listing.chunksize"),
                    priorLastKey, priorLastVersionId, true);
                // Amazon S3 returns object versions in the order in which they were
                // stored, with the most recently stored returned first.
                for(BaseVersionOrDeleteMarker marker : chunk.getItems()) {
                    final String key = PathNormalizer.normalize(marker.getKey());
                    if(String.valueOf(Path.DELIMITER).equals(key)) {
                        log.warn(String.format("Skipping prefix %s", key));
                        continue;
                    }
                    if(new Path(bucket, key, EnumSet.of(Path.Type.directory)).equals(directory)) {
                        // Placeholder object, skip
                        placeholderFound = true;
                        continue;
                    }
                    final PathAttributes attributes = new PathAttributes();
                    attributes.setVersionId("null".equals(marker.getVersionId()) ? null : marker.getVersionId());
                    if(!StringUtils.equals(lastKey, key)) {
                        // Reset revision for next file
                        revision = 0L;
                    }
                    attributes.setRevision(++revision);
                    attributes.setDuplicate((marker.isDeleteMarker() && marker.isLatest()) || !marker.isLatest());
                    if(marker.isDeleteMarker()) {
                        attributes.setCustom(Collections.singletonMap(KEY_DELETE_MARKER, Boolean.TRUE.toString()));
                    }
                    attributes.setModificationDate(marker.getLastModified().getTime());
                    attributes.setRegion(bucket.attributes().getRegion());
                    if(marker instanceof S3Version) {
                        final S3Version object = (S3Version) marker;
                        attributes.setSize(object.getSize());
                        if(StringUtils.isNotBlank(object.getEtag())) {
                            attributes.setETag(object.getEtag());
                        }
                        if(StringUtils.isNotBlank(object.getStorageClass())) {
                            attributes.setStorageClass(object.getStorageClass());
                        }
                    }
                    final Path f = new Path(directory, PathNormalizer.name(key), EnumSet.of(Path.Type.file), attributes);
                    children.add(f);
                    lastKey = key;
                }
                final String[] prefixes = chunk.getCommonPrefixes();
                for(String common : prefixes) {
                    if(String.valueOf(Path.DELIMITER).equals(common)) {
                        log.warn(String.format("Skipping prefix %s", common));
                        continue;
                    }
                    final String key = PathNormalizer.normalize(common);
                    if(new Path(bucket, key, EnumSet.of(Path.Type.directory)).equals(directory)) {
                        continue;
                    }
                    folders.add(this.submit(pool, bucket, common));
                }
                priorLastKey = chunk.getNextKeyMarker();
                priorLastVersionId = chunk.getNextVersionIdMarker();
                listener.chunk(directory, children);
            }
            while(priorLastKey != null);
            for(Future<Path> future : folders) {
                try {
                    children.add(future.get());
                }
                catch(InterruptedException e) {
                    log.error("Listing versioned objects failed with interrupt failure");
                    throw new ConnectionCanceledException(e);
                }
                catch(ExecutionException e) {
                    log.warn(String.format("Listing versioned objects failed with execution failure %s", e.getMessage()));
                    if(e.getCause() instanceof BackgroundException) {
                        throw (BackgroundException) e.getCause();
                    }
                    throw new BackgroundException(e.getCause());
                }
            }
            if(!placeholderFound && children.isEmpty()) {
                final ServiceException cause = new ServiceException();
                cause.setResponseCode(404);
                throw new S3ExceptionMappingService().map("Listing directory {0} failed", cause, directory);
            }
            return children;
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
        finally {
            // Cancel future tasks
            pool.shutdown(false);
        }
    }

    private Future<Path> submit(final ThreadPool pool, final Path bucket, final String common) {
        return pool.execute(new BackgroundExceptionCallable<Path>() {
            @Override
            public Path call() throws BackgroundException {
                final PathAttributes attributes = new PathAttributes();
                attributes.setRegion(bucket.attributes().getRegion());
                final Path prefix = new Path(String.format("%s%s", bucket.getAbsolute(), PathNormalizer.normalize(common)),
                    EnumSet.of(Path.Type.directory, Path.Type.placeholder), attributes);
                try {
                    final VersionOrDeleteMarkersChunk versions = session.getClient().listVersionedObjectsChunked(
                        bucket.getName(), common, String.valueOf(Path.DELIMITER), 1,
                        null, null, false);
                    if(versions.getItems().length == 1) {
                        final BaseVersionOrDeleteMarker version = versions.getItems()[0];
                        if(version.getKey().equals(common)) {
                            attributes.setVersionId("null".equals(version.getVersionId()) ? null : version.getVersionId());
                            if(version.isDeleteMarker()) {
                                attributes.setCustom(ImmutableMap.of(KEY_DELETE_MARKER, Boolean.TRUE.toString()));
                                attributes.setDuplicate(true);
                            }
                        }
                        else {
                            // no placeholder but objects inside - need to check if all of them are deleted
                            final StorageObjectsChunk unversioned = session.getClient().listObjectsChunked(bucket.getName(), common,
                                StringUtils.EMPTY, 1, null, false);
                            if(unversioned.getObjects().length == 0) {
                                attributes.setDuplicate(true);
                            }
                        }
                    }
                }
                catch(ServiceException e) {
                    throw new S3ExceptionMappingService().map("Listing directory {0} failed", e, prefix);
                }
                return prefix;
            }
        });
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        return this;
    }
}
