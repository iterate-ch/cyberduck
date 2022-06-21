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

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveVersioningFeatureTest extends AbstractDriveTest {

    @Test
    public void testList() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path room = new DriveDirectoryFeature(session, fileid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final DriveAttributesFinderFeature f = new DriveAttributesFinderFeature(session, fileid);
        final Path test = new DriveTouchFeature(session, fileid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(test.attributes().getVersionId(), new DriveAttributesFinderFeature(session, fileid).find(test).getVersionId());
        final DriveVersioningFeature feature = new DriveVersioningFeature(session, fileid);
        assertEquals(0, feature.list(test, new DisabledListProgressListener()).size());
        final PathAttributes initialAttributes = new PathAttributes(test.attributes());
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setExists(true);
        final DriveWriteFeature writer = new DriveWriteFeature(session, fileid);
        final StatusOutputStream<File> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        assertNull(new DriveAttributesFinderFeature(session, fileid).toAttributes(out.getStatus()).getVersionId());
        {
            final AttributedList<Path> versions = feature.list(test.withAttributes(new DriveAttributesFinderFeature(session, fileid).toAttributes(out.getStatus())), new DisabledListProgressListener());
            assertEquals(1, versions.size());
            assertEquals(initialAttributes.getChecksum(), versions.get(0).attributes().getChecksum());
        }
        final PathAttributes updated = new DriveAttributesFinderFeature(session, fileid).find(test.withAttributes(new DriveAttributesFinderFeature(session, fileid).toAttributes(out.getStatus())));
        assertNotEquals(initialAttributes.getChecksum(), updated.getChecksum());
        // Delete versions permanently
        final List<Path> files = feature.list(test, new DisabledListProgressListener()).toList();
        for(Path d : files) {
            assertTrue(new DriveThresholdDeleteFeature(session, fileid).isSupported(d));
            assertTrue(new DriveBatchDeleteFeature(session, fileid).isSupported(d));
            assertTrue(new DriveDeleteFeature(session, fileid).isSupported(d));
        }
        new DriveDeleteFeature(session, fileid).delete(files, new DisabledPasswordCallback(), new Delete.DisabledCallback());
        for(Path version : new DriveListService(session, fileid).list(room, new DisabledListProgressListener())) {
            new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(version), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}