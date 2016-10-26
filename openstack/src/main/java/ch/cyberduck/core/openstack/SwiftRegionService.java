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
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Location;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ch.iterate.openstack.swift.model.Region;

public class SwiftRegionService {
    private static final Logger log = Logger.getLogger(SwiftRegionService.class);

    private final SwiftSession session;

    private final PathContainerService containerService
            = new SwiftPathContainerService();

    private final SwiftLocationFeature location;

    public SwiftRegionService(final SwiftSession session) {
        this(session, new SwiftLocationFeature(session));
    }

    public SwiftRegionService(final SwiftSession session, final SwiftLocationFeature location) {
        this.session = session;
        this.location = location;
    }

    public Region lookup(final Path file) throws BackgroundException {
        final Path container = containerService.getContainer(file);
        if(Location.unknown.equals(new SwiftLocationFeature.SwiftRegion(container.attributes().getRegion()))) {
            return this.lookup(location.getLocation(container));
        }
        return this.lookup(new SwiftLocationFeature.SwiftRegion(file.attributes().getRegion()));
    }

    public Region lookup(final Location.Name location) throws InteroperabilityException {
        if(!session.isConnected()) {
            log.warn("Cannot determine region if not connected");
            return new Region(location.getIdentifier(), null, null);
        }
        for(Region region : session.getClient().getRegions()) {
            if(StringUtils.isBlank(region.getRegionId())) {
                continue;
            }
            if(region.getRegionId().equals(location.getIdentifier())) {
                return region;
            }
        }
        log.warn(String.format("Unknown region %s in authentication context", location));
        if(session.getClient().getRegions().isEmpty()) {
            throw new InteroperabilityException("No region found in authentication context");
        }
        for(Region region : session.getClient().getRegions()) {
            if(region.isDefault()) {
                log.warn(String.format("Fallback to default region %s", region.getRegionId()));
                return region;
            }
        }
        final Region region = session.getClient().getRegions().iterator().next();
        log.warn(String.format("Fallback to first region %s", region.getRegionId()));
        if(null == region.getStorageUrl()) {
            throw new InteroperabilityException(String.format("No storage endpoint found for region %s", region.getRegionId()));
        }
        if(null == region.getCDNManagementUrl()) {
            log.warn(String.format("No CDN management endpoint found for region %s", region.getRegionId()));
        }
        return region;
    }
}
