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
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;

public class LocalTouchFeature implements Touch {

    private final LocalSession session;

    public LocalTouchFeature(final LocalSession session) {
        this.session = session;
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        if(file.isFile()) {
            try {
                Files.createFile(session.getClient().getPath(file.getAbsolute()));
            }
            catch(FileAlreadyExistsException e) {
                //
            }
            catch(IOException e) {
                throw new LocalExceptionMappingService().map("Cannot create file {0}", e, file);
            }
        }
        return file;
    }

    @Override
    public boolean isSupported(final Path workdir) {
        return true;
    }

    @Override
    public Touch withWriter(final Write writer) {
        return this;
    }
}

