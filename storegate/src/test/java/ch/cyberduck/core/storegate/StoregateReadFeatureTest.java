package ch.cyberduck.core.storegate;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.storegate.io.swagger.client.model.File;
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

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class StoregateReadFeatureTest extends AbstractStoregateTest {

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final TransferStatus status = new TransferStatus();
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path room = new StoregateDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        try {
            new StoregateReadFeature(session, nodeid).read(new Path(room, "nosuchname", EnumSet.of(Path.Type.file)), status, new DisabledConnectionCallback());
        }
        finally {
            new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }

    @Test
    public void testReadInterrupt() throws Exception {
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus writeStatus = new TransferStatus();
        writeStatus.setLength(content.length);
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path folder = new StoregateDirectoryFeature(session, nodeid).mkdir(
            new Path(String.format("/My files/%s", new AlphanumericRandomStringService().random()),
                EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(folder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final StoregateWriteFeature writer = new StoregateWriteFeature(session, nodeid);
        final HttpResponseOutputStream<File> out = writer.write(test, writeStatus, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(writeStatus, writeStatus).transfer(new ByteArrayInputStream(content), out);
        // Unknown length in status
        final TransferStatus readStatus = new TransferStatus();
        // Read a single byte
        {
            final InputStream in = new StoregateReadFeature(session, nodeid).read(test, readStatus, new DisabledConnectionCallback());
            assertNotNull(in.read());
            in.close();
        }
        {
            final InputStream in = new StoregateReadFeature(session, nodeid).read(test, readStatus, new DisabledConnectionCallback());
            assertNotNull(in);
            in.close();
        }
        new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadRange() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path folder = new StoregateDirectoryFeature(session, nodeid).mkdir(
            new Path(String.format("/My files/%s", new AlphanumericRandomStringService().random()),
                EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new StoregateTouchFeature(session, nodeid).touch(test, new TransferStatus());
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final byte[] content = RandomUtils.nextBytes(1023);
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        out.close();
        final TransferStatus upload = new TransferStatus().withLength(content.length);
        upload.setExists(true);
        new DefaultUploadFeature<>(new StoregateWriteFeature(session, nodeid)).upload(
            test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(), upload,
            new DisabledConnectionCallback());
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setAppend(true);
        status.setOffset(100L);
        final InputStream in = new StoregateReadFeature(session, nodeid).read(test, status.withLength(content.length - 100), new DisabledConnectionCallback());
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadRangeUnknownLength() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path room = new StoregateDirectoryFeature(session, nodeid).mkdir(
            new Path(String.format("/My files/%s", new AlphanumericRandomStringService().random()),
                EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new StoregateTouchFeature(session, nodeid).touch(test, new TransferStatus());
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final byte[] content = RandomUtils.nextBytes(1000);
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        out.close();
        final TransferStatus upload = new TransferStatus().withLength(content.length);
        upload.setExists(true);
        new DefaultUploadFeature<>(new StoregateWriteFeature(session, nodeid)).upload(
            test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(), upload,
            new DisabledConnectionCallback());
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        status.setAppend(true);
        status.setOffset(100L);
        final InputStream in = new StoregateReadFeature(session, nodeid).read(test, status, new DisabledConnectionCallback());
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadCloseReleaseEntity() throws Exception {
        final TransferStatus status = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus writeStatus = new TransferStatus();
        writeStatus.setLength(content.length);
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path room = new StoregateDirectoryFeature(session, nodeid).mkdir(
            new Path(String.format("/My files/%s", new AlphanumericRandomStringService().random()),
                EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(room, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final StoregateWriteFeature writer = new StoregateWriteFeature(session, nodeid);
        final HttpResponseOutputStream<File> out = writer.write(test, writeStatus, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(writeStatus, writeStatus).transfer(new ByteArrayInputStream(content), out);
        final CountingInputStream in = new CountingInputStream(new StoregateReadFeature(session, nodeid).read(test, status, new DisabledConnectionCallback()));
        in.close();
        assertEquals(0L, in.getByteCount(), 0L);
        new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
