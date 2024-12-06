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

import org.apache.commons.lang3.RandomStringUtils;
import org.cryptomator.cryptolib.api.FileNameCryptor;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import com.google.common.io.BaseEncoding;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class CryptorCacheTest {

    @Test
    public void TestHashDirectoryId() {
        final FileNameCryptor mock = mock(FileNameCryptor.class);
        final CryptorCache cryptor = new CryptorCache(mock);
        when(mock.hashDirectoryId(any(byte[].class))).thenReturn("hashed");
        assertEquals("hashed", cryptor.hashDirectoryId("id".getBytes(StandardCharsets.US_ASCII)));
        assertEquals("hashed", cryptor.hashDirectoryId("id".getBytes(StandardCharsets.US_ASCII)));
        verify(mock, times(1)).hashDirectoryId(any(byte[].class));
        verifyNoMoreInteractions(mock);
    }

    @Test
    public void TestEncryptFilename() {
        final FileNameCryptor mock = mock(FileNameCryptor.class);
        final CryptorCache cryptor = new CryptorCache(mock);
        when(mock.encryptFilename(any(), any(), any())).thenReturn(RandomStringUtils.randomAscii(10));
        final String encrypted1 = cryptor.encryptFilename(CryptorCache.BASE32, "first", "id1".getBytes());
        verify(mock, times(1)).encryptFilename(any(), any(), any());
        assertEquals(encrypted1, cryptor.encryptFilename(CryptorCache.BASE32, "first", "id1".getBytes()));
        verify(mock, times(1)).encryptFilename(any(), any(), any());
        // ensure using reverse cache from encryption
        assertEquals("first", cryptor.decryptFilename(CryptorCache.BASE32, encrypted1, "id1".getBytes()));
        verify(mock, times(1)).encryptFilename(any(), any(), any());
        verifyNoMoreInteractions(mock);
        // cache miss on encoding
        cryptor.encryptFilename(BaseEncoding.base64Url(), "first", "id1".getBytes());
        verify(mock, times(2)).encryptFilename(any(), any(), any());
        // cache miss on cleartext
        cryptor.encryptFilename(CryptorCache.BASE32, "second", "id1".getBytes());
        verify(mock, times(3)).encryptFilename(any(), any(), any());
        // cache miss on byte[]
        cryptor.encryptFilename(CryptorCache.BASE32, "first", "id2".getBytes());
        verify(mock, times(4)).encryptFilename(any(), any(), any());
    }

    @Test
    public void TestDecryptFilename() {
        final FileNameCryptor mock = mock(FileNameCryptor.class);
        final CryptorCache cryptor = new CryptorCache(mock);
        when(mock.decryptFilename(any(), any(), any())).thenReturn(RandomStringUtils.randomAscii(10));
        final String decrypted1 = cryptor.decryptFilename(CryptorCache.BASE32, "first", "id1".getBytes());
        verify(mock, times(1)).decryptFilename(any(), any(), any());
        assertEquals(decrypted1, cryptor.decryptFilename(CryptorCache.BASE32, "first", "id1".getBytes()));
        verify(mock, times(1)).decryptFilename(any(), any(), any());
        // ensure using reverse cache from encryption
        assertEquals("first", cryptor.encryptFilename(CryptorCache.BASE32, decrypted1, "id1".getBytes()));
        verify(mock, times(1)).decryptFilename(any(), any(), any());
        verifyNoMoreInteractions(mock);
        // cache miss on encoding
        cryptor.decryptFilename(BaseEncoding.base64Url(), "first", "id1".getBytes());
        verify(mock, times(2)).decryptFilename(any(), any(), any());
        // cache miss on cleartext
        cryptor.decryptFilename(CryptorCache.BASE32, "second", "id1".getBytes());
        verify(mock, times(3)).decryptFilename(any(), any(), any());
        // cache miss on byte[]
        cryptor.decryptFilename(CryptorCache.BASE32, "first", "id2".getBytes());
        verify(mock, times(4)).decryptFilename(any(), any(), any());
    }
}
