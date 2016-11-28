package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Vault;

import org.apache.log4j.Logger;

public class VaultFinder implements Home {
    private static final Logger log = Logger.getLogger(VaultFinder.class);

    private final Vault vault;
    private final Home delegate;
    private final PasswordStore keychain;
    private final LoginCallback prompt;

    public VaultFinder(final Vault vault, final Home delegate, final PasswordStore keychain, final LoginCallback login) {
        this.vault = vault;
        this.delegate = delegate;
        this.keychain = keychain;
        this.prompt = login;
    }

    @Override
    public Path find() throws BackgroundException {
        return load(delegate.find());
    }

    @Override
    public Path find(final Path workdir, final String path) {
        return load(delegate.find(workdir, path));
    }

    private Path load(final Path home) {
        try {
            vault.load(home, keychain, prompt);
        }
        catch(BackgroundException e) {
            log.warn(String.format("Failure loading vault in %s", home));
        }
        return home;
    }
}
