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
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEObjectJSON;
import com.nimbusds.jose.crypto.MultiDecrypter;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import net.iharder.Base64;

import static org.junit.Assert.assertEquals;

public class UVFVaultTest {

    @Test
    public void testDecryptVaultUvf() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, JOSEException, ParseException {
        final String vaultUvf = IOUtils.toString(this.getClass().getResourceAsStream("/vault.uvf"), StandardCharsets.UTF_8);

        final byte[] memberKey = Base64.decode("iOsfVJ/a4ahOSF5wb9lPAx2BlvP7Kjr3yY3KXUYAoAQ=");
        final OctetSequenceKey jwk = new OctetSequenceKey.Builder(memberKey)
                .keyID("org.cryptomator.hub.memberkey")
                .algorithm(JWEAlgorithm.A256KW)
                .build();

        JWEObjectJSON jweObject = JWEObjectJSON.parse(vaultUvf);
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