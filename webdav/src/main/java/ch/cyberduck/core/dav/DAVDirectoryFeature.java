package ch.cyberduck.core.dav;

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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;

import com.github.sardine.impl.SardineException;

public class DAVDirectoryFeature implements Directory<Void> {

    private final DAVSession session;

    public DAVDirectoryFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public Path mkdir(final Path folder, final String region, final TransferStatus status) throws BackgroundException {
        try {
            session.getClient().createDirectory(new DAVPathEncoder().encode(folder));
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Cannot create folder {0}", e, folder);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map(e, folder);
        }
        return folder;
    }

    @Override
    public boolean isSupported(final Path workdir) {
        return true;
    }

    @Override
    public DAVDirectoryFeature withWriter(final Write<Void> writer) {
        return this;
    }
}
