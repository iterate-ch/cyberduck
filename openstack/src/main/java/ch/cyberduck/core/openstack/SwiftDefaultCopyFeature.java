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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DefaultPathContainerService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.text.MessageFormat;

import ch.iterate.openstack.swift.exception.GenericException;

public class SwiftDefaultCopyFeature implements Copy {

    private final PathContainerService containerService = new DefaultPathContainerService();
    private final SwiftSession session;
    private final SwiftRegionService regionService;

    public SwiftDefaultCopyFeature(final SwiftSession session) {
        this(session, new SwiftRegionService(session));
    }

    public SwiftDefaultCopyFeature(final SwiftSession session, final SwiftRegionService regionService) {
        this.session = session;
        this.regionService = regionService;
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback, final StreamListener listener) throws BackgroundException {
        try {
            // Copies file
            // If segmented file, copies manifest (creating a link between new object and original segments)
            // Use with caution.
            session.getClient().copyObject(regionService.lookup(source),
                containerService.getContainer(source).getName(), containerService.getKey(source),
                containerService.getContainer(target).getName(), containerService.getKey(target));
            listener.sent(status.getLength());
            // Copy original file attributes
            return target.withAttributes(source.attributes());
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Cannot copy {0}", e, source);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot copy {0}", e, source);
        }
    }

    @Override
    public void preflight(final Path source, final Path target) throws BackgroundException {
        if(containerService.isContainer(source)) {
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot copy {0}", "Error"), source)).withFile(source);
        }
        if(containerService.isContainer(target)) {
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot copy {0}", "Error"), source)).withFile(target);
        }
    }
}
