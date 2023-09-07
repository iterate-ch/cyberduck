package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;

import com.google.api.services.storage.model.StorageObject;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GoogleStorageWriteFeatureTest extends AbstractGoogleStorageTest {

    @Test(expected = InteroperabilityException.class)
    public void testWriteInvalidStorageClass() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus();
        status.setStorageClass("invalid");
        try {
            new GoogleStorageWriteFeature(session).write(test, status, new DisabledConnectionCallback()).close();
            fail();
        }
        catch(BackgroundException e) {
            assertEquals("Invalid argument. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test
    public void testWriteCustomMetadata() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus();
        final HashMap<String, String> metadata = new HashMap<>();
        metadata.put("k1", "v1");
        status.setMetadata(metadata);
        new GoogleStorageWriteFeature(session).write(test, status, new DisabledConnectionCallback()).close();
        assertEquals(metadata, new GoogleStorageMetadataFeature(session).getMetadata(test));
        metadata.put("k2", "v2");
        new GoogleStorageWriteFeature(session).write(test, status, new DisabledConnectionCallback()).close();
        assertEquals(metadata, new GoogleStorageMetadataFeature(session).getMetadata(test));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = InteroperabilityException.class)
    public void testWriteCannedAclUniformBucketLevelAccess() throws Exception {
        final Path container = new Path("cyberduck-test-eu-uniform-access", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus();
        status.setAcl(Acl.CANNED_PRIVATE);
        try {
            new GoogleStorageWriteFeature(session).write(test, status, new DisabledConnectionCallback()).close();
            fail();
        }
        catch(IOException e) {
            assertEquals(InteroperabilityException.class, e.getCause().getClass());
            InteroperabilityException failure = (InteroperabilityException) e.getCause();
            assertEquals("Cannot insert legacy ACL for an object when uniform bucket-level access is enabled. Read more at https://cloud.google.com/storage/docs/uniform-bucket-level-access. Please contact your web hosting service provider for assistance.",
                    failure.getDetail());
            throw failure;
        }
    }

    @Test
    public void testWritePublicReadCannedPublicAcl() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(1033);
        status.setLength(content.length);
        status.setAcl(Acl.CANNED_PUBLIC_READ);
        final HttpResponseOutputStream<StorageObject> out = new GoogleStorageWriteFeature(session).write(test, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertNotNull(out.getStatus().getGeneration());
        assertTrue(new GoogleStorageFindFeature(session).find(test));
        assertTrue(new GoogleStorageAccessControlListFeature(session)
                .getPermission(test).asList().contains(new Acl.UserAndRole(new Acl.GroupUser(Acl.GroupUser.EVERYONE), new Acl.Role(Acl.Role.READ))));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWritePublicReadCannedPrivateAcl() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(1033);
        status.setLength(content.length);
        status.setAcl(Acl.CANNED_PRIVATE);
        final HttpResponseOutputStream<StorageObject> out = new GoogleStorageWriteFeature(session).write(test, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertNotNull(out.getStatus().getGeneration());
        assertTrue(new GoogleStorageFindFeature(session).find(test));
        assertFalse(new GoogleStorageAccessControlListFeature(session)
                .getPermission(test).asList().contains(new Acl.UserAndRole(new Acl.GroupUser(Acl.GroupUser.EVERYONE), new Acl.Role(Acl.Role.READ))));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWrite() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, String.format("%s %s", new AlphanumericRandomStringService().random(), new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        {
            final TransferStatus status = new TransferStatus();
            final byte[] content = RandomUtils.nextBytes(2048);
            status.setModified(System.currentTimeMillis());
            status.setLength(content.length);
            status.setMime("application/octet-stream");
            status.setStorageClass("multi_regional");
            status.setMetadata(Collections.singletonMap("c", "d"));
            final HttpResponseOutputStream<StorageObject> out = new GoogleStorageWriteFeature(session).write(test, status, new DisabledConnectionCallback());
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
            out.close();
            assertNotNull(out.getStatus().getGeneration());
            assertTrue(new GoogleStorageFindFeature(session).find(test));
            final Write.Append append = new GoogleStorageWriteFeature(session).append(test, status.withRemote(new GoogleStorageAttributesFinderFeature(session).find(test)));
            assertFalse(append.append);
            assertEquals(content.length, append.size, 0L);
            final byte[] buffer = new byte[content.length];
            final InputStream in = new GoogleStorageReadFeature(session).read(test, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
            IOUtils.readFully(in, buffer);
            in.close();
            assertArrayEquals(content, buffer);
        }
        // Overwrite
        {
            final TransferStatus status = new TransferStatus();
            status.setExists(true);
            status.setModified(1530305150672L);
            final byte[] content = RandomUtils.nextBytes(1024);
            status.setLength(content.length);
            final HttpResponseOutputStream<StorageObject> out = new GoogleStorageWriteFeature(session).write(test, status, new DisabledConnectionCallback());
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
            out.close();
            assertNotNull(out.getStatus().getGeneration());
            final PathAttributes attributes = new GoogleStorageAttributesFinderFeature(session).find(test);
            assertEquals(content.length, attributes.getSize());
        }
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteArchiveStorageClass() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, String.format("%s %s", new AlphanumericRandomStringService().random(), new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(2048);
        status.setLength(content.length);
        status.setMime("application/octet-stream");
        status.setStorageClass("ARCHIVE");
        final HttpResponseOutputStream<StorageObject> out = new GoogleStorageWriteFeature(session).write(test, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        assertNotNull(out.getStatus().getGeneration());
        assertTrue(new GoogleStorageFindFeature(session).find(test));
        assertEquals("ARCHIVE", new GoogleStorageStorageClassFeature(session).getClass(test));
        final byte[] buffer = new byte[content.length];
        final InputStream in = new GoogleStorageReadFeature(session).read(test, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
