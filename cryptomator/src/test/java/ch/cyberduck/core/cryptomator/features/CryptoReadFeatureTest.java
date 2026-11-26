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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.cryptomator.impl.uvf.DefaultUVFVaultMetadataProvider;
import ch.cyberduck.core.cryptomator.impl.uvf.UVFVault;
import ch.cyberduck.core.cryptomator.impl.v8.CryptomatorVault;
import ch.cyberduck.core.cryptomator.impl.v8.MasterkeyVaultMetadataProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;

import org.apache.commons.io.IOUtils;
import org.cryptomator.cryptolib.api.CipherCombo;
import org.cryptomator.cryptolib.api.UvfUnlockedVault;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

import static ch.cyberduck.core.cryptomator.impl.v8.CryptomatorVaultTest.createJWT;
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
        final CryptomatorVault vault = new CryptomatorVault(home);

        vault.load(session, new MasterkeyVaultMetadataProvider(new VaultCredentials("vault")));
        CryptoReadFeature read = new CryptoReadFeature(session, null, vault);

        {
            assertEquals(0, read.chunk(0));
            assertEquals(0, read.chunk(1));
            assertEquals(1, read.chunk(32768));
            assertEquals(1, read.chunk(32769));
        }

        {
            assertEquals(vault.getHeaderSize(), read.align(1));
            assertEquals(vault.getHeaderSize(), read.align(88));
            assertEquals(vault.getHeaderSize(), read.align(89));
            assertEquals(vault.getHeaderSize(), read.align(15342));
            assertEquals(vault.getHeaderSize(), read.align(32767));
            assertEquals(vault.getHeaderSize() + 48 + 32768, read.align(32768));
            assertEquals(vault.getHeaderSize() + 48 + 32768, read.align(32769));
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
                                return IOUtils.toInputStream(createJWT(masterKey, CryptomatorVault.VAULT_VERSION, CipherCombo.SIV_GCM, "vault"), Charset.defaultCharset());
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
        final CryptomatorVault vault = new CryptomatorVault(home);
        vault.load(session, new MasterkeyVaultMetadataProvider(new VaultCredentials("vault")));
        CryptoReadFeature read = new CryptoReadFeature(null, null, vault);
        {
            assertEquals(0, read.chunk(0));
            assertEquals(0, read.chunk(1));
            assertEquals(1, read.chunk(32768));
            assertEquals(1, read.chunk(32769));
        }
        {
            assertEquals(vault.getHeaderSize(), read.align(1));
            assertEquals(vault.getHeaderSize(), read.align(88));
            assertEquals(vault.getHeaderSize(), read.align(89));
            assertEquals(vault.getHeaderSize(), read.align(15342));
            assertEquals(vault.getHeaderSize(), read.align(32767));
            assertEquals(vault.getHeaderSize() + 28 + 32768, read.align(32768));
            assertEquals(vault.getHeaderSize() + 28 + 32768, read.align(32769));
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
        final UvfUnlockedVault fixture = new DefaultUVFVaultMetadataProvider(new VaultCredentials("mypassphrase")).create();
        final String vaultUVF = new String(fixture.serializeConfig(), StandardCharsets.US_ASCII);
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
        final UVFVault vault = new UVFVault(home);
        vault.load(session, new DefaultUVFVaultMetadataProvider(new VaultCredentials("mypassphrase")));
        CryptoReadFeature read = new CryptoReadFeature(null, null, vault);
        // AES-256-GCM-32k with 32740 bytes payload per chunk
        {
            assertEquals(0, read.chunk(0));
            assertEquals(0, read.chunk(1));
            assertEquals(0, read.chunk(32739));
            assertEquals(1, read.chunk(32740));
            assertEquals(1, read.chunk(32741));
        }
        {
            assertEquals(vault.getHeaderSize(), read.align(1));
            assertEquals(vault.getHeaderSize(), read.align(88));
            assertEquals(vault.getHeaderSize(), read.align(32739));
            assertEquals(vault.getHeaderSize() + 28 + 32740, read.align(32740));
            assertEquals(vault.getHeaderSize() + 28 + 32740, read.align(32741));
        }
        {
            assertEquals(24, read.position(24));
            assertEquals(0, read.position(0));
            assertEquals(0, read.position(32740));
            assertEquals(1, read.position(32741));
        }
        vault.close();
    }
}
