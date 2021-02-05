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
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;

import static ch.cyberduck.core.googledrive.DriveFileidProvider.KEY_FILE_ID;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveWriteFeatureTest extends AbstractDriveTest {

    @Test
    public void testWrite() throws Exception {
        final Path test = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final DriveFileidProvider idProvider = new DriveFileidProvider(session).withCache(cache);
        String fileid;
        {
            final TransferStatus status = new TransferStatus();
            status.setMime("x-application/cyberduck");
            final byte[] content = RandomUtils.nextBytes(2048);
            status.setLength(content.length);
            final HttpResponseOutputStream<VersionId> out = new DriveWriteFeature(session, idProvider).write(test, status, new DisabledConnectionCallback());
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
            fileid = out.getStatus().id;
            assertNotNull(fileid);
            assertTrue(new DefaultFindFeature(session).find(test));
            test.attributes().setCustom(Collections.emptyMap());
            final PathAttributes attributes = new DriveListService(session, idProvider).list(test.getParent(), new DisabledListProgressListener()).get(test).attributes();
            assertEquals(fileid, attributes.getCustom().get(KEY_FILE_ID));
            assertEquals(content.length, attributes.getSize());
            final Write.Append append = new DriveWriteFeature(session, idProvider).append(test, status.getLength(), PathCache.empty());
            assertTrue(append.override);
            assertEquals(content.length, append.size, 0L);
            final byte[] buffer = new byte[content.length];
            final InputStream in = new DriveReadFeature(session, idProvider).read(test, new TransferStatus(), new DisabledConnectionCallback());
            IOUtils.readFully(in, buffer);
            in.close();
            assertArrayEquals(content, buffer);
            assertEquals("x-application/cyberduck", session.getClient().files().get(test.attributes().getCustom().get(KEY_FILE_ID)).execute().getMimeType());
        }
        {
            // overwrite
            final TransferStatus status = new TransferStatus();
            status.setMime("x-application/cyberduck");
            status.setExists(true);
            final byte[] content = RandomUtils.nextBytes(1024);
            status.setLength(content.length);
            final HttpResponseOutputStream<VersionId> out = new DriveWriteFeature(session, idProvider).write(test, status, new DisabledConnectionCallback());
            new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
            assertEquals(fileid, out.getStatus().id);
            final PathAttributes attributes = new DriveListService(session, idProvider).list(test.getParent(), new DisabledListProgressListener()).get(test).attributes();
            assertEquals(content.length, attributes.getSize());
            assertEquals("x-application/cyberduck", session.getClient().files().get(test.attributes().getCustom().get(KEY_FILE_ID)).execute().getMimeType());
        }
        new DriveDeleteFeature(session, idProvider).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWritePreviuoslyTrashed() throws Exception {
        final DriveFileidProvider fileid = new DriveFileidProvider(session).withCache(cache);
        final Path test = new DriveTouchFeature(session, fileid).touch(new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertTrue(new DriveFindFeature(session, fileid).find(test));
        assertTrue(new DriveAttributesFinderFeature(session, fileid).find(test).isDuplicate());
        assertTrue(new DefaultFindFeature(session).find(test));
        assertTrue(new DefaultAttributesFinderFeature(session).find(test).isDuplicate());
        new DriveTouchFeature(session, fileid).touch(test, new TransferStatus());
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}
