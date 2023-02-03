package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractDropboxTest;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.shared.DefaultUploadFeature;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import com.dropbox.core.v2.files.Metadata;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DropboxReadFeatureTest extends AbstractDropboxTest {

    @Test
    public void testReadInterrupt() throws Exception {
        final DropboxWriteFeature write = new DropboxWriteFeature(session);
        final TransferStatus writeStatus = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(66800);
        writeStatus.setLength(content.length);
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final OutputStream out = write.write(test, writeStatus, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        // Unknown length in status
        final TransferStatus readStatus = new TransferStatus();
        // Read a single byte
        {
            final InputStream in = new DropboxReadFeature(session).read(test, readStatus, new DisabledConnectionCallback());
            assertNotNull(in.read());
            in.close();
        }
        {
            final InputStream in = new DropboxReadFeature(session).read(test, readStatus, new DisabledConnectionCallback());
            assertNotNull(in);
            in.close();
        }
        new DropboxDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadRange() throws Exception {
        final Path drive = new DefaultHomeFinderService(session).find();
        final Path test = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DropboxTouchFeature(session).touch(test, new TransferStatus());

        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final byte[] content = RandomUtils.nextBytes(1000);
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        out.close();
        new DefaultUploadFeature<>(new DropboxWriteFeature(session)).upload(
            test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
            new TransferStatus().withLength(content.length),
            new DisabledConnectionCallback());
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setAppend(true);
        status.setOffset(100L);
        final DropboxReadFeature read = new DropboxReadFeature(session);
        assertTrue(read.offset(test));
        final InputStream in = read.read(test, status.withLength(content.length - 100), new DisabledConnectionCallback());
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new DropboxDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadCloseReleaseEntity() throws Exception {
        final TransferStatus status = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus writeStatus = new TransferStatus();
        writeStatus.setLength(content.length);
        final Path directory = new DropboxDirectoryFeature(session).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final DropboxWriteFeature writer = new DropboxWriteFeature(session);
        final HttpResponseOutputStream<Metadata> out = writer.write(test, writeStatus, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(writeStatus, writeStatus).transfer(new ByteArrayInputStream(content), out);
        final CountingInputStream in = new CountingInputStream(new DropboxReadFeature(session).read(test, status, new DisabledConnectionCallback()));
        in.close();
        assertEquals(0L, in.getByteCount(), 0L);
        new DropboxDeleteFeature(session).delete(Arrays.asList(test, directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadRevision() throws Exception {
        final byte[] content = RandomUtils.nextBytes(1645);
        final TransferStatus status = new TransferStatus().withLength(content.length);
        final Path directory = new DropboxDirectoryFeature(session).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final DropboxWriteFeature writer = new DropboxWriteFeature(session);
        final HttpResponseOutputStream<Metadata> out = writer.write(test, status, new DisabledConnectionCallback());
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        test.withAttributes(status.getResponse());
        assertNotNull(test.attributes().getVersionId());
        // Only latest version
        assertTrue(new DropboxVersioningFeature(session).list(test, new DisabledListProgressListener()).isEmpty());
        assertArrayEquals(content, IOUtils.readFully(new DropboxReadFeature(session).read(test, new TransferStatus(), new DisabledConnectionCallback()), content.length));
        new DropboxDeleteFeature(session).delete(Arrays.asList(test, directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
