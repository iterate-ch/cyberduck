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

import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.MemoryListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.vault.DecryptingListProgressListener;

public class CryptoAttributesFeature implements AttributesFinder {

    private final Session<?> session;
    private final AttributesFinder delegate;
    private final Vault vault;

    public CryptoAttributesFeature(final Session<?> session, final AttributesFinder delegate, final Vault cryptomator) {
        this.session = session;
        this.delegate = delegate;
        this.vault = cryptomator;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        final MemoryListProgressListener memory = new MemoryListProgressListener();
        // Fetch with any directory listing stored in memory encrypted
        final PathAttributes attributes = new DefaultPathAttributes(delegate.find(vault.encrypt(session, file, true), memory));
        final Path directory = file.getParent();
        // Decrypt directory listing and forward to proxy
        new DecryptingListProgressListener(session, vault, directory, listener).chunk(directory, memory.getContents());
        if(file.isFile()) {
            attributes.setSize(vault.toCleartextSize(0L, attributes.getSize()));
        }
        if(file.isDirectory()) {
            attributes.setSize(-1L);
        }
        attributes.setVault(vault.getHome());
        return attributes;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoAttributesFeature{");
        sb.append("delegate=").append(delegate);
        sb.append('}');
        return sb.toString();
    }
}
