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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.cryptomator.random.FastSecureRandomProvider;
import ch.cyberduck.core.vault.VaultCredentials;
import ch.cyberduck.core.vault.VaultException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.PasswordBasedDecrypter;
import com.nimbusds.jose.crypto.PasswordBasedEncrypter;

public class DefaultUVFVaultMetadataProvider implements UVFVaultMetadataProvider {

    private static final String UVF_SPEC_VERSION_KEY_PARAM = "uvf.spec.version";
    private static final String UVF_FILEFORMAT = "AES-256-GCM-32k";
    private static final String UVF_NAME_FORMAT = "AES-SIV-512-B64URL";

    public static final int PBKDF2_SALT_LENGTH = PasswordBasedEncrypter.MIN_SALT_LENGTH;
    public static final int PBKDF2_ITERATION_COUNT = PasswordBasedEncrypter.MIN_RECOMMENDED_ITERATION_COUNT;

    private final JWEObject metadata;
    private final VaultCredentials passphrase;

    public DefaultUVFVaultMetadataProvider(final VaultCredentials passphrase) {
        final JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.PBES2_HS512_A256KW, EncryptionMethod.A256GCM)
                .jwkURL(URI.create("jwks.json"))
                .contentType("json")
                .criticalParams(Collections.singleton(UVF_SPEC_VERSION_KEY_PARAM))
                .customParam(UVF_SPEC_VERSION_KEY_PARAM, 1)
                .keyID("org.cryptomator.uvf.vaultpassword")
                .build();
        final String kid = Base64.getUrlEncoder().encodeToString(new AlphanumericRandomStringService(4).random().getBytes(StandardCharsets.UTF_8));
        final byte[] rawSeed = new byte[32];
        FastSecureRandomProvider.get().provide().nextBytes(rawSeed);
        final byte[] kdfSalt = new byte[32];
        FastSecureRandomProvider.get().provide().nextBytes(kdfSalt);
        final Payload payload = new Payload(new HashMap<String, Object>() {{
            put("fileFormat", UVF_FILEFORMAT);
            put("nameFormat", UVF_NAME_FORMAT);
            put("seeds", new HashMap<String, String>() {{
                put(kid, Base64.getUrlEncoder().encodeToString(rawSeed));
            }});
            put("initialSeed", kid);
            put("latestSeed", kid);
            put("kdf", "HKDF-SHA512");
            put("kdfSalt", Base64.getUrlEncoder().encodeToString(kdfSalt));
        }});
        this.metadata = new JWEObject(header, payload);
        this.passphrase = passphrase;
    }

    public DefaultUVFVaultMetadataProvider(final JWEObject metadata, final VaultCredentials passphrase) {
        this.metadata = metadata;
        this.passphrase = passphrase;
    }

    public String computeRootDirIdHash() {
        return this.computeRootDirIdHash(metadata.getPayload().toString());
    }

    public byte[] computeRootDirUvf() {
        return this.computeRootDirUvf(metadata.getPayload().toString());
    }

    @Override
    public byte[] encrypt() throws VaultException {
        try {
            switch(metadata.getState()) {
                case DECRYPTED:
                case UNENCRYPTED:
                    metadata.encrypt(new PasswordBasedEncrypter(passphrase.getPassword(), PBKDF2_SALT_LENGTH, PBKDF2_ITERATION_COUNT));
                    break;
            }
        }
        catch(JOSEException e) {
            throw new VaultException("Failure encrypting metadata", e);
        }
        return metadata.serialize().getBytes(StandardCharsets.US_ASCII);
    }

    @Override
    public byte[] decrypt() throws VaultException {
        try {
            switch(metadata.getState()) {
                case UNENCRYPTED:
                    return metadata.getPayload().toString().getBytes(StandardCharsets.US_ASCII);
                case ENCRYPTED:
                    metadata.decrypt(new PasswordBasedDecrypter(passphrase.getPassword().getBytes(StandardCharsets.UTF_8), Collections.singleton(UVF_SPEC_VERSION_KEY_PARAM)));
                    break;
            }
        }
        catch(JOSEException e) {
            throw new VaultException("Failure decrypting metadata", e);
        }
        return metadata.getPayload().toString().getBytes(StandardCharsets.US_ASCII);
    }
}
