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
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import com.google.api.services.drive.model.File;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveWriteFeatureTest extends AbstractDriveTest {

    @Test
    public void testWrite() throws Exception {
        final DriveFileIdProvider idProvider = new DriveFileIdProvider(session);
        final Path folder = new DriveDirectoryFeature(session, idProvider).mkdir(
                new Path(DriveHomeFinderService.MYDRIVE_FOLDER, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path test = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        String fileid;
        {
            final TransferStatus status = new TransferStatus();
            status.setMime("x-application/cyberduck");
            status.setTimestamp(1620113107725L);
            final byte[] content = RandomUtils.nextBytes(2048);
            status.setLength(content.length);
            final HttpResponseOutputStream<File> out = new DriveWriteFeature(session, idProvider).write(test, status, new DisabledConnectionCallback());
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
            fileid = out.getStatus().getId();
            assertNotNull(fileid);
            assertTrue(new DefaultFindFeature(session).find(test));
            assertEquals(status.getTimestamp(), new DriveAttributesFinderFeature(session, idProvider).toAttributes(out.getStatus()).getModificationDate(), 0L);
            final PathAttributes attributes = new DriveAttributesFinderFeature(session, idProvider).find(test);
            assertEquals(new DriveAttributesFinderFeature(session, idProvider).toAttributes(out.getStatus()), attributes);
            assertEquals(fileid, attributes.getFileId());
            assertEquals(1620113107725L, attributes.getModificationDate());
            assertEquals(content.length, attributes.getSize());
            final Write.Append append = new DriveWriteFeature(session, idProvider).append(test, status.withRemote(new DriveAttributesFinderFeature(session, idProvider).find(test)));
            assertFalse(append.append);
            assertEquals(content.length, append.size, 0L);
            final byte[] buffer = new byte[content.length];
            final InputStream in = new DriveReadFeature(session, idProvider).read(test, new TransferStatus(), new DisabledConnectionCallback());
            IOUtils.readFully(in, buffer);
            in.close();
            assertArrayEquals(content, buffer);
            assertEquals("x-application/cyberduck", session.getClient().files().get(test.attributes().getFileId()).execute().getMimeType());
        }
        {
            // overwrite
            final TransferStatus status = new TransferStatus();
            status.setMime("x-application/cyberduck");
            status.setTimestamp(System.currentTimeMillis());
            status.setExists(true);
            final byte[] content = RandomUtils.nextBytes(1024);
            status.setLength(content.length);
            final HttpResponseOutputStream<File> out = new DriveWriteFeature(session, idProvider).write(test, status, new DisabledConnectionCallback());
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
            assertEquals(fileid, out.getStatus().getId());
            final PathAttributes attributes = new DriveListService(session, idProvider).list(test.getParent(), new DisabledListProgressListener()).get(test).attributes();
            assertEquals(content.length, attributes.getSize());
            assertEquals("x-application/cyberduck", session.getClient().files().get(test.attributes().getFileId()).execute().getMimeType());
            assertEquals(new DriveAttributesFinderFeature(session, idProvider).toAttributes(out.getStatus()), attributes);
        }
        new DriveDeleteFeature(session, idProvider).delete(Arrays.asList(test, folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWritePreviuoslyTrashed() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path directory = new DriveDirectoryFeature(session, fileid).mkdir(
            new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path test = new DriveTouchFeature(session, fileid).touch(new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String id = test.attributes().getFileId();
        assertNotNull(id);
        new DriveTrashFeature(session, fileid).delete(Collections.singletonList(test), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertNull(test.attributes().getFileId());
        assertFalse(new DriveFindFeature(session, fileid).find(test));
        test.attributes().withFileId(id);
        assertTrue(new DriveFindFeature(session, fileid).find(test));
        assertTrue(new DefaultFindFeature(session).find(test));
        assertTrue(new DriveAttributesFinderFeature(session, fileid).find(test).isHidden());
        // Files with duplicate flag (trashed) are filtered
        assertFalse(new DefaultFindFeature(session).find(new Path(test).withAttributes(PathAttributes.EMPTY)));
        final Path upload = new DriveTouchFeature(session, fileid).touch(test, new TransferStatus());
        assertEquals(upload.attributes(), new DriveAttributesFinderFeature(session, fileid).find(upload));
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(test, directory), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}
