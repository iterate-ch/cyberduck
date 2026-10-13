package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.EnumSet;
import java.util.Map;

public class AzureDeleteFeature implements Delete {

    private final AzureSession session;

    public AzureDeleteFeature(final AzureSession session) {
        this.session = session;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        this.proxy().delete(files, prompt, callback);
    }

    @Override
    public void preflight(final Path file) throws BackgroundException {
        this.proxy().preflight(file);
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return this.proxy().features(file);
    }

    private Delete proxy() {
        if(session.getStorageAccountInfo().isHierarchicalNamespaceEnabled()) {
            return new AzureDataLakeDeleteFeature(session);
        }
        return new AzureBlobDeleteFeature(session);
    }
}
