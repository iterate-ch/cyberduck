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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;

import org.junit.Test;

import java.util.EnumSet;
import java.util.HashMap;
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
                        public Path mkdir(final Path folder, final TransferStatus status) {
                            return folder;
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
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"));
        final CryptoBulkFeature<Map<TransferItem, TransferStatus>> bulk = new CryptoBulkFeature<Map<TransferItem, TransferStatus>>(session, new Bulk<Map<TransferItem, TransferStatus>>() {
            @Override
            public Map<TransferItem, TransferStatus> pre(final Transfer.Type type, final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback) {
                return files;
            }

            @Override
            public void post(final Transfer.Type type, final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback) {
                //
            }

            @Override
            public Bulk<Map<TransferItem, TransferStatus>> withDelete(final Delete delete) {
                return this;
            }

        }, new Delete() {
            @Override
            public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isRecursive() {
                throw new UnsupportedOperationException();
            }
        }, cryptomator);
        final HashMap<TransferItem, TransferStatus> files = new HashMap<>();
        final Path directory = new Path("/vault/directory", EnumSet.of(Path.Type.directory));
        files.put(new TransferItem(directory, new Local("/tmp/vault/directory")), new TransferStatus().exists(false));
        files.put(new TransferItem(new Path(directory, "file1", EnumSet.of(Path.Type.file)), new Local("/tmp/vault/directory/file1")), new TransferStatus().exists(false));
        files.put(new TransferItem(new Path(directory, "file2", EnumSet.of(Path.Type.file)), new Local("/tmp/vault/directory/file2")), new TransferStatus().exists(false));
        final Map<TransferItem, TransferStatus> pre = bulk.pre(Transfer.Type.upload, files, new DisabledConnectionCallback());
        assertEquals(3, pre.size());
        final Path encryptedDirectory = pre.keySet().stream().filter(new Predicate<TransferItem>() {
            @Override
            public boolean test(final TransferItem item) {
                return item.remote.isDirectory();
            }
        }).findFirst().get().remote;
        final String directoryId = encryptedDirectory.attributes().getDirectoryId();
        assertNotNull(directoryId);
        for(TransferItem file : pre.keySet().stream().filter(new Predicate<TransferItem>() {
            @Override
            public boolean test(final TransferItem item) {
                return item.remote.isFile();
            }
        }).collect(Collectors.toSet())) {
            assertEquals(encryptedDirectory, file.remote.getParent());
            assertEquals(directoryId, file.remote.getParent().attributes().getDirectoryId());
        }
    }

    @Test
    public void testPost() throws Exception {
        final Path vault = new Path("/vault", EnumSet.of(Path.Type.directory));
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Directory.class) {
                    return (T) new Directory() {
                        @Override
                        public Path mkdir(final Path folder, final TransferStatus status) {
                            return folder;
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
        final CryptoVault cryptomator = new CryptoVault(vault);
        cryptomator.create(session, null, new VaultCredentials("test"));
        final CryptoBulkFeature<Map<TransferItem, TransferStatus>> bulk = new CryptoBulkFeature<Map<TransferItem, TransferStatus>>(session, new Bulk<Map<TransferItem, TransferStatus>>() {
            @Override
            public Map<TransferItem, TransferStatus> pre(final Transfer.Type type, final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback) {
                return files;
            }

            @Override
            public void post(final Transfer.Type type, final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback) {
                //
            }

            @Override
            public Bulk<Map<TransferItem, TransferStatus>> withDelete(final Delete delete) {
                return this;
            }

        }, new Delete() {
            @Override
            public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean isRecursive() {
                throw new UnsupportedOperationException();
            }
        }, cryptomator);
        final HashMap<TransferItem, TransferStatus> files = new HashMap<>();
        final Path directory = new Path("/vault/directory", EnumSet.of(Path.Type.directory));
        files.put(new TransferItem(directory, new Local("/tmp/vault/directory")), new TransferStatus().exists(false));
        files.put(new TransferItem(new Path(directory, "file1", EnumSet.of(Path.Type.file)), new Local("/tmp/vault/directory/file1")), new TransferStatus().exists(false));
        files.put(new TransferItem(new Path(directory, "file2", EnumSet.of(Path.Type.file)), new Local("/tmp/vault/directory/file2")), new TransferStatus().exists(false));
        bulk.post(Transfer.Type.upload, files, new DisabledConnectionCallback());
    }
}
