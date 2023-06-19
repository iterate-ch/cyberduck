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
import ch.cyberduck.core.IndexedListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;

public class VaultFinderListProgressListener extends IndexedListProgressListener {
    private static final Logger log = LogManager.getLogger(VaultFinderListProgressListener.class);

    private final Session<?> session;
    private final VaultLookupListener listener;
    private final ListProgressListener progress;

    public VaultFinderListProgressListener(final Session<?> session, final VaultLookupListener listener, final ListProgressListener progress) {
        this.session = session;
        this.listener = listener;
        this.progress = progress;
    }

    @Override
    public void visit(final AttributedList<Path> list, final int index, final Path file) throws ConnectionCanceledException {
        final Path directory = file.getParent();
        if(new HostPreferences(session.getHost()).getProperty("cryptomator.vault.config.filename").equals(file.getName()) ||
                new HostPreferences(session.getHost()).getProperty("cryptomator.vault.masterkey.filename").equals(file.getName())) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Found vault config or masterkey file %s", file));
            }
            try {
                final Vault vault = listener.load(session, directory,
                        new HostPreferences(session.getHost()).getProperty("cryptomator.vault.masterkey.filename"),
                        new HostPreferences(session.getHost()).getProperty("cryptomator.vault.config.filename"),
                        new HostPreferences(session.getHost()).getProperty("cryptomator.vault.pepper").getBytes(StandardCharsets.UTF_8));
                if(vault.equals(Vault.DISABLED)) {
                    return;
                }
                throw new VaultFoundListCanceledException(vault, list);
            }
            catch(VaultUnlockCancelException e) {
                // Continue
            }
        }
    }

    @Override
    public void message(final String message) {
        progress.message(message);
    }
}
