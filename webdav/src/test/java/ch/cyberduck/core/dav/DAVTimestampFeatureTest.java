package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
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
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@Category(IntegrationTest.class)
public class DAVTimestampFeatureTest extends AbstractDAVTest {

    @Test
    public void testSetTimestamp() throws Exception {
        final TransferStatus status = new TransferStatus();
        final Path file = new DAVTouchFeature(session).touch(new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), status);
        new DAVTimestampFeature(session).setTimestamp(file, status.withModified(5100L));
        final PathAttributes attr = new DAVAttributesFinderFeature(session).find(file);
        assertEquals(5000L, attr.getModificationDate());
        assertEquals(status.getResponse(), attr);
        assertEquals(5000L, new DefaultAttributesFinderFeature(session).find(file).getModificationDate());
        new DAVDeleteFeature(session).delete(Collections.<Path>singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSetTimestampFolderExplicitImplicit() throws Exception {
        final Path folder = new DAVDirectoryFeature(session).mkdir(new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        new DAVTimestampFeature(session).setTimestamp(folder, 5100L);
        assertEquals(5000L, new DAVAttributesFinderFeature(session).find(folder).getModificationDate());
        assertEquals(5000L, new DefaultAttributesFinderFeature(session).find(folder).getModificationDate());
        Thread.sleep(1000L);
        final Path file = new DAVTouchFeature(session).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotEquals(5000L, new DAVAttributesFinderFeature(session).find(folder).getModificationDate());
        assertNotEquals(5000L, new DefaultAttributesFinderFeature(session).find(folder).getModificationDate());
        new DAVDeleteFeature(session).delete(Arrays.asList(file, folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
