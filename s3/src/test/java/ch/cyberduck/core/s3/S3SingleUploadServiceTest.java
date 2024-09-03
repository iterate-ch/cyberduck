package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.kms.KMSEncryptionFeature;
import ch.cyberduck.core.local.LocalTouchFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3SingleUploadServiceTest extends AbstractS3Test {

    @Test
    public void testDecorate() throws Exception {
        final NullInputStream n = new NullInputStream(1L);
        final S3Session session = new S3Session(new Host(new S3Protocol()));
        assertSame(NullInputStream.class, new S3SingleUploadService(session,
                new S3WriteFeature(session, new S3AccessControlListFeature(session))).decorate(n, null).getClass());
    }

    @Test
    public void testUpload() throws Exception {
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final S3SingleUploadService service = new S3SingleUploadService(session, new S3WriteFeature(session, acl));
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = UUID.randomUUID() + ".txt";
        final Path test = new Path(container, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final byte[] random = RandomUtils.nextBytes(1000);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(random, out);
        out.close();
        final TransferStatus status = new TransferStatus();
        status.setLength(random.length);
        status.setMime("text/plain");
        service.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), status, new DisabledLoginCallback());
        assertTrue(new S3FindFeature(session, acl).find(test));
        final PathAttributes attr = new S3AttributesFinderFeature(session, acl).find(test);
        assertEquals(status.getResponse().getChecksum(), attr.getChecksum());
        assertEquals(status.getResponse().getETag(), attr.getETag());
        assertEquals(random.length, attr.getSize());
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testUploadSSE() throws Exception {
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final S3SingleUploadService service = new S3SingleUploadService(session, new S3WriteFeature(session, acl));
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = UUID.randomUUID() + ".txt";
        final Path test = new Path(container, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final byte[] random = RandomUtils.nextBytes(1000);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(random, out);
        out.close();
        final TransferStatus status = new TransferStatus();
        status.setLength(random.length);
        status.setEncryption(KMSEncryptionFeature.SSE_KMS_DEFAULT);
        service.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), status, new DisabledLoginCallback());
        assertTrue(new S3FindFeature(session, acl).find(test));
        final PathAttributes attributes = new S3AttributesFinderFeature(session, acl).find(test);
        assertEquals(random.length, attributes.getSize());
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testUploadWithSHA256Checksum() throws Exception {
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final S3SingleUploadService service = new S3SingleUploadService(session, new S3WriteFeature(session, acl));
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = UUID.randomUUID() + ".txt";
        final Path test = new Path(container, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final byte[] random = RandomUtils.nextBytes(1000);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(random, out);
        out.close();
        final TransferStatus status = new TransferStatus();
        status.setLength(random.length);
        service.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), status, new DisabledLoginCallback());
        assertTrue(new S3FindFeature(session, acl).find(test));
        final PathAttributes attributes = new S3AttributesFinderFeature(session, acl).find(test);
        assertEquals(random.length, attributes.getSize());
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test(expected = NotfoundException.class)
    public void testUploadInvalidContainer() throws Exception {
        final S3SingleUploadService m = new S3SingleUploadService(session, new S3WriteFeature(session, new S3AccessControlListFeature(session)));
        final Path container = new Path("nosuchcontainer.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        LocalTouchFactory.get().touch(local);
        final TransferStatus status = new TransferStatus();
        m.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                status, new DisabledLoginCallback());
    }
}
