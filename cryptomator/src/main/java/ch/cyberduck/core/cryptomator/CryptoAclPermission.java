package ch.cyberduck.core.cryptomator;

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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AclPermission;

import java.util.List;

public class CryptoAclPermission implements AclPermission {

    private final Session<?> session;
    private final AclPermission delegate;
    private final CryptoVault cryptomator;

    public CryptoAclPermission(final Session<?> session, final AclPermission delegate, final CryptoVault cryptomator) {

        this.session = session;
        this.delegate = delegate;
        this.cryptomator = cryptomator;
    }

    @Override
    public Acl getPermission(final Path file) throws BackgroundException {
        return delegate.getPermission(cryptomator.encrypt(session, file));
    }

    @Override
    public void setPermission(final Path file, final Acl acl) throws BackgroundException {
        delegate.setPermission(cryptomator.encrypt(session, file), acl);
    }

    @Override
    public List<Acl.User> getAvailableAclUsers() {
        return delegate.getAvailableAclUsers();
    }

    @Override
    public List<Acl.Role> getAvailableAclRoles(final List<Path> files) {
        return delegate.getAvailableAclRoles(files);
    }

    @Override
    public Acl getDefault(final Local file) {
        return delegate.getDefault(file);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoAclPermission{");
        sb.append("delegate=").append(delegate);
        sb.append('}');
        return sb.toString();
    }
}
