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

import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.ThreadedDeleteFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class LocalDeleteFeature extends ThreadedDeleteFeature implements Delete {

    private final LocalSession session;

    public LocalDeleteFeature(final LocalSession session) {
        this.session = session;
    }

    @Override
    public void delete(final List<Path> files, final LoginCallback prompt, final Callback callback) throws BackgroundException {
        for(Path file : files) {
            if(file.isFile() || file.isSymbolicLink()) {
                this.submit(file, new Implementation() {
                    @Override
                    public void delete(final Path file) throws BackgroundException {
                        callback.delete(file);
                        try {
                            Files.delete(session.getClient().getPath(file.getAbsolute()));
                        }
                        catch(IOException e) {
                            throw new LocalExceptionMappingService().map("Cannot delete {0}", e, file);
                        }
                    }
                });
            }
        }
        // Await and shutdown
        this.await();
        for(Path file : files) {
            if(file.isDirectory() && !file.isSymbolicLink()) {
                callback.delete(file);
                try {
                    Files.delete(session.getClient().getPath(file.getAbsolute()));
                }
                catch(IOException e) {
                    throw new LocalExceptionMappingService().map("Cannot delete {0}", e, file);
                }
            }
        }
    }
}
