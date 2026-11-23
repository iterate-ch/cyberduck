package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.features.Metadata;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;

import org.jets3t.service.model.StorageObject;
import org.junit.Test;

import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class S3AttributesAdapterTest {

    @Test
    public void testMtime() {
        final StorageObject object = new StorageObject();
        object.addMetadata("ETag", "a43c1b0aa53a0c908810c06ab1ff3967");
        object.addMetadata("Mtime", "1647683127.160620746");
        assertEquals(1647683127000L, new S3AttributesAdapter(new Host(new S3Protocol())).toAttributes(object).getModificationDate());
    }

    @Test
    public void testLastModified() {
        final StorageObject object = new StorageObject();
        object.setLastModifiedDate(new Date(1647683127000L));
        assertEquals(1647683127000L, new S3AttributesAdapter(new Host(new S3Protocol())).toAttributes(object).getModificationDate());
    }

    @Test
    public void testIgnoreDateHeader() {
        final StorageObject object = new StorageObject();
        object.addMetadata("Date", new Date(1647683127000L));
        assertEquals(-1L, new S3AttributesAdapter(new Host(new S3Protocol())).toAttributes(object).getModificationDate());
    }

    @Test
    public void testMtimeInvalid() {
        final StorageObject object = new StorageObject();
        object.addMetadata("Mtime", "Invalid");
        assertEquals(-1, new S3AttributesAdapter(new Host(new S3Protocol())).toAttributes(object).getModificationDate());
    }

    @Test
    public void testChecksumPlaintext() {
        final StorageObject object = new StorageObject();
        object.setETag("\"a43c1b0aa53a0c908810c06ab1ff3967\"");
        assertEquals(new Checksum(HashAlgorithm.md5, "a43c1b0aa53a0c908810c06ab1ff3967"),
                new S3AttributesAdapter(new Host(new S3Protocol())).toAttributes(object).getChecksum());
    }

    @Test
    public void testChecksumSSES3() {
        // ETag is the MD5 of the object data with SSE-S3
        final StorageObject object = new StorageObject();
        object.setETag("\"a43c1b0aa53a0c908810c06ab1ff3967\"");
        object.setServerSideEncryptionAlgorithm("AES256");
        assertEquals(new Checksum(HashAlgorithm.md5, "a43c1b0aa53a0c908810c06ab1ff3967"),
                new S3AttributesAdapter(new Host(new S3Protocol())).toAttributes(object).getChecksum());
    }

    @Test
    public void testChecksumSSEKMS() {
        // ETag is not the MD5 of the object data with SSE-KMS
        final StorageObject object = new StorageObject();
        object.setETag("\"a43c1b0aa53a0c908810c06ab1ff3967\"");
        object.setServerSideEncryptionAlgorithm("aws:kms");
        assertEquals(Checksum.NONE, new S3AttributesAdapter(new Host(new S3Protocol())).toAttributes(object).getChecksum());
    }

    @Test
    public void testMetadataEmpty() {
        assertTrue(S3AttributesAdapter.metadata(new StorageObject()).isEmpty());
    }

    @Test
    public void testMetadataUserDefined() {
        final StorageObject object = new StorageObject();
        object.addMetadata("my-key", "value");
        object.addMetadata("Another", "other");
        final Map<String, String> metadata = S3AttributesAdapter.metadata(object);
        assertEquals(2, metadata.size());
        assertEquals("value", metadata.get("my-key"));
        assertEquals("other", metadata.get("Another"));
    }

    @Test
    public void testMetadataHttpHeaderNamesNormalized() {
        final StorageObject object = new StorageObject();
        object.addMetadata("content-type", "text/plain");
        object.addMetadata("CACHE-CONTROL", "no-cache");
        final Map<String, String> metadata = S3AttributesAdapter.metadata(object);
        assertEquals("text/plain", metadata.get("Content-Type"));
        assertEquals("no-cache", metadata.get("Cache-Control"));
        assertFalse(metadata.containsKey("content-type"));
        assertFalse(metadata.containsKey("CACHE-CONTROL"));
    }

    @Test
    public void testMetadataCustomKeyCaseUnchanged() {
        final StorageObject object = new StorageObject();
        object.addMetadata("My-Custom-Key", "value");
        assertEquals("value", S3AttributesAdapter.metadata(object).get("My-Custom-Key"));
    }

    @Test
    public void testMetadataFilterManagedHeaders() {
        final StorageObject object = new StorageObject();
        object.addMetadata("storage-class", "STANDARD");
        object.addMetadata("Server-Side-Encryption", "AES256");
        object.addMetadata("expiration", "never");
        object.addMetadata("Checksum-SHA256", "abc");
        object.addMetadata("version-id", "1");
        object.addMetadata("keep", "me");
        final Map<String, String> metadata = S3AttributesAdapter.metadata(object);
        assertEquals(1, metadata.size());
        assertEquals("me", metadata.get("keep"));
    }

    @Test
    public void testMetadataNotRetrieved() {
        // Object from listing without metadata
        final StorageObject object = new StorageObject();
        assertSame(Metadata.EMPTY_METADATA, new S3AttributesAdapter(new Host(new S3Protocol())).toAttributes(object).getMetadata());
    }

    @Test
    public void testMetadataCompleteWithoutUserMetadata() {
        // Object from HEAD request without metadata
        final StorageObject object = new StorageObject();
        object.addMetadata("storage-class", "STANDARD");
        object.setMetadataComplete(true);
        final Map<String, String> metadata = new S3AttributesAdapter(new Host(new S3Protocol())).toAttributes(object).getMetadata();
        assertTrue(metadata.isEmpty());
        assertNotSame(Metadata.EMPTY_METADATA, metadata);
    }

    @Test
    public void testMetadataAdvertisedInAttributes() {
        final StorageObject object = new StorageObject();
        object.addMetadata("my-key", "value");
        assertEquals("value", new S3AttributesAdapter(new Host(new S3Protocol())).toAttributes(object).getMetadata().get("my-key"));
    }
}
