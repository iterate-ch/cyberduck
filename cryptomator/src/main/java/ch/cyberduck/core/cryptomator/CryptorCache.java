package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.cache.LRUCache;

import org.cryptomator.cryptolib.api.AuthenticationFailedException;
import org.cryptomator.cryptolib.api.FileNameCryptor;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Objects;

import com.google.common.io.BaseEncoding;

public class CryptorCache {

    public static final BaseEncoding BASE32 = BaseEncoding.base32();

    private final LRUCache<ByteBuffer, String> directoryIdCache = LRUCache.build(250);
    private final LRUCache<CacheKey, String> decryptCache = LRUCache.build(5000);
    private final LRUCache<CacheKey, String> encryptCache = LRUCache.build(5000);

    private final FileNameCryptor impl;

    public CryptorCache(final FileNameCryptor impl) {
        this.impl = impl;
    }

    public String hashDirectoryId(final byte[] cleartextDirectoryId) {
        final ByteBuffer wrap = ByteBuffer.wrap(cleartextDirectoryId);
        if(!directoryIdCache.contains(wrap)) {
            directoryIdCache.put(wrap, impl.hashDirectoryId(cleartextDirectoryId));
        }
        return directoryIdCache.get(wrap);
    }

    public String encryptFilename(final BaseEncoding encoding, final String cleartextName, final byte[] associatedData) {
        final CacheKey key = new CacheKey(encoding, cleartextName, associatedData);
        if(encryptCache.contains(key)) {
            return encryptCache.get(key);
        }
        final String ciphertextName = impl.encryptFilename(encoding, cleartextName, associatedData);
        encryptCache.put(key, ciphertextName);
        decryptCache.put(new CacheKey(encoding, ciphertextName, associatedData), cleartextName);
        return ciphertextName;
    }

    public String decryptFilename(final BaseEncoding encoding, final String ciphertextName, final byte[] associatedData) throws AuthenticationFailedException {
        final CacheKey key = new CacheKey(encoding, ciphertextName, associatedData);
        if(decryptCache.contains(key)) {
            return decryptCache.get(key);
        }
        final String cleartextName = impl.decryptFilename(encoding, ciphertextName, associatedData);
        decryptCache.put(key, cleartextName);
        encryptCache.put(new CacheKey(encoding, cleartextName, associatedData), ciphertextName);
        return cleartextName;
    }

    private static class CacheKey {
        private final BaseEncoding encoding;
        private final String value;
        private final byte[] data;

        public CacheKey(final BaseEncoding encoding, final String value, final byte[] data) {
            this.encoding = encoding;
            this.value = value;
            this.data = data;
        }

        @Override
        public boolean equals(final Object o) {
            if(this == o) {
                return true;
            }
            if(o == null || getClass() != o.getClass()) {
                return false;
            }
            final CacheKey cacheKey = (CacheKey) o;
            return Objects.equals(encoding, cacheKey.encoding) &&
                Objects.equals(value, cacheKey.value) &&
                Arrays.equals(data, cacheKey.data);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(encoding, value);
            result = 31 * result + Arrays.hashCode(data);
            return result;
        }
    }
}
