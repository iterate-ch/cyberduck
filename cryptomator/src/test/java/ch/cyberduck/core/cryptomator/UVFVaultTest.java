package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEObjectJSON;
import com.nimbusds.jose.crypto.MultiDecrypter;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import net.iharder.Base64;

import static ch.cyberduck.core.cryptomator.UVFVault.computeRootDirId;
import static ch.cyberduck.core.cryptomator.UVFVault.computeRootDirIdHash;
import static org.junit.Assert.assertEquals;

public class UVFVaultTest {

    @Test
    public void testDecryptVaultUvfWithMemberKey() throws IOException, JOSEException, ParseException {
        final byte[] privateMemberKey = Base64.decode("iOsfVJ/a4ahOSF5wb9lPAx2BlvP7Kjr3yY3KXUYAoAQ=");
        final OctetSequenceKey jwk = new OctetSequenceKey.Builder(privateMemberKey)
                .keyID("org.cryptomator.hub.memberkey")
                .algorithm(JWEAlgorithm.A256KW)
                .build();

        final String vaultUvf = IOUtils.toString(this.getClass().getResourceAsStream("/vault.uvf"), StandardCharsets.UTF_8);
        final JWEObjectJSON jweObject = JWEObjectJSON.parse(vaultUvf);
        jweObject.decrypt(new MultiDecrypter(jwk));

        final Map<String, Object> payload = jweObject.getPayload().toJSONObject();

        assertEquals(9, payload.size());
        assertEquals("AES-256-GCM-32k", payload.get("fileFormat"));
        assertEquals("AES-SIV-512-B64URL", payload.get("nameFormat"));
        assertEquals(new HashMap<String, String>() {{
            put("59by", "E_v5PPBw5rb_3yzMILWn_LqnoSq1oOpXaOGLiWsgVIs");
        }}, payload.get("seeds"));
        assertEquals("59by", payload.get("initialSeed"));
        assertEquals("59by", payload.get("latestSeed"));
        assertEquals("HKDF-SHA512", payload.get("kdf"));
        assertEquals("xCW36cK17Fp0UWGMMNBjCB8Hg7Zcn__dfAB3dL_QfWM", payload.get("kdfSalt"));
        assertEquals(new HashMap<String, Object>() {{
            put("enabled", Boolean.valueOf("true"));
            put("maxWotDepth", Long.valueOf("3"));
        }}, payload.get("org.cryptomator.automaticAccessGrant"));
        assertEquals(new HashMap<String, String>() {{
            put("provider", "71b910e0-2ecc-46de-a871-8db28549677e");
            put("defaultPath", "handmade");
            put("nickname", "my first vault MinIO S3 static");
            put("region", null);
            put("username", "minioadmin");
            put("password", "minioadmin");
        }}, payload.get("cloud.katta.storage"));
    }

    @Test
    public void testComputeRootDirIdHash() {
        final String kdfSalt = "xCW36cK17Fp0UWGMMNBjCB8Hg7Zcn__dfAB3dL_QfWM";
        final String initialSeed = "E_v5PPBw5rb_3yzMILWn_LqnoSq1oOpXaOGLiWsgVIs";
        byte[] rootDirId = computeRootDirId(kdfSalt, initialSeed);
        String rootDirIdHash = computeRootDirIdHash(kdfSalt, initialSeed, rootDirId);
        // empty directory structure: d/QN/NRGNYIEE3XXH5CZFDDCYD7ZUIKI4QB
        assertEquals("QNNRGNYIEE3XXH5CZFDDCYD7ZUIKI4QB", rootDirIdHash);
    }

    @Test
    public void testDecryptVaultUvfWithRecoveryKey() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, ParseException, JOSEException {
        final String jwks = IOUtils.toString(this.getClass().getResourceAsStream("/jwks.json"), StandardCharsets.UTF_8);
        final JWKSet recoveredJwks = JWKSet.parse(jwks);
        assertEquals(1, recoveredJwks.getKeys().size());
        final ECKey publicRecoveryKey = (ECKey) recoveredJwks.getKeys().get(0);

        final String encodedPrivateRecoveryKey = "MIG/AgEAMBAGByqGSM49AgEGBSuBBAAiBIGnMIGkAgEBBDCC5kPnBCyV5Ssjrd9w4BtIKJEQWygl7XW1y6ecNdt+IryL2ds5ofyrdb7jmxQP+DWgBwYFK4EEACKhZANiAATzjvRHcZGkKy7xS3fSom/lUYEAv12EVjxnf61OI5Y1NcZFrkghOqM9dtBwmRrOiSw1WVamCmiFVdVDasdR0RMxSF1hrK9XQS6lRTk6J5+nMiCczVz4SMM8C6EsFpNMFiI=";
        final EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(java.util.Base64.getDecoder().decode(encodedPrivateRecoveryKey));
        final KeyFactory keyFactory = KeyFactory.getInstance("EC");
        final PrivateKey privateRecoveryKey = keyFactory.generatePrivate(privateKeySpec);
        final ECKey jwk = new ECKey.Builder(publicRecoveryKey).privateKey(privateRecoveryKey).build();

        final String vaultUvf = IOUtils.toString(this.getClass().getResourceAsStream("/vault.uvf"), StandardCharsets.UTF_8);
        final JWEObjectJSON jweObject = JWEObjectJSON.parse(vaultUvf);
        jweObject.decrypt(new MultiDecrypter(jwk));
        final Map<String, Object> payload = jweObject.getPayload().toJSONObject();

        assertEquals(9, payload.size());
        assertEquals("AES-256-GCM-32k", payload.get("fileFormat"));
        assertEquals("AES-SIV-512-B64URL", payload.get("nameFormat"));
        assertEquals(new HashMap<String, String>() {{
            put("59by", "E_v5PPBw5rb_3yzMILWn_LqnoSq1oOpXaOGLiWsgVIs");
        }}, payload.get("seeds"));
        assertEquals("59by", payload.get("initialSeed"));
        assertEquals("59by", payload.get("latestSeed"));
        assertEquals("HKDF-SHA512", payload.get("kdf"));
        assertEquals("xCW36cK17Fp0UWGMMNBjCB8Hg7Zcn__dfAB3dL_QfWM", payload.get("kdfSalt"));
        assertEquals(new HashMap<String, Object>() {{
            put("enabled", Boolean.valueOf("true"));
            put("maxWotDepth", Long.valueOf("3"));
        }}, payload.get("org.cryptomator.automaticAccessGrant"));
        assertEquals(new HashMap<String, String>() {{
            put("provider", "71b910e0-2ecc-46de-a871-8db28549677e");
            put("defaultPath", "handmade");
            put("nickname", "my first vault MinIO S3 static");
            put("region", null);
            put("username", "minioadmin");
            put("password", "minioadmin");
        }}, payload.get("cloud.katta.storage"));
    }
}