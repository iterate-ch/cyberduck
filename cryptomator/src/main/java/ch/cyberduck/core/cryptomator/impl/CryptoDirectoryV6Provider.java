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

import ch.cyberduck.core.CacheReference;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.RandomStringService;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.cache.LRUCache;
import ch.cyberduck.core.cryptomator.ContentReader;
import ch.cyberduck.core.cryptomator.CryptoDirectory;
import ch.cyberduck.core.cryptomator.CryptoFilename;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.cryptomator.CryptorCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CryptoDirectoryV6Provider implements CryptoDirectory {
    private static final Logger log = LogManager.getLogger(CryptoDirectoryV6Provider.class);

    private static final String DATA_DIR_NAME = "d";

    private final Path dataRoot;
    private final Path home;
    private final CryptoFilename filenameProvider;
    private final CryptorCache filenameCryptor;

    private final RandomStringService random
            = new UUIDRandomStringService();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final LRUCache<CacheReference<Path>, byte[]> cache = LRUCache.build(
            PreferencesFactory.get().getInteger("cryptomator.cache.size"));

    public static final byte[] ROOT_DIR_ID = new byte[0];

    public CryptoDirectoryV6Provider(final Path vault, final CryptoFilename filenameProvider, final CryptorCache filenameCryptor) {
        this.home = vault;
        this.dataRoot = new Path(vault, DATA_DIR_NAME, vault.getType());
        this.filenameProvider = filenameProvider;
        this.filenameCryptor = filenameCryptor;
    }

    @Override
    public String toEncrypted(final Session<?> session, final byte[] directoryId, final String filename, final EnumSet<Path.Type> type) throws BackgroundException {
        final String prefix = type.contains(Path.Type.directory) ? CryptoVault.DIR_PREFIX : "";
        final String ciphertextName = prefix + filenameCryptor.encryptFilename(CryptorCache.BASE32, filename, directoryId);
        log.debug("Encrypted filename {} to {}", filename, ciphertextName);
        return filenameProvider.deflate(session, ciphertextName);
    }

    @Override
    public Path toEncrypted(final Session<?> session, final byte[] directoryId, final Path directory) throws BackgroundException {
        if(!directory.isDirectory()) {
            throw new NotfoundException(directory.getAbsolute());
        }
        if(new SimplePathPredicate(directory).test(home) || directory.isChild(home)) {
            final PathAttributes attributes = new PathAttributes(directory.attributes());
            // The root of the vault is a different target directory and file ids always correspond to the metadata file
            attributes.setVersionId(null);
            attributes.setFileId(null);
            // Remember random directory id for use in vault
            final byte[] id = this.toDirectoryId(session, directory, directoryId);
            log.debug("Use directory ID '{}' for folder {}", id, directory);
            attributes.setDirectoryId(id);
            attributes.setDecrypted(directory);
            final String directoryIdHash = filenameCryptor.hashDirectoryId(id);
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

    protected byte[] toDirectoryId(final Session<?> session, final Path directory, final byte[] directoryId) throws BackgroundException {
        if(new SimplePathPredicate(home).test(directory)) {
            return ROOT_DIR_ID;
        }
        try {
            lock.readLock().lock();
            if(cache.contains(new SimplePathPredicate(directory))) {
                final byte[] existing = cache.get(new SimplePathPredicate(directory));
                if(!Arrays.equals(existing, directoryId)) {
                    log.warn("Do not override already cached id {} with {}", existing, directoryId);
                }
                return existing;
            }
        }
        finally {
            lock.readLock().unlock();
        }
        try {
            log.debug("Acquire lock for {}", directory);
            lock.writeLock().lock();
            final byte[] id = null != directoryId ? this.load(session, directory) : directoryId;
            cache.put(new SimplePathPredicate(directory), id);
            return id;
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    protected byte[] load(final Session<?> session, final Path directory) throws BackgroundException {
        final Path parent = this.toEncrypted(session, directory.getParent().attributes().getDirectoryId(), directory.getParent());
        final String cleartextName = directory.getName();
        final String ciphertextName = this.toEncrypted(session, parent.attributes().getDirectoryId(), cleartextName, EnumSet.of(Path.Type.directory));
        // Read directory id from file
        try {
            log.debug("Read directory ID for folder {} from {}", directory, ciphertextName);
            final Path metadataFile = new Path(parent, ciphertextName, EnumSet.of(Path.Type.file, Path.Type.encrypted));
            return new ContentReader(session).readBytes(metadataFile);
        }
        catch(NotfoundException e) {
            log.warn("Missing directory ID for folder {}", directory);
            return random.random().getBytes(StandardCharsets.US_ASCII);
        }
    }

    public void delete(final Path directory) {
        try {
            lock.writeLock().lock();
            cache.remove(new SimplePathPredicate(directory));
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void destroy() {
        try {
            lock.writeLock().lock();
            cache.clear();
        }
        finally {
            lock.writeLock().unlock();
        }
    }
}
