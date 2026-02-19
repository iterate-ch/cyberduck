package ch.cyberduck.core.cryptomator.features;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.cryptomator.impl.v8.CryptoVault;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultMetadataCallbackProvider;
import ch.cyberduck.core.vault.VaultCredentials;

import org.apache.commons.io.IOUtils;
import org.cryptomator.cryptolib.api.CryptorProvider;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.EnumSet;

import static ch.cyberduck.core.cryptomator.impl.v8.CryptoVaultTest.createJWT;
import static org.junit.Assert.assertEquals;

public class CryptoReadFeatureTest {

    @Test
    public void testCalculations_CTR() throws Exception {
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Read.class) {
                    return (T) new Read() {
                        @Override
                        public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                            final String masterKey = "{\n" +
                                    "  \"scryptSalt\": \"NrC7QGG/ouc=\",\n" +
                                    "  \"scryptCostParam\": 16384,\n" +
                                    "  \"scryptBlockSize\": 8,\n" +
                                    "  \"primaryMasterKey\": \"Q7pGo1l0jmZssoQh9rXFPKJE9NIXvPbL+HcnVSR9CHdkeR8AwgFtcw==\",\n" +
                                    "  \"hmacMasterKey\": \"xzBqT4/7uEcQbhHFLC0YmMy4ykVKbuvJEA46p1Xm25mJNuTc20nCbw==\",\n" +
                                    "  \"versionMac\": \"hlNr3dz/CmuVajhaiGyCem9lcVIUjDfSMLhjppcXOrM=\",\n" +
                                    "  \"version\": 7\n" +
                                    "}";
                            if("masterkey.cryptomator".equals(file.getName())) {
                                return IOUtils.toInputStream(masterKey, Charset.defaultCharset());
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
        final CryptoVault vault = new CryptoVault(home);

        assertEquals(home, vault.load(session, new DefaultVaultMetadataCallbackProvider(new DisabledPasswordCallback() {
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials("vault");
            }
        })).getHome());
        CryptoReadFeature read = new CryptoReadFeature(session, null, vault);

        {
            assertEquals(0, read.chunk(0));
            assertEquals(0, read.chunk(1));
            assertEquals(1, read.chunk(32768));
            assertEquals(1, read.chunk(32769));
        }

        {
            assertEquals(vault.getFileHeaderCryptor().headerSize(), read.align(1));
            assertEquals(vault.getFileHeaderCryptor().headerSize(), read.align(88));
            assertEquals(vault.getFileHeaderCryptor().headerSize(), read.align(89));
            assertEquals(vault.getFileHeaderCryptor().headerSize(), read.align(15342));
            assertEquals(vault.getFileHeaderCryptor().headerSize(), read.align(32767));
            assertEquals(vault.getFileHeaderCryptor().headerSize() + 48 + 32768, read.align(32768));
            assertEquals(vault.getFileHeaderCryptor().headerSize() + 48 + 32768, read.align(32769));
        }

        {
            assertEquals(24, read.position(24));
            assertEquals(0, read.position(0));
            assertEquals(0, read.position(32768));
            assertEquals(1, read.position(32769));
        }
        vault.close();
    }

    @Test
    public void testCalculations_GCM() throws Exception {
        final NullSession session = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T _getFeature(final Class<T> type) {
                if(type == Read.class) {
                    return (T) new Read() {
                        @Override
                        public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                            final String masterKey = "{\n" +
                                    "  \"scryptSalt\": \"NrC7QGG/ouc=\",\n" +
                                    "  \"scryptCostParam\": 16384,\n" +
                                    "  \"scryptBlockSize\": 8,\n" +
                                    "  \"primaryMasterKey\": \"Q7pGo1l0jmZssoQh9rXFPKJE9NIXvPbL+HcnVSR9CHdkeR8AwgFtcw==\",\n" +
                                    "  \"hmacMasterKey\": \"xzBqT4/7uEcQbhHFLC0YmMy4ykVKbuvJEA46p1Xm25mJNuTc20nCbw==\",\n" +
                                    "  \"versionMac\": \"hlNr3dz/CmuVajhaiGyCem9lcVIUjDfSMLhjppcXOrM=\",\n" +
                                    "  \"version\": 8\n" +
                                    "}";
                            if("masterkey.cryptomator".equals(file.getName())) {
                                return IOUtils.toInputStream(masterKey, Charset.defaultCharset());
                            }
                            if("vault.cryptomator".equals(file.getName())) {
                                return IOUtils.toInputStream(createJWT(masterKey, CryptoVault.VAULT_VERSION, CryptorProvider.Scheme.SIV_GCM, "vault"), Charset.defaultCharset());
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
        final CryptoVault vault = new CryptoVault(home);
        assertEquals(home, vault.load(session, new DefaultVaultMetadataCallbackProvider(new DisabledPasswordCallback() {
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials("vault");
            }
        })).getHome());
        CryptoReadFeature read = new CryptoReadFeature(null, null, vault);
        {
            assertEquals(0, read.chunk(0));
            assertEquals(0, read.chunk(1));
            assertEquals(1, read.chunk(32768));
            assertEquals(1, read.chunk(32769));
        }
        {
            assertEquals(vault.getFileHeaderCryptor().headerSize(), read.align(1));
            assertEquals(vault.getFileHeaderCryptor().headerSize(), read.align(88));
            assertEquals(vault.getFileHeaderCryptor().headerSize(), read.align(89));
            assertEquals(vault.getFileHeaderCryptor().headerSize(), read.align(15342));
            assertEquals(vault.getFileHeaderCryptor().headerSize(), read.align(32767));
            assertEquals(vault.getFileHeaderCryptor().headerSize() + 28 + 32768, read.align(32768));
            assertEquals(vault.getFileHeaderCryptor().headerSize() + 28 + 32768, read.align(32769));
        }
        {
            assertEquals(24, read.position(24));
            assertEquals(0, read.position(0));
            assertEquals(0, read.position(32768));
            assertEquals(1, read.position(32769));
        }
        vault.close();
    }

    @Test
    public void testCalculations_UVF() throws Exception {
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
        vault.load(session, new DefaultVaultMetadataCallbackProvider(new DisabledPasswordCallback() {
            public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) {
                return new VaultCredentials("mypassphrase");
            }
        }));
        CryptoReadFeature read = new CryptoReadFeature(null, null, vault);
        {
            assertEquals(0, read.chunk(0));
            assertEquals(0, read.chunk(1));
            assertEquals(1, read.chunk(32768));
            assertEquals(1, read.chunk(32769));
        }
        {
            assertEquals(vault.getFileHeaderCryptor().headerSize(), read.align(1));
            assertEquals(vault.getFileHeaderCryptor().headerSize(), read.align(88));
            assertEquals(vault.getFileHeaderCryptor().headerSize(), read.align(89));
            assertEquals(vault.getFileHeaderCryptor().headerSize(), read.align(15342));
            assertEquals(vault.getFileHeaderCryptor().headerSize(), read.align(32767));
            assertEquals(vault.getFileHeaderCryptor().headerSize() + 28 + 32768, read.align(32768));
            assertEquals(vault.getFileHeaderCryptor().headerSize() + 28 + 32768, read.align(32769));
        }
        {
            assertEquals(24, read.position(24));
            assertEquals(0, read.position(0));
            assertEquals(0, read.position(32768));
            assertEquals(1, read.position(32769));
        }
        vault.close();
    }
}
