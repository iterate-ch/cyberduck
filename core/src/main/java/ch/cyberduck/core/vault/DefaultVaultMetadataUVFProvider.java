package ch.cyberduck.core.vault;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.LoginCanceledException;

public class DefaultVaultMetadataUVFProvider implements VaultMetadataUVFProvider {

    private final byte[] metadata;
    private final byte[] rootDirectoryMetadata;
    private final String dirPath;
    private final JWKCallback callback;

    public DefaultVaultMetadataUVFProvider(final byte[] metadata, final byte[] rootDirectoryMetadata, final String dirPath, final JWKCallback callback) {
        this.metadata = metadata;
        this.rootDirectoryMetadata = rootDirectoryMetadata;
        this.dirPath = dirPath;
        this.callback = callback;
    }

    @Override
    public void close(final String input) {
        callback.close(input);
    }

    @Override
    public JWKCredentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
        return callback.prompt(bookmark, title, reason, options);
    }

    @Override
    public byte[] getMetadata() {
        return metadata;
    }

    @Override
    public byte[] getRootDirectoryMetadata() {
        return rootDirectoryMetadata;
    }

    @Override
    public String getDirPath() {
        return dirPath;
    }
}
