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
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;

import ch.iterate.openstack.swift.exception.GenericException;

public class SwiftDirectoryFeature implements Directory {

    private SwiftSession session;

    private PathContainerService containerService
            = new SwiftPathContainerService();

    private SwiftRegionService regionService;

    public SwiftDirectoryFeature(final SwiftSession session) {
        this(session, new SwiftRegionService(session));
    }

    public SwiftDirectoryFeature(final SwiftSession session, final SwiftRegionService regionService) {
        this.session = session;
        this.regionService = regionService;
    }

    @Override
    public void mkdir(final Path file) throws BackgroundException {
        this.mkdir(file, null, null);
    }

    @Override
    public void mkdir(final Path file, final String region, final TransferStatus status) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                // Create container at top level
                session.getClient().createContainer(regionService.lookup(
                        new SwiftLocationFeature.SwiftRegion(region)), file.getName());
            }
            else {
                // Create virtual directory.
                session.getClient().storeObject(regionService.lookup(file),
                        containerService.getContainer(file).getName(),
                        new ByteArrayInputStream(new byte[]{}), "application/directory", containerService.getKey(file),
                        Collections.<String, String>emptyMap());
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
    }
}
