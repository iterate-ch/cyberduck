package ch.cyberduck.core.cryptomator.impl.v8;

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
import ch.cyberduck.core.UUIDRandomStringService;
import ch.cyberduck.core.cryptomator.impl.CryptoFilenameV7Provider;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.VoidStatusOutputStream;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultException;

import org.cryptomator.cryptolib.api.CipherCombo;
import org.cryptomator.cryptolib.api.Cvf8LockedVault;
import org.cryptomator.cryptolib.api.Cvf8UnlockedVault;
import org.cryptomator.cryptolib.api.CvfMasterkey;
import org.cryptomator.cryptolib.api.LockedVault;
import org.cryptomator.cryptolib.common.DestroyableSecretKey;
import org.cryptomator.cryptolib.common.MasterkeyFile;
import org.cryptomator.cryptolib.common.MasterkeyFileAccess;
import org.junit.Test;

import javax.crypto.Mac;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import static org.junit.Assert.*;

/**
 * The master key file of vault format 8 is referenced by the key ID of the vault configuration
 */
public class CryptomatorVaultKeyIdTest {

    private static final String PASSPHRASE = "test";
    private static final Path HOME = new Path("/vault", EnumSet.of(Path.Type.directory));

    private static final byte[] PEPPER = PreferencesFactory.get().getProperty("cryptomator.vault.pepper").getBytes(StandardCharsets.UTF_8);

    /**
     * Files of vault by name
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
                                storage.put(file.getName(), this.toByteArray());
                            }
                        });
                    }
                };
            }
            if(type == Read.class) {
                return (T) new Read() {
                    @Override
                    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                        if(!storage.containsKey(file.getName())) {
                            throw new NotfoundException(file.getName());
                        }
                        return new ByteArrayInputStream(storage.get(file.getName()));
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

    /**
     * @param masterkeyFilename Name of master key file configured in preferences
     */
    private static CryptomatorVault vault(final String masterkeyFilename) {
        return new CryptomatorVault(HOME,
                new Path(HOME, masterkeyFilename, EnumSet.of(Path.Type.file, Path.Type.vaultmetadata)),
                new Path(HOME, "vault.cryptomator", EnumSet.of(Path.Type.file, Path.Type.vaultmetadata)),
                PEPPER);
    }

    @Test
    public void testCreateWithCustomMasterkeyFilename() throws Exception {
        final CryptomatorVault vault = vault("masterkey.custom");
        vault.create(session, null, new MasterkeyVaultMetadataProvider(new VaultCredentials(PASSPHRASE)));
        vault.close();
        // Master key file written where the key ID of the configuration points to for other clients
        final URI keyId = ((Cvf8LockedVault) LockedVault.parse(storage.get("vault.cryptomator"))).keyId();
        assertEquals(URI.create("masterkeyfile:masterkey.cryptomator"), keyId);
        assertTrue(storage.containsKey("masterkey.cryptomator"));
        assertFalse(storage.containsKey("masterkey.custom"));
        // Can be opened again with custom name in preferences
        final CryptomatorVault reopened = vault("masterkey.custom");
        reopened.load(session, new MasterkeyVaultMetadataProvider(new VaultCredentials(PASSPHRASE)));
        assertEquals(Vault.State.open, reopened.getState());
        assertEquals("masterkey.cryptomator", reopened.getMasterkeyPath().getName());
        reopened.close();
    }

    @Test
    public void testLoadMasterkeyReferencedByKeyId() throws Exception {
        try(CvfMasterkey masterkey = CvfMasterkey.generate(FastSecureRandomProvider.get().provide())) {
            storage.put("masterkey.other", masterkeyFile(masterkey, 999));
            storage.put("vault.cryptomator", config(masterkey, "masterkeyfile:masterkey.other"));
        }
        final CryptomatorVault vault = vault("masterkey.cryptomator");
        vault.load(session, new MasterkeyVaultMetadataProvider(new VaultCredentials(PASSPHRASE)));
        assertEquals(Vault.State.open, vault.getState());
        assertEquals(new Path(HOME, "masterkey.other", EnumSet.of(Path.Type.file, Path.Type.vaultmetadata)), vault.getMasterkeyPath());
        vault.close();
    }

    @Test
    public void testLoadFormat7WithoutConfig() throws Exception {
        try(CvfMasterkey masterkey = CvfMasterkey.generate(FastSecureRandomProvider.get().provide())) {
            // Name configured in preferences when there is no configuration with key ID
            storage.put("masterkey.custom", masterkeyFile(masterkey, 7));
        }
        final CryptomatorVault vault = vault("masterkey.custom");
        vault.load(session, new MasterkeyVaultMetadataProvider(new VaultCredentials(PASSPHRASE)));
        assertEquals(Vault.State.open, vault.getState());
        assertEquals(CipherCombo.SIV_CTRMAC, ((Cvf8UnlockedVault) vault.getUnlockedVault()).cipherCombo());
        assertEquals("masterkey.custom", vault.getMasterkeyPath().getName());
        vault.close();
    }

    @Test
    public void testLoadKeyIdOutsideVault() throws Exception {
        this.assertInvalidKeyId("masterkeyfile:../masterkey.cryptomator");
        this.assertInvalidKeyId("masterkeyfile:d/masterkey.cryptomator");
        this.assertInvalidKeyId("masterkeyfile:");
        this.assertInvalidKeyId("masterkeyfile:..");
    }

    @Test
    public void testLoadUnsupportedKeyId() throws Exception {
        this.assertInvalidKeyId("hub+https://hub.example.com/api/vaults/1");
    }

    private void assertInvalidKeyId(final String keyId) throws Exception {
        storage.clear();
        try(CvfMasterkey masterkey = CvfMasterkey.generate(FastSecureRandomProvider.get().provide())) {
            storage.put("masterkey.cryptomator", masterkeyFile(masterkey, 999));
            storage.put("vault.cryptomator", config(masterkey, keyId));
        }
        try {
            vault("masterkey.cryptomator").load(session, new MasterkeyVaultMetadataProvider(new VaultCredentials(PASSPHRASE)));
            fail(keyId);
        }
        catch(VaultException e) {
            // Expected
        }
    }

    /**
     * @return Master key file with given version as written by Cryptomator
     */
    private static byte[] masterkeyFile(final CvfMasterkey masterkey, final int version) throws Exception {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        new MasterkeyFileAccess(PEPPER, FastSecureRandomProvider.get().provide()).persist(masterkey, out, PASSPHRASE);
        final MasterkeyFile file = MasterkeyFile.read(new InputStreamReader(new ByteArrayInputStream(out.toByteArray()), StandardCharsets.UTF_8));
        file.version = version;
        try(DestroyableSecretKey macKey = masterkey.getMacKey()) {
            final Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(macKey);
            file.versionMac = mac.doFinal(ByteBuffer.allocate(Integer.BYTES).putInt(version).array());
        }
        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        try(Writer writer = new OutputStreamWriter(result, StandardCharsets.UTF_8)) {
            file.write(writer);
        }
        return result.toByteArray();
    }

    /**
     * @return Vault configuration signed with the master key
     */
    private static byte[] config(final CvfMasterkey masterkey, final String keyId) throws Exception {
        final SignedJWT token = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).keyID(keyId).build(),
                new JWTClaimsSet.Builder()
                        .jwtID(new UUIDRandomStringService().random())
                        .claim("format", CryptomatorVault.VAULT_VERSION)
                        .claim("cipherCombo", CipherCombo.SIV_GCM.name())
                        .claim("shorteningThreshold", CryptoFilenameV7Provider.DEFAULT_NAME_SHORTENING_THRESHOLD)
                        .build());
        final byte[] rawKey = masterkey.getEncoded().clone();
        try {
            token.sign(new MACSigner(rawKey));
            return token.serialize().getBytes(StandardCharsets.US_ASCII);
        }
        finally {
            Arrays.fill(rawKey, (byte) 0x00);
        }
    }
}
