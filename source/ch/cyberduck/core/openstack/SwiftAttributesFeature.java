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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.date.InvalidDateException;
import ch.cyberduck.core.date.RFC1123DateFormatter;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Attributes;

import org.apache.log4j.Logger;

import java.io.IOException;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.ContainerInfo;
import ch.iterate.openstack.swift.model.ObjectMetadata;

/**
 * @version $Id$
 */
public class SwiftAttributesFeature implements Attributes {
    private static final Logger log = Logger.getLogger(SwiftAttributesFeature.class);

    private SwiftSession session;

    private PathContainerService containerService
            = new PathContainerService();

    private RFC1123DateFormatter dateParser
            = new RFC1123DateFormatter();

    public SwiftAttributesFeature(SwiftSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                final ContainerInfo info = session.getClient().getContainerInfo(new SwiftRegionService(session).lookup(containerService.getContainer(file)),
                        containerService.getContainer(file).getName());
                final PathAttributes attributes = new PathAttributes(Path.DIRECTORY_TYPE | Path.VOLUME_TYPE);
                attributes.setSize(info.getTotalSize());
                attributes.setRegion(info.getRegion().getRegionId());
                return attributes;
            }
            else {
                final ObjectMetadata metadata = session.getClient().getObjectMetaData(new SwiftRegionService(session).lookup(containerService.getContainer(file)),
                        containerService.getContainer(file).getName(), containerService.getKey(file));
                final PathAttributes attributes = new PathAttributes(
                        "application/directory".equals(metadata.getMimeType()) ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
                attributes.setSize(Long.valueOf(metadata.getContentLength()));
                try {
                    attributes.setModificationDate(dateParser.parse(metadata.getLastModified()).getTime());
                }
                catch(InvalidDateException e) {
                    log.warn(String.format("%s is not  RFC 1123 format %s", metadata.getLastModified(), e.getMessage()));
                }
                attributes.setChecksum(metadata.getETag());
                attributes.setETag(metadata.getETag());
                if("application/directory".equals(metadata.getMimeType())) {
                    attributes.setPlaceholder(true);
                }
                return attributes;
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Cannot read file attributes", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot read file attributes", e, file);
        }
    }

    @Override
    public Attributes withCache(final Cache cache) {
        return this;
    }
}
