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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Location;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.iterate.openstack.swift.Client;
import ch.iterate.openstack.swift.exception.AuthorizationException;
import ch.iterate.openstack.swift.exception.ContainerNotFoundException;
import ch.iterate.openstack.swift.model.Region;

public class SwiftLocationFeature implements Location {
    private static final Logger log = Logger.getLogger(SwiftLocationFeature.class);

    private final SwiftSession session;

    private final PathContainerService containerService
            = new SwiftPathContainerService();

    private final Map<Path, Name> cache = new HashMap<Path, Name>();

    public SwiftLocationFeature(final SwiftSession session) {
        this.session = session;
    }

    @Override
    public Set<Name> getLocations() {
        final Set<Name> locations = new LinkedHashSet<Name>();
        final List<Region> regions = new ArrayList<Region>(session.getClient().getRegions());
        Collections.sort(regions, new Comparator<Region>() {
            @Override
            public int compare(final Region r1, final Region r2) {
                if(r1.isDefault()) {
                    return -1;
                }
                if(r2.isDefault()) {
                    return 1;
                }
                return 0;
            }
        });
        for(Region region : regions) {
            if(StringUtils.isBlank(region.getRegionId())) {
                // v1 authentication contexts do not have region support
                continue;
            }
            locations.add(new SwiftRegion(region.getRegionId()));
        }
        return locations;
    }

    @Override
    public Name getLocation(final Path file) throws BackgroundException {
        final Path container = containerService.getContainer(file);
        if(cache.containsKey(container)) {
            return cache.get(container);
        }
        if(Location.unknown.equals(new SwiftRegion(container.attributes().getRegion()))) {
            final SwiftRegion region = new SwiftRegion(session.getHost().getRegion());
            if(Location.unknown.equals(region)) {
                final Client client = session.getClient();
                for(Region r : client.getRegions()) {
                    try {
                        cache.put(container, new SwiftRegion(
                                        client.getContainerInfo(r, container.getName()).getRegion().getRegionId())
                        );
                    }
                    catch(ContainerNotFoundException | AuthorizationException e) {
                        log.warn(String.format("Failure finding container %s in region %s", container, r.getRegionId()));
                    }
                    catch(IOException e) {
                        throw new DefaultIOExceptionMappingService().map(e);
                    }
                }
                if(!cache.containsKey(container)) {
                    throw new NotfoundException(container.getAbsolute());
                }
            }
            else {
                cache.put(container, region);
            }
        }
        else {
            final SwiftRegion r = new SwiftRegion(container.attributes().getRegion());
            cache.put(container, r);
        }
        return cache.get(container);
    }

    public static final class SwiftRegion extends Name {

        public SwiftRegion(String identifier) {
            super(identifier);
        }

        @Override
        public String toString() {
            final String identifier = getIdentifier();
            if(null == identifier) {
                return LocaleFactory.localizedString("Unknown");
            }
            return LocaleFactory.localizedString(this.getIdentifier(), "Mosso");
        }
    }
}
