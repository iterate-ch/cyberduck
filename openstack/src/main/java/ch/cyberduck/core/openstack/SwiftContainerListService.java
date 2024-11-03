package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.RootListService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import ch.iterate.openstack.swift.Client;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.Container;
import ch.iterate.openstack.swift.model.Region;

public class SwiftContainerListService implements RootListService {
    private static final Logger log = LogManager.getLogger(SwiftContainerListService.class);

    private final SwiftSession session;
    private final SwiftLocationFeature.SwiftRegion region;

    public SwiftContainerListService(final SwiftSession session, final SwiftLocationFeature.SwiftRegion region) {
        this.session = session;
        this.region = region;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        log.debug("List containers for {}", session);
        try {
            final AttributedList<Path> containers = new AttributedList<>();
            final int limit = new HostPreferences(session.getHost()).getInteger("openstack.list.container.limit");
            final Client client = session.getClient();
            for(final Region r : client.getRegions()) {
                if(region.getIdentifier() != null) {
                    if(!StringUtils.equals(r.getRegionId(), region.getIdentifier())) {
                        log.warn("Skip region {}", r);
                        continue;
                    }
                }
                try {
                    // List all containers
                    List<Container> chunk;
                    String marker = null;
                    do {
                        chunk = client.listContainers(r, limit, marker);
                        for(final Container f : chunk) {
                            final PathAttributes attributes = new PathAttributes();
                            attributes.setRegion(f.getRegion().getRegionId());
                            containers.add(new Path(String.format("/%s", f.getName()),
                                EnumSet.of(Path.Type.volume, Path.Type.directory), attributes));
                            marker = f.getName();
                        }
                        listener.chunk(directory, containers);
                    }
                    while(!chunk.isEmpty());
                }
                catch(GenericException e) {
                    if(e.getHttpStatusCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
                        log.warn("Ignore failure {} for region {}", e, region);
                        continue;
                    }
                    throw e;
                }
            }
            return containers;
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
