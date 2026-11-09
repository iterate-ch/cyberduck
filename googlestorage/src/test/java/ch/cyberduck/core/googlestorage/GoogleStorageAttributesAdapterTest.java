package ch.cyberduck.core.googlestorage;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import com.google.api.services.storage.model.StorageObject;

import static org.junit.Assert.assertEquals;

public class GoogleStorageAttributesAdapterTest {

    private final GoogleStorageAttributesFinderFeature f = new GoogleStorageAttributesFinderFeature(
            new GoogleStorageSession(new Host(new GoogleStorageProtocol()), new DefaultX509TrustManager(), new DefaultX509KeyManager()));

    @Test
    public void testChecksumBase64Md5Hash() throws Exception {
        final byte[] md5 = MessageDigest.getInstance("MD5").digest("test".getBytes(StandardCharsets.UTF_8));
        // md5Hash of an object is returned base64 encoded
        final StorageObject object = new StorageObject().setMd5Hash(Base64.encodeBase64String(md5));
        final PathAttributes attributes = f.toAttributes(object);
        assertEquals(new Checksum(HashAlgorithm.md5, Hex.encodeHexString(md5)), attributes.getChecksum());
        assertEquals("098f6bcd4621d373cade4e832627b4f6", attributes.getChecksum().hash);
    }

    @Test
    public void testChecksumMissing() {
        // Composite objects have no md5Hash
        assertEquals(Checksum.NONE, f.toAttributes(new StorageObject()).getChecksum());
    }
}
