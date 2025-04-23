package ch.cyberduck.core.ctera.directio;

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

import org.jose4j.keys.AesKey;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DecryptKey {
    private static final String ENCRYPTION_ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final String ENCRYPTION_KEY_ALGORITHM = "AES";

    private String encryptedKey;
    private String decryptedKey;

    public DecryptKey(final String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public void decrypt(final String wrappingKey) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, JsonProcessingException {
        final byte[] decoded = java.util.Base64.getDecoder().decode(wrappingKey);
        final AesKey wrappingKeyObj = new AesKey(Arrays.copyOf(decoded, 32));

        final Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        cipher.init(Cipher.UNWRAP_MODE, wrappingKeyObj);
        final byte[] wrappedKey = Base64.getDecoder().decode(this.encryptedKey);
        final Key unwrappedKey = cipher.unwrap(wrappedKey, ENCRYPTION_KEY_ALGORITHM, Cipher.SECRET_KEY);

        final byte[] keyBytes = unwrappedKey.getEncoded();
        final String jsonString = new String(keyBytes, StandardCharsets.UTF_8);
        final ObjectMapper mapper = new ObjectMapper();
        this.decryptedKey = mapper.readValue(jsonString, String.class);
    }

    public String getDecryptedKey() {
        return decryptedKey;
    }
}