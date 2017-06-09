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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.cryptomator.impl.CryptoFilenameProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class CryptoDeleteFeature implements Delete {
    private static final Logger log = Logger.getLogger(CryptoDeleteFeature.class);

    private final Session<?> session;
    private final Delete proxy;
    private final CryptoVault vault;
    private final CryptoFilenameProvider filenameProvider;

    public CryptoDeleteFeature(final Session<?> session, final Delete proxy, final CryptoVault vault) {
        this.session = session;
        this.proxy = proxy;
        this.vault = vault;
        this.filenameProvider = vault.getFilenameProvider();
    }

    @Override
    public void delete(final List<Path> files, final LoginCallback prompt, final Callback callback) throws BackgroundException {
        final List<Path> encrypted = new ArrayList<>();
        for(Path f : files) {
            if(!f.equals(vault.getHome())) {
                final Path encrypt = vault.encrypt(session, f);
                encrypted.add(encrypt);
                final Path metadata = vault.encrypt(session, f, true);
                if(f.isDirectory()) {
                    // Delete metadata file for directory
                    encrypted.add(metadata);
                }
                if(filenameProvider.isDeflated(metadata.getName())) {
                    final Path metadataFile = filenameProvider.resolve(metadata.getName());
                    encrypted.add(metadataFile);
                }
            }
        }
        if(!encrypted.isEmpty()) {
            proxy.delete(encrypted, prompt, callback);
        }
        for(Path f : files) {
            if(f.equals(vault.getHome())) {
                final List<Path> metadata = new ArrayList<>();
                if(!proxy.isRecursive()) {
                    final Find find = session._getFeature(Find.class);
                    final Path dataRoot = new Path(vault.getHome(), "d", EnumSet.of(Path.Type.directory));
                    if(find.find(dataRoot)) {
                        for(Path d : session._getFeature(ListService.class).list(dataRoot, new DisabledListProgressListener()).toList()) {
                            metadata.addAll(session._getFeature(ListService.class).list(d, new DisabledListProgressListener()).toList());
                            metadata.add(d);
                        }
                        metadata.add(dataRoot);
                    }
                    final Path metaRoot = new Path(vault.getHome(), "m", EnumSet.of(Path.Type.directory));
                    if(find.find(metaRoot)) {
                        for(Path m : session._getFeature(ListService.class).list(metaRoot, new DisabledListProgressListener()).toList()) {
                            metadata.addAll(session._getFeature(ListService.class).list(m, new DisabledListProgressListener()).toList());
                            metadata.add(m);
                        }
                        metadata.add(metaRoot);
                    }
                    metadata.add(new Path(vault.getHome(), "masterkey.cryptomator", EnumSet.of(Path.Type.file)));
                }
                metadata.add(f);
                proxy.delete(metadata, prompt, callback);
            }
        }
    }

    @Override
    public boolean isSupported(final Path file) {
        return proxy.isSupported(file);
    }

    @Override
    public boolean isRecursive() {
        return proxy.isRecursive();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoDeleteFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
