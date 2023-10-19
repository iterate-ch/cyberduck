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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.CryptoFilename;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.cryptomator.impl.CryptoDirectoryV7Provider;
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

public class CryptoDeleteV7Feature implements Delete, Trash {
    private static final Logger log = LogManager.getLogger(CryptoDeleteV7Feature.class);

    private final Session<?> session;
    private final Delete proxy;
    private final CryptoVault vault;
    private final CryptoFilename filenameProvider;

    public CryptoDeleteV7Feature(final Session<?> session, final Delete proxy, final CryptoVault vault) {
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
                if(f.isDirectory()) {
                    final Path backup = new Path(encrypt, CryptoDirectoryV7Provider.BACKUP_DIRECTORY_METADATAFILE,
                            EnumSet.of(AbstractPath.Type.file));
                    try {
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Deleting directory id backup file %s", backup));
                        }
                        proxy.delete(Collections.singletonList(backup), prompt, callback);
                    }
                    catch(NotfoundException | AccessDeniedException e) {
                        if(log.isDebugEnabled()) {
                            log.error(String.format("Failure %s deleting directory id backup file %s", e, encrypt));
                        }
                    }
                }
                try {
                    proxy.delete(Collections.singletonList(encrypt), prompt, callback);
                }
                catch(NotfoundException | AccessDeniedException e) {
                    if(f.isDirectory()) {
                        log.error(String.format("Failure %s deleting directory %s", e, encrypt));
                    }
                    else {
                        throw e;
                    }
                }
                final Path metadata = vault.encrypt(session, f, true);
                if(f.isDirectory()) {
                    final Path metadataFile = new Path(metadata, CryptoDirectoryV7Provider.DIRECTORY_METADATAFILE, EnumSet.of(Path.Type.file));
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Add metadata file %s", metadataFile));
                    }
                    metadataFiles.add(metadataFile);
                    metadataFiles.add(metadata);
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Add metadata folder %s", metadata));
                    }
                    vault.getDirectoryProvider().delete(f);
                }
                if(filenameProvider.isDeflated(metadata.getName())) {
                    filenameProvider.invalidate(filenameProvider.inflate(session, metadata.getName()));
                    final Path metadataFile = filenameProvider.resolve(metadata.getName());
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Add metadata file %s", metadata));
                    }
                    metadataFiles.add(metadataFile);
                }
            }
            if(!metadataFiles.isEmpty()) {
                try {
                    proxy.delete(metadataFiles, prompt, callback);
                }
                catch(NotfoundException e) {
                    log.error(String.format("Failure %s deleting file %s", e, metadataFiles));
                }
            }
        }
        for(Path f : files.keySet()) {
            if(f.equals(vault.getHome())) {
                log.warn(String.format("Recursively delete vault %s", f));
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
                    if(vault.getMasterkey() != null) {
                        metadata.add(vault.getMasterkey());
                    }
                    if(find.find(vault.getConfig())) {
                        metadata.add(vault.getConfig());
                    }
                }
                metadata.add(f);
                proxy.delete(metadata, prompt, callback);
            }
        }
    }

    @Override
    public void preflight(final Path file) throws BackgroundException {
        proxy.preflight(file);
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
