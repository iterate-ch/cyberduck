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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class EueDeleteFeatureTest extends AbstractEueSessionTest {

    @Test
    public void testDeleteFolder() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final Path directory = new EueDirectoryFeature(session, fileid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        assertTrue(new EueFindFeature(session, fileid).find(directory, new DisabledListProgressListener()));
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse((new EueFindFeature(session, fileid).find(directory, new DisabledListProgressListener())));
    }

    @Test
    public void testDeleteFile() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final Path folder = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory));
        final Path file1 = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path file2 = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new EueDirectoryFeature(session, fileid).mkdir(folder, new TransferStatus());
        createFile(file1, RandomUtils.nextBytes(511));
        createFile(file2, RandomUtils.nextBytes(214));
        assertTrue(new EueFindFeature(session, fileid).find(file1));
        assertTrue(new EueFindFeature(session, fileid).find(file2));
        new EueDeleteFeature(session, fileid).delete(Arrays.asList(file1, file2), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse((new EueFindFeature(session, fileid).find(file1, new DisabledListProgressListener())));
        assertFalse((new EueFindFeature(session, fileid).find(file2, new DisabledListProgressListener())));
        assertTrue(new EueFindFeature(session, fileid).find(folder, new DisabledListProgressListener()));
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse((new EueFindFeature(session, fileid).find(folder, new DisabledListProgressListener())));
    }

    @Test(expected = NotfoundException.class)
    public void testNotfound() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final Path file = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        fail();
    }

    @Test(expected = NotfoundException.class)
    public void testDoubleDelete() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final Path file = new EueTouchFeature(session, fileid).touch(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String resourceId = file.attributes().getFileId();
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        try {
            new EueDeleteFeature(session, fileid).delete(Collections.singletonList(
                    file.withAttributes(new PathAttributes().withFileId(resourceId))), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
        catch(NotfoundException e) {
            fail();
        }
        try {
            new EueDeleteFeature(session, fileid, false).delete(Collections.singletonList(
                    file.withAttributes(new PathAttributes().withFileId(resourceId))), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
        catch(NotfoundException e) {
            fail();
        }
        try {
            new EueDeleteFeature(session, fileid, false).delete(Collections.singletonList(
                    file.withAttributes(new PathAttributes().withFileId(resourceId))), new DisabledLoginCallback(), new Delete.DisabledCallback());
            fail();
        }
        catch(NotfoundException e) {
            assertEquals("Error. NOT_FOUND. Please contact your web hosting service provider for assistance.", e.getDetail());
            throw e;
        }
    }
}
