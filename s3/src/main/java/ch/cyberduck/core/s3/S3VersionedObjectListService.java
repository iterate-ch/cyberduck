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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.preferences.HostPreferences;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.common.collect.ImmutableMap;

public class S3VersionedObjectListService extends S3AbstractListService implements ListService {
    private static final Logger log = Logger.getLogger(S3VersionedObjectListService.class);

    public static final String KEY_DELETE_MARKER = "delete_marker";

    private final PathContainerService containerService;
    private final S3Session session;
    private final S3AttributesFinderFeature attributes;
    private final Integer concurrency;

    /**
     * Reference previous versions in file attributes
     */
    private final boolean references;
    /**
     * Use HEAD request for every object found to add complete metadata in file attributes
     */
    private final boolean metadata;

    public S3VersionedObjectListService(final S3Session session) {
        this(session, new HostPreferences(session.getHost()).getInteger("s3.listing.concurrency"),
            new HostPreferences(session.getHost()).getBoolean("s3.versioning.references.enable"));
    }

    public S3VersionedObjectListService(final S3Session session, final boolean references) {
        this(session, new HostPreferences(session.getHost()).getInteger("s3.listing.concurrency"), references);
    }

    public S3VersionedObjectListService(final S3Session session, final Integer concurrency, final boolean references) {
        this(session, concurrency, references, new HostPreferences(session.getHost()).getBoolean("s3.listing.metadata.enable"));
    }

    /**
     * @param session     Connection
     * @param concurrency Number of threads to handle prefixes
     * @param references  Set references of previous versions in file attributes
     */
    public S3VersionedObjectListService(final S3Session session, final Integer concurrency, final boolean references, final boolean metadata) {
        super(session);
        this.session = session;
        this.attributes = new S3AttributesFinderFeature(session);
        this.concurrency = concurrency;
        this.references = references;
        this.containerService = session.getFeature(PathContainerService.class);
        this.metadata = metadata;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final ThreadPool pool = ThreadPoolFactory.get("list", concurrency);
        try {
            final String prefix = this.createPrefix(directory);
            final Path bucket = containerService.getContainer(directory);
            final AttributedList<Path> children = new AttributedList<>();
            final List<Future<Path>> folders = new ArrayList<>();
            String priorLastKey = null;
            String priorLastVersionId = null;
            long revision = 0L;
            String lastKey = null;
            boolean hasDirectoryPlaceholder = containerService.isContainer(directory);
            do {
                final VersionOrDeleteMarkersChunk chunk = session.getClient().listVersionedObjectsChunked(
                    bucket.getName(), prefix, String.valueOf(Path.DELIMITER),
                    new HostPreferences(session.getHost()).getInteger("s3.listing.chunksize"),
                    priorLastKey, priorLastVersionId, false);
                // Amazon S3 returns object versions in the order in which they were stored, with the most recently stored returned first.
                for(BaseVersionOrDeleteMarker marker : chunk.getItems()) {
                    final String key = PathNormalizer.normalize(URIEncoder.decode(marker.getKey()));
                    if(String.valueOf(Path.DELIMITER).equals(key)) {
                        log.warn(String.format("Skipping prefix %s", key));
                        continue;
                    }
                    if(new SimplePathPredicate(new Path(bucket, key, EnumSet.of(Path.Type.directory))).test(directory)) {
                        // Placeholder object, skip
                        hasDirectoryPlaceholder = true;
                        continue;
                    }
                    final PathAttributes attr = new PathAttributes();
                    attr.setVersionId("null".equals(marker.getVersionId()) ? null : marker.getVersionId());
                    if(!StringUtils.equals(lastKey, key)) {
                        // Reset revision for next file
                        revision = 0L;
                    }
                    attr.setRevision(++revision);
                    attr.setDuplicate(marker.isDeleteMarker() && marker.isLatest() || !marker.isLatest());
                    if(marker.isDeleteMarker()) {
                        attr.setCustom(Collections.singletonMap(KEY_DELETE_MARKER, String.valueOf(true)));
                    }
                    attr.setModificationDate(marker.getLastModified().getTime());
                    attr.setRegion(bucket.attributes().getRegion());
                    if(marker instanceof S3Version) {
                        final S3Version object = (S3Version) marker;
                        attr.setSize(object.getSize());
                        if(StringUtils.isNotBlank(object.getEtag())) {
                            attr.setETag(StringUtils.removeEnd(StringUtils.removeStart(object.getEtag(), "\""), "\""));
                        }
                        if(StringUtils.isNotBlank(object.getStorageClass())) {
                            attr.setStorageClass(object.getStorageClass());
                        }
                    }
                    final Path f = new Path(directory.isDirectory() ? directory : directory.getParent(),
                        PathNormalizer.name(key), EnumSet.of(Path.Type.file), attr);
                    if(metadata) {
                        f.withAttributes(attributes.find(f));
                    }
                    children.add(f);
                    lastKey = key;
                }
                if(references) {
                    for(Path f : children) {
                        if(f.attributes().isDuplicate()) {
                            final Path latest = children.find(new LatestVersionPathPredicate(f));
                            if(latest != null) {
                                // Reference version
                                final AttributedList<Path> versions = new AttributedList<>(latest.attributes().getVersions());
                                versions.add(f);
                                latest.attributes().setVersions(versions);
                            }
                            else {
                                log.warn(String.format("No current version found for %s", f));
                            }
                        }
                    }
                }
                final String[] prefixes = chunk.getCommonPrefixes();
                for(String common : prefixes) {
                    if(String.valueOf(Path.DELIMITER).equals(common)) {
                        log.warn(String.format("Skipping prefix %s", common));
                        continue;
                    }
                    final String key = PathNormalizer.normalize(URIEncoder.decode(common));
                    if(new SimplePathPredicate(new Path(bucket, key, EnumSet.of(Path.Type.directory))).test(directory)) {
                        continue;
                    }
                    folders.add(this.submit(pool, bucket, directory, URIEncoder.decode(common)));
                }
                priorLastKey = null != chunk.getNextKeyMarker() ? URIEncoder.decode(chunk.getNextKeyMarker()) : null;
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
            listener.chunk(directory, children);
            if(!hasDirectoryPlaceholder && children.isEmpty()) {
                // Only for AWS
                if(S3Session.isAwsHostname(session.getHost().getHostname())) {
                    throw new NotfoundException(directory.getAbsolute());
                }
                // Handle missing prefix for directory placeholders in Minio
                final VersionOrDeleteMarkersChunk chunk = session.getClient().listVersionedObjectsChunked(
                    PathNormalizer.name(URIEncoder.encode(bucket.getName())), String.format("%s%s", this.createPrefix(directory.getParent()), directory.getName()), String.valueOf(Path.DELIMITER), 1, null, null, false);
                if(Arrays.stream(chunk.getCommonPrefixes()).map(URIEncoder::decode).noneMatch(common -> common.equals(prefix))) {
                    throw new NotfoundException(directory.getAbsolute());
                }
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

    private Future<Path> submit(final ThreadPool pool, final Path bucket, final Path directory, final String common) {
        return pool.execute(new BackgroundExceptionCallable<Path>() {
            @Override
            public Path call() throws BackgroundException {
                final PathAttributes attr = new PathAttributes();
                attr.setRegion(bucket.attributes().getRegion());
                final Path prefix = new Path(directory, PathNormalizer.name(common),
                        EnumSet.of(Path.Type.directory, Path.Type.placeholder), attr);
                try {
                    final VersionOrDeleteMarkersChunk versions = session.getClient().listVersionedObjectsChunked(
                            bucket.getName(), common, null, 1,
                            null, null, false);
                    if(versions.getItems().length == 1) {
                        final BaseVersionOrDeleteMarker version = versions.getItems()[0];
                        if(URIEncoder.decode(version.getKey()).equals(common)) {
                            attr.setVersionId("null".equals(version.getVersionId()) ? null : version.getVersionId());
                            if(version.isDeleteMarker()) {
                                attr.setCustom(ImmutableMap.of(KEY_DELETE_MARKER, Boolean.TRUE.toString()));
                            }
                        }
                        // no placeholder but objects inside - need to check if all of them are deleted
                        final StorageObjectsChunk unversioned = session.getClient().listObjectsChunked(bucket.getName(), common,
                            null, 1, null, false);
                        if(unversioned.getObjects().length == 0) {
                            attr.setDuplicate(true);
                        }
                    }
                    return prefix;
                }
                catch(ServiceException e) {
                    throw new S3ExceptionMappingService().map("Listing directory {0} failed", e, prefix);
                }
            }
        });
    }

    private static final class LatestVersionPathPredicate extends SimplePathPredicate {
        public LatestVersionPathPredicate(final Path f) {
            super(f);
        }

        @Override
        public boolean test(final Path test) {
            if(super.test(test)) {
                return !test.attributes().isDuplicate();
            }
            return false;
        }
    }
}
