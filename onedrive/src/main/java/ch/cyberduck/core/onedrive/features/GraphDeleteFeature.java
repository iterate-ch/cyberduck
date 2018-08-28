package ch.cyberduck.core.onedrive.features;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.onedrive.GraphExceptionMappingService;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.onedrive.OneDriveSession;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveItem;

import java.io.IOException;
import java.util.List;

public class GraphDeleteFeature implements Delete {
    private static final Logger logger = Logger.getLogger(GraphDeleteFeature.class);

    private final PathContainerService containerService
        = new PathContainerService();

    private final GraphSession session;

    public GraphDeleteFeature(GraphSession session) {
        this.session = session;
    }

    @Deprecated
    public GraphDeleteFeature(OneDriveSession session) {
        this.session = session;
    }

    @Override
    public void delete(final List<Path> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        for(Path file : files) {
            if(containerService.isContainer(file)) {
                continue;
            }
            callback.delete(file);

            try {
                final OneDriveItem item = session.toItem(file);
                item.delete();
            }
            catch(NotfoundException e) {
                logger.warn(String.format("Cannot delete %s. Not found.", file));
            }
            catch(OneDriveAPIException e) {
                throw new GraphExceptionMappingService().map("Cannot delete {0}", e, file);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map("Cannot delete {0}", e, file);
            }
        }
    }

    @Override
    public boolean isSupported(final Path file) {
        return !containerService.isContainer(file);
    }

    @Override
    public boolean isRecursive() {
        return true;
    }
}
