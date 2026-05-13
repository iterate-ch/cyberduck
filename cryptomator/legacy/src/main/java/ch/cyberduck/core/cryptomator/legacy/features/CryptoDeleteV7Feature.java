package ch.cyberduck.core.cryptomator.legacy.features;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.cryptomator.legacy.CryptoFilename;
import ch.cyberduck.core.cryptomator.legacy.CryptomatorVault;
import ch.cyberduck.core.cryptomator.legacy.impl.CryptoDirectoryV7Provider;
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
    private final CryptomatorVault cryptomator;

    public CryptoDeleteV7Feature(final Session<?> session, final Delete proxy, final CryptomatorVault cryptomator) {
        this.session = session;
        this.proxy = proxy;
        this.cryptomator = cryptomator;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        for(Path f : files.keySet()) {
            final List<Path> metadataFiles = new ArrayList<>();
            if(!new SimplePathPredicate(f).test(cryptomator.getHome())) {
                final Path encrypt = cryptomator.encrypt(session, f);
                if(f.isDirectory()) {
                    final Path backup = new Path(encrypt, CryptoDirectoryV7Provider.BACKUP_DIRECTORY_METADATAFILE,
                            EnumSet.of(Path.Type.file));
                    try {
                        log.debug("Deleting directory id backup file {}", backup);
                        proxy.delete(Collections.singletonList(backup), prompt, callback);
                    }
                    catch(NotfoundException | AccessDeniedException e) {
                        if(log.isDebugEnabled()) {
                            log.error("Failure {} deleting directory id backup file {}", e, encrypt);
                        }
                    }
                }
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
                final Path metadata = cryptomator.encrypt(session, f, true);
                if(f.isDirectory()) {
                    final Path metadataFile = new Path(metadata, CryptoDirectoryV7Provider.DIRECTORY_METADATAFILE, EnumSet.of(Path.Type.file));
                    log.debug("Add metadata file {}", metadataFile);
                    metadataFiles.add(metadataFile);
                    metadataFiles.add(metadata);
                    log.debug("Add metadata folder {}", metadata);
                    cryptomator.getDirectoryProvider().delete(f);
                }
                final CryptoFilename filenameProvider = cryptomator.getFilenameProvider();
                if(filenameProvider.isDeflated(metadata.getName())) {
                    filenameProvider.invalidate(filenameProvider.inflate(session, metadata.getName()));
                    final Path metadataFile = filenameProvider.resolve(metadata.getName());
                    log.debug("Add metadata file {}", metadata);
                    metadataFiles.add(metadataFile);
                }
            }
            if(!metadataFiles.isEmpty()) {
                try {
                    proxy.delete(metadataFiles, prompt, callback);
                }
                catch(NotfoundException e) {
                    log.error("Failure {} deleting file {}", e, metadataFiles);
                }
            }
        }
        for(Path f : files.keySet()) {
            if(new SimplePathPredicate(f).test(cryptomator.getHome())) {
                log.warn("Recursively delete vault {}", f);
                final List<Path> metadata = new ArrayList<>();
                if(!proxy.features(f).contains(Delete.Flags.recursive)) {
                    final Find find = session._getFeature(Find.class);
                    final Path dataRoot = new Path(f, "d", f.getType());
                    if(find.find(dataRoot)) {
                        for(Path d : session._getFeature(ListService.class).list(dataRoot, new DisabledListProgressListener()).toList()) {
                            metadata.addAll(session._getFeature(ListService.class).list(d, new DisabledListProgressListener()).toList());
                            metadata.add(d);
                        }
                        metadata.add(dataRoot);
                    }
                    if(cryptomator.getMasterkey() != null) {
                        metadata.add(cryptomator.getMasterkey());
                    }
                    if(find.find(cryptomator.getConfig())) {
                        metadata.add(cryptomator.getConfig());
                    }
                }
                metadata.add(f);
                proxy.delete(metadata, prompt, callback);
            }
        }
    }

    @Override
    public void preflight(final Path file) throws BackgroundException {
        proxy.preflight(cryptomator.encrypt(session, file));
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return proxy.features(file);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoDeleteV7Feature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
