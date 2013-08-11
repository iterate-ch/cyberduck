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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Location;

import java.util.HashSet;
import java.util.Set;

import ch.iterate.openstack.swift.model.Region;

/**
 * @version $Id$
 */
class SwiftLocationFeature implements Location {

    private SwiftSession session;

    public SwiftLocationFeature(final SwiftSession session) {
        this.session = session;
    }

    @Override
    public Set<String> getLocations() {
        final Set<String> regions = new HashSet<String>();
        for(Region region : session.getClient().getRegions()) {
            regions.add(region.getRegionId());
        }
        return regions;
    }

    @Override
    public String getLocation(final Path container) throws BackgroundException {
        return new PathContainerService().getContainer(container).attributes().getRegion();
    }
}
