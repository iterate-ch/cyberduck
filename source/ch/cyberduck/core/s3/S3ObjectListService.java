package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.ServiceExceptionMappingService;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.StorageObjectsChunk;
import org.jets3t.service.VersionOrDeleteMarkersChunk;
import org.jets3t.service.model.BaseVersionOrDeleteMarker;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.S3Version;
import org.jets3t.service.model.StorageObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @version $Id$
 */
public class S3ObjectListService implements ListService {
    private static final Logger log = Logger.getLogger(S3Session.class);

    private S3Session session;

    public S3ObjectListService(final S3Session session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path file) throws BackgroundException {
        try {
            // Keys can be listed by prefix. By choosing a common prefix
            // for the names of related keys and marking these keys with
            // a special character that delimits hierarchy, you can use the list
            // operation to select and browse keys hierarchically
            String prefix = StringUtils.EMPTY;
            if(!new PathContainerService().isContainer(file)) {
                // estricts the response to only contain results that begin with the
                // specified prefix. If you omit this optional argument, the value
                // of Prefix for your query will be the empty string.
                // In other words, the results will be not be restricted by prefix.
                prefix = new PathContainerService().getKey(file);
                if(!prefix.endsWith(String.valueOf(Path.DELIMITER))) {
                    prefix += Path.DELIMITER;
                }
            }
            // If this optional, Unicode string parameter is included with your request,
            // then keys that contain the same string between the prefix and the first
            // occurrence of the delimiter will be rolled up into a single result
            // element in the CommonPrefixes collection. These rolled-up keys are
            // not returned elsewhere in the response.
            final AttributedList<Path> children = new AttributedList<Path>();
            children.addAll(this.listObjects(
                    new PathContainerService().getContainer(file), file, prefix, String.valueOf(Path.DELIMITER)));
            if(Preferences.instance().getBoolean("s3.revisions.enable")) {
                if(new S3VersioningFeature(session).getConfiguration(new PathContainerService().getContainer(file)).isEnabled()) {
                    String priorLastKey = null;
                    String priorLastVersionId = null;
                    do {
                        final VersionOrDeleteMarkersChunk chunk = session.getClient().listVersionedObjectsChunked(
                                new PathContainerService().getContainer(file).getName(), prefix, String.valueOf(Path.DELIMITER),
                                Preferences.instance().getInteger("s3.listing.chunksize"),
                                priorLastKey, priorLastVersionId, true);
                        children.addAll(this.listVersions(new PathContainerService().getContainer(file), file,
                                Arrays.asList(chunk.getItems())));
                        priorLastKey = chunk.getNextKeyMarker();
                        priorLastVersionId = chunk.getNextVersionIdMarker();
                    }
                    while(priorLastKey != null);
                }
            }
            return children;
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Listing directory failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
    }

    private AttributedList<Path> listObjects(final Path bucket, final Path parent, final String prefix, final String delimiter)
            throws IOException, ServiceException, BackgroundException {
        final AttributedList<Path> children = new AttributedList<Path>();
        // Null if listing is complete
        String priorLastKey = null;
        do {
            // Read directory listing in chunks. List results are always returned
            // in lexicographic (alphabetical) order.
            final StorageObjectsChunk chunk = session.getClient().listObjectsChunked(
                    bucket.getName(), prefix, delimiter,
                    Preferences.instance().getInteger("s3.listing.chunksize"), priorLastKey);

            final StorageObject[] objects = chunk.getObjects();
            for(StorageObject object : objects) {
                final Path p = new Path(parent, Path.getName(PathNormalizer.normalize(object.getKey())), Path.FILE_TYPE);
                p.attributes().setSize(object.getContentLength());
                p.attributes().setModificationDate(object.getLastModifiedDate().getTime());
                p.attributes().setRegion(bucket.attributes().getRegion());
                p.attributes().setStorageClass(object.getStorageClass());
                p.attributes().setEncryption(object.getServerSideEncryptionAlgorithm());
                // Directory placeholders
                if(object.isDirectoryPlaceholder()) {
                    p.attributes().setType(Path.DIRECTORY_TYPE);
                    p.attributes().setPlaceholder(true);
                }
                else if(0 == object.getContentLength()) {
                    if("application/x-directory".equals(
                            new S3ObjectDetailService(session).getDetails(p).getContentType())) {
                        p.attributes().setType(Path.DIRECTORY_TYPE);
                        p.attributes().setPlaceholder(true);
                    }
                }
                final Object etag = object.getMetadataMap().get(StorageObject.METADATA_HEADER_ETAG);
                if(null != etag) {
                    final String checksum = etag.toString().replaceAll("\"", StringUtils.EMPTY);
                    p.attributes().setChecksum(checksum);
                    if(checksum.equals("d66759af42f282e1ba19144df2d405d0")) {
                        // Fix #5374 s3sync.rb interoperability
                        p.attributes().setType(Path.DIRECTORY_TYPE);
                        p.attributes().setPlaceholder(true);
                    }
                }
                if(object instanceof S3Object) {
                    p.attributes().setVersionId(((S3Object) object).getVersionId());
                }
                children.add(p);
            }
            final String[] prefixes = chunk.getCommonPrefixes();
            for(String common : prefixes) {
                if(common.equals(String.valueOf(Path.DELIMITER))) {
                    log.warn("Skipping prefix " + common);
                    continue;
                }
                final Path p = new Path(parent, Path.getName(PathNormalizer.normalize(common)), Path.DIRECTORY_TYPE);
                if(children.contains(p.getReference())) {
                    // There is already a placeholder object
                    continue;
                }
                p.attributes().setRegion(bucket.attributes().getRegion());
                p.attributes().setPlaceholder(false);
                children.add(p);
            }
            priorLastKey = chunk.getPriorLastKey();
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
            if((marker.isDeleteMarker() && marker.isLatest())
                    || !marker.isLatest()) {
                // Latest version already in default listing
                final Path p = new Path(parent, Path.getName(PathNormalizer.normalize(marker.getKey())), Path.FILE_TYPE);
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
