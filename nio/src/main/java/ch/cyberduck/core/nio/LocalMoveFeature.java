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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

import java.nio.file.NoSuchFileException;
import java.util.Collections;
import java.util.EnumSet;

public class LocalMoveFeature implements Move {

    private final LocalSession session;

    public LocalMoveFeature(final LocalSession session) {
        this.session = session;
    }

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        if(!new LocalFindFeature(session).find(file)) {
            throw new NotfoundException(file.getAbsolute());
        }
        if(status.isExists()) {
            new LocalDeleteFeature(session).delete(Collections.singletonMap(renamed, status), new DisabledPasswordCallback(), callback);
        }
        if(!session.toPath(file).toFile().renameTo(session.toPath(renamed).toFile())) {
            throw new LocalExceptionMappingService().map("Cannot rename {0}", new NoSuchFileException(file.getName()), file);
        }
        // Copy attributes from original file
        return renamed.withAttributes(new LocalAttributesFinderFeature(session).find(renamed));
    }

    @Override
    public EnumSet<Flags> features(final Path source, final Path target) {
        return EnumSet.of(Flags.recursive);
    }

}
