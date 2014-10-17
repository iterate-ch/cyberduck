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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;

import org.apache.commons.lang3.StringUtils;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.StorageObject;

import java.util.EnumSet;

/**
 * @version $Id$
 */
public class S3DirectoryFeature implements Directory {

    private S3Session session;

    private PathContainerService containerService
            = new S3PathContainerService();

    public S3DirectoryFeature(final S3Session session) {
        this.session = session;
    }

    @Override
    public void mkdir(final Path file) throws BackgroundException {
        this.mkdir(file, null);
    }

    @Override
    public void mkdir(final Path file, final String region) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                final S3BucketCreateService service = new S3BucketCreateService(session);
                if(StringUtils.isBlank(region)) {
                    service.create(file, Preferences.instance().getProperty("s3.location"));
                }
                else {
                    service.create(file, region);
                }
            }
            else {
                // Set type to placeholder
                file.setType(EnumSet.of(Path.Type.directory, Path.Type.placeholder));
                // Add placeholder object
                final StorageObject object = new StorageObject(containerService.getKey(file));
                object.setBucketName(containerService.getContainer(file).getName());
                object.setContentLength(0);
                object.setContentType("application/x-directory");
                session.getClient().putObject(containerService.getContainer(file).getName(), object);
            }
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
    }
}
