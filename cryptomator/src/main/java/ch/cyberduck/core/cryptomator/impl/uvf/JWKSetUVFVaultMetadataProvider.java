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

import java.util.Collections;
import java.util.Optional;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWEObjectJSON;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MultiDecrypter;
import com.nimbusds.jose.crypto.MultiEncrypter;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;

public class JWKSetUVFVaultMetadataProvider implements UVFVaultMetadataProvider {

    private static final String UVF_SPEC_VERSION_KEY_PARAM = "uvf.spec.version";

    private final JWEObjectJSON metadata;
    private final JWKSet jwk;

    /**
     * The cached decrypted payload of the JWE object. Preserves the original
     * plaintext across that state transition.
     */
    private Payload payload;

    /**
     * Constructs a new instance of {@code DefaultVaultMetadataUVFProvider}.
     *
     * @param metadata The JWE object representing the vault metadata in encrypted or unencrypted state.
     * @param jwk      The {@code JWKCallback} instance used for key management and related operations.
     */
    public JWKSetUVFVaultMetadataProvider(final JWEObjectJSON metadata, final JWKSet jwk) {
        this.metadata = metadata;
        this.jwk = jwk;
        if(metadata.getState() == JWEObject.State.UNENCRYPTED) {
            this.payload = metadata.getPayload();
        }
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
    public String encrypt() throws VaultException {
        switch(metadata.getState()) {
            case UNENCRYPTED:
                encrypt(metadata, jwk);
                break;
        }
        return metadata.serializeGeneral();
    }

    private static JWEObjectJSON encrypt(final JWEObjectJSON jwe, final JWKSet keys) throws VaultException {
        try {
            jwe.encrypt(new MultiEncrypter(keys));
            return jwe;
        }
        catch(JOSEException | IllegalStateException e) {
            throw new VaultException("Failure encrypting metadata", e);
        }
    }

    @Override
    public String decrypt() throws VaultException {
        if(payload == null) {
            // Encrypted state
            final Optional<JWK> key = jwk.getKeys().stream().findFirst();
            if(!key.isPresent()) {
                throw new VaultException("Missing key");
            }
            payload = decrypt(metadata, key.get());
        }
        return payload.toString();
    }

    private static Payload decrypt(final JWEObjectJSON jwe, final JWK key) throws VaultException {
        final Object uvfSpecVersion = jwe.getHeader().getCustomParams().get(UVF_SPEC_VERSION_KEY_PARAM);
        if(null == uvfSpecVersion) {
            throw new VaultException(String.format("Missing value for critical header %s",
                    UVF_SPEC_VERSION_KEY_PARAM));
        }
        if(1 != Integer.parseInt(uvfSpecVersion.toString())) {
            throw new VaultException(String.format("Unexpected value \"%s\" for critical header %s. Expected \"1\"",
                    UVF_SPEC_VERSION_KEY_PARAM, uvfSpecVersion));
        }
        try {
            jwe.decrypt(new MultiDecrypter(key, Collections.singleton(UVF_SPEC_VERSION_KEY_PARAM)));
            return jwe.getPayload();
        }
        catch(JOSEException | IllegalStateException e) {
            throw new VaultException("Failure decrypting metadata", e);
        }
    }

    @Override
    public void close() {
        payload = null;
    }
}
