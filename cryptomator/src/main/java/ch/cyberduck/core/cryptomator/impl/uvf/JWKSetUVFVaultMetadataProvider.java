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

import ch.cyberduck.core.vault.VaultException;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObjectJSON;
import com.nimbusds.jose.crypto.MultiDecrypter;
import com.nimbusds.jose.crypto.MultiEncrypter;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;

public class JWKSetUVFVaultMetadataProvider implements UVFVaultMetadataProvider {

    private static final String UVF_SPEC_VERSION_KEY_PARAM = "uvf.spec.version";

    private final JWEObjectJSON metadata;
    private final JWKSet jwk;

    /**
     * Constructs a new instance of {@code DefaultVaultMetadataUVFProvider}.
     *
     * @param jwk The {@code JWKCallback} instance used for key management and related operations.
     */
    public JWKSetUVFVaultMetadataProvider(final JWEObjectJSON metadata, final JWKSet jwk) {
        this.metadata = metadata;
        this.jwk = jwk;
    }

    @Override
    public String computeRootDirIdHash() {
        return this.computeRootDirIdHash(metadata.getPayload().toString());
    }

    @Override
    public byte[] computeRootDirUvf() {
        return this.computeRootDirUvf(metadata.getPayload().toString());
    }

    @Override
    public byte[] encrypt() throws VaultException {
        try {
            switch(metadata.getState()) {
                // The JWE object must be in an encrypted or decrypted state
                case UNENCRYPTED:
                case DECRYPTED:
                    metadata.encrypt(new MultiEncrypter(jwk));
                    break;
            }
        }
        catch(JOSEException e) {
            throw new VaultException("Failure encrypting metadata", e);
        }
        return metadata.serializeGeneral().getBytes(StandardCharsets.US_ASCII);
    }

    @Override
    public byte[] decrypt() throws VaultException {
        final Optional<JWK> key = jwk.getKeys().stream().findFirst();
        if(!key.isPresent()) {
            throw new VaultException("Missing key");
        }
        try {
            switch(metadata.getState()) {
                case UNENCRYPTED:
                    return metadata.getPayload().toString().getBytes(StandardCharsets.US_ASCII);
                case ENCRYPTED:
                    metadata.decrypt(new MultiDecrypter(key.get(), Collections.singleton(UVF_SPEC_VERSION_KEY_PARAM)));
                    break;
            }
        }
        catch(JOSEException e) {
            throw new VaultException("Failure decrypting metadata", e);
        }
        return metadata.serializeGeneral().getBytes(StandardCharsets.US_ASCII);
    }
}
