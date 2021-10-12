package ch.cyberduck.core.gmxcloud;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static ch.cyberduck.core.AbstractPath.Type.directory;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class GmxcloudListServiceTest extends AbstractGmxcloudTest {

    @Test
    public void existing_resource_returns_non_empty_list() throws Exception {
        GmxcloudIdProvider fileid = new GmxcloudIdProvider(session);
        final Path folder = new Path("/TestFolderToDelete", EnumSet.of(directory));
        new GmxcloudDirectoryFeature(session, fileid).mkdir(folder, new TransferStatus());
        final Path folder2 = new Path("/TestFolderToDelete/Tester2", EnumSet.of(directory));
        new GmxcloudDirectoryFeature(session, fileid).mkdir(folder2, new TransferStatus());
        final AttributedList<Path> list = new GmxcloudListService(session, fileid).list(folder, new DisabledListProgressListener());
        assertNotNull(list);
        assertFalse(list.isEmpty());
        new GmxcloudDeleteFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse((new GmxcloudFindFeature(session, fileid).find(folder2, new DisabledListProgressListener())));
    }

    @Test(expected = NotfoundException.class)
    public void nonexisting_resource_returns_empty_list() throws Exception {
        GmxcloudIdProvider fileid = new GmxcloudIdProvider(session);
        final AttributedList<Path> list = new GmxcloudListService(session, fileid).list(new Path("NON_EXISTING_CONTAINER", EnumSet.of(directory)), new DisabledListProgressListener());
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }


}
