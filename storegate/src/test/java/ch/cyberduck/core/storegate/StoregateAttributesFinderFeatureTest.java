package ch.cyberduck.core.storegate;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DefaultPathPredicate;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.storegate.io.swagger.client.model.RootFolder;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class StoregateAttributesFinderFeatureTest extends AbstractStoregateTest {

    @Test
    public void testFindRoot() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final StoregateAttributesFinderFeature f = new StoregateAttributesFinderFeature(session, nodeid);
        assertNotNull(f.find(new Path("/", EnumSet.of(Path.Type.directory))));
        assertNotEquals(PathAttributes.EMPTY, f.find(new Path("/", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testDefaultPaths() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        for(Path container : new StoregateListService(session, nodeid).list(Home.ROOT, new DisabledListProgressListener())) {
            assertEquals(container.attributes(), new StoregateAttributesFinderFeature(session, nodeid).find(container));
        }
        for(RootFolder root : session.roots()) {
            assertNotEquals(PathAttributes.EMPTY, new StoregateAttributesFinderFeature(session, nodeid).find(new Path(root.getPath(), EnumSet.of(Path.Type.directory))));
        }
    }

    @Test
    public void testFind() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path room = new StoregateDirectoryFeature(session, nodeid).mkdir(
                new Path(String.format("/My files/%s", new AlphanumericRandomStringService().random()),
                        EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        assertTrue(room.attributes().getPermission().isExecutable());
        final Path test = new StoregateTouchFeature(session, nodeid).touch(
                new Path(room, String.format("%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file)), new TransferStatus());
        final PathAttributes attr = new StoregateAttributesFinderFeature(session, nodeid).find(test);
        assertEquals(attr, new StoregateAttributesFinderFeature(session, nodeid).find(new Path(test.getParent(), StringUtils.upperCase(test.getName()), test.getType())));
        assertEquals(attr, new StoregateAttributesFinderFeature(session, nodeid).find(new Path(test.getParent(), StringUtils.lowerCase(test.getName()), test.getType())));
        assertNotEquals(0L, attr.getModificationDate());
        assertEquals(Checksum.NONE, attr.getChecksum());
        assertNull(attr.getETag());
        assertNotNull(attr.getFileId());
        assertFalse(attr.getPermission().isExecutable());
        assertTrue(attr.getPermission().isReadable());
        assertTrue(attr.getPermission().isWritable());
        final Path list = new StoregateListService(session, nodeid).list(room, new DisabledListProgressListener())
                .find(new DefaultPathPredicate(test));
        assertEquals(attr, list.attributes());
        new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testChangedNodeId() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path room = new StoregateDirectoryFeature(session, nodeid).mkdir(
                new Path(String.format("/My files/%s", new AlphanumericRandomStringService().random()),
                        EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new StoregateTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String latestnodeid = test.attributes().getFileId();
        assertNotNull(latestnodeid);
        // Assume previously seen but changed on server
        nodeid.cache(test, String.valueOf(RandomUtils.nextLong()));
        final StoregateAttributesFinderFeature f = new StoregateAttributesFinderFeature(session, nodeid);
        assertEquals(latestnodeid, f.find(test).getFileId());
        new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
