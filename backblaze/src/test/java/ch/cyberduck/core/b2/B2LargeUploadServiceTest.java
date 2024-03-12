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
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.SHA1ChecksumCompute;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.StreamProgress;
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

import synapticloop.b2.response.BaseB2Response;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2LargeUploadServiceTest extends AbstractB2Test {

    @Test
    public void testUpload() throws Exception {
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));

        final Path test = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());

        // Each segment, except the last, must be larger than 100MB.
        final int length = 5 * 1000 * 1000 + 1;
        final byte[] content = RandomUtils.nextBytes(length);

        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        out.close();
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Checksum checksum = new SHA1ChecksumCompute().compute(new ByteArrayInputStream(content), new TransferStatus());
        status.setChecksum(checksum);

        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final B2LargeUploadService upload = new B2LargeUploadService(session, fileid,
                new B2WriteFeature(session, fileid), 5 * 1000L * 1000L, 5);

        upload.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                status, new DisabledConnectionCallback());
        final PathAttributes attr = new B2AttributesFinderFeature(session, fileid).find(test);
        assertNotEquals(Checksum.NONE, attr.getChecksum());
        assertEquals(checksum, attr.getChecksum());
        status.validate();
        assertTrue(status.isComplete());
        assertEquals(content.length, status.getResponse().getSize());

        assertTrue(new DefaultFindFeature(session).find(test));
        final InputStream in = new B2ReadFeature(session, fileid).read(test, new TransferStatus(), new DisabledConnectionCallback());
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
        final int length = 6 * 1000 * 1000;
        final byte[] content = RandomUtils.nextBytes(length);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final AtomicBoolean interrupt = new AtomicBoolean();
        final BytecountStreamListener count = new BytecountStreamListener();
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final B2LargeUploadService service = new B2LargeUploadService(session, fileid,
                new B2WriteFeature(session, fileid), 5 * 1000L * 1000L, 1);
        try {
            service.upload(test, new Local(System.getProperty("java.io.tmpdir"), name) {
                @Override
                public InputStream getInputStream() throws AccessDeniedException {
                    return new CountingInputStream(super.getInputStream()) {
                        @Override
                        protected void beforeRead(int n) throws IOException {
                            throw new IOException();
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
        assertEquals(0L, count.getSent(), 0L);
        assertFalse(status.isComplete());
        assertEquals(TransferStatus.UNKNOWN_LENGTH, status.getResponse().getSize());
        final Write.Append resume = service.append(test, status);
        assertTrue(resume.append);
        assertEquals(0L, resume.size, 0L);
        final TransferStatus append = new TransferStatus().append(true).withLength(content.length);
        service.upload(test, local,
                new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(), append,
                new DisabledLoginCallback());
        assertEquals(content.length, append.getResponse().getSize());
        assertTrue(new B2FindFeature(session, fileid).find(test));
        assertEquals(content.length, new B2AttributesFinderFeature(session, fileid).find(test).getSize());
        assertTrue(append.isComplete());
        final byte[] buffer = new byte[content.length];
        final InputStream in = new B2ReadFeature(session, fileid).read(test, new TransferStatus(), new DisabledConnectionCallback());
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
        final int length = 6 * 1000 * 1000;
        final byte[] content = RandomUtils.nextBytes(length);
        IOUtils.write(content, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final AtomicBoolean interrupt = new AtomicBoolean();
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final B2LargeUploadService feature = new B2LargeUploadService(session, fileid,
                new B2WriteFeature(session, fileid), 5 * 1000L * 1000L, 1) {
            @Override
            public BaseB2Response upload(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener, final TransferStatus status, final StreamCancelation cancel, final StreamProgress progress, final ConnectionCallback callback) throws BackgroundException {
                if(!interrupt.get()) {
                    if(status.getOffset() >= 5L * 1000L * 1000L) {
                        throw new ConnectionTimeoutException("Test");
                    }
                }
                return super.upload(file, local, throttle, listener, status, cancel, progress, callback);
            }
        };
        final BytecountStreamListener count = new BytecountStreamListener();
        try {
            feature.upload(test, new Local(System.getProperty("java.io.tmpdir"), name), new BandwidthThrottle(BandwidthThrottle.UNLIMITED), count, status, new DisabledLoginCallback());
        }
        catch(BackgroundException e) {
            // Expected
            interrupt.set(true);
        }
        assertTrue(interrupt.get());
        assertEquals(5 * 1000L * 1000L, count.getSent(), 0L);
        assertFalse(status.isComplete());
        assertEquals(TransferStatus.UNKNOWN_LENGTH, status.getResponse().getSize());
        final Write.Append appendStatus = feature.append(test, status);
        assertTrue(appendStatus.append);
        assertEquals(5 * 1000L * 1000L, appendStatus.size, 0L);
        final Path upload = new Path(test).withType(EnumSet.of(Path.Type.file, Path.Type.upload));
        assertTrue(new B2FindFeature(session, fileid).find(upload));
        assertEquals(5 * 1000L * 1000L, new B2AttributesFinderFeature(session, fileid).find(upload).getSize(), 0L);
        final TransferStatus append = new TransferStatus().append(true).withLength(2L * 1000L * 1000L).withOffset(5 * 1000L * 1000L);
        feature.upload(test, local,
                new BandwidthThrottle(BandwidthThrottle.UNLIMITED), count, append,
                new DisabledLoginCallback());
        assertEquals(6 * 1000L * 1000L, count.getSent());
        assertTrue(append.isComplete());
        assertEquals(content.length, append.getResponse().getSize());
        assertTrue(new B2FindFeature(session, fileid).find(test));
        assertEquals(6 * 1000L * 1000L, new B2AttributesFinderFeature(session, fileid).find(test).getSize(), 0L);
        final byte[] buffer = new byte[content.length];
        final InputStream in = new B2ReadFeature(session, fileid).read(test, new TransferStatus(), new DisabledConnectionCallback());
        IOUtils.readFully(in, buffer);
        in.close();
        assertArrayEquals(content, buffer);
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }
}
