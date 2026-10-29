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
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.MD5ChecksumCompute;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.apache.commons.lang3.RandomUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;
import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveUploadFeatureTest extends AbstractDriveTest {

    @Test
    public void testWrite() throws Exception {
        final TransferStatus status = new TransferStatus();
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = "test".getBytes(StandardCharsets.UTF_8);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        IOUtils.closeQuietly(out);
        status.setLength(content.length);
        final Path test = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final DriveUploadFeature upload = new DriveUploadFeature();
        upload.upload(new DriveWriteFeature(session, fileid), test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), ProgressListener.noop, StreamListener.noop,
                status, ConnectionCallback.noop);
        test.attributes().setFileId(fileid.getFileId(test));
        assertTrue(session.getFeature(Find.class).find(test));
        assertEquals(content.length, new DriveListService(session, fileid).list(test.getParent(), new DisabledListProgressListener()).get(test).attributes().getSize(), 0L);
        assertEquals(content.length, upload.append(test, status.setRemote(new DriveAttributesFinderFeature(session, fileid).find(test))).offset, 0L);
        {
            final byte[] buffer = new byte[content.length];
            IOUtils.readFully(new DriveReadFeature(session, fileid).read(test, new TransferStatus(), ConnectionCallback.noop), buffer);
            assertArrayEquals(content, buffer);
        }
        {
            final byte[] buffer = new byte[content.length - 1];
            final InputStream in = new DriveReadFeature(session, fileid).read(test, new TransferStatus().setLength(content.length).setAppend(true).setOffset(1L), ConnectionCallback.noop);
            IOUtils.readFully(in, buffer);
            IOUtils.closeQuietly(in);
            final byte[] reference = new byte[content.length - 1];
            System.arraycopy(content, 1, reference, 0, content.length - 1);
            assertArrayEquals(reference, buffer);
        }
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(test), LoginCallback.noop,
            new Delete.DisabledCallback());
    }

    @Test
    public void testUploadVerifyChecksum() throws Exception {
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = RandomUtils.nextBytes(1025);
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        IOUtils.closeQuietly(out);
        final Path test = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final DriveUploadFeature upload = new DriveUploadFeature();
        final TransferStatus status = new TransferStatus().setLength(content.length);
        assertTrue(upload.features(test, status).contains(Upload.Flags.checksumResponse));
        assertEquals(HashAlgorithm.md5, upload.checksum(test, status).getAlgorithm());
        // Calculated while streaming
        status.setVerify(upload.checksum(test, status).getAlgorithm());
        upload.upload(new DriveWriteFeature(session, fileid), test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), ProgressListener.noop, StreamListener.noop,
                status, ConnectionCallback.noop);
        test.attributes().setFileId(fileid.getFileId(test));
        final Checksum expected = new MD5ChecksumCompute().compute(new ByteArrayInputStream(content), new TransferStatus());
        assertEquals(expected, status.getDigest());
        // Checksum returned by the server
        assertEquals(expected, status.getResponse().getChecksum());
        assertEquals(expected, new DriveAttributesFinderFeature(session, fileid).find(test).getChecksum());
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
        local.delete();
    }
}
