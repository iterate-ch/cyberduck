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
import ch.cyberduck.core.io.DefaultStreamCloser;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.model.StorageObject;

public class SwiftDirectoryFeature implements Directory<StorageObject> {

    private final PathContainerService containerService
            = new SwiftPathContainerService();

    private final SwiftSession session;
    private final SwiftRegionService regionService;

    private Write<StorageObject> writer;

    public SwiftDirectoryFeature(final SwiftSession session) {
        this(session, new SwiftRegionService(session));
    }

    public SwiftDirectoryFeature(final SwiftSession session, final SwiftRegionService regionService) {
        this(session, regionService, new SwiftWriteFeature(session, regionService));
    }

    public SwiftDirectoryFeature(final SwiftSession session, final SwiftRegionService regionService, final Write<StorageObject> writer) {
        this.session = session;
        this.regionService = regionService;
        this.writer = writer;
    }

    @Override
    public void mkdir(final Path file) throws BackgroundException {
        this.mkdir(file, null, new TransferStatus());
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
                status.setMime("application/directory");
                status.setLength(0L);
                new DefaultStreamCloser().close(writer.write(file, status));
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
    }

    @Override
    public SwiftDirectoryFeature withWriter(final Write<StorageObject> writer) {
        this.writer = writer;
        return this;
    }
}
