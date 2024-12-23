package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Optional;

public class DisabledMoveFeature implements Move {

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback,
                     final ConnectionCallback connectionCallback) throws BackgroundException {
        throw new UnsupportedException();
    }

    @Override
    public void preflight(final Path source, final Optional<Path> target) throws BackgroundException {
        throw new AccessDeniedException(LocaleFactory.localizedString("Unsupported", "Error"));
    }
}
