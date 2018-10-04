package ch.cyberduck.core.nio;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class LocalCopyFeature implements Copy {

    private final LocalSession session;

    public LocalCopyFeature(final LocalSession session) {
        this.session = session;
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            Files.copy(session.toPath(source), session.toPath(target), StandardCopyOption.REPLACE_EXISTING);
            // Copy attributes from original file
            return new Path(target.getParent(), target.getName(), target.getType(),
                new LocalAttributesFinderFeature(session).find(target, new DisabledListProgressListener()));
        }
        catch(IOException e) {
            throw new LocalExceptionMappingService().map("Cannot copy {0}", e, source);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return true;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return true;
    }

    @Override
    public Copy withTarget(final Session<?> session) {
        return this;
    }
}
