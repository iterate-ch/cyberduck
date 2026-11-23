package ch.cyberduck.core.googledrive;

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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.api.services.drive.model.File;

import static ch.cyberduck.core.googledrive.DriveHomeFinderService.MYDRIVE_FOLDER;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveMetadataFeatureTest extends AbstractDriveTest {

    @Test
    public void setMetadata() throws Exception {
        final Path test = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final TransferStatus status = new TransferStatus();
        new DriveTouchFeature(session, fileid).touch(new DriveWriteFeature(session, fileid), test, status);
        final DriveMetadataFeature feature = new DriveMetadataFeature(session, fileid);
        assertEquals(Collections.emptyMap(), feature.getMetadata(test));
        feature.setMetadata(test, Collections.singletonMap("test", "t"));
        final Map<String, String> metadata = feature.getMetadata(test);
        assertEquals(Collections.singletonMap("test", "t"), metadata);
        test.attributes().setMetadata(metadata);
        feature.setMetadata(test, status.setMetadata(Collections.emptyMap()));
        assertFalse(status.getResponse().getMetadata().containsKey("test"));
        assertFalse(feature.getMetadata(test).containsKey("test"));
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testChangedFileId() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path room = new DriveDirectoryFeature(session, fileid).mkdir(
                new DriveWriteFeature(session, fileid), new Path(MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path test = new DriveTouchFeature(session, fileid).touch(new DriveWriteFeature(session, fileid), new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String latestfileid = test.attributes().getFileId();
        assertNotNull(latestfileid);
        // Assume previously seen but changed on server
        fileid.cache(test, String.valueOf(RandomUtils.nextLong()));
        final DriveMetadataFeature f = new DriveMetadataFeature(session, fileid);
        try {
            f.getMetadata(test);
        }
        catch(NotfoundException e) {
            assertNull(test.attributes().getFileId());
        }
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(room), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testSetMetadataFoundWithAttributesFinderAndListService() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path folder = new DriveDirectoryFeature(session, fileid).mkdir(new DriveWriteFeature(session, fileid),
                new Path(MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final String t = new AlphanumericRandomStringService().random();
        final Path test = new DriveTouchFeature(session, fileid).touch(new DriveWriteFeature(session, fileid),
                new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)),
                new TransferStatus().setMetadata(Collections.singletonMap("touch", t)));
        assertEquals(t, test.attributes().getMetadata().get("touch"));
        final DriveMetadataFeature feature = new DriveMetadataFeature(session, fileid);
        final Map<String, String> metadata = new HashMap<>(feature.getMetadata(test));
        assertEquals(t, metadata.get("touch"));
        final String v = new AlphanumericRandomStringService().random();
        metadata.put("test", v);
        feature.setMetadata(test, new TransferStatus().setMetadata(metadata));
        final Map<String, String> updated = feature.getMetadata(test);
        assertEquals(v, updated.get("test"));
        assertEquals(t, updated.get("touch"));
        final PathAttributes attributes = new DriveAttributesFinderFeature(session, fileid).find(test);
        assertEquals(updated, attributes.getMetadata());
        final AttributedList<Path> list = new DriveListService(session, fileid).list(folder, new DisabledListProgressListener());
        final Path found = list.find(new SimplePathPredicate(test));
        assertNotNull(found);
        assertEquals(updated, found.attributes().getMetadata());
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(folder), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testWriteWithMetadata() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path folder = new DriveDirectoryFeature(session, fileid).mkdir(new DriveWriteFeature(session, fileid),
                new Path(MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path test = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final String t = new AlphanumericRandomStringService().random();
        final byte[] content = RandomUtils.nextBytes(128);
        final TransferStatus status = new TransferStatus().setLength(content.length).setMetadata(Collections.singletonMap("write", t));
        final HttpResponseOutputStream<File> out = new DriveWriteFeature(session, fileid).write(test, status, ConnectionCallback.noop);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        assertEquals(t, new DriveAttributesFinderFeature(session, fileid).toAttributes(out.getStatus()).getMetadata().get("write"));
        assertEquals(t, new DriveMetadataFeature(session, fileid).getMetadata(test).get("write"));
        assertEquals(t, new DriveAttributesFinderFeature(session, fileid).find(test).getMetadata().get("write"));
        final Path found = new DriveListService(session, fileid).list(folder, new DisabledListProgressListener()).find(new SimplePathPredicate(test));
        assertNotNull(found);
        assertEquals(t, found.attributes().getMetadata().get("write"));
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(folder), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testSetMetadataTooLarge() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path test = new DriveTouchFeature(session, fileid).touch(new DriveWriteFeature(session, fileid),
                new Path(DriveHomeFinderService.MYDRIVE_FOLDER, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DriveMetadataFeature feature = new DriveMetadataFeature(session, fileid);
        final Map<String, String> metadata = Collections.singletonMap("k", StringUtils.repeat('a', 124));
        // Same limit as when writing metadata with upload
        assertThrows(InteroperabilityException.class, () -> feature.setMetadata(test, metadata));
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testComparisonAfterSetMetadata() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path test = new DriveTouchFeature(session, fileid).touch(new DriveWriteFeature(session, fileid),
                new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new DriveListService(session, fileid).features(test.getParent()).contains(ListService.Flags.metadata));
        final PathAttributes before = new DriveListService(session, fileid).list(test.getParent(), new DisabledListProgressListener())
                .find(new SimplePathPredicate(test)).attributes();
        new DriveMetadataFeature(session, fileid).setMetadata(test.withAttributes(before), Collections.singletonMap("user_tags", "Red"));
        final Path found = new DriveListService(session, fileid).list(test.getParent(), new DisabledListProgressListener())
                .find(new SimplePathPredicate(test));
        assertEquals("Red", found.attributes().getMetadata().get("user_tags"));
        // Change of metadata only not detected by comparison service. Requires comparing metadata from listing
        assertEquals(Comparison.equal, session.getFeature(ComparisonService.class).compare(Path.Type.file, before, found.attributes()));
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(found), LoginCallback.noop, new Delete.DisabledCallback());
    }
}
