package ch.cyberduck.core.owncloud;

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
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.dav.DAVDirectoryFeature;
import ch.cyberduck.core.dav.DAVMetadataFeature;
import ch.cyberduck.core.dav.DAVTouchFeature;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.nextcloud.NextcloudWriteFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class OwncloudMetadataFeatureTest extends AbstractOwncloudTest {

    @Test
    public void testSetMetadataFile() throws Exception {
        final Path test = new DAVTouchFeature(session).touch(new NextcloudWriteFeature(session), new Path(new DefaultHomeFinderService(session).find(),
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final DAVMetadataFeature feature = new DAVMetadataFeature(session);
        final String v1 = new AlphanumericRandomStringService().random();
        feature.setMetadata(test, new TransferStatus().setMetadata(Collections.singletonMap("Test1", v1)));
        this.assertMetadata(test, Collections.singletonMap("Test1", v1));
        // Add key
        final Map<String, String> update = new HashMap<>(feature.getMetadata(test));
        final String v2 = new AlphanumericRandomStringService().random();
        update.put("Test2", v2);
        feature.setMetadata(test, new TransferStatus().setMetadata(update));
        this.assertMetadata(test, update);
        // Remove all
        feature.setMetadata(test, new TransferStatus().setMetadata(Collections.emptyMap()));
        this.assertMetadata(test, Collections.emptyMap());
        new OwncloudDeleteFeature(session).delete(Collections.singletonMap(test, new TransferStatus()), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSetMetadataFolder() throws Exception {
        final Path test = new DAVDirectoryFeature(session).mkdir(new NextcloudWriteFeature(session), new Path(new DefaultHomeFinderService(session).find(),
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final String v = new AlphanumericRandomStringService().random();
        new DAVMetadataFeature(session).setMetadata(test, new TransferStatus().setMetadata(Collections.singletonMap("Test", v)));
        this.assertMetadata(test, Collections.singletonMap("Test", v));
        new OwncloudDeleteFeature(session).delete(Collections.singletonMap(test, new TransferStatus()), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMetadataRetainedOnOverwrite() throws Exception {
        final Path test = new DAVTouchFeature(session).touch(new NextcloudWriteFeature(session), new Path(new DefaultHomeFinderService(session).find(),
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String v = new AlphanumericRandomStringService().random();
        new DAVMetadataFeature(session).setMetadata(test, new TransferStatus().setMetadata(Collections.singletonMap("Test", v)));
        final byte[] content = RandomUtils.nextBytes(128);
        final TransferStatus status = new TransferStatus().setLength(content.length).setExists(true);
        final HttpResponseOutputStream<?> out = new NextcloudWriteFeature(session).write(test, status, ConnectionCallback.noop);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        assertEquals(content.length, new OwncloudAttributesFinderFeature(session).find(test).getSize());
        this.assertMetadata(test, Collections.singletonMap("Test", v));
        new OwncloudDeleteFeature(session).delete(Collections.singletonMap(test, new TransferStatus()), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    /**
     * Assert metadata is found with metadata feature, attributes finder and listing of parent directory
     */
    private void assertMetadata(final Path file, final Map<String, String> expected) throws Exception {
        assertEquals(expected, new DAVMetadataFeature(session).getMetadata(file));
        assertEquals(expected, new OwncloudAttributesFinderFeature(session).find(file).getMetadata());
        final Path found = new OwncloudRootListService(session).list(file.getParent(), new DisabledListProgressListener()).find(new SimplePathPredicate(file));
        assertNotNull(found);
        assertEquals(expected, found.attributes().getMetadata());
    }

    @Test
    public void testComparisonAfterSetMetadata() throws Exception {
        final Path test = new DAVTouchFeature(session).touch(new NextcloudWriteFeature(session),
                new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new OwncloudRootListService(session).features(test.getParent()).contains(ListService.Flags.metadata));
        final PathAttributes before = new OwncloudRootListService(session).list(test.getParent(), new DisabledListProgressListener())
                .find(new SimplePathPredicate(test)).attributes();
        new DAVMetadataFeature(session).setMetadata(test.withAttributes(before), Collections.singletonMap("user_tags", "Red"));
        final Path found = new OwncloudRootListService(session).list(test.getParent(), new DisabledListProgressListener())
                .find(new SimplePathPredicate(test));
        assertEquals("Red", found.attributes().getMetadata().get("user_tags"));
        // Change of metadata only not detected by comparison service. Requires comparing metadata from listing
        assertEquals(Comparison.equal, session.getFeature(ComparisonService.class).compare(Path.Type.file, before, found.attributes()));
        new OwncloudDeleteFeature(session).delete(Collections.singletonList(found), LoginCallback.noop, new Delete.DisabledCallback());
    }
}
