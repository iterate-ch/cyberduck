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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.io.BaseEncoding;

public class CryptoDirectoryV7Provider implements CryptoDirectory {
    private static final Logger log = LogManager.getLogger(CryptoDirectoryV7Provider.class);

    private static final String DATA_DIR_NAME = "d";

    public static final byte[] ROOT_DIR_ID = new byte[0];

    private final AbstractVault vault;
    private final Path dataRoot;
    private final Path home;
    private final CryptoFilename filenameProvider;

    private final RandomStringService random
            = new UUIDRandomStringService();

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();
    protected final LRUCache<CacheReference<Path>, byte[]> cache = LRUCache.build(
            PreferencesFactory.get().getInteger("cryptomator.cache.size"));


    public CryptoDirectoryV7Provider(final AbstractVault vault, final CryptoFilename filenameProvider) {
        this.home = vault.getHome();
        this.dataRoot = new Path(vault.getHome(), DATA_DIR_NAME, vault.getHome().getType());
        this.filenameProvider = filenameProvider;
        this.vault = vault;
    }

    @Override
    public String toEncrypted(final Session<?> session, final Path parent, final String filename, final EnumSet<Path.Type> type) throws BackgroundException {
        final String ciphertextName = this.vault.getCryptor().fileNameCryptor().encryptFilename(BaseEncoding.base64Url(), filename, this.getOrCreateDirectoryId(session, parent)) + vault.getRegularFileExtension();
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
            // Remember random directory id for use in vault
            final byte[] id = this.getOrCreateDirectoryId(session, directory);
            log.debug("Use directory ID '{}' for folder {}", id, directory);
            attributes.setDirectoryId(id);
            attributes.setDecrypted(directory);
            //TODO generalize whole method but must extract fileNameCryptor too
            final String directoryIdHash = this.vault.getCryptor().fileNameCryptor().hashDirectoryId(id);
            // Intermediate directory
            final Path intermediate = new Path(dataRoot, directoryIdHash.substring(0, 2), dataRoot.getType());
            // Add encrypted type
            final EnumSet<Path.Type> type = EnumSet.copyOf(directory.getType());
            type.add(Path.Type.encrypted);
            type.remove(Path.Type.decrypted);
            return new Path(intermediate, directoryIdHash.substring(2), type, attributes);
        }
        throw new NotfoundException(directory.getAbsolute());
    }

    protected byte[] toDirectoryId(final Session<?> session, final Path directory) throws BackgroundException {
        if(new SimplePathPredicate(home).test(directory)) {
            return ROOT_DIR_ID;
        }
        lock.readLock().lock();
        try {
            if(cache.contains(new SimplePathPredicate(directory))) {
                return cache.get(new SimplePathPredicate(directory));
            }
        }
        finally {
            lock.readLock().unlock();
        }
        try {
            log.debug("Acquire lock for {}", directory);
            lock.writeLock().lock();
            final byte[] id = this.load(session, directory);
            cache.put(new SimplePathPredicate(directory), id);
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
    public byte[] getOrCreateDirectoryId(final Session<?> session, final Path file) throws BackgroundException {
        if(file.attributes().getDirectoryId() != null) {
            return file.attributes().getDirectoryId();
        }
        final Path decrypted = file.getType().contains(AbstractPath.Type.encrypted) ? file.attributes().getDecrypted() : file;
        return this.toDirectoryId(session, decrypted.getType().contains(AbstractPath.Type.file) ? decrypted.getParent() : decrypted);
    }

    @Override
    public byte[] createDirectoryId(final Path directory) {
        lock.writeLock().lock();
        try {
            final byte[] directoryId = random.random().getBytes(StandardCharsets.US_ASCII);
            cache.put(new SimplePathPredicate(directory), directoryId);
            return directoryId;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    protected byte[] load(final Session<?> session, final Path directory) throws BackgroundException {
        final Path encryptedParent = this.toEncrypted(session, directory.getParent());
        final String cleartextName = directory.getName();
        final String ciphertextName = this.toEncrypted(session, directory.getParent(), cleartextName, EnumSet.of(Path.Type.directory));
        final Path metadataParent = new Path(encryptedParent, ciphertextName, EnumSet.of(Path.Type.directory));
        // Read directory id from file
        try {
            log.debug("Read directory ID for folder {} from {}", directory, ciphertextName);
            final Path metadataFile = new Path(metadataParent, vault.getDirectoryMetadataFilename(), EnumSet.of(Path.Type.file, Path.Type.encrypted));
            return new ContentReader(session).readBytes(metadataFile);
        }
        catch(NotfoundException e) {
            log.warn("Missing directory ID for folder {}", directory);
            return random.random().getBytes(StandardCharsets.US_ASCII);
        }
    }
}
