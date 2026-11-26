package ch.cyberduck.core.cryptomator;

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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.VoidStatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultVersion;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.Assert.assertEquals;

/**
 * Callers save the passphrase to the keychain after the vault is created or loaded
 */
@RunWith(Parameterized.class)
public class DefaultVaultProviderTest {

    private static final String PASSPHRASE = "test";
    private static final Path HOME = new Path("/vault", EnumSet.of(Path.Type.directory));

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{{VaultVersion.Type.V8}, {VaultVersion.Type.UVF}});
    }

    @Parameterized.Parameter
    public VaultVersion.Type type;

    /**
     * Files by absolute path
     */
    private final Map<String, byte[]> storage = new ConcurrentHashMap<>();

    private final NullSession session = new NullSession(new Host(new TestProtocol())) {
        @Override
        @SuppressWarnings("unchecked")
        public <T> T _getFeature(final Class<T> type) {
            if(type == Directory.class) {
                return (T) new Directory() {
                    @Override
                    public Path mkdir(final Write writer, final Path folder, final TransferStatus status) {
                        return folder;
                    }
                };
            }
            if(type == Write.class) {
                return (T) new Write<Void>() {
                    @Override
                    public StatusOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) {
                        return new VoidStatusOutputStream(new ByteArrayOutputStream() {
                            @Override
                            public void close() {
                                storage.put(file.getAbsolute(), this.toByteArray());
                            }
                        });
                    }
                };
            }
            if(type == Read.class) {
                return (T) new Read() {
                    @Override
                    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                        if(!storage.containsKey(file.getAbsolute())) {
                            throw new NotfoundException(file.getAbsolute());
                        }
                        return new ByteArrayInputStream(storage.get(file.getAbsolute()));
                    }

                    @Override
                    public boolean offset(final Path file) {
                        return false;
                    }
                };
            }
            return super._getFeature(type);
        }
    };

    @Test
    public void testCreateKeepsPassphrase() throws Exception {
        final VaultCredentials credentials = new VaultCredentials(PASSPHRASE).setSaved(true);
        final AbstractVault vault = new DefaultVaultProvider(session).create(session, null, HOME, new VaultVersion(type), credentials);
        assertEquals(Vault.State.open, vault.getState());
        assertEquals(PASSPHRASE, credentials.getPassword());
        vault.close();
    }

    @Test
    public void testLoadKeepsPassphrase() throws Exception {
        final DefaultVaultProvider provider = new DefaultVaultProvider(session);
        provider.create(session, null, HOME, new VaultVersion(type), new VaultCredentials(PASSPHRASE)).close();
        final VaultCredentials credentials = new VaultCredentials(PASSPHRASE).setSaved(true);
        final AbstractVault vault = provider.load(session, HOME, new VaultVersion(type), credentials);
        assertEquals(Vault.State.open, vault.getState());
        assertEquals(PASSPHRASE, credentials.getPassword());
        vault.close();
    }
}
