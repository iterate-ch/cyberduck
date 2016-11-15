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
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.StorageObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SwiftSegmentService {
    private static final Logger log = Logger.getLogger(SwiftSegmentService.class);

    private final SwiftSession session;

    private final PathContainerService containerService
            = new SwiftPathContainerService();

    private final ISO8601DateParser dateParser
            = new ISO8601DateParser();

    /**
     * Segement files prefix
     */
    private final String prefix;

    private final SwiftRegionService regionService;

    public SwiftSegmentService(final SwiftSession session) {
        this(session, new SwiftRegionService(session));
    }

    public SwiftSegmentService(final SwiftSession session, final String prefix) {
        this(session, new SwiftRegionService(session), prefix);
    }

    public SwiftSegmentService(final SwiftSession session, final SwiftRegionService regionService) {
        this(session, regionService, PreferencesFactory.get().getProperty("openstack.upload.largeobject.segments.prefix"));
    }

    public SwiftSegmentService(final SwiftSession session, final SwiftRegionService regionService, final String prefix) {
        this.session = session;
        this.prefix = prefix;
        this.regionService = regionService;
    }

    public List<Path> list(final Path file) throws BackgroundException {
        try {
            final Path container = containerService.getContainer(file);
            final Map<String, List<StorageObject>> segments
                    = session.getClient().listObjectSegments(regionService.lookup(container),
                    container.getName(), containerService.getKey(file));
            if(null == segments) {
                // Not a large object
                return Collections.emptyList();
            }
            final List<Path> objects = new ArrayList<Path>();
            if(segments.containsKey(container.getName())) {
                for(StorageObject s : segments.get(container.getName())) {
                    final Path segment = new Path(container, s.getName(), EnumSet.of(Path.Type.file));
                    segment.attributes().setSize(s.getSize());
                    try {
                        segment.attributes().setModificationDate(dateParser.parse(s.getLastModified()).getTime());
                    }
                    catch(InvalidDateException e) {
                        log.warn(String.format("%s is not ISO 8601 format %s", s.getLastModified(), e.getMessage()));
                    }
                    if(StringUtils.isNotBlank(s.getMd5sum())) {
                        segment.attributes().setChecksum(Checksum.parse(s.getMd5sum()));
                    }
                    objects.add(segment);
                }
            }
            return objects;
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    public String basename(final Path file, final Long size) {
        return String.format("%s%s/%d", prefix, containerService.getKey(file), size);
    }

    public String name(final Path file, final Long size, int segmentNumber) {
        return String.format("%s/%08d", this.basename(file, size), segmentNumber);
    }

    /**
     * Create the appropriate manifest structure for a static large object (SLO).
     * The number of object segments is limited to a configurable amount, default 1000. Each segment,
     * except for the final one, must be at least 1 megabyte (configurable).
     *
     * @param objects Ordered list of segments
     * @return ETag returned by the simple upload total size of segment uploaded path of segment
     */
    public String manifest(final String container, final List<StorageObject> objects) {
        JsonArray manifestSLO = new JsonArray();
        for(StorageObject s : objects) {
            JsonObject segmentJSON = new JsonObject();
            // this is the container and object name in the format {container-name}/{object-name}
            segmentJSON.addProperty("path", String.format("/%s/%s", container, s.getName()));
            // MD5 checksum of the content of the segment object
            segmentJSON.addProperty("etag", s.getMd5sum());
            segmentJSON.addProperty("size_bytes", s.getSize());
            manifestSLO.add(segmentJSON);
        }
        return manifestSLO.toString();
    }

    /**
     * The value of the ETag header is calculated by taking
     * the ETag value of each segment, concatenating them together, and then returning the MD5 checksum of the result.
     *
     * @param checksum Checksum compute service
     * @param objects  Files
     * @return Concatenated checksum
     */
    public Checksum checksum(final ChecksumCompute checksum, final List<StorageObject> objects) throws ChecksumException {
        final StringBuilder concatenated = new StringBuilder();
        for(StorageObject s : objects) {
            concatenated.append(s.getMd5sum());
        }
        return checksum.compute(IOUtils.toInputStream(concatenated.toString(), Charset.defaultCharset()));
    }
}
