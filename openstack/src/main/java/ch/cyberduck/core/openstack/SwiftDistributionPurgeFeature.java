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
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.features.Purge;
import ch.cyberduck.core.exception.BackgroundException;

import java.io.IOException;
import java.util.List;

import ch.iterate.openstack.swift.exception.GenericException;

public class SwiftDistributionPurgeFeature implements Purge {

    private SwiftSession session;

    private PathContainerService containerService
            = new SwiftPathContainerService();

    private SwiftRegionService regionService;

    public SwiftDistributionPurgeFeature(final SwiftSession session) {
        this(session, new SwiftRegionService(session));
    }

    public SwiftDistributionPurgeFeature(final SwiftSession session, final SwiftRegionService regionService) {
        this.session = session;
        this.regionService = regionService;
    }

    @Override
    public void invalidate(final Path container, final Distribution.Method method, final List<Path> files, final LoginCallback prompt) throws BackgroundException {
        try {
            for(Path file : files) {
                if(containerService.isContainer(file)) {
                    session.getClient().purgeCDNContainer(regionService.lookup(containerService.getContainer(file)),
                            container.getName(), null);
                }
                else {
                    session.getClient().purgeCDNObject(regionService.lookup(containerService.getContainer(file)),
                            container.getName(), containerService.getKey(file), null);
                }
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Cannot write CDN configuration", e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot write CDN configuration", e);
        }
    }
}
