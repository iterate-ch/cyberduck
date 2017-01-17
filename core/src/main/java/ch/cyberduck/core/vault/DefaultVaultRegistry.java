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

import ch.cyberduck.core.ListService;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.vault.registry.*;

import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArraySet;

public class DefaultVaultRegistry extends CopyOnWriteArraySet<Vault> implements VaultRegistry {
    private static final Logger log = Logger.getLogger(DefaultVaultRegistry.class);

    private final PasswordStore keychain;
    private final PasswordCallback prompt;

    public DefaultVaultRegistry(final PasswordCallback prompt) {
        this(PasswordStoreFactory.get(), prompt);
    }

    public DefaultVaultRegistry(final PasswordStore keychain, final PasswordCallback prompt) {
        this.keychain = keychain;
        this.prompt = prompt;
    }

    public DefaultVaultRegistry(final PasswordStore keychain, final PasswordCallback prompt, final Vault... vaults) {
        super(Arrays.asList(vaults));
        this.keychain = keychain;
        this.prompt = prompt;
    }

    @Override
    public void found(final Vault vault) throws VaultUnlockCancelException {
        // Add if absent
        this.add(vault);
    }

    @Override
    public boolean contains(final Vault vault) {
        return super.contains(vault);
    }

    @Override
    public void clear() {
        if(log.isInfoEnabled()) {
            log.info("Close vaults");
        }
        this.forEach(Vault::close);
        super.clear();
    }

    @Override
    public Vault find(final Session session, final Path file) throws ConnectionCanceledException {
        return this.find(session, file, true);
    }

    /**
     * @param session Connection
     * @param file    File
     * @param lookup  Find and load any vault
     * @return Open or disabled vault
     */
    public Vault find(final Session session, final Path file, final boolean lookup) throws ConnectionCanceledException {
        for(Vault vault : this) {
            if(vault.contains(file)) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Found vault %s for file %s", vault, file));
                }
                return vault;
            }
        }
        if(lookup) {
            final LoadingVaultLookupListener listener = new LoadingVaultLookupListener(session, this, prompt);
            if(file.attributes().getVault() != null) {
                return this.find(file, listener);
            }
            final Path directory = file.getParent();
            if(directory.attributes().getVault() != null) {
                return this.find(directory, listener);
            }
        }
        return Vault.DISABLED;
    }

    private Vault find(final Path directory, final LoadingVaultLookupListener listener) throws ConnectionCanceledException {
        final Vault vault = VaultFactory.get(directory.attributes().getVault(), keychain);
        listener.found(vault);
        return vault;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Session<?> session, final Class<T> type, final T proxy) {
        if(null == proxy) {
            // No proxying for disabled features
            return null;
        }
        if(type == ListService.class) {
            return (T) new VaultRegistryListService(session, (ListService) proxy, this,
                    new LoadingVaultLookupListener(session, this, prompt), keychain);
        }
        if(type == Bulk.class) {
            return (T) new VaultRegistryBulkFeature(session, (Bulk) proxy, this);
        }
        if(type == Touch.class) {
            return (T) new VaultRegistryTouchFeature(session, ((Touch) proxy), this);
        }
        if(type == Directory.class) {
            return (T) new VaultRegistryDirectoryFeature(session, (Directory) proxy, this);
        }
        if(type == Upload.class) {
            return (T) new VaultRegistryUploadFeature(session, (Upload) proxy, this);
        }
        if(type == Download.class) {
            return (T) new VaultRegistryDownloadFeature(session, (Download) proxy, this);
        }
        if(type == Read.class) {
            return (T) new VaultRegistryReadFeature(session, (Read) proxy, this);
        }
        if(type == Write.class) {
            return (T) new VaultRegistryWriteFeature(session, (Write) proxy, this);
        }
        if(type == MultipartWrite.class) {
            return (T) new VaultRegistryMultipartWriteFeature(session, (MultipartWrite) proxy, this);
        }
        if(type == Move.class) {
            return (T) new VaultRegistryMoveFeature(session, (Move) proxy, this);
        }
        if(type == AttributesFinder.class) {
            return (T) new VaultRegistryAttributesFeature(session, (AttributesFinder) proxy, this);
        }
        if(type == Find.class) {
            return (T) new VaultRegistryFindFeature(session, (Find) proxy, this);
        }
        if(type == UrlProvider.class) {
            return (T) new VaultRegistryUrlProvider(session, (UrlProvider) proxy, this);
        }
        if(type == IdProvider.class) {
            return (T) new VaultRegistryIdProvider(session, (IdProvider) proxy, this);
        }
        if(type == Delete.class) {
            return (T) new VaultRegistryDeleteFeature(session, (Delete) proxy, this);
        }
        if(type == Symlink.class) {
            return (T) new VaultRegistrySymlinkFeature(session, (Symlink) proxy, this);
        }
        if(type == Headers.class) {
            return (T) new VaultRegistryHeadersFeature(session, (Headers) proxy, this);
        }
        if(type == Compress.class) {
            return (T) new VaultRegistryCompressFeature(session, (Compress) proxy, this);
        }
        if(type == UnixPermission.class) {
            return (T) new VaultRegistryUnixPermissionFeature(session, (UnixPermission) proxy, this);
        }
        if(type == AclPermission.class) {
            return (T) new VaultRegistryAclPermissionFeature(session, (AclPermission) proxy, this);
        }
        if(type == Copy.class) {
            return (T) new VaultRegistryCopyFeature(session, (Copy) proxy, this);
        }
        if(type == Timestamp.class) {
            return (T) new VaultRegistryTimestampFeature(session, (Timestamp) proxy, this);
        }
        if(type == Encryption.class) {
            return (T) new VaultRegistryEncryptionFeature(session, (Encryption) proxy, this);
        }
        if(type == Lifecycle.class) {
            return (T) new VaultRegistryLifecycleFeature(session, (Lifecycle) proxy, this);
        }
        if(type == Location.class) {
            return (T) new VaultRegistryLocationFeature(session, (Location) proxy, this);
        }
        if(type == Lock.class) {
            return (T) new VaultRegistryLockFeature<>(session, (Lock) proxy, this);
        }
        if(type == Logging.class) {
            return (T) new VaultRegistryLoggingFeature(session, (Logging) proxy, this);
        }
        if(type == Redundancy.class) {
            return (T) new VaultRegistryRedundancyFeature(session, (Redundancy) proxy, this);
        }
        if(type == Search.class) {
            return (T) new VaultRegistrySearchFeature(session, (Search) proxy, this);
        }
        if(type == TransferAcceleration.class) {
            return (T) new VaultRegistryTransferAccelerationFeature<>(session, (TransferAcceleration) proxy, this);
        }
        if(type == Versioning.class) {
            return (T) new VaultRegistryVersioningFeature(session, (Versioning) proxy, this);
        }
        return proxy;
    }
}
