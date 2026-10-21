package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;

public class AzureBlobFindFeature implements Find {

    private final AzureSession session;
    private final AzureBlobAttributesFinderFeature attributes;
    private final PathContainerService containerService
            = new DirectoryDelimiterPathContainerService();

    public AzureBlobFindFeature(final AzureSession session) {
        this.session = session;
        this.attributes = new AzureBlobAttributesFinderFeature(session);
    }

    @Override
    public boolean find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return true;
        }
        try {
            if(containerService.isContainer(file)) {
                return session.getClient().getBlobServiceClient().getBlobContainerClient(containerService.getContainer(file).getName()).exists();
            }
            attributes.find(file, listener);
            return true;
        }
        catch(NotfoundException e) {
            return false;
        }
        catch(RuntimeException e) {
            throw new AzureExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }
}
