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
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.StorageObjectsChunk;
import org.jets3t.service.model.StorageObject;

import java.util.Arrays;
import java.util.EnumSet;

public class S3ObjectListService extends S3AbstractListService implements ListService {
    private static final Logger log = LogManager.getLogger(S3ObjectListService.class);

    private final PathContainerService containerService;
    private final S3Session session;
    private final S3AttributesFinderFeature attributes;

    private final boolean metadata;

    public S3ObjectListService(final S3Session session, final S3AccessControlListFeature acl) {
        this(session, acl, new HostPreferences(session.getHost()).getBoolean("s3.listing.metadata.enable"));
    }

    public S3ObjectListService(final S3Session session, final S3AccessControlListFeature acl, final boolean metadata) {
        super(session);
        this.session = session;
        this.attributes = new S3AttributesFinderFeature(session, acl);
        this.containerService = session.getFeature(PathContainerService.class);
        this.metadata = metadata;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return this.list(directory, listener, String.valueOf(Path.DELIMITER));
    }

    protected AttributedList<Path> list(final Path directory, final ListProgressListener listener, final String delimiter) throws BackgroundException {
        return this.list(directory, listener, delimiter, new HostPreferences(session.getHost()).getInteger("s3.listing.chunksize"));
    }

    protected AttributedList<Path> list(final Path directory, final ListProgressListener listener, final String delimiter, final int chunksize) throws BackgroundException {
        try {
            final String prefix = this.createPrefix(directory);
            if(log.isDebugEnabled()) {
                log.debug("List with prefix {}", prefix);
            }
            // If this optional, Unicode string parameter is included with your request,
            // then keys that contain the same string between the prefix and the first
            // occurrence of the delimiter will be rolled up into a single result
            // element in the CommonPrefixes collection. These rolled-up keys are
            // not returned elsewhere in the response.
            final Path bucket = containerService.getContainer(directory);
            final AttributedList<Path> objects = new AttributedList<>();
            // Null if listing is complete
            String priorLastKey = null;
            boolean hasDirectoryPlaceholder = bucket.isRoot() || containerService.isContainer(directory);
            do {
                // Read directory listing in chunks. List results are always returned
                // in lexicographic (alphabetical) order.
                final StorageObjectsChunk chunk = session.getClient().listObjectsChunked(
                        bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(), prefix, delimiter,
                        chunksize, priorLastKey, false);

                for(StorageObject object : chunk.getObjects()) {
                    final String key = URIEncoder.decode(object.getKey());
                    if(new SimplePathPredicate(PathNormalizer.compose(bucket, key)).test(directory)) {
                        if(log.isDebugEnabled()) {
                            log.debug("Skip placeholder key {}", key);
                        }
                        hasDirectoryPlaceholder = true;
                        continue;
                    }
                    final EnumSet<Path.Type> types = object.getKey().endsWith(String.valueOf(Path.DELIMITER))
                            ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file);
                    final Path f;
                    final PathAttributes attr = new S3AttributesAdapter(session.getHost()).toAttributes(object);
                    // Copy bucket location
                    attr.setRegion(bucket.attributes().getRegion());
                    if(null == delimiter) {
                        f = new Path(String.format("%s/%s", bucket.getAbsolute(), key), types, attr);
                    }
                    else {
                        f = new Path(directory.isDirectory() ? directory : directory.getParent(), PathNormalizer.name(key), types, attr);
                    }
                    if(metadata) {
                        f.withAttributes(attributes.find(f));
                    }
                    objects.add(f);
                }
                final String[] prefixes = chunk.getCommonPrefixes();
                for(String common : prefixes) {
                    if(log.isDebugEnabled()) {
                        log.debug("Handle common prefix {}", common);
                    }
                    final String key = StringUtils.chomp(URIEncoder.decode(common), String.valueOf(Path.DELIMITER));
                    if(new SimplePathPredicate(PathNormalizer.compose(bucket, key)).test(directory)) {
                        continue;
                    }
                    final Path f;
                    final PathAttributes attr = new PathAttributes();
                    attr.setRegion(bucket.attributes().getRegion());
                    if(null == delimiter) {
                        f = new Path(String.format("%s/%s", bucket.getAbsolute(), key),
                                EnumSet.of(Path.Type.directory, Path.Type.placeholder), attr);
                    }
                    else {
                        f = new Path(directory.isDirectory() ? directory : directory.getParent(), PathNormalizer.name(key),
                                EnumSet.of(Path.Type.directory, Path.Type.placeholder), attr);
                    }
                    objects.add(f);
                }
                priorLastKey = null != chunk.getPriorLastKey() ? URIEncoder.decode(chunk.getPriorLastKey()) : null;
                listener.chunk(directory, objects);
            }
            while(priorLastKey != null);
            if(!hasDirectoryPlaceholder && objects.isEmpty()) {
                // Only for AWS
                if(S3Session.isAwsHostname(session.getHost().getHostname())) {
                    if(StringUtils.isEmpty(RequestEntityRestStorageService.findBucketInHostname(session.getHost()))) {
                        if(log.isWarnEnabled()) {
                            log.warn("No placeholder found for directory {}", directory);
                        }
                        throw new NotfoundException(directory.getAbsolute());
                    }
                }
                else {
                    // Handle missing prefix for directory placeholders in Minio
                    final StorageObjectsChunk chunk = session.getClient().listObjectsChunked(
                            bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(),
                            String.format("%s%s", this.createPrefix(directory.getParent()), directory.getName()), delimiter, 1, null);
                    if(Arrays.stream(chunk.getCommonPrefixes()).map(URIEncoder::decode).noneMatch(common -> common.equals(prefix))) {
                        throw new NotfoundException(directory.getAbsolute());
                    }
                }
            }
            return objects;
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }
}
