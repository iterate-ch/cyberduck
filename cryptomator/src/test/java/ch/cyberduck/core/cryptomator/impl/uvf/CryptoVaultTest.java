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
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cryptomator.impl.DefaultVaultMetadataCredentialsProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultMetadata;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.cryptomator.cryptolib.api.DirectoryMetadata;
import org.junit.Test;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

public class CryptoVaultTest {

    @Test
    public void testLoad() throws Exception {
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Read.class) {
                    return (T) new Read() {
                        @Override
                        public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                            final String vaultUVF = "eyJwMnMiOiJSSE5FQ2xxSWlqSSIsInAyYyI6MTAwMCwiamt1Ijoiandrcy5qc29uIiwia2lkIjoib3JnLmNyeXB0b21hdG9yLnV2Zi52YXVsdHBhc3N3b3JkIiwiY3R5IjoianNvbiIsImVuYyI6IkEyNTZHQ00iLCJhbGciOiJQQkVTMi1IUzUxMitBMjU2S1cifQ.Vy4KNvINWBF3wvmfZzrXdjv2Xer1tR6-SY8Dz0R-abCyR4HH9AMh9g.t8nwlY6eJ2KOmHhu.gInAlb1PhzwVqUUPA10v_86G7Ezg-W08cvgDlZGfGVpqRM1iH3TyE89-fiSvYU7NqV-9Eus5ulhA5YXW0mfr9SOecm4l_ZxXVBvsyYdZt8o_kpXho3w-DmSzPn8JG2hzkpVZPOqOeltc9ATosUGk_zw2M30POBqGF02RjFj5pbchQmpo539LIzyQQ7alYOlR3QdACxIM7ZsXrCsM-5BN_zIpBQXNEXrehbKtDYUchr-jB-ZMHHn9q13EhLFOeSXF1kje482c3H5ZbKDFJMNzyHqZLyPPrafrcStPolrd4Rcwl84ZidCnfnEcuLP8vr0sw9fqyxsJKuTDFhwEmGq47aZUHiQ.2PRDGEDRlC3sffJOhRYzyg";

                            if("vault.uvf".equals(file.getName())) {
                                return IOUtils.toInputStream(vaultUVF, Charset.defaultCharset());
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
        final ch.cyberduck.core.cryptomator.impl.uvf.CryptoVault vault = new ch.cyberduck.core.cryptomator.impl.uvf.CryptoVault(home);
        vault.load(session, new DisabledPasswordCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials("mypassphrase");
            }
        }, new DefaultVaultMetadataCredentialsProvider(null));
        assertTrue(vault.getFileContentCryptor().getClass().getName().contains("v3"));
        assertTrue(vault.getFileHeaderCryptor().getClass().getName().contains("v3"));
        assertEquals(Vault.State.open, vault.getState());
        assertNotSame(home, vault.encrypt(session, home));
        assertEquals(vault.encrypt(session, home), vault.encrypt(session, home));
        final Path directory = new Path(home, "dir", EnumSet.of(Path.Type.directory));
        assertNull(directory.attributes().getVault());
        assertEquals(home, vault.encrypt(session, directory).attributes().getVaultMetadata().root);
        assertEquals(VaultMetadata.Type.UVF, vault.encrypt(session, directory).attributes().getVaultMetadata().type);
        assertEquals(home, directory.attributes().getVaultMetadata().root);
        assertEquals(VaultMetadata.Type.UVF, directory.attributes().getVaultMetadata().type);
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
                        vault.getCryptor().directoryContentCryptor().decryptDirectoryMetadata(
                                vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory))).attributes().getDirectoryId()
                        ),
                        vault.getCryptor().directoryContentCryptor().decryptDirectoryMetadata(
                                vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory))).attributes().getDirectoryId()
                        )
                )
        );
        assertTrue(
                equalMetadata(
                        vault.getCryptor().directoryContentCryptor().decryptDirectoryMetadata(
                                vault.encrypt(session, vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory)))).attributes().getDirectoryId()
                        ),
                        vault.getCryptor().directoryContentCryptor().decryptDirectoryMetadata(
                                vault.encrypt(session, vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory)))).attributes().getDirectoryId()
                        )
                )
        );
        assertNull(vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory)), true).attributes().getDirectoryId());
        assertNull(vault.encrypt(session, vault.encrypt(session, new Path(home, "dir", EnumSet.of(Path.Type.directory))), true).attributes().getDirectoryId());
        vault.close();
        assertEquals(Vault.State.closed, vault.getState());
    }

    private boolean equalMetadata(final DirectoryMetadata a, final DirectoryMetadata b) throws Exception {
        if(a == b) {
            return true;
        }
        if(a == null || b == null) {
            return false;
        }
        if(!a.getClass().equals(b.getClass())) {
            return false;
        }

        for(Field field : FieldUtils.getAllFields(a.getClass())) {
            Object valueA = FieldUtils.readField(field, a, true);
            Object valueB = FieldUtils.readField(field, b, true);

            if(valueA == null && valueB == null) {
                continue;
            }
            if(valueA == null || valueB == null) {
                return false;
            }
            // byte[] Arrays mit Arrays.equals vergleichen
            if(valueA instanceof byte[] && valueB instanceof byte[]) {
                if(!Arrays.equals((byte[]) valueA, (byte[]) valueB)) {
                    return false;
                }
            }
            else if(!valueA.equals(valueB)) {
                return false;
            }
        }
        return true;
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
        final ch.cyberduck.core.cryptomator.impl.uvf.CryptoVault vault = new ch.cyberduck.core.cryptomator.impl.uvf.CryptoVault(home);
        final DefaultVaultMetadataCredentialsProvider provider = new DefaultVaultMetadataCredentialsProvider(new VaultCredentials("mypassphrase"));
        vault.create(session, null, provider);
    }
}
