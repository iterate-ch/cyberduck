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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;

import java.io.IOException;

import ch.iterate.openstack.swift.exception.GenericException;

/**
 * @version $Id$
 */
public class SwiftFindFeature implements Find {

    private SwiftSession session;

    private PathContainerService containerService
            = new PathContainerService();

    public SwiftFindFeature(final SwiftSession session) {
        this.session = session;
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                try {
                    return session.getClient().containerExists(session.getRegion(containerService.getContainer(file)),
                            file.getName());
                }
                catch(GenericException e) {
                    throw new SwiftExceptionMappingService().map("Cannot read file attributes", e, file);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map("Cannot read file attributes", e, file);
                }
            }
            final SwiftMetadataFeature feature = new SwiftMetadataFeature(session);
            feature.getMetadata(file);
            return true;
        }
        catch(NotfoundException e) {
            return false;
        }
    }
}
