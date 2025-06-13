package ch.cyberduck.core.cryptomator.features;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.CryptoFilename;
import ch.cyberduck.core.cryptomator.CryptoVaultInterface;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Trash;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class CryptoDeleteV6Feature implements Delete, Trash {
    private static final Logger log = LogManager.getLogger(CryptoDeleteV6Feature.class);

    private final Session<?> session;
    private final Delete proxy;
    private final CryptoVaultInterface vault;
    private final CryptoFilename filenameProvider;

    public CryptoDeleteV6Feature(final Session<?> session, final Delete proxy, final CryptoVaultInterface vault) {
        this.session = session;
        this.proxy = proxy;
        this.vault = vault;
        this.filenameProvider = vault.getFilenameProvider();
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        for(Path f : files.keySet()) {
            final List<Path> metadataFiles = new ArrayList<>();
            if(!f.equals(vault.getHome())) {
                final Path encrypt = vault.encrypt(session, f);
                try {
                    proxy.delete(Collections.singletonList(encrypt), prompt, callback);
                }
                catch(NotfoundException | AccessDeniedException e) {
                    if(f.isDirectory()) {
                        log.error("Failure {} deleting directory {}", e, encrypt);
                    }
                    else {
                        throw e;
                    }
                }
                final Path metadata = vault.encrypt(session, f, true);
                if(f.isDirectory()) {
                    // Delete metadata file for directory
                    log.debug("Add metadata file {}", metadata);
                    metadataFiles.add(metadata);
                    vault.getDirectoryProvider().delete(f);
                }
                if(filenameProvider.isDeflated(metadata.getName())) {
                    filenameProvider.invalidate(filenameProvider.inflate(session, metadata.getName()));
                    final Path metadataFile = filenameProvider.resolve(metadata.getName());
                    log.debug("Add metadata file {}", metadata);
                    metadataFiles.add(metadataFile);
                }
            }
            if(!metadataFiles.isEmpty()) {
                proxy.delete(metadataFiles, prompt, callback);
            }
        }
        for(Path f : files.keySet()) {
            if(f.equals(vault.getHome())) {
                log.warn("Recursively delete vault {}", f);
                final List<Path> metadata = new ArrayList<>();
                if(!proxy.isRecursive()) {
                    final Find find = session._getFeature(Find.class);
                    final Path dataRoot = new Path(f, "d", f.getType());
                    if(find.find(dataRoot)) {
                        for(Path d : session._getFeature(ListService.class).list(dataRoot, new DisabledListProgressListener()).toList()) {
                            metadata.addAll(session._getFeature(ListService.class).list(d, new DisabledListProgressListener()).toList());
                            metadata.add(d);
                        }
                        metadata.add(dataRoot);
                    }
                    final Path metaRoot = new Path(f, "m", f.getType());
                    if(find.find(metaRoot)) {
                        for(Path m : session._getFeature(ListService.class).list(metaRoot, new DisabledListProgressListener()).toList()) {
                            for(Path m2 : session._getFeature(ListService.class).list(m, new DisabledListProgressListener()).toList()) {
                                metadata.addAll(session._getFeature(ListService.class).list(m2, new DisabledListProgressListener()).toList());
                                metadata.add(m2);
                            }
                            metadata.add(m);
                        }
                        metadata.add(metaRoot);
                    }
                    metadata.add(vault.getMasterkey());
                }
                metadata.add(f);
                proxy.delete(metadata, prompt, callback);
            }
        }
    }

    @Override
    public void preflight(final Path file) throws BackgroundException {
        proxy.preflight(vault.encrypt(session, file));
    }

    @Override
    public EnumSet<Flags> features() {
        return proxy.features();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoDeleteV6Feature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
