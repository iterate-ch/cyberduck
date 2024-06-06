package ch.cyberduck.core.deepbox;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

@Category(IntegrationTest.class)
public class DeepboxDirectoryFeatureTest extends AbstractDeepboxTest {

    @Test
    public void testCreateDirectory() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final DeepboxDirectoryFeature directory = new DeepboxDirectoryFeature(session, nodeid);
        // Fails with  'Cannot create folder abcd.', detail='Api access to box denied for deepbox-desktop-app-int.', cause='ch.cyberduck.core.deepbox.io.swagger.client.ApiException: {"timestamp":"2024-06-06T05:45:22.449+00:00","status":403,"error":"Forbidden","message":"api access to box denied for deepbox-desktop-app-int"}'}
        final Path p = new Path("/Mountainduck Buddies/My Box/Documents/Bookkeeping/abcd", EnumSet.of(Path.Type.directory));
        directory.mkdir(p, new TransferStatus());
        /*assertTrue(new StoregateFindFeature(session, nodeid).find(folder));
        // Can create again regardless if exists
        new StoregateDirectoryFeature(session, nodeid).mkdir(folder, new TransferStatus());
        new StoregateDeleteFeature(session, nodeid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DefaultFindFeature(session).find(folder));*/
    }
}