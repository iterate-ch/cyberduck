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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.date.InvalidDateException;
import ch.cyberduck.core.date.RFC1123DateFormatter;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.io.Checksum;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

import ch.iterate.openstack.swift.Constants;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.ContainerInfo;
import ch.iterate.openstack.swift.model.ObjectMetadata;
import ch.iterate.openstack.swift.model.Region;

public class SwiftAttributesFinderFeature implements AttributesFinder {
    private static final Logger log = Logger.getLogger(SwiftAttributesFinderFeature.class);

    private final SwiftSession session;

    private final PathContainerService containerService
            = new PathContainerService();

    private final RFC1123DateFormatter dateParser
            = new RFC1123DateFormatter();

    private final SwiftRegionService regionService;

    public SwiftAttributesFinderFeature(SwiftSession session) {
        this(session, new SwiftRegionService(session));
    }

    public SwiftAttributesFinderFeature(final SwiftSession session, final SwiftRegionService regionService) {
        this.session = session;
        this.regionService = regionService;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }
        final Region region = regionService.lookup(file);
        try {
            if(containerService.isContainer(file)) {
                final ContainerInfo info = session.getClient().getContainerInfo(region,
                        containerService.getContainer(file).getName());
                final PathAttributes attributes = new PathAttributes();
                attributes.setSize(info.getTotalSize());
                attributes.setRegion(info.getRegion().getRegionId());
                return attributes;
            }
            final PathAttributes attributes = new PathAttributes();
            final ObjectMetadata metadata = session.getClient().getObjectMetaData(region,
                    containerService.getContainer(file).getName(), containerService.getKey(file));
            if(file.isDirectory()) {
                if(!StringUtils.equals("application/directory", metadata.getMimeType())) {
                    throw new NotfoundException(String.format("Path %s is file", file.getAbsolute()));
                }
            }
            if(file.isFile()) {
                if(StringUtils.equals("application/directory", metadata.getMimeType())) {
                    throw new NotfoundException(String.format("Path %s is directory", file.getAbsolute()));
                }
            }
            attributes.setSize(Long.valueOf(metadata.getContentLength()));
            try {
                attributes.setModificationDate(dateParser.parse(metadata.getLastModified()).getTime());
            }
            catch(InvalidDateException e) {
                log.warn(String.format("%s is not RFC 1123 format %s", metadata.getLastModified(), e.getMessage()));
            }
            if(StringUtils.isNotBlank(metadata.getETag())) {
                final String etag = StringUtils.removePattern(metadata.getETag(), "\"");
                attributes.setETag(etag);
                if(metadata.getMetaData().containsKey(Constants.X_STATIC_LARGE_OBJECT)) {
                    // For manifest files, the ETag in the response for a GET or HEAD on the manifest file is the MD5 sum of
                    // the concatenated string of ETags for each of the segments in the manifest.
                    attributes.setChecksum(Checksum.NONE);
                }
                else {
                    attributes.setChecksum(Checksum.parse(etag));
                }
            }
            return attributes;
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public AttributesFinder withCache(final PathCache cache) {
        return this;
    }
}
