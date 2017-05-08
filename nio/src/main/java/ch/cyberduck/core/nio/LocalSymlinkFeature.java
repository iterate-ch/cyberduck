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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Symlink;

import java.io.IOException;
import java.nio.file.Files;

public class LocalSymlinkFeature implements Symlink {

    private final LocalSession session;

    public LocalSymlinkFeature(final LocalSession session) {
        this.session = session;
    }

    @Override
    public void symlink(final Path file, final String target) throws BackgroundException {
        try {
            Files.createSymbolicLink(
                    session.getClient().getPath(file.getAbsolute()), session.getClient().getPath(target));
        }
        catch(IOException e) {
            throw new LocalExceptionMappingService().map("Cannot create file {0}", e, file);
        }
    }
}
