package ch.cyberduck.core.cryptomator.impl;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.vault.VaultCredentials;

public class DefaultVaultMetadataCredentialsProvider implements VaultMetadataCredentialsProvider {

    private final VaultCredentials credentials;

    public DefaultVaultMetadataCredentialsProvider(final VaultCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public VaultCredentials getCredentials() {
        return credentials;
    }

    @Override
    public void close(final String input) {
        //
    }

    @Override
    public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
        return credentials;
    }
}
