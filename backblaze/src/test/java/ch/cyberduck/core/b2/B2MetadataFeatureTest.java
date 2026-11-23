package ch.cyberduck.core.b2;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2MetadataFeatureTest extends AbstractB2Test {

    @Test
    public void testSetMetadata() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path container = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String t = new AlphanumericRandomStringService().random();
        final Path test = new B2TouchFeature(session, fileid).touch(new B2WriteFeature(session, fileid),
                new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus()
                        .setModified(System.currentTimeMillis())
                        .setMetadata(Collections.singletonMap("touch", t)));
        final String versionId = test.attributes().getVersionId();
        final B2MetadataFeature feature = new B2MetadataFeature(session, fileid);
        final Map<String, String> metadata = new HashMap<>(feature.getMetadata(test));
        assertTrue(metadata.containsKey(B2MetadataFeature.X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS));
        assertEquals(t, metadata.get("touch"));
        final String modified = metadata.get(B2MetadataFeature.X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS);
        metadata.put("tags", "a,b");
        final TransferStatus status = new TransferStatus().setMetadata(metadata);
        feature.setMetadata(test, status);
        assertNotNull(status.getResponse());
        assertNotEquals(versionId, status.getResponse().getVersionId());
        assertEquals(status.getResponse().getVersionId(), fileid.getVersionId(new Path(test.getParent(), test.getName(), test.getType())));
        final Map<String, String> updated = feature.getMetadata(test);
        assertEquals("a,b", updated.get("tags"));
        assertEquals(t, updated.get("touch"));
        // Preserved
        assertEquals(modified, updated.get(B2MetadataFeature.X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS));
        final AttributedList<Path> list = new B2ObjectListService(session, fileid).list(container, new DisabledListProgressListener());
        final Path found = list.find(f -> f.getName().equals(test.getName()) && !f.attributes().isDuplicate());
        assertNotNull(found);
        assertEquals("a,b", found.attributes().getMetadata().get("tags"));
        assertEquals(t, found.attributes().getMetadata().get("touch"));
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testSetMetadataTooLarge() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path container = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new B2TouchFeature(session, fileid).touch(new B2WriteFeature(session, fileid),
                new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final B2MetadataFeature feature = new B2MetadataFeature(session, fileid);
        final Map<String, String> metadata = Collections.singletonMap("k", StringUtils.repeat('a', 8 * 1024));
        final TransferStatus status = new TransferStatus().setMetadata(metadata);
        // Same limit as when writing metadata with upload
        assertThrows(InteroperabilityException.class, () -> feature.setMetadata(test, status));
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testComparisonAfterSetMetadata() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path test = new B2TouchFeature(session, fileid).touch(new B2WriteFeature(session, fileid),
                new Path(new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume)), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new B2ListService(session, fileid).features(test.getParent()).contains(ListService.Flags.metadata));
        final PathAttributes before = new B2ListService(session, fileid).list(test.getParent(), new DisabledListProgressListener())
                .find(new SimplePathPredicate(test)).attributes();
        new B2MetadataFeature(session, fileid).setMetadata(test.withAttributes(before), Collections.singletonMap("user_tags", "Red"));
        final Path found = new B2ListService(session, fileid).list(test.getParent(), new DisabledListProgressListener())
                .find(new SimplePathPredicate(test));
        assertEquals("Red", found.attributes().getMetadata().get("user_tags"));
        // Change of metadata only not detected by comparison service. Requires comparing metadata from listing
        assertEquals(Comparison.equal, session.getFeature(ComparisonService.class).compare(Path.Type.file, before, found.attributes()));
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(found), LoginCallback.noop, new Delete.DisabledCallback());
    }
}
