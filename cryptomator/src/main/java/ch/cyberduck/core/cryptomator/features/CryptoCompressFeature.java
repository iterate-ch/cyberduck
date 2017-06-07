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

import ch.cyberduck.core.Archive;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Compress;
import ch.cyberduck.core.features.Vault;

import java.util.ArrayList;
import java.util.List;

public class CryptoCompressFeature implements Compress {

    private final Session<?> session;
    private final Compress delegate;
    private final Vault vault;

    public CryptoCompressFeature(final Session<?> session, final Compress delegate, final Vault vault) {
        this.session = session;
        this.delegate = delegate;
        this.vault = vault;
    }

    @Override
    public void archive(final Archive archive, final Path workdir, final List<Path> files, final ProgressListener listener, final TranscriptListener transcript) throws BackgroundException {
        final List<Path> encrypted = new ArrayList<>();
        for(Path f : files) {
            encrypted.add(vault.encrypt(session, f));
        }
        delegate.archive(archive, vault.encrypt(session, workdir), encrypted, listener, transcript);
    }

    @Override
    public void unarchive(final Archive archive, final Path file, final ProgressListener listener, final TranscriptListener transcript) throws BackgroundException {
        delegate.unarchive(archive, vault.encrypt(session, file), listener, transcript);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoCompressFeature{");
        sb.append("delegate=").append(delegate);
        sb.append('}');
        return sb.toString();
    }
}
