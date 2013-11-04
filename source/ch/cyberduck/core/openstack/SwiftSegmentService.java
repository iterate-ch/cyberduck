package ch.cyberduck.core.openstack;

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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.date.ISO8601DateParser;
import ch.cyberduck.core.date.InvalidDateException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.StorageObject;

/**
 * @version $Id$
 */
public class SwiftSegmentService {
    private static final Logger log = Logger.getLogger(SwiftSegmentService.class);

    private SwiftSession session;

    private PathContainerService containerService
            = new PathContainerService();

    private ISO8601DateParser dateParser
            = new ISO8601DateParser();

    public SwiftSegmentService(SwiftSession session) {
        this.session = session;
    }

    public SwiftSegmentService(PathContainerService containerService, Delete delete) {
        this.containerService = containerService;
    }

    public List<Path> list(final Path file) throws BackgroundException {
        try {
            final Path container = containerService.getContainer(file);
            final Map<String, List<StorageObject>> segments
                    = session.getClient().listObjectSegments(session.getRegion(container),
                    container.getName(), containerService.getKey(file));
            if(null == segments) {
                // Not a large object
                return Collections.emptyList();
            }
            final List<Path> objects = new ArrayList<Path>();
            if(segments.containsKey(container.getName())) {
                for(StorageObject s : segments.get(container.getName())) {
                    final Path segment = new Path(container, s.getName(), Path.FILE_TYPE);
                    segment.attributes().setSize(s.getSize());
                    try {
                        segment.attributes().setModificationDate(dateParser.parse(s.getLastModified()).getTime());
                    }
                    catch(InvalidDateException e) {
                        log.warn(String.format("%s is not ISO 8601 format %s", s.getLastModified(), e.getMessage()));
                    }
                    segment.attributes().setChecksum(s.getMd5sum());
                    objects.add(segment);
                }
            }
            return objects;
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Cannot read file attributes", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot read file attributes", e, file);
        }
    }
}
