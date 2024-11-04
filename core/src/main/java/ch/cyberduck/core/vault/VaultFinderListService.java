package ch.cyberduck.core.vault;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VaultFinderListService implements ListService {
    private static final Logger log = LogManager.getLogger(VaultFinderListService.class);

    private final Session<?> session;
    private final ListService delegate;
    private final VaultLookupListener loader;
    private final int filecount;

    public VaultFinderListService(final Session<?> session, final ListService delegate, final VaultLookupListener loader, final ListProgressListener listener) {
        this.session = session;
        this.delegate = delegate;
        this.loader = loader;
        this.filecount = new HostPreferences(session.getHost()).getInteger("cryptomator.vault.autodetect.filecount");
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> list = delegate.list(directory, new VaultFinderListProgressListener(session, loader, listener, filecount));
            if(list.size() < filecount) {
                listener.chunk(directory, list);
            }
            return list;
        }
        catch(VaultFoundListCanceledException finder) {
            final Vault cryptomator = finder.getVault();
            log.info("Found vault {}", cryptomator);
            return delegate.list(cryptomator.encrypt(session, directory), new DecryptingListProgressListener(session, cryptomator, listener));
        }
    }

    @Override
    public void preflight(final Path directory) throws BackgroundException {
        delegate.preflight(directory);
    }
}
