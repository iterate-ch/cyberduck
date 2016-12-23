package ch.cyberduck.core.vault.registry;

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

import ch.cyberduck.core.Archive;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Compress;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

import java.util.List;

public class VaultRegistryCompressFeature implements Compress {
    private final DefaultVaultRegistry registry;
    private final Session<?> session;
    private final Compress proxy;

    public VaultRegistryCompressFeature(final Session<?> session, final Compress proxy, final DefaultVaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public void archive(final Archive archive, final Path workdir, final List<Path> files, final ProgressListener listener, final TranscriptListener transcript) throws BackgroundException {
        registry.find(workdir).getFeature(session, Compress.class, proxy).archive(archive, workdir, files, listener, transcript);
    }

    @Override
    public void unarchive(final Archive archive, final Path file, final ProgressListener listener, final TranscriptListener transcript) throws BackgroundException {
        registry.find(file).getFeature(session, Compress.class, proxy).unarchive(archive, file, listener, transcript);
    }
}
