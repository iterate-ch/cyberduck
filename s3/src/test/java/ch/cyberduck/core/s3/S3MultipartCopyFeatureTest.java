package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3MultipartCopyFeatureTest extends AbstractS3Test {

    @Test
    public void testCopy() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(1023);
        final TransferStatus status = new TransferStatus().withLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final OutputStream out = new S3WriteFeature(session, acl).write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        test.attributes().setSize(content.length);
        final Path copy = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));

        final S3MultipartCopyFeature feature = new S3MultipartCopyFeature(session, acl);
        feature.copy(test, copy, status, new DisabledConnectionCallback(), new DisabledStreamListener());
        assertTrue(new S3FindFeature(session, acl).find(test));
        assertEquals(content.length, new S3AttributesFinderFeature(session, acl).find(test).getSize());
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertTrue(new S3FindFeature(session, acl).find(copy));
        assertEquals(content.length, new S3AttributesFinderFeature(session, acl).find(copy).getSize());
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyBucketNameInHostname() throws Exception {
        final Path test = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(1023);
        final TransferStatus status = new TransferStatus().withLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(virtualhost);
        final OutputStream out = new S3WriteFeature(virtualhost, acl).write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        test.attributes().setSize(content.length);
        final Path copy = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final S3MultipartCopyFeature feature = new S3MultipartCopyFeature(virtualhost, acl);
        feature.copy(test, copy, status, new DisabledConnectionCallback(), new DisabledStreamListener());
        assertTrue(new S3FindFeature(virtualhost, acl).find(test));
        assertEquals(content.length, new S3AttributesFinderFeature(virtualhost, acl).find(test).getSize());
        new S3DefaultDeleteFeature(virtualhost, acl).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertTrue(new S3FindFeature(virtualhost, acl).find(copy));
        assertEquals(content.length, new S3AttributesFinderFeature(virtualhost, acl).find(copy).getSize());
        new S3DefaultDeleteFeature(virtualhost, acl).delete(Collections.singletonList(copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyAWS4Signature() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(1023);
        final TransferStatus status = new TransferStatus().withLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final OutputStream out = new S3WriteFeature(session, acl).write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        out.close();
        test.attributes().setSize(content.length);
        final Path copy = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));

        final S3MultipartCopyFeature feature = new S3MultipartCopyFeature(session, acl);
        feature.copy(test, copy, status, new DisabledConnectionCallback(), new DisabledStreamListener());
        assertTrue(new S3FindFeature(session, acl).find(test));
        assertEquals(content.length, new S3AttributesFinderFeature(session, acl).find(test).getSize());
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertTrue(new S3FindFeature(session, acl).find(copy));
        assertEquals(content.length, new S3AttributesFinderFeature(session, acl).find(copy).getSize());
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
