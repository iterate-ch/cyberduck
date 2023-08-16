package ch.cyberduck.core.cryptomator.features;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.vault.DecryptingListProgressListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CryptoListService implements ListService {
    private static final Logger log = LogManager.getLogger(CryptoListService.class);

    private final Session<?> session;
    private final ListService delegate;
    private final Vault vault;

    public CryptoListService(final Session<?> session, final ListService delegate, final Vault vault) {
        this.session = session;
        this.delegate = delegate;
        this.vault = vault;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return delegate.list(vault.encrypt(session, directory), new DecryptingListProgressListener(session, vault, listener));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoListService{");
        sb.append("delegate=").append(delegate);
        sb.append('}');
        return sb.toString();
    }
}
