package ch.cyberduck.core.onedrive.features;

/*
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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LockedException;
import ch.cyberduck.core.features.Lock;
import ch.cyberduck.core.onedrive.GraphExceptionMappingService;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.http.HttpStatus;
import org.nuxeo.onedrive.client.Files;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.types.DriveItem;
import org.nuxeo.onedrive.client.types.Publication;

import java.io.IOException;

public class GraphLockFeature implements Lock<String> {
    private final GraphSession session;
    private final GraphFileIdProvider fileid;

    public GraphLockFeature(final GraphSession session, final GraphFileIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public String lock(final Path file) throws BackgroundException {
        final Publication publication;
        try {
            final DriveItem item = session.getItem(file);
            Files.checkout(item);
            publication = Files.publication(item);
        }
        catch(OneDriveAPIException e) {
            if(e.getResponseCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                throw new LockedException(e.getMessage(), e);
            }
            throw new GraphExceptionMappingService(fileid).map("Failure to checkout file {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
        return publication.getVersionId();
    }

    @Override
    public void unlock(final Path file, final String token) throws BackgroundException {
        try {
            Files.checkin(session.getItem(file), String.format("%s-%s",
                PreferencesFactory.get().getProperty("application.name"),
                new AlphanumericRandomStringService().random()));
        }
        catch(OneDriveAPIException e) {
            throw new GraphExceptionMappingService(fileid).map("Failure to check in file {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
    }
}
