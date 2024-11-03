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
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.cryptomator.CryptorCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CryptoDirectoryV6Provider implements CryptoDirectory {
    private static final Logger log = LogManager.getLogger(CryptoDirectoryV6Provider.class);

    private static final String DATA_DIR_NAME = "d";
    private static final String ROOT_DIR_ID = StringUtils.EMPTY;

    private final Path dataRoot;
    private final Path home;
    private final CryptoVault cryptomator;

    private final RandomStringService random
        = new UUIDRandomStringService();

    private final Lock lock = new ReentrantLock();

    private final LRUCache<CacheReference<Path>, String> cache = LRUCache.build(
        PreferencesFactory.get().getInteger("cryptomator.cache.size"));

    public CryptoDirectoryV6Provider(final Path vault, final CryptoVault cryptomator) {
        this.home = vault;
        this.dataRoot = new Path(vault, DATA_DIR_NAME, vault.getType());
        this.cryptomator = cryptomator;
    }

    @Override
    public String toEncrypted(final Session<?> session, final String directoryId, final String filename, final EnumSet<Path.Type> type) throws BackgroundException {
        final String prefix = type.contains(Path.Type.directory) ? CryptoVault.DIR_PREFIX : "";
        final String ciphertextName = prefix + cryptomator.getFileNameCryptor().encryptFilename(CryptorCache.BASE32, filename, directoryId.getBytes(StandardCharsets.UTF_8));
        log.debug("Encrypted filename {} to {}", filename, ciphertextName);
        return cryptomator.getFilenameProvider().deflate(session, ciphertextName);
    }

    @Override
    public Path toEncrypted(final Session<?> session, final String directoryId, final Path directory) throws BackgroundException {
        if(!directory.isDirectory()) {
            throw new NotfoundException(directory.getAbsolute());
        }
        if(new SimplePathPredicate(directory).test(home) || directory.isChild(home)) {
            final PathAttributes attributes = new PathAttributes(directory.attributes());
            // The root of the vault is a different target directory and file ids always correspond to the metadata file
            attributes.withVersionId(null);
            attributes.withFileId(null);
            // Remember random directory id for use in vault
            final String id = this.toDirectoryId(session, directory, directoryId);
            log.debug("Use directory ID '{}' for folder {}", id, directory);
            attributes.setDirectoryId(id);
            attributes.setDecrypted(directory);
            final String directoryIdHash = cryptomator.getFileNameCryptor().hashDirectoryId(id);
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

    private String toDirectoryId(final Session<?> session, final Path directory, final String directoryId) throws BackgroundException {
        if(new SimplePathPredicate(home).test(directory)) {
            return ROOT_DIR_ID;
        }
        if(StringUtils.isBlank(directoryId)) {
            if(cache.contains(new SimplePathPredicate(directory))) {
                return cache.get(new SimplePathPredicate(directory));
            }
            try {
                log.debug("Acquire lock for {}", directory);
                lock.lock();
                final String id = this.load(session, directory);
                cache.put(new SimplePathPredicate(directory), id);
                return id;
            }
            finally {
                lock.unlock();
            }
        }
        if(!cache.contains(new SimplePathPredicate(directory))) {
            cache.put(new SimplePathPredicate(directory), directoryId);
        }
        else {
            final String existing = cache.get(new SimplePathPredicate(directory));
            if(!existing.equals(directoryId)) {
                log.warn("Do not override already cached id {} with {}", existing, directoryId);
            }
        }
        return cache.get(new SimplePathPredicate(directory));
    }

    protected String load(final Session<?> session, final Path directory) throws BackgroundException {
        final Path parent = this.toEncrypted(session, directory.getParent().attributes().getDirectoryId(), directory.getParent());
        final String cleartextName = directory.getName();
        final String ciphertextName = this.toEncrypted(session, parent.attributes().getDirectoryId(), cleartextName, EnumSet.of(Path.Type.directory));
        // Read directory id from file
        try {
            log.debug("Read directory ID for folder {} from {}", directory, ciphertextName);
            final Path metadataFile = new Path(parent, ciphertextName, EnumSet.of(Path.Type.file, Path.Type.encrypted));
            return new ContentReader(session).read(metadataFile);
        }
        catch(NotfoundException e) {
            log.warn("Missing directory ID for folder {}", directory);
            return random.random();
        }
    }

    public void delete(final Path directory) {
        cache.remove(new SimplePathPredicate(directory));
    }

    @Override
    public void destroy() {
        cache.clear();
    }
}
