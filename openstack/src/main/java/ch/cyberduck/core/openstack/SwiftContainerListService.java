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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.RootListService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import ch.iterate.openstack.swift.Client;
import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.Container;
import ch.iterate.openstack.swift.model.Region;

public class SwiftContainerListService implements RootListService {
    private static final Logger log = Logger.getLogger(SwiftContainerListService.class);

    private final SwiftSession session;

    private final Preferences preferences
            = PreferencesFactory.get();

    private final SwiftLocationFeature.SwiftRegion region;

    public SwiftContainerListService(final SwiftSession session, final SwiftLocationFeature.SwiftRegion region) {
        this.session = session;
        this.region = region;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("List containers for %s", session));
        }
        try {
            final AttributedList<Path> containers = new AttributedList<Path>();
            final int limit = preferences.getInteger("openstack.list.container.limit");
            final Client client = session.getClient();
            for(final Region region : client.getRegions()) {
                if(this.region.getIdentifier() != null) {
                    if(!StringUtils.equals(region.getRegionId(), this.region.getIdentifier())) {
                        log.warn(String.format("Skip region %s", region));
                        continue;
                    }
                }
                // List all containers
                List<Container> chunk;
                String marker = null;
                do {
                    chunk = client.listContainers(region, limit, marker);
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
            return containers;
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        return this;
    }
}
