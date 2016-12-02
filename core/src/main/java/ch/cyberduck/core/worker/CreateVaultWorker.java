package ch.cyberduck.core.worker;

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

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.impl.CryptoVault;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;

public class CreateVaultWorker extends Worker<Boolean> {

    private final Path directory;
    private final String region;
    private final PasswordStore keychain;
    private final PasswordCallback login;

    public CreateVaultWorker(final Path directory, final String region, final PasswordStore keychain, final PasswordCallback login) {
        this.directory = directory;
        this.region = region;
        this.keychain = keychain;
        this.login = login;
    }

    @Override
    public Boolean run(final Session<?> session) throws BackgroundException {
        try {
            session.withVault(new CryptoVault(directory, keychain, login).create(session, region));
        }
        catch(LoginCanceledException e) {
            return false;
        }
        return true;
    }
}
