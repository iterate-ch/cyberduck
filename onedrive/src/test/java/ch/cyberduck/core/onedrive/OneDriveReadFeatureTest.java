package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class OneDriveReadFeatureTest extends AbstractOneDriveTest {

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final TransferStatus status = new TransferStatus();
        try {
            final Path drive = new Path("/587e132bbff8c44a", EnumSet.of(Path.Type.volume));
            new OneDriveReadFeature(session).read(new Path(drive, "nosuchname", EnumSet.of(Path.Type.file)), status, new DisabledConnectionCallback());
        }
        catch(NotfoundException e) {
            assertEquals("Not Found. Item does not exist. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }

    @Test
    public void testReadInterrupt() throws Exception {
        final Path drive = new Path("/587e132bbff8c44a", EnumSet.of(Path.Type.volume));
        final Path test = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new OneDriveTouchFeature(session).touch(test, new TransferStatus());
        // Unknown length in status
        final TransferStatus status = new TransferStatus();
        // Read a single byte
        {
            final InputStream in = new OneDriveReadFeature(session).read(test, status, new DisabledConnectionCallback());
            assertNotNull(in.read());
            in.close();
        }
        {
            final InputStream in = new OneDriveReadFeature(session).read(test, status, new DisabledConnectionCallback());
            assertNotNull(in);
            in.close();
        }
        new OneDriveDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadRange() throws Exception {
        final Path drive = new Path("/587e132bbff8c44a", EnumSet.of(Path.Type.volume));
        final Path test = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new OneDriveTouchFeature(session).touch(test, new TransferStatus());

        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final byte[] content = RandomUtils.nextBytes(1000);
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        out.close();
        new DefaultUploadFeature<Void>(new OneDriveWriteFeature(session)).upload(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                new TransferStatus().length(content.length),
                new DisabledConnectionCallback());
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setAppend(true);
        status.setOffset(100L);
        final OneDriveReadFeature read = new OneDriveReadFeature(session);
        assertTrue(read.offset(test));
        final InputStream in = read.read(test, status.length(content.length - 100), new DisabledConnectionCallback());
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new OneDriveDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadRangeUnknownLength() throws Exception {
        final Path drive = new Path("/587e132bbff8c44a", EnumSet.of(Path.Type.volume));
        final Path test = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new OneDriveTouchFeature(session).touch(test, new TransferStatus());

        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final byte[] content = RandomUtils.nextBytes(1000);
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        out.close();
        new DefaultUploadFeature<Void>(new OneDriveWriteFeature(session)).upload(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                new TransferStatus().length(content.length),
                new DisabledConnectionCallback());
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        status.setAppend(true);
        status.setOffset(100L);
        final InputStream in = new OneDriveReadFeature(session).read(test, status, new DisabledConnectionCallback());
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new OneDriveDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
