package ch.cyberduck.core.onedrive;

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
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.onedrive.features.GraphDeleteFeature;
import ch.cyberduck.core.onedrive.features.GraphDirectoryFeature;
import ch.cyberduck.core.onedrive.features.GraphFileIdProvider;
import ch.cyberduck.core.onedrive.features.GraphTouchFeature;
import ch.cyberduck.core.onedrive.features.GraphVersioningFeature;
import ch.cyberduck.core.onedrive.features.GraphWriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.nuxeo.onedrive.client.types.DriveItem;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class OneDriveVersioningFeatureTest extends AbstractOneDriveTest {

    @Test
    public void testList() throws Exception {
        final GraphFileIdProvider fileid = new GraphFileIdProvider(session);
        final Path room = new GraphDirectoryFeature(session, fileid).mkdir(
                new Path(new OneDriveHomeFinderService().find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new GraphTouchFeature(session, fileid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNull(test.attributes().getVersionId());
        final GraphVersioningFeature feature = new GraphVersioningFeature(session, fileid);
        assertEquals(0, feature.list(test, new DisabledListProgressListener()).size());
        // Add initial content
        {
            final byte[] content = RandomUtils.nextBytes(213);
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length);
            status.setExists(true);
            final GraphWriteFeature writer = new GraphWriteFeature(session, fileid);
            final StatusOutputStream<DriveItem.Metadata> out = writer.write(test, status, new DisabledConnectionCallback());
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        }
        assertEquals(test.attributes().getFileId(), new GraphAttributesFinderFeature(session, fileid).find(test).getFileId());
        assertEquals(0, feature.list(test, new DisabledListProgressListener()).size());
        final PathAttributes initialAttributes = new PathAttributes(test.attributes());
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setExists(true);
        final GraphWriteFeature writer = new GraphWriteFeature(session, fileid);
        final StatusOutputStream<DriveItem.Metadata> out = writer.write(test, status, new DisabledConnectionCallback());
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        assertNull(new GraphAttributesFinderFeature(session, fileid).toAttributes(out.getStatus()).getVersionId());
        final PathAttributes updated = new GraphAttributesFinderFeature(session, fileid).find(test);
        assertNotEquals(initialAttributes.getETag(), updated.getETag());
        {
            final AttributedList<Path> versions = feature.list(test, new DisabledListProgressListener());
            assertEquals(1, versions.size());
            assertEquals(213, versions.get(0).attributes().getSize(), 0L);
            feature.revert(versions.get(0));
        }
        // Delete versions permanently
        {
            final List<Path> versions = feature.list(test, new DisabledListProgressListener()).toList();
            assertEquals(2, versions.size());
            assertEquals(32769L, versions.get(0).attributes().getSize());
            assertEquals(213L, versions.get(1).attributes().getSize(), 0L);
            for(Path f : versions) {
                assertEquals(test.attributes().getFileId(), f.attributes().getFileId());
                assertFalse(new GraphDeleteFeature(session, fileid).isSupported(f));
            }
        }
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}