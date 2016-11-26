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
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Home;

import org.apache.log4j.Logger;

public class CryptoHomeFinder implements Home {
    private static final Logger log = Logger.getLogger(CryptoHomeFinder.class);

    private final Session session;
    private final Home delegate;
    private final PasswordStore keychain;
    private final LoginCallback prompt;

    public CryptoHomeFinder(final Session session, final Home delegate, final PasswordStore keychain, final LoginCallback prompt) {
        this.session = session;
        this.delegate = delegate;
        this.keychain = keychain;
        this.prompt = prompt;
    }

    @Override
    public Path find() throws BackgroundException {
        final Path home = delegate.find();
        try {
            final SessionCryptomatorLoader loader = session.getCrypto();
            loader.load(home, keychain, prompt);
        }
        catch(BackgroundException e) {
            log.warn(String.format("Failure loading vault in %s", home));
        }
        return home;
    }

    @Override
    public Path find(final Path workdir, final String path) {
        final Path home = delegate.find(workdir, path);
        return home;
    }
}
