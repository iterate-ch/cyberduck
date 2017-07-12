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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;

import org.junit.Test;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CryptoBulkFeatureTest {

    @Test
    public void testPreDirectoryId() throws Exception {
        final Path vault = new Path("/vault", EnumSet.of(Path.Type.directory));
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Directory.class) {
                    return (T) new Directory() {
                        @Override
                        public Path mkdir(final Path folder, final String region, final TransferStatus status) throws BackgroundException {
                            return folder;
                        }

                        @Override
                        public boolean isSupported(final Path workdir) {
                            return true;
                        }

                        @Override
                        public Directory withWriter(final Write writer) {
                            return this;
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        final CryptoVault cryptomator = new CryptoVault(vault, new DisabledPasswordStore());
        cryptomator.create(session, null, new VaultCredentials("test"));
        final CryptoBulkFeature<Map<Path, TransferStatus>> bulk = new CryptoBulkFeature<Map<Path, TransferStatus>>(session, new Bulk<Map<Path, TransferStatus>>() {
            @Override
            public Map<Path, TransferStatus> pre(final Transfer.Type type, final Map<Path, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
                return files;
            }

            @Override
            public void post(final Transfer.Type type, final Map<Path, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
                //
            }

            @Override
            public Bulk<Map<Path, TransferStatus>> withDelete(final Delete delete) {
                return this;
            }
        }, new Delete() {
            @Override
            public void delete(final List<Path> files, final LoginCallback prompt, final Callback callback) throws BackgroundException {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isSupported(final Path file) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isRecursive() {
                throw new UnsupportedOperationException();
            }
        }, cryptomator);
        final HashMap<Path, TransferStatus> files = new HashMap<>();
        final Path directory = new Path("/vault/directory", EnumSet.of(Path.Type.directory));
        files.put(directory, new TransferStatus().exists(false));
        files.put(new Path(directory, "file1", EnumSet.of(Path.Type.file)), new TransferStatus().exists(false));
        files.put(new Path(directory, "file2", EnumSet.of(Path.Type.file)), new TransferStatus().exists(false));
        final Map<Path, TransferStatus> pre = bulk.pre(Transfer.Type.upload, files, new DisabledConnectionCallback());
        assertEquals(3, pre.size());
        final Path encryptedDirectory = pre.keySet().stream().filter(new Predicate<Path>() {
            @Override
            public boolean test(final Path path) {
                return path.isDirectory();
            }
        }).findFirst().get();
        final String directoryId = encryptedDirectory.attributes().getDirectoryId();
        assertNotNull(directoryId);
        for(Path file : pre.keySet().stream().filter(new Predicate<Path>() {
            @Override
            public boolean test(final Path path) {
                return path.isFile();
            }
        }).collect(Collectors.toSet())) {
            assertEquals(encryptedDirectory, file.getParent());
            assertEquals(directoryId, file.getParent().attributes().getDirectoryId());
        }
    }
}