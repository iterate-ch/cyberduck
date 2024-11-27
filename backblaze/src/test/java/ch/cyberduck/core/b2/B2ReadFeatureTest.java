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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.SHA1ChecksumCompute;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultDownloadFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CountingInputStream;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import synapticloop.b2.response.BaseB2Response;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2ReadFeatureTest extends AbstractB2Test {

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        new B2ReadFeature(session, new B2VersionIdProvider(session)).read(new Path(bucket, "nosuchname", EnumSet.of(Path.Type.file)), new TransferStatus(), new DisabledConnectionCallback());
    }

    @Test
    public void testReadChunkedTransfer() throws Exception {
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(923);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setChecksum(new SHA1ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final HttpResponseOutputStream<BaseB2Response> out = new B2WriteFeature(session, fileid).write(file, status, new DisabledConnectionCallback());
        IOUtils.write(content, out);
        out.close();
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        assertEquals(-1L, local.attributes().getSize());
        new DefaultDownloadFeature(new B2ReadFeature(session, fileid)).download(file, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), new TransferStatus() {
                    @Override
                    public void setLength(long length) {
                        assertEquals(923L, length);
                        // Ignore update. As with unknown length for chunked transfer
                    }
                }, new DisabledLoginCallback());
        assertEquals(923L, local.attributes().getSize());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadInterrupt() throws Exception {
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(923);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setChecksum(new SHA1ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final HttpResponseOutputStream<BaseB2Response> out = new B2WriteFeature(session, fileid).write(file, status, new DisabledConnectionCallback());
        IOUtils.write(content, out);
        out.close();
        {
            // Unknown length in status
            // Read a single byte
            final InputStream in = new B2ReadFeature(session, fileid).read(file, new TransferStatus(), new DisabledConnectionCallback());
            assertNotNull(in.read());
            in.close();
        }
        {
            final InputStream in = new B2ReadFeature(session, fileid).read(file, new TransferStatus(), new DisabledConnectionCallback());
            assertNotNull(in);
            in.close();
        }
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadRange() throws Exception {
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        new B2TouchFeature(session, fileid).touch(test, new TransferStatus());

        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = RandomUtils.nextBytes(1000);
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        out.close();
        final BaseB2Response reply = new B2SingleUploadService(session, new B2WriteFeature(session, fileid)).upload(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledProgressListener(), new DisabledStreamListener(),
                new TransferStatus().withLength(content.length),
                new DisabledConnectionCallback());
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setAppend(true);
        status.setOffset(100L);
        final InputStream in = new B2ReadFeature(session, fileid).read(test, status.withLength(content.length - 100), new DisabledConnectionCallback());
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadRangeUnknownLength() throws Exception {
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        new B2TouchFeature(session, fileid).touch(test, new TransferStatus());

        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = RandomUtils.nextBytes(1000);
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        out.close();
        new B2SingleUploadService(session, new B2WriteFeature(session, fileid)).upload(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledProgressListener(), new DisabledStreamListener(),
                new TransferStatus().withLength(content.length),
                new DisabledConnectionCallback());
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        status.setAppend(true);
        status.setOffset(100L);
        final InputStream in = new B2ReadFeature(session, fileid).read(test, status, new DisabledConnectionCallback());
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadCloseReleaseEntity() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final int length = 2048;
        final byte[] content = RandomUtils.nextBytes(length);
        final TransferStatus status = new TransferStatus().withLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final OutputStream out = new B2WriteFeature(session, fileid).write(file, status, new DisabledConnectionCallback());
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        final CountingInputStream in = new CountingInputStream(new B2ReadFeature(session, fileid).read(file, status, new DisabledConnectionCallback()));
        in.close();
        assertEquals(0L, in.getByteCount(), 0L);
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testChangedNodeId() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new B2TouchFeature(session, fileid).touch(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String latestnodeid = test.attributes().getVersionId();
        assertNotNull(latestnodeid);
        // Assume previously seen but changed on server
        final String invalidId = String.valueOf(RandomUtils.nextLong());
        test.attributes().setVersionId(invalidId);
        fileid.cache(test, invalidId);
        try {
            final InputStream in = new B2ReadFeature(session, fileid).read(test, new TransferStatus().withRemote(test.attributes()), new DisabledLoginCallback());
            fail();
        }
        catch(NotfoundException e) {
            //
        }
        assertNull(test.attributes().getVersionId());
        new B2DeleteFeature(session, fileid).delete(new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener()).toList(), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
