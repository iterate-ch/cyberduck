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
import org.cryptomator.cryptolib.api.DirectoryMetadata;
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
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Read.class) {
                    return (T) new Read() {
                        @Override
                        public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                            final String vaultUVF = "eyJ1dmYuc3BlYy52ZXJzaW9uIjoxLCJwMnMiOiJwRWtoX1JQNVdwVSIsInAyYyI6MTAwMCwiY3JpdCI6WyJ1dmYuc3BlYy52ZXJzaW9uIl0sImprdSI6Imp3a3MuanNvbiIsImtpZCI6Im9yZy5jcnlwdG9tYXRvci51dmYudmF1bHRwYXNzd29yZCIsImN0eSI6Impzb24iLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiUEJFUzItSFM1MTIrQTI1NktXIn0.Juj2_l_pQjecxsE8gdMhv_jDdcKcyFN5uxjfdC0u2HpD9fj4NLaIfw.pXH6VLzWNURg7OPK.8G_woIWtt7s3jwDhJ1i1QabXPZiH1T5Yu2OgeraqtQPr852om4X51UtBOcqdIRmjadC7xSY2pxLYT8khOvm-r6ecRXOr_ZHBuFgQkFkQSj2QLZNYCoyGiSyeq_pRWTkaXPAM-4VvTFHk3xgs19cSUlPv2aImjQ2GMMnbD-8raSI3yeqkajJzdNAxkrsSRqa8-zY9j29ghigg114biR4OUMscZmq0zy7lqQtLcVw4M5-O-pBueDft2Gfnl6RlR7RRkJw-i-nMPhdrYXmu01oUkQUIZAb86rnymEO2C0uHEdvS55pPjWiUp9XnENzSYka3e1lR-hd7N1uRXjJ7HONELh6Efvw.LSyGUqlWdfBosogJJMnazg";
                            final byte[] dirUVF = new byte[]{117, 118, 102, 0, 97, 103, 114, 98, 87, -5, -42, 105, 82, 93, -27, -63, 50, 104, -102, -6, -8, -43, 50, 66, -88, 73, -68, 25, -52, -105, 111, -57, 27, -20, 26, 104, -67, 73, -19, -104, -16, 57, -83, -106, -128, 12, -101, -39, 20, 80, 28, 95, 1, -38, -23, -89, -40, 52, 127, 28, -109, -124, -31, 11, -121, -103, -75, 31, 46, 101, -66, 64, 79, -15, -3, 30, 4, -108, -87, -105, 71, -43, 34, -46, -43, -48, -84, 86, -63, 118, 51, -124, -119, -44, -64, -4, -36, -42, 27, -70, -73, -101, -117, 70, 110, 5, 104, 61, 0, -17, -10, 99, -127, 90, -127, -110, -118, -84, -128, -11, 26, -103, 39, -34, -41, -67, -126, 108};

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
        assertTrue(vault.getFileContentCryptor().getClass().getName().contains("v3"));
        assertTrue(vault.getFileHeaderCryptor().getClass().getName().contains("v3"));
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
        final UVFVault vault = new UVFVault(home);
        final DefaultUVFVaultMetadataProvider provider = new DefaultUVFVaultMetadataProvider(new VaultCredentials("mypassphrase"));
        vault.create(session, null, provider);
    }
}
