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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class VaultFinderListProgressListener extends IndexedListProgressListener {
    private static final Logger log = LogManager.getLogger(VaultFinderListProgressListener.class);

    private final Session<?> session;
    private final VaultLookupListener lookup;
    private final ListProgressListener proxy;
    private final String config;
    private final String masterkey;
    private final byte[] pepper;
    // Number of files to wait for until proxy is notified of files
    private final int filecount;
    private final AtomicBoolean canceled = new AtomicBoolean();

    public VaultFinderListProgressListener(final Session<?> session, final VaultLookupListener lookup, final ListProgressListener proxy, final int filecount) {
        this.session = session;
        this.lookup = lookup;
        this.proxy = proxy;
        this.config = new HostPreferences(session.getHost()).getProperty("cryptomator.vault.config.filename");
        this.masterkey = new HostPreferences(session.getHost()).getProperty("cryptomator.vault.masterkey.filename");
        this.pepper = new HostPreferences(session.getHost()).getProperty("cryptomator.vault.pepper").getBytes(StandardCharsets.UTF_8);
        this.filecount = filecount;
    }

    @Override
    public VaultFinderListProgressListener reset() throws ConnectionCanceledException {
        super.reset();
        proxy.reset();
        return this;
    }

    @Override
    public void chunk(final Path folder, final AttributedList<Path> list) throws ConnectionCanceledException {
        // Defer notification until we can be sure no vault is found
        if(!canceled.get() && list.size() < filecount) {
            if(log.isDebugEnabled()) {
                log.debug("Delay chunk notification for file listing of folder {}", folder);
            }
            try {
                super.chunk(folder, list);
            }
            catch(VaultUnlockCancelException e) {
                // No longer prompt
                canceled.set(true);
            }
        }
        else {
            // Delegate
            proxy.chunk(folder, list);
        }
    }

    @Override
    public void visit(final AttributedList<Path> list, final int index, final Path file) throws ConnectionCanceledException {
        final Path directory = file.getParent();
        if(config.equals(file.getName()) || masterkey.equals(file.getName())) {
            if(log.isInfoEnabled()) {
                log.info("Found vault config or masterkey file {}", file);
            }
            final Vault vault = lookup.load(session, directory, masterkey, config, pepper);
            if(vault.equals(Vault.DISABLED)) {
                return;
            }
            throw new VaultFoundListCanceledException(vault, list);
        }
    }

    @Override
    public void message(final String message) {
        proxy.message(message);
    }

    @Override
    public void finish(final Path directory, final AttributedList<Path> list, final Optional<BackgroundException> e) throws ConnectionCanceledException {
        proxy.finish(directory, list, e);
    }
}
