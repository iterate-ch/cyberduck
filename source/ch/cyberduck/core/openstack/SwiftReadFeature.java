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
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.io.InputStream;

import ch.iterate.openstack.swift.exception.GenericException;

/**
 * @version $Id$
 */
public class SwiftReadFeature implements Read {

    private PathContainerService containerService
            = new PathContainerService();

    private SwiftSession session;

    public SwiftReadFeature(final SwiftSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            if(status.isAppend()) {
                return session.getClient().getObject(new SwiftRegionService(session).lookup(containerService.getContainer(file)),
                        containerService.getContainer(file).getName(), containerService.getKey(file),
                        status.getCurrent(), status.getLength());
            }
            return session.getClient().getObject(new SwiftRegionService(session).lookup(containerService.getContainer(file)),
                    containerService.getContainer(file).getName(), containerService.getKey(file));
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Download failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
    }

    @Override
    public boolean append(final Path file) {
        return true;
    }
}
