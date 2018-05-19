package ch.cyberduck.core.onedrive;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;

import org.nuxeo.onedrive.client.OneDriveAPI;
import org.nuxeo.onedrive.client.OneDriveFile;
import org.nuxeo.onedrive.client.OneDriveFolder;
import org.nuxeo.onedrive.client.OneDriveItem;

public abstract class GraphSession extends HttpSession<OneDriveAPI> {
    protected GraphSession(final Host host, final ThreadLocalHostnameDelegatingTrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    public OneDriveItem toItem(final Path currentPath) throws BackgroundException {
        return this.toItem(currentPath, true);
    }

    public abstract OneDriveItem toItem(final Path currentPath, final boolean resolveLastItem) throws BackgroundException;

    public OneDriveFile toFile(final Path currentPath) throws BackgroundException {
        return this.toFile(currentPath, true);
    }

    public OneDriveFile toFile(final Path currentPath, final boolean resolveLastItem) throws BackgroundException {
        final OneDriveItem item = this.toItem(currentPath, resolveLastItem);
        if(!(item instanceof OneDriveFile)) {
            throw new NotfoundException(String.format("%s is not a file.", currentPath.getAbsolute()));
        }
        return (OneDriveFile) item;
    }

    public OneDriveFolder toFolder(final Path currentPath) throws BackgroundException {
        return this.toFolder(currentPath, true);
    }

    public OneDriveFolder toFolder(final Path currentPath, final boolean resolveLastItem) throws BackgroundException {
        final OneDriveItem item = this.toItem(currentPath, resolveLastItem);
        if(!(item instanceof OneDriveFolder)) {
            throw new NotfoundException(String.format("%s is not a folder.", currentPath.getAbsolute()));
        }
        return (OneDriveFolder) item;
    }
}
