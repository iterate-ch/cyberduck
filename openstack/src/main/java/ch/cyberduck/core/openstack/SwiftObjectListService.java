package ch.cyberduck.core.openstack;

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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.date.ISO8601DateParser;
import ch.cyberduck.core.date.InvalidDateException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.StorageObject;

/**
 * @version $Id$
 */
public class SwiftObjectListService implements ListService {
    private static final Logger log = Logger.getLogger(SwiftObjectListService.class);

    private SwiftSession session;

    private PathContainerService containerService
            = new SwiftPathContainerService();

    private ISO8601DateParser dateParser
            = new ISO8601DateParser();

    private SwiftRegionService regionService;

    public SwiftObjectListService(final SwiftSession session) {
        this(session, new SwiftRegionService(session));
    }

    public SwiftObjectListService(final SwiftSession session, final SwiftRegionService regionService) {
        this.session = session;
        this.regionService = regionService;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<Path>();
            final int limit = PreferencesFactory.get().getInteger("openstack.list.object.limit");
            String marker = null;
            List<StorageObject> list;
            do {
                final Path container = containerService.getContainer(directory);
                list = session.getClient().listObjectsStartingWith(regionService.lookup(container), container.getName(),
                        containerService.isContainer(directory) ? StringUtils.EMPTY : containerService.getKey(directory) + Path.DELIMITER,
                        null, limit, marker, Path.DELIMITER);
                for(StorageObject object : list) {
                    final PathAttributes attributes = new PathAttributes();
                    attributes.setOwner(container.attributes().getOwner());
                    attributes.setRegion(container.attributes().getRegion());
                    if(StringUtils.isNotBlank(object.getMd5sum())) {
                        // For manifest files, the ETag in the response for a GET or HEAD on the manifest file is the MD5 sum of
                        // the concatenated string of ETags for each of the segments in the manifest.
                        attributes.setChecksum(Checksum.parse(object.getMd5sum()));
                    }
                    attributes.setSize(object.getSize());
                    final String lastModified = object.getLastModified();
                    if(lastModified != null) {
                        try {
                            attributes.setModificationDate(dateParser.parse(lastModified).getTime());
                        }
                        catch(InvalidDateException e) {
                            log.warn(String.format("%s is not ISO 8601 format %s", lastModified, e.getMessage()));
                        }
                    }
                    final EnumSet<AbstractPath.Type> types = "application/directory"
                            .equals(object.getMimeType()) ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file);
                    if(StringUtils.endsWith(object.getName(), String.valueOf(Path.DELIMITER))) {
                        if(children.contains(new Path(directory, PathNormalizer.name(object.getName()), EnumSet.of(Path.Type.directory), attributes))) {
                            // There is already a real placeholder file with application/directory MIME type. Only
                            // add virtual directory if the placeholder object is missing
                            continue;
                        }
                        types.add(Path.Type.placeholder);
                    }
                    children.add(new Path(directory, PathNormalizer.name(object.getName()), types, attributes));
                    marker = object.getName();
                }
                listener.chunk(directory, children);
            }
            while(list.size() == limit);
            return children;
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, directory);
        }
    }
}
