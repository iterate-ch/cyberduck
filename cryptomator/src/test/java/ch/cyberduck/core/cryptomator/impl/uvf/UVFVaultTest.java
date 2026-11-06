package ch.cyberduck.core.cryptomator.impl.uvf;

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
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.cryptomator.cryptolib.api.VaultDirectory;
import org.cryptomator.cryptolib.api.UvfUnlockedVault;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class UVFVaultTest {

    @Test
    public void testLoad() throws Exception {
        // Vault metadata and directory metadata of a new vault with the same seeds
        final UvfUnlockedVault fixture = new DefaultUVFVaultMetadataProvider(new VaultCredentials("mypassphrase")).create();
        final String vaultUVF = new String(fixture.serializeConfig(), StandardCharsets.US_ASCII);
        final byte[] dirUVF = fixture.newDirectory().encryptedMetadata();
        fixture.destroy();
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Read.class) {
                    return (T) new Read() {
                        @Override
                        public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {

                            if("vault.uvf".equals(file.getName())) {
                                return IOUtils.toInputStream(vaultUVF, StandardCharsets.US_ASCII);
                            }
                            if("dir.uvf".equals(file.getName())) {
                                return new ByteArrayInputStream(dirUVF);
                            }
                            throw new NotfoundException(String.format("%s not found", file.getName()));
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
        final Path home = new Path("/", EnumSet.of((Path.Type.directory)));
        final UVFVault vault = new UVFVault(home);
        vault.load(session, new DefaultUVFVaultMetadataProvider(new VaultCredentials("mypassphrase")));
        assertTrue(vault.getUnlockedVault() instanceof UvfUnlockedVault);
        assertEquals(Vault.State.open, vault.getState());
        assertNotSame(home, vault.encrypt(session, home));
        assertEquals(vault.encrypt(session, home), vault.encrypt(session, home));
        final Path directory = new Path(home, "dir", EnumSet.of(Path.Type.directory));
        assertNull(directory.attributes().getVaultVersion());
        vault.getDirectoryProvider().createDirectoryId(directory);
        assertEquals(vault.encrypt(session, directory), vault.encrypt(session, directory));
        assertEquals(new Path(home, directory.getName(), EnumSet.of(Path.Type.directory, Path.Type.decrypted)), vault.decrypt(session, vault.encrypt(session, directory, true)));
        final Path placeholder = new Path(home, "placeholder", EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        assertTrue(vault.encrypt(session, placeholder, true).getType().contains(Path.Type.placeholder));
        assertTrue(vault.decrypt(session, vault.encrypt(session, placeholder, true)).getType().contains(Path.Type.placeholder));
        assertEquals(new Path(home, placeholder.getName(), EnumSet.of(Path.Type.directory, Path.Type.placeholder, Path.Type.decrypted)), vault.decrypt(session, vault.encrypt(session, placeholder, true)));
        assertNotEquals(
                vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory))),
                vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory)), true)
        );
        assertTrue(
                equalMetadata(
                        vault.getUnlockedVault().directory(
                                vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory))).attributes().getDirectoryId()
                        ),
                        vault.getUnlockedVault().directory(
                                vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory))).attributes().getDirectoryId()
                        )
                )
        );
        assertTrue(
                equalMetadata(
                        vault.getUnlockedVault().directory(
                                vault.encrypt(session, vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory)))).attributes().getDirectoryId()
                        ),
                        vault.getUnlockedVault().directory(
                                vault.encrypt(session, vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory)))).attributes().getDirectoryId()
                        )
                )
        );
        assertNull(vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory)), true).attributes().getDirectoryId());
        assertNull(vault.encrypt(session, vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory))), true).attributes().getDirectoryId());
        vault.close();
        assertEquals(Vault.State.closed, vault.getState());
    }

    private boolean equalMetadata(final VaultDirectory a, final VaultDirectory b) {
        return Arrays.equals(a.dirId(), b.dirId()) && a.dirPath().equals(b.dirPath());
    }

    @Test
    public void testCreate() throws Exception {
        final Path home = new Path("/vault", EnumSet.of(Path.Type.directory));
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Directory.class) {
                    return (T) new Directory() {

                        @Override
                        public Path mkdir(final Write writer, final Path folder, final TransferStatus status) {
                            assertTrue(folder.equals(home) || folder.isChild(home));
                            return folder;
                        }
                    };
                }
                return super._getFeature(type);
            }
        };
        final UVFVault vault = new UVFVault(home);
        final DefaultUVFVaultMetadataProvider provider = new DefaultUVFVaultMetadataProvider(new VaultCredentials("mypassphrase"));
        vault.create(session, null, provider);
    }
}
