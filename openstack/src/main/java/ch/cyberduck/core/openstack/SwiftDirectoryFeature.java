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
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;

import ch.iterate.openstack.swift.exception.GenericException;

public class SwiftDirectoryFeature implements Directory {

    private final SwiftSession session;

    private final PathContainerService containerService
            = new SwiftPathContainerService();

    private final SwiftRegionService regionService;

    private final Write write;

    public SwiftDirectoryFeature(final SwiftSession session) {
        this(session, new SwiftRegionService(session));
    }

    public SwiftDirectoryFeature(final SwiftSession session, final SwiftRegionService regionService) {
        this(session, regionService, session.getFeature(Write.class));
    }

    public SwiftDirectoryFeature(final SwiftSession session, final SwiftRegionService regionService, final Write write) {
        this.session = session;
        this.regionService = regionService;
        this.write = write;
    }

    @Override
    public void mkdir(final Path file) throws BackgroundException {
        this.mkdir(file, null, null);
    }

    @Override
    public void mkdir(final Path file, final String region, TransferStatus status) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                // Create container at top level
                session.getClient().createContainer(regionService.lookup(
                        new SwiftLocationFeature.SwiftRegion(region)), file.getName());
            }
            else {
                if(null == status) {
                    status = new TransferStatus();
                }
                status.setMime("application/directory");
                try {
                    write.write(file, status.length(0L)).close();
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map("Cannot create folder {0}", e, file);
                }
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
