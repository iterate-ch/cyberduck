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

import ch.cyberduck.core.MappingMimeTypeService;
import ch.cyberduck.core.MimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.transfer.TransferStatus;

import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3Object;

public class S3TouchFeature implements Touch {

    private final S3Session session;

    private final PathContainerService containerService
            = new S3PathContainerService();

    private final MimeTypeService mapping
            = new MappingMimeTypeService();

    private final S3WriteFeature write;

    private Encryption.Properties encryption = Encryption.Properties.NONE;

    private String storageClass;

    public S3TouchFeature(final S3Session session) {
        this.session = session;
        this.write = new S3WriteFeature(session);
    }

    public S3TouchFeature withStorage(final String storageClass) {
        this.storageClass = storageClass;
        return this;
    }

    public S3TouchFeature withEncryption(final Encryption.Properties encryption) {
        this.encryption = encryption;
        return this;
    }

    @Override
    public void touch(final Path file) throws BackgroundException {
        try {
            final TransferStatus status = new TransferStatus();
            status.setMime(mapping.getMime(file.getName()));
            status.setEncryption(encryption);
            status.setStorageClass(storageClass);
            final S3Object key = write.getDetails(containerService.getKey(file), status);
            session.getClient().putObject(containerService.getContainer(file).getName(), key);
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
