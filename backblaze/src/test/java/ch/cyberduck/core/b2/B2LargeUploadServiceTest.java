package ch.cyberduck.core.b2;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.SHA1ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2LargeUploadServiceTest extends AbstractB2Test {

    @Test
    public void testUpload() throws Exception {
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));

        final Path test = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());

        // Each segment, except the last, must be larger than 100MB.
        final int length = 100 * 1024 * 1024 + 1;
        final byte[] content = RandomUtils.nextBytes(length);

        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final TransferStatus status = new TransferStatus().withLength(0L);
        status.setLength(content.length);
        final Checksum checksum = new SHA1ChecksumCompute().compute(new ByteArrayInputStream(content), new TransferStatus().withLength(0L));
        status.setChecksum(checksum);

        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final B2LargeUploadService upload = new B2LargeUploadService(session, fileid, new B2WriteFeature(session, fileid),
            PreferencesFactory.get().getLong("b2.upload.largeobject.size"),
            PreferencesFactory.get().getInteger("b2.upload.largeobject.concurrency"));

        upload.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
            status, new DisabledConnectionCallback());
        assertEquals(checksum, new B2AttributesFinderFeature(session, fileid).find(test).getChecksum());
        status.validate();
        assertTrue(status.isComplete());
        assertEquals(content.length, status.getResponse().getSize());

        assertTrue(new DefaultFindFeature(session).find(test));
        final InputStream in = new B2ReadFeature(session, fileid).read(test, new TransferStatus().withLength(0L), new DisabledConnectionCallback());
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
        new StreamCopier(status, status).transfer(in, buffer);
        in.close();
        buffer.close();
        assertArrayEquals(content, buffer.toByteArray());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testAppendNoPartCompleted() throws Exception {
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final String name = new AlphanumericRandomStringService().random();
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final int length = 102 * 1024 * 1024;
        final byte[] content = RandomUtils.nextBytes(length);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus().withLength(0L);
        status.setLength(content.length);
        final AtomicBoolean interrupt = new AtomicBoolean();
        final BytecountStreamListener count = new BytecountStreamListener();
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final B2LargeUploadService service = new B2LargeUploadService(session, fileid, new B2WriteFeature(session, fileid), 100 * 1024L * 1024L, 1);
        try {
            service.upload(test, new Local(System.getProperty("java.io.tmpdir"), name) {
                @Override
                public InputStream getInputStream() throws AccessDeniedException {
                    return new CountingInputStream(super.getInputStream()) {
                        @Override
                        protected void beforeRead(int n) throws IOException {
                            if(count.getSent() >= 5 * 1024L * 1024L) {
                                throw new IOException();
                            }
                        }
                    };
                }
            }, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), count, status, new DisabledLoginCallback());
        }
        catch(BackgroundException e) {
            // Expected
            interrupt.set(true);
        }
        assertTrue(interrupt.get());
        assertEquals(5 * 1024L * 1024L, count.getSent(), 0L);
        assertFalse(status.isComplete());
        assertEquals(TransferStatus.UNKNOWN_LENGTH, status.getResponse().getSize());

        final TransferStatus append = new TransferStatus().withLength(0L).append(true).withLength(content.length);
        service.upload(test, local,
                new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(), append,
                new DisabledLoginCallback());
        assertEquals(content.length, append.getResponse().getSize());
        assertTrue(new B2FindFeature(session, fileid).find(test));
        assertEquals(content.length, new B2AttributesFinderFeature(session, fileid).find(test).getSize());
        assertTrue(append.isComplete());
        final byte[] buffer = new byte[content.length];
        final InputStream in = new B2ReadFeature(session, fileid).read(test, new TransferStatus().withLength(0L), new DisabledConnectionCallback());
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testAppendSecondPart() throws Exception {
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final String name = new AlphanumericRandomStringService().random();
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final int length = 102 * 1024 * 1024;
        final byte[] content = RandomUtils.nextBytes(length);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus().withLength(0L);
        status.setLength(content.length);
        final AtomicBoolean interrupt = new AtomicBoolean();
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final B2LargeUploadService feature = new B2LargeUploadService(session, fileid, new B2WriteFeature(session, fileid), 100L * 1024L * 1024L, 1);
        final BytecountStreamListener count = new BytecountStreamListener();
        try {
            feature.upload(test, new Local(System.getProperty("java.io.tmpdir"), name) {
                @Override
                public InputStream getInputStream() throws AccessDeniedException {
                    return new CountingInputStream(super.getInputStream()) {
                        @Override
                        protected void beforeRead(int n) throws IOException {
                            if(count.getSent() >= 100L * 1024L * 1024L) {
                                throw new IOException();
                            }
                        }
                    };
                }
            }, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), count, status, new DisabledLoginCallback());
        }
        catch(BackgroundException e) {
            // Expected
            interrupt.set(true);
        }
        assertTrue(interrupt.get());
        assertEquals(100L * 1024L * 1024L, count.getSent(), 0L);
        assertFalse(status.isComplete());
        assertEquals(TransferStatus.UNKNOWN_LENGTH, status.getResponse().getSize());
        assertTrue(feature.append(test, status).append);
        final Path upload = new Path(test).withType(EnumSet.of(Path.Type.file, Path.Type.upload));
        assertTrue(new B2FindFeature(session, fileid).find(upload));
        assertEquals(100L * 1024L * 1024L, new B2AttributesFinderFeature(session, fileid).find(upload).getSize(), 0L);
        final TransferStatus append = new TransferStatus().withLength(0L).append(true).withLength(2L * 1024L * 1024L).withOffset(100L * 1024L * 1024L);
        feature.upload(test, local,
                new BandwidthThrottle(BandwidthThrottle.UNLIMITED), count, append,
                new DisabledLoginCallback());
        assertEquals(102L * 1024L * 1024L, count.getSent());
        assertTrue(append.isComplete());
        assertEquals(content.length, append.getResponse().getSize());
        assertTrue(new B2FindFeature(session, fileid).find(test));
        assertEquals(102L * 1024L * 1024L, new B2AttributesFinderFeature(session, fileid).find(test).getSize(), 0L);
        final byte[] buffer = new byte[content.length];
        final InputStream in = new B2ReadFeature(session, fileid).read(test, new TransferStatus().withLength(0L), new DisabledConnectionCallback());
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }
}
