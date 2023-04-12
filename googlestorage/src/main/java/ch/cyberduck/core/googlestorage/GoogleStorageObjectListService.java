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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Objects;
import com.google.api.services.storage.model.StorageObject;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Uninterruptibles;

public class GoogleStorageObjectListService implements ListService {
    private static final Logger log = LogManager.getLogger(GoogleStorageObjectListService.class);

    private final GoogleStorageSession session;
    private final GoogleStorageAttributesFinderFeature attributes;
    private final PathContainerService containerService;
    private final Integer concurrency;

    public GoogleStorageObjectListService(final GoogleStorageSession session) {
        this(session, new HostPreferences(session.getHost()).getInteger("googlestorage.listing.concurrency"));
    }

    public GoogleStorageObjectListService(final GoogleStorageSession session, final Integer concurrency) {
        this.session = session;
        this.attributes = new GoogleStorageAttributesFinderFeature(session);
        this.containerService = session.getFeature(PathContainerService.class);
        this.concurrency = concurrency;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return this.list(directory, listener, String.valueOf(Path.DELIMITER));
    }

    protected AttributedList<Path> list(final Path directory, final ListProgressListener listener, final String delimiter) throws BackgroundException {
        final VersioningConfiguration versioning = new HostPreferences(session.getHost()).getBoolean("googlestorage.listing.versioning.enable") &&
                null != session.getFeature(Versioning.class) ? session.getFeature(Versioning.class).getConfiguration(
                containerService.getContainer(directory)
        ) : VersioningConfiguration.empty();
        return this.list(directory, listener, delimiter, new HostPreferences(session.getHost()).getInteger("googlestorage.listing.chunksize"), versioning);
    }

    protected AttributedList<Path> list(final Path directory, final ListProgressListener listener, final String delimiter, final int chunksize,
                                        final VersioningConfiguration versioning) throws BackgroundException {
        final ThreadPool pool = ThreadPoolFactory.get("list", concurrency);
        try {
            final Path bucket = containerService.getContainer(directory);
            final AttributedList<Path> objects = new AttributedList<>();
            Objects response;
            long revision = 0L;
            String lastKey = null;
            String page = null;
            boolean hasDirectoryPlaceholder = containerService.isContainer(directory);
            do {
                final Storage.Objects.List request = session.getClient().objects().list(bucket.getName())
                        .setPageToken(page)
                        // lists all versions of an object as distinct results. The default is false
                        .setVersions(versioning.isEnabled())
                        .setMaxResults((long) chunksize)
                        .setDelimiter(delimiter)
                        .setPrefix(this.createPrefix(directory));
                if(bucket.attributes().getCustom().containsKey(GoogleStorageAttributesFinderFeature.KEY_REQUESTER_PAYS)) {
                    request.setUserProject(session.getHost().getCredentials().getUsername());
                }
                response = request.execute();
                if(response.getItems() != null) {
                    for(StorageObject object : response.getItems()) {
                        final String key = object.getName();
                        if(new SimplePathPredicate(PathNormalizer.compose(bucket, key)).test(directory)) {
                            if(log.isDebugEnabled()) {
                                log.debug(String.format("Skip placeholder key %s", key));
                            }
                            hasDirectoryPlaceholder = true;
                            continue;
                        }
                        if(!StringUtils.equals(lastKey, key)) {
                            // Reset revision for next file
                            revision = 0L;
                        }
                        final EnumSet<Path.Type> types = object.getName().endsWith(String.valueOf(Path.DELIMITER))
                                ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file);
                        final Path file;
                        final PathAttributes attr = attributes.toAttributes(object);
                        if(types.contains(Path.Type.file)) {
                            attr.setRevision(++revision);
                        }
                        // Copy bucket location
                        attr.setRegion(bucket.attributes().getRegion());
                        if(null == delimiter) {
                            // When searching for files recursively
                            file = new Path(String.format("%s/%s", bucket.getAbsolute(), key), types, attr);
                        }
                        else {
                            file = new Path(directory.isDirectory() ? directory : directory.getParent(), PathNormalizer.name(key), types, attr);
                        }
                        objects.add(file);
                        lastKey = key;
                    }
                }
                if(response.getPrefixes() != null) {
                    final List<Future<Path>> folders = new ArrayList<>();
                    for(String prefix : response.getPrefixes()) {
                        final String key = StringUtils.chomp(prefix, String.valueOf(Path.DELIMITER));
                        if(new SimplePathPredicate(PathNormalizer.compose(bucket, key)).test(directory)) {
                            continue;
                        }
                        if(versioning.isEnabled()) {
                            folders.add(this.submit(pool, bucket, directory, prefix));
                        }
                        else {
                            final Path file;
                            final PathAttributes attributes = new PathAttributes();
                            attributes.setRegion(bucket.attributes().getRegion());
                            if(null == delimiter) {
                                // When searching for files recursively
                                file = new Path(String.format("%s/%s", bucket.getAbsolute(), key), EnumSet.of(Path.Type.directory, Path.Type.placeholder), attributes);
                            }
                            else {
                                file = new Path(directory, PathNormalizer.name(key), EnumSet.of(Path.Type.directory, Path.Type.placeholder), attributes);
                            }
                            folders.add(ConcurrentUtils.constantFuture(file));
                        }
                    }
                    for(Future<Path> f : folders) {
                        try {
                            objects.add(Uninterruptibles.getUninterruptibly(f));
                        }
                        catch(ExecutionException e) {
                            log.warn(String.format("Listing versioned objects failed with execution failure %s", e.getMessage()));
                            Throwables.throwIfInstanceOf(Throwables.getRootCause(e), BackgroundException.class);
                            throw new DefaultExceptionMappingService().map(Throwables.getRootCause(e));
                        }
                    }
                }
                page = response.getNextPageToken();
                objects.filter(objects, (o1, o2) -> session.getHost().getProtocol().getListComparator().compare(o1.getName(), o2.getName()), null);
                listener.chunk(directory, objects);
            }
            while(page != null);
            if(!hasDirectoryPlaceholder && objects.isEmpty()) {
                if(log.isWarnEnabled()) {
                    log.warn(String.format("No placeholder found for directory %s", directory));
                }
                throw new NotfoundException(directory.getAbsolute());
            }
            return objects;
        }
        catch(IOException e) {
            throw new GoogleStorageExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }

    /**
     * Determine path from prefix. Path will have duplicate marker set in attributes when all containing files for the
     * prefix have a delete marker set.
     *
     * @param pool      Thread pool to run task with
     * @param bucket    Bucket
     * @param directory The directory for which contents are listed
     * @param prefix    URI decoded common prefix found in directory
     * @return Path to add to directory list
     */
    private Future<Path> submit(final ThreadPool pool, final Path bucket, final Path directory, final String prefix) {
        return pool.execute(new BackgroundExceptionCallable<Path>() {
            @Override
            public Path call() throws BackgroundException {
                final PathAttributes attr = new PathAttributes();
                attr.setRegion(bucket.attributes().getRegion());
                final String key = StringUtils.chomp(prefix, String.valueOf(Path.DELIMITER));
                try {
                    final Storage.Objects.List list = session.getClient().objects().list(bucket.getName())
                            .setVersions(true)
                            .setMaxResults(1L)
                            .setPrefix(prefix);
                    if(bucket.attributes().getCustom().containsKey(GoogleStorageAttributesFinderFeature.KEY_REQUESTER_PAYS)) {
                        list.setUserProject(session.getHost().getCredentials().getUsername());
                    }
                    final Objects versions = list.execute();
                    if(null != versions.getItems() && versions.getItems().size() == 1) {
                        final StorageObject version = versions.getItems().get(0);
                        if(version.getName().equals(prefix)) {
                            attr.setVersionId(String.valueOf(version.getGeneration()));
                        }
                        // Check if all of them are deleted
                        final Storage.Objects.List request = session.getClient().objects().list(bucket.getName())
                                .setVersions(false)
                                .setMaxResults(1L)
                                .setPrefix(prefix);
                        if(bucket.attributes().getCustom().containsKey(GoogleStorageAttributesFinderFeature.KEY_REQUESTER_PAYS)) {
                            request.setUserProject(session.getHost().getCredentials().getUsername());
                        }
                        final Objects unversioned = request.execute();
                        if(null == unversioned.getItems() || unversioned.getItems().size() == 0) {
                            attr.setDuplicate(true);
                        }
                    }
                    return new Path(directory, PathNormalizer.name(key), EnumSet.of(Path.Type.directory, Path.Type.placeholder), attr);
                }
                catch(IOException e) {
                    throw new GoogleStorageExceptionMappingService().map("Listing directory {0} failed", e, directory);
                }
            }
        });
    }

    protected String createPrefix(final Path directory) {
        // Keys can be listed by prefix. By choosing a common prefix
        // for the names of related keys and marking these keys with
        // a special character that delimits hierarchy, you can use the list
        // operation to select and browse keys hierarchically
        String prefix = StringUtils.EMPTY;
        if(!containerService.isContainer(directory)) {
            // Restricts the response to only contain results that begin with the
            // specified prefix. If you omit this optional argument, the value
            // of Prefix for your query will be the empty string.
            // In other words, the results will be not be restricted by prefix.
            prefix = containerService.getKey(directory);
            if(StringUtils.isBlank(prefix)) {
                return StringUtils.EMPTY;
            }
            if(directory.isDirectory()) {
                if(!prefix.endsWith(String.valueOf(Path.DELIMITER))) {
                    prefix += Path.DELIMITER;
                }
            }
        }
        return prefix;
    }
}
