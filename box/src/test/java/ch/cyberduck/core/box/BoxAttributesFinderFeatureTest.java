package ch.cyberduck.core.box;

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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class BoxAttributesFinderFeatureTest extends AbtractBoxTest {

    @Test(expected = NotfoundException.class)
    public void testFindNotFound() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final Path folder = new BoxDirectoryFeature(session, fileid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path test = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final BoxAttributesFinderFeature f = new BoxAttributesFinderFeature(session, fileid);
        try {
            f.find(test);
        }
        finally {
            new BoxDeleteFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }

    @Test
    public void testFindRoot() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final BoxAttributesFinderFeature f = new BoxAttributesFinderFeature(session, fileid);
        final PathAttributes attributes = f.find(new Path("/", EnumSet.of(Path.Type.volume, Path.Type.directory)));
        assertNotEquals(PathAttributes.EMPTY, attributes);
    }

    @Test
    public void testFindFile() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final Path folder = new BoxDirectoryFeature(session, fileid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final long folderModification = new BoxAttributesFinderFeature(session, fileid).find(folder).getModificationDate();
        final Path test = new BoxTouchFeature(session, fileid)
                .touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        assertEquals(folderModification, new BoxAttributesFinderFeature(session, fileid).find(folder).getModificationDate(), 0L);
        final BoxAttributesFinderFeature f = new BoxAttributesFinderFeature(session, fileid);
        final PathAttributes attributes = f.find(test);
        assertEquals(0L, attributes.getSize());
        assertNotEquals(-1L, attributes.getModificationDate());
        assertNotNull(attributes.getFileId());
        assertNotNull(attributes.getETag());
        assertNotNull(attributes.getChecksum().algorithm);
        assertTrue(attributes.getPermission().isReadable());
        assertTrue(attributes.getPermission().isWritable());
        // Test wrong type
        try {
            f.find(new Path(test.getAbsolute(), EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new BoxDeleteFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindDirectory() throws Exception {
        final BoxFileidProvider fileid = new BoxFileidProvider(session);
        final Path folder = new BoxDirectoryFeature(session, fileid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path test = new BoxDirectoryFeature(session, fileid).mkdir(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final BoxAttributesFinderFeature f = new BoxAttributesFinderFeature(session, fileid);
        final PathAttributes attributes = f.find(test);
        assertNotEquals(-1L, attributes.getSize());
        assertNotEquals(-1L, attributes.getModificationDate());
        assertNull(attributes.getChecksum().algorithm);
        assertNull(attributes.getETag());
        assertTrue(attributes.getPermission().isReadable());
        assertTrue(attributes.getPermission().isWritable());
        assertTrue(attributes.getPermission().isExecutable());
        // Test wrong type
        try {
            f.find(new Path(test.getAbsolute(), EnumSet.of(Path.Type.file)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new BoxDeleteFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
