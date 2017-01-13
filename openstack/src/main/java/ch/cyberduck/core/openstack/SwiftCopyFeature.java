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
import ch.cyberduck.core.features.Copy;

import java.io.IOException;

import ch.iterate.openstack.swift.exception.GenericException;

public class SwiftCopyFeature implements Copy {

    private final SwiftSession session;

    private final PathContainerService containerService
            = new PathContainerService();

    private final SwiftRegionService regionService;

    public SwiftCopyFeature(final SwiftSession session) {
        this(session, new SwiftRegionService(session));
    }

    public SwiftCopyFeature(final SwiftSession session, final SwiftRegionService regionService) {
        this.session = session;
        this.regionService = regionService;
    }

    @Override
    public void copy(final Path source, final Path copy) throws BackgroundException {
        try {
            if(source.isFile()) {
                session.getClient().copyObject(regionService.lookup(source),
                        containerService.getContainer(source).getName(), containerService.getKey(source),
                        containerService.getContainer(copy).getName(), containerService.getKey(copy));
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Cannot copy {0}", e, source);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot copy {0}", e, source);
        }
    }
}
