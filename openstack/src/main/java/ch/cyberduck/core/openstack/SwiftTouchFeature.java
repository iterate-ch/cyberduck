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
import ch.cyberduck.core.MappingMimeTypeService;
import ch.cyberduck.core.MimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;

public class SwiftTouchFeature implements Touch {

    private SwiftSession session;

    final PathContainerService containerService
            = new SwiftPathContainerService();

    private MimeTypeService mapping
            = new MappingMimeTypeService();

    private SwiftRegionService regionService;

    public SwiftTouchFeature(final SwiftSession session) {
        this(session, new SwiftRegionService(session));
    }

    public SwiftTouchFeature(final SwiftSession session, final SwiftRegionService regionService) {
        this.session = session;
        this.regionService = regionService;
    }

    @Override
    public void touch(final Path file) throws BackgroundException {
        try {
            session.getClient().storeObject(regionService.lookup(file),
                    containerService.getContainer(file).getName(),
                    new ByteArrayInputStream(new byte[]{}), mapping.getMime(file.getName()), containerService.getKey(file),
                    Collections.<String, String>emptyMap());
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create file {0}", e, file);
        }
    }

    @Override
    public boolean isSupported(final Path workdir) {
        // Creating files is only possible inside a container.
        return !workdir.isRoot();
    }
}
