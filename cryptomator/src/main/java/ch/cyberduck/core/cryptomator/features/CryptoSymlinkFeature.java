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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.features.Vault;

public class CryptoSymlinkFeature implements Symlink {
    private final Session<?> session;
    private final Symlink delegate;
    private final Vault vault;

    public CryptoSymlinkFeature(final Session<?> session, final Symlink delegate, final Vault vault) {
        this.session = session;
        this.delegate = delegate;
        this.vault = vault;
    }

    @Override
    public void symlink(final Path file, final String target) throws BackgroundException {
        delegate.symlink(vault.encrypt(session, file), target);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoSymlinkFeature{");
        sb.append("delegate=").append(delegate);
        sb.append('}');
        return sb.toString();
    }
}
