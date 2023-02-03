package ch.cyberduck.core.googledrive;

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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
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

import com.google.api.services.drive.model.File;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveReadFeatureTest extends AbstractDriveTest {

    @Test
    public void testAppend() throws Exception {
        assertTrue(new DriveReadFeature(null, new DriveFileIdProvider(session)).offset(new Path("/", EnumSet.of(Path.Type.file))));
    }

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final TransferStatus status = new TransferStatus();
        new DriveReadFeature(session, new DriveFileIdProvider(session)).read(new Path(DriveHomeFinderService.MYDRIVE_FOLDER, "nosuchname", EnumSet.of(Path.Type.file)), status, new DisabledConnectionCallback());
    }

    @Test
    public void testReadRange() throws Exception {
        final String name = "ä-" + new AlphanumericRandomStringService().random();
        final Path test = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, name, EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), name);
        final byte[] content = RandomUtils.nextBytes(1023);
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        out.close();
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        new DriveUploadFeature(session, fileid).upload(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                new TransferStatus().withLength(content.length),
                new DisabledConnectionCallback());
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setAppend(true);
        status.setOffset(100L);
        final InputStream in = new DriveReadFeature(session, fileid).read(test, status.withLength(content.length - 100), new DisabledConnectionCallback());
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadWhitespace() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path file = new DriveTouchFeature(session, fileid).touch(new Path(DriveHomeFinderService.MYDRIVE_FOLDER, String.format("t %s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(0, new DriveAttributesFinderFeature(session, fileid).find(file).getSize());
        final CountingInputStream in = new CountingInputStream(new DriveReadFeature(session, fileid).read(file, new TransferStatus(), new DisabledConnectionCallback()));
        in.close();
        assertEquals(0L, in.getByteCount(), 0L);
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadPath() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path directory = new DriveDirectoryFeature(session, fileid).mkdir(new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new DriveTouchFeature(session, fileid).touch(new Path(directory, String.format("t %s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(0, new DriveAttributesFinderFeature(session, fileid).find(file).getSize());
        final CountingInputStream in = new CountingInputStream(new DriveReadFeature(session, fileid).read(file, new TransferStatus(), new DisabledConnectionCallback()));
        in.close();
        assertEquals(0L, in.getByteCount(), 0L);
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(file, directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadEmpty() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path directory = new DriveDirectoryFeature(session, fileid).mkdir(new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new DriveTouchFeature(session, fileid).touch(new Path(directory, String.format("t %s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(0, new DriveAttributesFinderFeature(session, fileid).find(file).getSize());
        final CountingInputStream in = new CountingInputStream(new DriveReadFeature(session, fileid).read(file, new TransferStatus(), new DisabledConnectionCallback()));
        in.close();
        assertEquals(0L, in.getByteCount(), 0L);
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(file, directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadCloseReleaseEntity() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final TransferStatus status = new TransferStatus();
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus writeStatus = new TransferStatus();
        writeStatus.setLength(content.length);
        final Path directory = new DriveDirectoryFeature(session, fileid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final DriveWriteFeature writer = new DriveWriteFeature(session, fileid);
        final HttpResponseOutputStream<File> out = writer.write(test, writeStatus, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(writeStatus, writeStatus).transfer(new ByteArrayInputStream(content), out);
        final CountingInputStream in = new CountingInputStream(new DriveReadFeature(session, fileid).read(test, status, new DisabledConnectionCallback()));
        in.close();
        assertEquals(0L, in.getByteCount(), 0L);
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(test, directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadRevision() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path directory = new DriveDirectoryFeature(session, fileid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(1645);
        {
            final TransferStatus status = new TransferStatus().withLength(content.length);
            final DriveWriteFeature writer = new DriveWriteFeature(session, fileid);
            final HttpResponseOutputStream<File> out = writer.write(test, status, new DisabledConnectionCallback());
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        }
        final Path versioned = new DriveVersioningFeature(session, fileid).list(test, new DisabledListProgressListener()).find(new SimplePathPredicate(test));
        assertNotNull(versioned.attributes().getVersionId());
        assertTrue(versioned.attributes().isDuplicate());
        assertEquals(content.length, versioned.attributes().getSize());
        assertArrayEquals(content, IOUtils.readFully(new DriveReadFeature(session, fileid).read(versioned, new TransferStatus(), new DisabledConnectionCallback()), content.length));
        // New version
        {
            final byte[] newcontent = RandomUtils.nextBytes(1045);
            final TransferStatus status = new TransferStatus().withLength(newcontent.length);
            final DriveWriteFeature writer = new DriveWriteFeature(session, fileid);
            final HttpResponseOutputStream<File> out = writer.write(test, status.exists(true), new DisabledConnectionCallback());
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(newcontent), out);
        }
        assertEquals(2, new DriveVersioningFeature(session, fileid).list(test, new DisabledListProgressListener()).size());
        // Permanently delete revision
        //new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(versioned), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertTrue(new DriveFindFeature(session, fileid).find(test));
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(test, directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
