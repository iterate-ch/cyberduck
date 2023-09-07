/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

package ch.cyberduck.core.storegate;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.time.Instant;
import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class StoregateTimestampFeatureTest extends AbstractStoregateTest {

    @Test
    public void testSetTimestamp() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path room = new StoregateDirectoryFeature(session, nodeid).mkdir(new Path(
            String.format("/My files/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path file = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new StoregateTouchFeature(session, nodeid).touch(file, new TransferStatus().withMime("x-application/cyberduck"));
        assertNotNull(new StoregateAttributesFinderFeature(session, nodeid).find(file));
        final long modified = Instant.now().minusSeconds(5 * 24 * 60 * 60).getEpochSecond() * 1000;
        final TransferStatus status = new TransferStatus().withModified(modified);
        new StoregateTimestampFeature(session, nodeid).setTimestamp(file, status);
        final PathAttributes attr = new StoregateAttributesFinderFeature(session, nodeid).find(file);
        assertEquals(modified, attr.getModificationDate());
        assertEquals(modified, new DefaultAttributesFinderFeature(session).find(file).getModificationDate());
        assertEquals(attr, status.getResponse());
        new StoregateDeleteFeature(session, nodeid).delete(Arrays.asList(file, room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSetTimestampDirectory() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path room = new StoregateDirectoryFeature(session, nodeid).mkdir(new Path(
            String.format("/My files/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new StoregateDirectoryFeature(session, nodeid).mkdir(test, null);
        assertNotNull(new StoregateAttributesFinderFeature(session, nodeid).find(test));
        final long modified = Instant.now().minusSeconds(5 * 24 * 60 * 60).getEpochSecond() * 1000;
        new StoregateTimestampFeature(session, nodeid).setTimestamp(test, modified);
        assertEquals(modified, new StoregateAttributesFinderFeature(session, nodeid).find(test).getModificationDate());
        new StoregateDeleteFeature(session, nodeid).delete(Arrays.asList(test, room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
