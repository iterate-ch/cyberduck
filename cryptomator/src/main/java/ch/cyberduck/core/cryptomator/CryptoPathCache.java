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

import java.util.Set;

public final class CryptoPathCache implements Cache<Path> {

    private final Cache<Path> delegate;

    public CryptoPathCache(final Cache<Path> delegate) {
        this.delegate = delegate;
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
    public AttributedList<Path> put(final Path folder, final AttributedList<Path> children) {
        return delegate.put(folder.attributes().getDecrypted(), children);
    }

    @Override
    public AttributedList<Path> get(final Path folder) {
        return delegate.get(folder.attributes().getDecrypted());
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
