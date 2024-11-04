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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DefaultPathContainerService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.StorageObject;

public class SwiftObjectListService implements ListService {
    private static final Logger log = LogManager.getLogger(SwiftObjectListService.class);

    private final SwiftSession session;
    private final PathContainerService containerService = new DefaultPathContainerService();
    private final SwiftRegionService regionService;
    private final SwiftAttributesFinderFeature attributes;

    public SwiftObjectListService(final SwiftSession session) {
        this(session, new SwiftRegionService(session));
    }

    public SwiftObjectListService(final SwiftSession session, final SwiftRegionService regionService) {
        this.session = session;
        this.regionService = regionService;
        this.attributes = new SwiftAttributesFinderFeature(session, regionService);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final String prefix = containerService.isContainer(directory) ? StringUtils.EMPTY : containerService.getKey(directory) + Path.DELIMITER;
        return this.list(directory, listener, prefix);
    }

    protected AttributedList<Path> list(final Path directory, final ListProgressListener listener, final String prefix) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<>();
            final int limit = new HostPreferences(session.getHost()).getInteger("openstack.list.object.limit");
            String marker = null;
            List<StorageObject> list;
            final Path container = containerService.getContainer(directory);
            do {
                list = session.getClient().listObjectsStartingWith(regionService.lookup(container), container.getName(),
                        prefix, null, limit, marker, Path.DELIMITER);
                for(StorageObject object : list) {
                    final PathAttributes attr = attributes.toAttributes(object);
                    String name = StringUtils.removeStart(object.getName(), prefix);
                    if(StringUtils.endsWith(name, String.valueOf(Path.DELIMITER))) {
                        // Must remove trailing delimiter
                        name = StringUtils.removeEnd(name, String.valueOf(Path.DELIMITER));
                        if(children.contains(new Path(directory, name, EnumSet.of(Path.Type.directory), attr))) {
                            // There is already a real placeholder file with application/directory MIME type. Only add virtual directory if the placeholder object is missing
                            continue;
                        }
                    }
                    final EnumSet<Path.Type> types = SwiftDirectoryFeature.DIRECTORY_MIME_TYPE.equals(object.getMimeType()) ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file);
                    attr.setOwner(container.attributes().getOwner());
                    attr.setRegion(container.attributes().getRegion());
                    children.add(new Path(directory, name, types, attr));
                    marker = object.getName();
                }
                listener.chunk(directory, children);
            }
            while(list.size() == limit);
            if(!containerService.isContainer(directory)) {
                if(children.isEmpty()) {
                    try {
                        // Do not throw failure if placeholder is found
                        final List<StorageObject> chunk = session.getClient().listObjectsStartingWith(regionService.lookup(container), container.getName(),
                                containerService.getKey(directory), null, 1, null, Path.DELIMITER);
                        if(chunk.stream().filter(object -> SwiftDirectoryFeature.DIRECTORY_MIME_TYPE.equals(object.getMimeType()))
                                .map(StorageObject::getName).noneMatch(key -> key.equals(containerService.getKey(directory)))) {
                            log.warn("No placeholder found for directory {}", directory);
                            throw new NotfoundException(directory.getAbsolute());
                        }
                    }
                    catch(GenericException e) {
                        throw new SwiftExceptionMappingService().map("Listing directory {0} failed", e, directory);
                    }
                    catch(IOException e) {
                        throw new DefaultIOExceptionMappingService().map(e, directory);
                    }
                }
            }
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
