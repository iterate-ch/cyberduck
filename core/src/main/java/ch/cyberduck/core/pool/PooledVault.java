package ch.cyberduck.core.pool;

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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Vault;

import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicInteger;

public final class PooledVault implements Vault {
    private static final Logger log = Logger.getLogger(PooledVault.class);

    private final Vault delegate;
    private final AtomicInteger open = new AtomicInteger(0);

    public PooledVault(final Vault delegate) {
        this.delegate = delegate;
    }

    @Override
    public Vault create(final Session<?> session, final String region, final PasswordCallback prompt) throws BackgroundException {
        return delegate.create(session, region, prompt);
    }

    @Override
    public Vault load(final Session<?> session, final PasswordCallback prompt) throws BackgroundException {
        if(1 == open.incrementAndGet()) {
            delegate.load(session, prompt);
        }
        return this;
    }

    /**
     * Pool that is not closed on disconnect. Close vault when pool is shutdown instead.
     */
    @Override
    public void close() {
        if(0 == open.decrementAndGet()) {
            delegate.close();
        }
        else {
            log.warn(String.format("Keep vault %s open for pool", delegate));
        }
    }

    @Override
    public boolean contains(final Path file) {
        return delegate.contains(file);
    }

    @Override
    public Path encrypt(final Session<?> session, final Path file) throws BackgroundException {
        return delegate.encrypt(session, file);
    }

    @Override
    public Path encrypt(final Session<?> session, final Path file, final boolean metadata) throws BackgroundException {
        return delegate.encrypt(session, file, metadata);
    }

    @Override
    public Path decrypt(final Session<?> session, final Path directory, final Path file) throws BackgroundException {
        return delegate.decrypt(session, directory, file);
    }

    @Override
    public long toCiphertextSize(final long cleartextFileSize) {
        return delegate.toCiphertextSize(cleartextFileSize);
    }

    @Override
    public long toCleartextSize(final long ciphertextFileSize) throws BackgroundException {
        return delegate.toCleartextSize(ciphertextFileSize);
    }

    @Override
    public <T> T getFeature(final Session<?> session, final Class<T> type, final T impl) {
        return delegate.getFeature(session, type, impl);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PooledVault{");
        sb.append("delegate=").append(delegate);
        sb.append('}');
        return sb.toString();
    }
}
