package ch.cyberduck.core.onedrive.features;/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Lock;
import ch.cyberduck.core.onedrive.GraphExceptionMappingService;
import ch.cyberduck.core.onedrive.GraphSession;

import org.nuxeo.onedrive.client.Files;
import org.nuxeo.onedrive.client.OneDriveAPIException;

import java.io.IOException;

public class GraphLockFeature implements Lock {
    private final GraphSession session;

    public GraphLockFeature(final GraphSession session) {
        this.session = session;
    }

    @Override
    public Object lock(final Path file) throws BackgroundException {
        try {
            Files.checkout(session.toItem(file));
        }
        catch(OneDriveAPIException e) {
            throw new GraphExceptionMappingService().map("Failure to checkout file {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
        return null;
    }

    @Override
    public void unlock(final Path file, final Object token) throws BackgroundException {
        try {
            Files.checkin(session.toItem(file), "Updated by Mountain Duck");
        }
        catch(OneDriveAPIException e) {
            throw new GraphExceptionMappingService().map("Failure to check in file {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
    }
}
