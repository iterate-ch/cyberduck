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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.CacheReference;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.features.Vault;

import java.util.Set;

public final class CryptoPathCache implements Cache<Path> {

    private final Session<?> session;
    private final Cache<Path> delegate;
    private final Vault vault;

    public CryptoPathCache(final Session<?> session, final Cache<Path> delegate, final Vault vault) {
        this.session = session;
        this.delegate = delegate;
        this.vault = vault;
    }

    @Override
    public boolean isHidden(final Path file) {
        return delegate.isHidden(file.attributes().getDecrypted());
    }

    @Override
    public boolean isValid(final Path file) {
        return delegate.isValid(file.attributes().getDecrypted());
    }

    @Override
    public boolean isCached(final Path folder) {
        return delegate.isCached(folder.attributes().getDecrypted());
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public AttributedList<Path> put(final Path folder, final AttributedList<Path> encrypted) {
        final AttributedList<Path> list = new AttributedList<>();
        // Swap with decrypted paths
        for(int i = 0; i < encrypted.size(); i++) {
            final Path f = encrypted.get(i);
            if(f.getType().contains(Path.Type.encrypted)) {
                list.add(i, f.attributes().getDecrypted());
            }
            else {
                list.add(i, f);
            }
        }
        return delegate.put(folder.attributes().getDecrypted(), list);
    }

    @Override
    public AttributedList<Path> get(final Path folder) {
        final AttributedList<Path> decrypted = delegate.get(folder.attributes().getDecrypted());
        final AttributedList<Path> list = new AttributedList<>();
        // Swap with encrypted paths
        for(int i = 0; i < decrypted.size(); i++) {
            final Path f = decrypted.get(i);
            if(f.getType().contains(Path.Type.decrypted)) {
                list.add(i, f.attributes().getEncrypted());
            }
            else {
                list.add(i, f);
            }
        }
        return list;
    }

    @Override
    public AttributedList<Path> remove(final Path folder) {
        return delegate.remove(folder.attributes().getDecrypted());
    }

    @Override
    public Set<Path> keySet() {
        return delegate.keySet();
    }

    @Override
    public void invalidate(final Path folder) {
        delegate.invalidate(folder.attributes().getDecrypted());
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Path lookup(final CacheReference<Path> reference) {
        return delegate.lookup(reference);
    }
}
