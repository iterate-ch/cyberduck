package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Directory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * @version $Id:$
 */
public class AzureDirectoryFeature implements Directory {

    private AzureSession session;

    private PathContainerService containerService
            = new PathContainerService();

    public AzureDirectoryFeature(AzureSession session) {
        this.session = session;
    }

    @Override
    public void mkdir(Path file) throws BackgroundException {
        this.mkdir(file, null);
    }

    @Override
    public void mkdir(Path file, String region) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                // Container name must be lower case.
                session.getClient().getContainerReference(containerService.getContainer(file).getName()).create();
            }
            else {
                // Create delimiter placeholder
                session.getClient().getContainerReference(containerService.getContainer(file).getName())
                        .getBlockBlobReference(containerService.getKey(file).concat(String.valueOf(Path.DELIMITER))).upload(
                        new ByteArrayInputStream(new byte[]{}), 0L);
            }
        }
        catch(URISyntaxException e) {
            throw new NotfoundException(e.getMessage(), e);
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
    }
}
