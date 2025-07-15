package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AzureFindFeature implements Find {
    private static final Logger log = LogManager.getLogger(AzureFindFeature.class);

    private final AzureSession session;
    private final AzureAttributesFinderFeature attributes;
    private final PathContainerService containerService
            = new DirectoryDelimiterPathContainerService();

    public AzureFindFeature(final AzureSession session) {
        this.session = session;
        this.attributes = new AzureAttributesFinderFeature(session);
    }

    @Override
    public boolean find(Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return true;
        }
        try {
            final boolean found;
            if(containerService.isContainer(file)) {
                return session.getClient().getBlobContainerClient(containerService.getContainer(file).getName()).exists();
            }
            attributes.find(file, listener);
            return true;
        }
        catch(NotfoundException e) {
            return false;
        }
    }
}
