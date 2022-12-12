package ch.cyberduck.core.eue;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
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
public class EueFindFeatureTest extends AbstractEueSessionTest {

    @Test
    public void testFindRoot() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final EueFindFeature f = new EueFindFeature(session, fileid);
        assertTrue(f.find(new Path("/", EnumSet.of(Path.Type.volume, Path.Type.directory))));
    }

    @Test
    public void testFindNotFound() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final Path test = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final EueFindFeature f = new EueFindFeature(session, fileid);
        assertFalse(f.find(test));
    }

    @Test
    public void testFindTrash() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final Path directory = new Path("Gelöschte Dateien", EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        assertTrue(new EueFindFeature(session, fileid).find(directory, new DisabledListProgressListener()));
    }

    @Test
    public void testFindDirectory() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final Path folder = new EueDirectoryFeature(session, fileid).mkdir(
                new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new EueFindFeature(session, fileid).find(folder));
        assertFalse(new EueFindFeature(session, fileid).find(new Path(folder.getAbsolute(), EnumSet.of(Path.Type.file))));
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindFile() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final Path file = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new EueTouchFeature(session, fileid).touch(file, new TransferStatus());
        assertTrue(new EueFindFeature(session, fileid).find(file));
        assertFalse(new EueFindFeature(session, fileid).find(new Path(file.getAbsolute(), EnumSet.of(Path.Type.directory))));
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFind() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final Path folder1 = new EueDirectoryFeature(session, fileid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertEquals(folder1.attributes().getFileId(), new EueResourceIdProvider(session).getFileId(folder1));
        assertTrue(new EueFindFeature(session, fileid).find(folder1, new DisabledListProgressListener()));
        // Test case insensitivity
        assertTrue(new EueFindFeature(session, fileid).find(new Path(StringUtils.lowerCase(folder1.getName()), EnumSet.of(Path.Type.directory)), new DisabledListProgressListener()));
        assertTrue(new EueFindFeature(session, fileid).find(new Path(StringUtils.upperCase(folder1.getName()), EnumSet.of(Path.Type.directory)), new DisabledListProgressListener()));
        assertTrue(new DefaultFindFeature(session).find(folder1, new DisabledListProgressListener()));
        final Path folder1Folder2 = new EueDirectoryFeature(session, fileid).mkdir(new Path(folder1,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertEquals(folder1Folder2.attributes().getFileId(), new EueResourceIdProvider(session).getFileId(folder1Folder2));
        assertTrue(new EueFindFeature(session, fileid).find(folder1Folder2, new DisabledListProgressListener()));
        assertTrue(new DefaultFindFeature(session).find(folder1Folder2, new DisabledListProgressListener()));
        final Path folder1Folder2Folder3 = new EueDirectoryFeature(session, fileid).mkdir(new Path(folder1Folder2,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertEquals(folder1Folder2Folder3.attributes().getFileId(), new EueResourceIdProvider(session).getFileId(folder1Folder2Folder3));
        assertTrue(new EueFindFeature(session, fileid).find(folder1Folder2Folder3, new DisabledListProgressListener()));
        assertTrue(new DefaultFindFeature(session).find(folder1Folder2Folder3, new DisabledListProgressListener()));
        final Path folder2TestFile = createFile(fileid, new Path(folder1Folder2, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), RandomUtils.nextBytes(124));
        assertEquals(folder2TestFile.attributes().getFileId(), new EueResourceIdProvider(session).getFileId(folder2TestFile));
        assertTrue(new EueFindFeature(session, fileid).find(folder2TestFile, new DisabledListProgressListener()));
        // Test case insensitivity
        assertTrue(new EueFindFeature(session, fileid).find(new Path(folder2TestFile.getParent(), StringUtils.lowerCase(folder2TestFile.getName()), EnumSet.of(Path.Type.file)), new DisabledListProgressListener()));
        assertTrue(new EueFindFeature(session, fileid).find(new Path(folder2TestFile.getParent(), StringUtils.upperCase(folder2TestFile.getName()), EnumSet.of(Path.Type.file)), new DisabledListProgressListener()));
        assertTrue(new DefaultFindFeature(session).find(folder2TestFile, new DisabledListProgressListener()));
        final Path folder3TestFile = createFile(fileid, new Path(folder1Folder2Folder3, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), RandomUtils.nextBytes(1024));
        assertEquals(folder3TestFile.attributes().getFileId(), new EueResourceIdProvider(session).getFileId(folder3TestFile));
        assertTrue(new EueFindFeature(session, fileid).find(folder3TestFile, new DisabledListProgressListener()));
        assertTrue(new DefaultFindFeature(session).find(folder3TestFile, new DisabledListProgressListener()));
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(folder1), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse((new EueFindFeature(session, fileid).find(folder1, new DisabledListProgressListener())));
    }
}
