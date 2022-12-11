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
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

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
        final DriveUploadFeature upload = new DriveUploadFeature(session, fileid);
        upload.upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
            status, new DisabledConnectionCallback());
        test.attributes().setFileId(fileid.getFileId(test));
        assertTrue(session.getFeature(Find.class).find(test));
        assertEquals(content.length, new DriveListService(session, fileid).list(test.getParent(), new DisabledListProgressListener()).get(test).attributes().getSize(), 0L);
        assertEquals(content.length, new DriveWriteFeature(session, fileid).append(test, status.withRemote(new DriveAttributesFinderFeature(session, fileid).find(test))).size, 0L);
        {
            final byte[] buffer = new byte[content.length];
            IOUtils.readFully(new DriveReadFeature(session, fileid).read(test, new TransferStatus(), new DisabledConnectionCallback()), buffer);
            assertArrayEquals(content, buffer);
        }
        {
            final byte[] buffer = new byte[content.length - 1];
            final InputStream in = new DriveReadFeature(session, fileid).read(test, new TransferStatus().withLength(content.length).append(true).withOffset(1L), new DisabledConnectionCallback());
            IOUtils.readFully(in, buffer);
            IOUtils.closeQuietly(in);
            final byte[] reference = new byte[content.length - 1];
            System.arraycopy(content, 1, reference, 0, content.length - 1);
            assertArrayEquals(reference, buffer);
        }
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(),
            new Delete.DisabledCallback());
    }
}
