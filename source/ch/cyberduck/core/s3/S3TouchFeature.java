package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;

import org.jets3t.service.ServiceException;
import org.jets3t.service.model.StorageObject;

/**
 * @version $Id$
 */
public class S3TouchFeature implements Touch {

    private S3Session session;

    private PathContainerService containerService = new PathContainerService();

    public S3TouchFeature(final S3Session session) {
        this.session = session;
    }

    @Override
    public void touch(final Path file) throws BackgroundException {
        try {
            session.getClient().putObject(containerService.getContainer(file).getName(),
                    new StorageObject(containerService.getKey(file)));
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot create file {0}", e, file);
        }
    }

    @Override
    public boolean isSupported(final Path workdir) {
        // Creating files is only possible inside a bucket.
        return !workdir.isRoot();
    }
}
