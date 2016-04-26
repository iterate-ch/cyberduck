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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3Object;

public class S3DirectoryFeature implements Directory {

    private S3Session session;

    private PathContainerService containerService
            = new S3PathContainerService();

    private S3WriteFeature write;

    public S3DirectoryFeature(final S3Session session) {
        this.session = session;
        this.write = new S3WriteFeature(session);
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
                    service.create(file, PreferencesFactory.get().getProperty("s3.location"));
                }
                else {
                    service.create(file, region);
                }
            }
            else {
                // Add placeholder object
                final TransferStatus status = new TransferStatus();
                status.setMime("application/x-directory");
                final S3Object key = write.getDetails(containerService.getKey(file).concat(String.valueOf(Path.DELIMITER)), status);
                session.getClient().putObject(containerService.getContainer(file).getName(), key);
            }
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
    }
}
