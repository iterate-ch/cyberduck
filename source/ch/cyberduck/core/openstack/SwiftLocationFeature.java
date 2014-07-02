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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Location;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ch.iterate.openstack.swift.model.Region;

/**
 * @version $Id$
 */
public class SwiftLocationFeature implements Location {

    private SwiftSession session;

    public SwiftLocationFeature(final SwiftSession session) {
        this.session = session;
    }

    @Override
    public Set<String> getLocations() {
        final Set<String> locations = new LinkedHashSet<String>();
        final List<Region> regions = new ArrayList<Region>(session.getClient().getRegions());
        Collections.sort(regions, new Comparator<Region>() {
            @Override
            public int compare(Region r1, Region r2) {
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
            locations.add(region.getRegionId());
        }
        return locations;
    }

    @Override
    public String getLocation(final Path container) throws BackgroundException {
        return new SwiftPathContainerService().getContainer(container).attributes().getRegion();
    }
}
