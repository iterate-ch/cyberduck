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
import ch.cyberduck.core.DefaultPathContainerService;
import ch.cyberduck.core.DisabledConnectionCallback;
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

    public static final String DIRECTORY_MIME_TYPE = "application/directory";

    private final PathContainerService containerService = new DefaultPathContainerService();
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
    public Path mkdir(final Path folder, final TransferStatus status) throws BackgroundException {
        try {
            if(containerService.isContainer(folder)) {
                // Create container at top level
                session.getClient().createContainer(regionService.lookup(
                    new SwiftLocationFeature.SwiftRegion(status.getRegion())), folder.getName());
                return folder.withAttributes(new SwiftAttributesFinderFeature(session, regionService).find(folder));
            }
            else {
                status.setMime(DIRECTORY_MIME_TYPE);
                status.setLength(0L);
                new DefaultStreamCloser().close(writer.write(folder, status, new DisabledConnectionCallback()));
                return folder.withAttributes(status.getResponse());
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Cannot create folder {0}", e, folder);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create folder {0}", e, folder);
        }
    }

    @Override
    public SwiftDirectoryFeature withWriter(final Write<StorageObject> writer) {
        this.writer = writer;
        return this;
    }
}
