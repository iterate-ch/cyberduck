package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Collections;

public class GoogleStorageMoveFeature implements Move {

    private final GoogleStorageDeleteFeature delete;
    private final GoogleStorageCopyFeature proxy;

    public GoogleStorageMoveFeature(final GoogleStorageSession session) {
        this.proxy = new GoogleStorageCopyFeature(session);
        this.delete = new GoogleStorageDeleteFeature(session);
    }

    @Override
    public Path move(final Path source, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        final Path copy = proxy.copy(source, renamed, status, connectionCallback, new DisabledStreamListener());
        delete.delete(Collections.singletonMap(source, status), connectionCallback, callback);
        return copy;
    }

    @Override
    public void preflight(final Path source, final Path directory, final String filename) throws BackgroundException {
        proxy.preflight(source, directory, filename);
        delete.preflight(source);
    }
}
