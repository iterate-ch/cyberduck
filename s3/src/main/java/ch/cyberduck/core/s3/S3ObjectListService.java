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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.StorageObjectsChunk;
import org.jets3t.service.VersionOrDeleteMarkersChunk;
import org.jets3t.service.model.BaseVersionOrDeleteMarker;
import org.jets3t.service.model.S3Version;
import org.jets3t.service.model.StorageObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class S3ObjectListService implements ListService {
    private static final Logger log = Logger.getLogger(S3ObjectListService.class);

    private final Preferences preferences
            = PreferencesFactory.get();

    private final S3Session session;

    private final PathContainerService containerService
            = new S3PathContainerService();

    private final S3AttributesFinderFeature attributes;

    public S3ObjectListService(final S3Session session) {
        this.session = session;
        this.attributes = new S3AttributesFinderFeature(session);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
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
                if(!prefix.endsWith(String.valueOf(Path.DELIMITER))) {
                    prefix += Path.DELIMITER;
                }
            }
            // If this optional, Unicode string parameter is included with your request,
            // then keys that contain the same string between the prefix and the first
            // occurrence of the delimiter will be rolled up into a single result
            // element in the CommonPrefixes collection. These rolled-up keys are
            // not returned elsewhere in the response.
            final AttributedList<Path> objects = new AttributedList<Path>();
            final Path container = containerService.getContainer(directory);
            objects.addAll(this.listObjects(container, directory, prefix, String.valueOf(Path.DELIMITER), listener));
            final Versioning feature = session.getFeature(Versioning.class);
            if(feature != null && feature.getConfiguration(container).isEnabled()) {
                String priorLastKey = null;
                String priorLastVersionId = null;
                do {
                    final VersionOrDeleteMarkersChunk chunk = session.getClient().listVersionedObjectsChunked(
                            container.getName(), prefix, String.valueOf(Path.DELIMITER),
                            preferences.getInteger("s3.listing.chunksize"),
                            priorLastKey, priorLastVersionId, true);
                    objects.addAll(this.listVersions(container, directory,
                            Arrays.asList(chunk.getItems())));
                    priorLastKey = chunk.getNextKeyMarker();
                    priorLastVersionId = chunk.getNextVersionIdMarker();
                    listener.chunk(directory, objects);
                }
                while(priorLastKey != null);
            }
            return objects;
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, directory);
        }
    }

    private AttributedList<Path> listObjects(final Path bucket, final Path parent,
                                             final String prefix, final String delimiter,
                                             final ListProgressListener listener)
            throws IOException, ServiceException, BackgroundException {
        final AttributedList<Path> children = new AttributedList<Path>();
        // Null if listing is complete
        String priorLastKey = null;
        do {
            // Read directory listing in chunks. List results are always returned
            // in lexicographic (alphabetical) order.
            final StorageObjectsChunk chunk = session.getClient().listObjectsChunked(
                    PathNormalizer.name(URIEncoder.encode(bucket.getName())), prefix, delimiter,
                    preferences.getInteger("s3.listing.chunksize"), priorLastKey);

            final StorageObject[] objects = chunk.getObjects();
            for(StorageObject object : objects) {
                final String key = PathNormalizer.normalize(object.getKey());
                if(new Path(bucket, key, EnumSet.of(Path.Type.directory)).equals(parent)) {
                    continue;
                }
                final EnumSet<AbstractPath.Type> types = object.isDirectoryPlaceholder()
                        ? EnumSet.of(Path.Type.directory, Path.Type.placeholder) : EnumSet.of(Path.Type.file);
                final Path file = new Path(parent, PathNormalizer.name(key), types,
                        attributes.convert(object));
                // Copy bucket location
                file.attributes().setRegion(bucket.attributes().getRegion());
                children.add(file);
            }
            final String[] prefixes = chunk.getCommonPrefixes();
            for(String common : prefixes) {
                if(common.equals(String.valueOf(Path.DELIMITER))) {
                    log.warn(String.format("Skipping prefix %s", common));
                    continue;
                }
                final String key = PathNormalizer.normalize(common);
                if(new Path(bucket, key, EnumSet.of(Path.Type.directory)).equals(parent)) {
                    continue;
                }
                final Path file = new Path(parent, PathNormalizer.name(key), EnumSet.of(Path.Type.directory, Path.Type.placeholder));
                file.attributes().setRegion(bucket.attributes().getRegion());
                children.add(file);
            }
            priorLastKey = chunk.getPriorLastKey();
            listener.chunk(parent, children);
        }
        while(priorLastKey != null);
        return children;
    }

    private List<Path> listVersions(final Path bucket, final Path parent, final List<BaseVersionOrDeleteMarker> versionOrDeleteMarkers)
            throws IOException, ServiceException {
        // Amazon S3 returns object versions in the order in which they were
        // stored, with the most recently stored returned first.
        Collections.sort(versionOrDeleteMarkers, new Comparator<BaseVersionOrDeleteMarker>() {
            @Override
            public int compare(BaseVersionOrDeleteMarker o1, BaseVersionOrDeleteMarker o2) {
                return o1.getLastModified().compareTo(o2.getLastModified());
            }
        });
        final List<Path> versions = new ArrayList<Path>();
        int i = 0;
        for(BaseVersionOrDeleteMarker marker : versionOrDeleteMarkers) {
            if((marker.isDeleteMarker() && marker.isLatest()) || !marker.isLatest()) {
                // Latest version already in default listing
                final String key = PathNormalizer.normalize(marker.getKey());
                if(new Path(bucket, key, EnumSet.of(Path.Type.directory)).equals(parent)) {
                    continue;
                }
                final Path p = new Path(parent, PathNormalizer.name(key), EnumSet.of(Path.Type.file));
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
                versions.add(p);
            }
        }
        return versions;
    }
}
