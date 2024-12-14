package ch.cyberduck.core.storegate;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class StoregateIdProviderTest extends AbstractStoregateTest {

    @Test
    public void getFileIdFile() throws Exception {
        final StoregateIdProvider nodeid = new StoregateIdProvider(session);
        final Path home = new Path("/My files", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = String.format("%s", new AlphanumericRandomStringService().random());
        final Path file = new StoregateTouchFeature(session, nodeid).touch(new Path(home, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        nodeid.clear();
        final String nodeId = nodeid.getFileId(new Path(home, name, EnumSet.of(Path.Type.file)));
        assertNotNull(nodeId);
        assertEquals(nodeId, nodeid.getFileId(new Path(home.withAttributes(PathAttributes.EMPTY), name, EnumSet.of(Path.Type.file))));
        nodeid.clear();
        assertEquals(nodeId, nodeid.getFileId(new Path(home, StringUtils.upperCase(name), EnumSet.of(Path.Type.file))));
        nodeid.clear();
        assertEquals(nodeId, nodeid.getFileId(new Path(home, StringUtils.lowerCase(name), EnumSet.of(Path.Type.file))));
        try {
            assertNull(nodeid.getFileId(new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file))));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}