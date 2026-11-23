package ch.cyberduck.core.box;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.box.io.swagger.client.model.File;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Metadata;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
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

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class BoxMetadataFeatureTest extends AbstractBoxTest {

    @Test
    public void testSetMetadataFile() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final Path test = new BoxTouchFeature(session, fileid).touch(new BoxWriteFeature(session, fileid),
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final BoxMetadataFeature feature = new BoxMetadataFeature(session, fileid);
        assertTrue(feature.getMetadata(test).isEmpty());
        final Map<String, String> metadata = new HashMap<>();
        metadata.put("k1", "v1");
        metadata.put("k2", "v2");
        // Create instance
        feature.setMetadata(test, metadata);
        assertEquals(metadata, feature.getMetadata(test));
        // Update instance with add, replace and remove
        final Map<String, String> update = new HashMap<>();
        update.put("k1", "v1-updated");
        update.put("k3", "v3");
        feature.setMetadata(test, update);
        assertEquals(update, feature.getMetadata(test));
        // Remove instance
        feature.setMetadata(test, Collections.emptyMap());
        assertTrue(feature.getMetadata(test).isEmpty());
        new BoxDeleteFeature(session, fileid).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testSetMetadataFolder() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final Path directory = new BoxDirectoryFeature(session, fileid).mkdir(new BoxWriteFeature(session, fileid), new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        final BoxMetadataFeature feature = new BoxMetadataFeature(session, fileid);
        assertTrue(feature.getMetadata(directory).isEmpty());
        final Map<String, String> metadata = Collections.singletonMap("k", "v");
        feature.setMetadata(directory, metadata);
        assertEquals(metadata, feature.getMetadata(directory));
        new BoxDeleteFeature(session, fileid).delete(Collections.singletonList(directory), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testSetMetadataFoundWithAttributesFinderAndListService() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final Path folder = new BoxDirectoryFeature(session, fileid).mkdir(new BoxWriteFeature(session, fileid), new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        final String t = new AlphanumericRandomStringService().random();
        final Path test = new BoxTouchFeature(session, fileid).touch(new BoxWriteFeature(session, fileid),
                new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final BoxMetadataFeature feature = new BoxMetadataFeature(session, fileid);
        feature.setMetadata(test, Collections.singletonMap("touch", t));
        final Map<String, String> metadata = new HashMap<>(feature.getMetadata(test));
        assertEquals(Collections.singletonMap("touch", t), metadata);
        final String v = new AlphanumericRandomStringService().random();
        metadata.put("test", v);
        feature.setMetadata(test, new TransferStatus().setMetadata(metadata));
        final Map<String, String> updated = feature.getMetadata(test);
        assertEquals(metadata, updated);
        assertEquals(updated, new BoxAttributesFinderFeature(session, fileid).find(test).getMetadata());
        final Path found = new BoxListService(session, fileid).list(folder, new DisabledListProgressListener()).find(new SimplePathPredicate(test));
        assertNotNull(found);
        assertEquals(updated, found.attributes().getMetadata());
        new BoxDeleteFeature(session, fileid).delete(Collections.singletonList(folder), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testWriteWithMetadata() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final Path folder = new BoxDirectoryFeature(session, fileid).mkdir(new BoxWriteFeature(session, fileid), new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        final Path test = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final String t = new AlphanumericRandomStringService().random();
        final byte[] content = RandomUtils.nextBytes(128);
        final TransferStatus status = new TransferStatus().setLength(content.length).setMetadata(Collections.singletonMap("write", t));
        status.setChecksum(session.getFeature(Upload.class).checksum(test, status).compute(new ByteArrayInputStream(content), status));
        final HttpResponseOutputStream<File> out = new BoxWriteFeature(session, fileid).write(test, status, ConnectionCallback.noop);
        new StreamCopier(new TransferStatus(), new TransferStatus()).transfer(new ByteArrayInputStream(content), out);
        // Metadata not set with upload but with separate request in upload filter
        assertFalse(new BoxWriteFeature(session, fileid).features(test).contains(Write.Flags.metadata));
        assertTrue(new BoxMetadataFeature(session, fileid).getMetadata(test).isEmpty());
        new BoxMetadataFeature(session, fileid).setMetadata(test, status.getMetadata());
        assertEquals(t, new BoxAttributesFinderFeature(session, fileid).find(test).getMetadata().get("write"));
        final Path found = new BoxListService(session, fileid).list(folder, new DisabledListProgressListener()).find(new SimplePathPredicate(test));
        assertNotNull(found);
        assertEquals(t, found.attributes().getMetadata().get("write"));
        new BoxDeleteFeature(session, fileid).delete(Collections.singletonList(folder), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testSetMetadataTooLarge() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final Path test = new BoxTouchFeature(session, fileid).touch(new BoxWriteFeature(session, fileid),
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final BoxMetadataFeature feature = new BoxMetadataFeature(session, fileid);
        // Rejected between 256 KiB and 1 MiB
        final Map<String, String> metadata = Collections.singletonMap("k", StringUtils.repeat('a', 1024 * 1024));
        assertThrows(InteroperabilityException.class, () -> feature.setMetadata(test, metadata));
        new BoxDeleteFeature(session, fileid).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testComparisonAfterSetMetadata() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final Path test = new BoxTouchFeature(session, fileid).touch(new BoxWriteFeature(session, fileid),
                new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new BoxListService(session, fileid).features(test.getParent()).contains(ListService.Flags.metadata));
        final PathAttributes before = new BoxListService(session, fileid).list(test.getParent(), new DisabledListProgressListener())
                .find(new SimplePathPredicate(test)).attributes();
        new BoxMetadataFeature(session, fileid).setMetadata(test.withAttributes(before), Collections.singletonMap("user_tags", "Red"));
        final Path found = new BoxListService(session, fileid).list(test.getParent(), new DisabledListProgressListener())
                .find(new SimplePathPredicate(test));
        assertEquals("Red", found.attributes().getMetadata().get("user_tags"));
        // Change of metadata only not detected by comparison service. Requires comparing metadata from listing
        assertEquals(Comparison.equal, session.getFeature(ComparisonService.class).compare(Path.Type.file, before, found.attributes()));
        new BoxDeleteFeature(session, fileid).delete(Collections.singletonList(found), LoginCallback.noop, new Delete.DisabledCallback());
    }
}
