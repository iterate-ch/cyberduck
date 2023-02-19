package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import com.google.api.services.drive.model.File;

import static ch.cyberduck.core.googledrive.DriveHomeFinderService.MYDRIVE_FOLDER;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveVersioningFeatureTest extends AbstractDriveTest {

    @Test
    public void testList() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path room = new DriveDirectoryFeature(session, fileid).mkdir(
                new Path(MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final DriveAttributesFinderFeature attr = new DriveAttributesFinderFeature(session, fileid);
        final Path test = new DriveTouchFeature(session, fileid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(test.attributes().getVersionId(), attr.find(test).getVersionId());
        final DriveVersioningFeature feature = new DriveVersioningFeature(session, fileid);
        assertEquals(0, feature.list(test, new DisabledListProgressListener()).size());
        final PathAttributes initialAttributes = new PathAttributes(test.attributes());
        {
            final byte[] content = RandomUtils.nextBytes(32769);
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length);
            status.setExists(true);
            final DriveWriteFeature writer = new DriveWriteFeature(session, fileid);
            final StatusOutputStream<File> out = writer.write(test, status, new DisabledConnectionCallback());
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
            assertNull(attr.toAttributes(out.getStatus()).getVersionId());
            final AttributedList<Path> versions = feature.list(test.withAttributes(attr.toAttributes(out.getStatus())), new DisabledListProgressListener());
            assertEquals(1, versions.size());
            assertEquals(initialAttributes.getChecksum(), versions.get(0).attributes().getChecksum());
            final PathAttributes updated = attr.find(test.withAttributes(attr.toAttributes(out.getStatus())));
            assertNotEquals(initialAttributes.getChecksum(), updated.getChecksum());
        }
        {
            final byte[] content = RandomUtils.nextBytes(1647);
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length);
            status.setExists(true);
            final DriveWriteFeature writer = new DriveWriteFeature(session, fileid);
            final StatusOutputStream<File> out = writer.write(test, status, new DisabledConnectionCallback());
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
            assertNull(attr.toAttributes(out.getStatus()).getVersionId());
            final List<Path> versions = feature.list(test.withAttributes(attr.toAttributes(out.getStatus())), new DisabledListProgressListener()).toList();
            assertEquals(32769L, versions.get(0).attributes().getSize());
            assertEquals(0L, versions.get(1).attributes().getSize());
        }
        // Delete versions permanently
        final AttributedList<Path> versions = feature.list(test.withAttributes(attr.find(test)), new DisabledListProgressListener());
        for(Path d : versions.toList()) {
            assertTrue(new DriveThresholdDeleteFeature(session, fileid).isSupported(d));
            assertTrue(new DriveBatchDeleteFeature(session, fileid).isSupported(d));
            assertTrue(new DriveDeleteFeature(session, fileid).isSupported(d));
        }
        new DriveDeleteFeature(session, fileid).delete(versions.toList(), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        for(Path version : new DriveListService(session, fileid).list(room, new DisabledListProgressListener())) {
            new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(version), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}