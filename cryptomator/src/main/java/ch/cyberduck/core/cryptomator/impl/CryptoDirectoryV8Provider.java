package ch.cyberduck.core.cryptomator.impl;

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
import ch.cyberduck.core.CacheReference;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.RandomStringService;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.cache.LRUCache;
import ch.cyberduck.core.cryptomator.AbstractVault;
import ch.cyberduck.core.cryptomator.ContentReader;
import ch.cyberduck.core.cryptomator.CryptoDirectory;
import ch.cyberduck.core.cryptomator.CryptoFilename;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cryptomator.cryptolib.api.DirectoryMetadata;

import java.util.EnumSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CryptoDirectoryV8Provider implements CryptoDirectory {
    private static final Logger log = LogManager.getLogger(CryptoDirectoryV8Provider.class);

    private static final String DATA_DIR_NAME = "d";

    private final AbstractVault vault;
    private final Path dataRoot;
    private final Path home;
    private final CryptoFilename filenameProvider;

    private final RandomStringService random
            = new UUIDRandomStringService();

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();
    protected final LRUCache<CacheReference<Path>, byte[]> cache = LRUCache.build(
            PreferencesFactory.get().getInteger("cryptomator.cache.size"));


    public CryptoDirectoryV8Provider(final AbstractVault vault, final CryptoFilename filenameProvider) {
        this.home = vault.getHome();
        this.dataRoot = new Path(vault.getHome(), DATA_DIR_NAME, vault.getHome().getType());
        this.filenameProvider = filenameProvider;
        this.vault = vault;
    }

    @Override
    public String toEncrypted(final Session<?> session, final Path parent, final String filename, final EnumSet<Path.Type> type) throws BackgroundException {
        final DirectoryMetadata dirMetadata = this.getOrCreateDirectoryId(session, parent);
        this.vault.getCryptor().directoryContentCryptor().fileNameEncryptor(dirMetadata).encrypt(filename);
        final String ciphertextName = this.vault.getCryptor().directoryContentCryptor().fileNameEncryptor(dirMetadata).encrypt(filename);
        log.debug("Encrypted filename {} to {}", filename, ciphertextName);
        return filenameProvider.deflate(session, ciphertextName);
    }

    @Override
    public Path toEncrypted(final Session<?> session, final Path directory) throws BackgroundException {
        if(!directory.isDirectory()) {
            throw new NotfoundException(directory.getAbsolute());
        }
        if(new SimplePathPredicate(directory).test(home) || directory.isChild(home)) {
            final PathAttributes attributes = new PathAttributes(directory.attributes());
            // The root of the vault is a different target directory and file ids always correspond to the metadata file
            attributes.setVersionId(null);
            attributes.setFileId(null);
            // Remember random directory metadata for use in vault
            final DirectoryMetadata dirMetadata = this.getOrCreateDirectoryId(session, directory);
            log.debug("Use directory ID '{}' for folder {}", dirMetadata, directory);
            attributes.setDirectoryId(this.vault.getCryptor().directoryContentCryptor().encryptDirectoryMetadata(dirMetadata));
            attributes.setDecrypted(directory);
            final String dirPath = this.vault.getCryptor().directoryContentCryptor().dirPath(dirMetadata);
            final String[] segments = StringUtils.split(dirPath, '/');
            // Intermediate directory
            final Path intermediate = new Path(dataRoot, segments[1], dataRoot.getType());
            // Add encrypted type
            final EnumSet<Path.Type> type = EnumSet.copyOf(directory.getType());
            type.add(Path.Type.encrypted);
            type.remove(Path.Type.decrypted);
            return new Path(intermediate, segments[2], type, attributes);
        }
        throw new NotfoundException(directory.getAbsolute());
    }

    protected DirectoryMetadata toDirectoryId(final Session<?> session, final Path directory) throws BackgroundException {
        if(new SimplePathPredicate(home).test(directory)) {
            return this.vault.getRootDirId();
        }
        lock.readLock().lock();
        try {
            if(cache.contains(new SimplePathPredicate(directory))) {
                return this.vault.getCryptor().directoryContentCryptor().decryptDirectoryMetadata(cache.get(new SimplePathPredicate(directory)));
            }
        }
        finally {
            lock.readLock().unlock();
        }
        try {
            log.debug("Acquire lock for {}", directory);
            lock.writeLock().lock();
            final DirectoryMetadata id = this.load(session, directory);
            cache.put(new SimplePathPredicate(directory), this.vault.getCryptor().directoryContentCryptor().encryptDirectoryMetadata(id));
            return id;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public void delete(final Path directory) {
        lock.writeLock().lock();
        try {
            cache.remove(new SimplePathPredicate(directory));
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void destroy() {
        lock.writeLock().lock();
        try {
            cache.clear();
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public DirectoryMetadata getOrCreateDirectoryId(final Session<?> session, final Path file) throws BackgroundException {
        if(file.attributes().getDirectoryId() != null) {
            return this.vault.getCryptor().directoryContentCryptor().decryptDirectoryMetadata(file.attributes().getDirectoryId());
        }
        final Path decrypted = file.getType().contains(AbstractPath.Type.encrypted) ? file.attributes().getDecrypted() : file;
        return this.toDirectoryId(session, decrypted.getType().contains(AbstractPath.Type.file) ? decrypted.getParent() : decrypted);
    }

    @Override
    public DirectoryMetadata createDirectoryId(final Path directory) {
        lock.writeLock().lock();
        try {
            final DirectoryMetadata metadata = vault.getCryptor().directoryContentCryptor().newDirectoryMetadata();
            final byte[] encrypted = vault.getCryptor().directoryContentCryptor().encryptDirectoryMetadata(metadata);
            cache.put(new SimplePathPredicate(directory), encrypted);
            return metadata;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    protected DirectoryMetadata load(final Session<?> session, final Path directory) throws BackgroundException {
        final Path encryptedParent = this.toEncrypted(session, directory.getParent());
        final String cleartextName = directory.getName();
        final String ciphertextName = this.toEncrypted(session, directory.getParent(), cleartextName, EnumSet.of(Path.Type.directory));
        final Path metadataParent = new Path(encryptedParent, ciphertextName, EnumSet.of(Path.Type.directory));
        // Read directory id from file
        try {
            log.debug("Read directory ID for folder {} from {}", directory, ciphertextName);
            final Path metadataFile = new Path(metadataParent, vault.getDirectoryMetadataFilename(), EnumSet.of(Path.Type.file, Path.Type.encrypted));
            final byte[] bytes = new ContentReader(session).readBytes(metadataFile);
            return this.vault.getCryptor().directoryContentCryptor().decryptDirectoryMetadata(bytes);
        }
        catch(NotfoundException e) {
            log.warn("Missing directory ID for folder {}", directory);
            throw e;
            //TODO check if we need this fallback
            //return random.random().getBytes(StandardCharsets.US_ASCII);
        }
    }
}
