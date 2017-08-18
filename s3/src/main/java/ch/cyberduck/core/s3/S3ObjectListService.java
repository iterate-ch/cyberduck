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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.StorageObjectsChunk;
import org.jets3t.service.model.StorageObject;

import java.util.EnumSet;

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
        return this.list(directory, listener, String.valueOf(Path.DELIMITER), preferences.getInteger("s3.listing.chunksize"));
    }

    public AttributedList<Path> list(final Path directory, final ListProgressListener listener, final String delimiter, final int chunksize) throws BackgroundException {
        try {
            final String prefix = this.createPrefix(directory);
            // If this optional, Unicode string parameter is included with your request,
            // then keys that contain the same string between the prefix and the first
            // occurrence of the delimiter will be rolled up into a single result
            // element in the CommonPrefixes collection. These rolled-up keys are
            // not returned elsewhere in the response.
            final Path bucket = containerService.getContainer(directory);
            final AttributedList<Path> children = new AttributedList<Path>();
            // Null if listing is complete
            String priorLastKey = null;
            do {
                // Read directory listing in chunks. List results are always returned
                // in lexicographic (alphabetical) order.
                final StorageObjectsChunk chunk = session.getClient().listObjectsChunked(
                        PathNormalizer.name(URIEncoder.encode(bucket.getName())), prefix, delimiter,
                        chunksize, priorLastKey);

                final StorageObject[] objects = chunk.getObjects();
                for(StorageObject object : objects) {
                    final String key = PathNormalizer.normalize(object.getKey());
                    if(new Path(bucket, key, EnumSet.of(Path.Type.directory)).equals(directory)) {
                        continue;
                    }
                    final EnumSet<AbstractPath.Type> types = object.getKey().endsWith(String.valueOf(Path.DELIMITER))
                            ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file);
                    final Path file;
                    final PathAttributes attributes = this.attributes.convert(object);
                    // Copy bucket location
                    attributes.setRegion(bucket.attributes().getRegion());
                    if(null == delimiter) {
                        file = new Path(String.format("%s%s%s", bucket.getAbsolute(), String.valueOf(Path.DELIMITER), key), types, attributes);
                    }
                    else {
                        file = new Path(directory, PathNormalizer.name(key), types, attributes);
                    }
                    children.add(file);
                }
                final String[] prefixes = chunk.getCommonPrefixes();
                for(String common : prefixes) {
                    if(common.equals(String.valueOf(Path.DELIMITER))) {
                        log.warn(String.format("Skipping prefix %s", common));
                        continue;
                    }
                    final String key = PathNormalizer.normalize(common);
                    if(new Path(bucket, key, EnumSet.of(Path.Type.directory)).equals(directory)) {
                        continue;
                    }
                    final Path file;
                    final PathAttributes attributes = new PathAttributes();
                    if(null == delimiter) {
                        file = new Path(String.format("%s%s%s", bucket.getAbsolute(), String.valueOf(Path.DELIMITER), key), EnumSet.of(Path.Type.directory, Path.Type.placeholder), attributes);
                    }
                    else {
                        file = new Path(directory, PathNormalizer.name(key), EnumSet.of(Path.Type.directory, Path.Type.placeholder), attributes);
                    }
                    attributes.setRegion(bucket.attributes().getRegion());
                    children.add(file);
                }
                priorLastKey = chunk.getPriorLastKey();
                listener.chunk(directory, children);
            }
            while(priorLastKey != null);
            return children;
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
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
            if(!prefix.endsWith(String.valueOf(Path.DELIMITER))) {
                prefix += Path.DELIMITER;
            }
        }
        return prefix;
    }
}
