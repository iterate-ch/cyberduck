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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class GmxcloudDirectoryFeatureTest extends AbstractGmxcloudTest {

    @Test
    public void testForRecursiveDirectoryCreation() throws Exception {
        GmxcloudIdProvider fileid = new GmxcloudIdProvider(session);
        final Path nestedFolderPath = new GmxcloudDirectoryFeature(session, fileid).mkdir(new Path("/Folder1/Folder2/Folder3", EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());

        final Path folder1Path = new Path("/Folder1", EnumSet.of(AbstractPath.Type.directory));
        assertTrue(new GmxcloudFindFeature(session, fileid).find(folder1Path, new DisabledListProgressListener()));
        assertTrue(new DefaultFindFeature(session).find(folder1Path, new DisabledListProgressListener()));

        assertTrue(new GmxcloudFindFeature(session, fileid).find(new Path("/Folder1/Folder2", EnumSet.of(AbstractPath.Type.directory)), new DisabledListProgressListener()));
        assertTrue(new DefaultFindFeature(session).find(new Path("/Folder1/Folder2", EnumSet.of(AbstractPath.Type.directory)), new DisabledListProgressListener()));

        assertTrue(new GmxcloudFindFeature(session, fileid).find(new Path("/Folder1/Folder2/Folder3", EnumSet.of(AbstractPath.Type.directory)), new DisabledListProgressListener()));
        assertTrue(new DefaultFindFeature(session).find(new Path("/Folder1/Folder2/Folder3", EnumSet.of(AbstractPath.Type.directory)), new DisabledListProgressListener()));

        new GmxcloudDeleteFeature(session, fileid).delete(Collections.singletonList(folder1Path), new DisabledLoginCallback(), new Delete.DisabledCallback());

        assertFalse((new GmxcloudFindFeature(session, fileid).find(nestedFolderPath, new DisabledListProgressListener())));
    }

}
