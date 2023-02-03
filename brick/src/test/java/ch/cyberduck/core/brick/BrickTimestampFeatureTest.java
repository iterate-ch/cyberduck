package ch.cyberduck.core.brick;

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

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class BrickTimestampFeatureTest extends AbstractBrickTest {

    @Test
    public void testSetTimestampFile() throws Exception {
        final Path file = new BrickTouchFeature(session).touch(new Path(new DefaultHomeFinderService(session).find(),
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final TransferStatus status = new TransferStatus().withTimestamp(5000L);
        new BrickTimestampFeature(session).setTimestamp(file, status);
        final PathAttributes attr = new BrickAttributesFinderFeature(session).find(file);
        assertEquals(5000L, attr.getModificationDate());
        assertEquals(attr, status.getResponse());
        assertEquals(5000L, new DefaultAttributesFinderFeature(session).find(file).getModificationDate());
        new BrickDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSetTimestampDirectory() throws Exception {
        final Path file = new BrickDirectoryFeature(session).mkdir(new Path(new DefaultHomeFinderService(session).find(),
            new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new BrickTimestampFeature(session).setTimestamp(file, 5000L);
        assertEquals(5000L, new BrickAttributesFinderFeature(session).find(file).getModificationDate());
        assertEquals(5000L, new DefaultAttributesFinderFeature(session).find(file).getModificationDate());
        new BrickDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
