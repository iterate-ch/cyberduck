package ch.cyberduck.core.nio;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;

public class LocalMoveFeature implements Move {

    private final LocalSession session;
    private Delete delete;

    public LocalMoveFeature(final LocalSession session) {
        this.session = session;
        this.delete = new LocalDeleteFeature(session);
    }

    @Override
    public void move(final Path file, final Path renamed, final boolean exists, final Delete.Callback callback) throws BackgroundException {
        try {
            if(exists) {
                delete.delete(Collections.singletonList(renamed), new DisabledLoginCallback(), callback);
            }
            Files.move(session.getClient().getPath(file.getAbsolute()), session.getClient().getPath(renamed.getAbsolute()));
        }
        catch(IOException e) {
            throw new LocalExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public boolean isRecursive(final Path source) {
        return true;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return true;
    }

    @Override
    public Move withDelete(final Delete delete) {
        this.delete = delete;
        return this;
    }

}
