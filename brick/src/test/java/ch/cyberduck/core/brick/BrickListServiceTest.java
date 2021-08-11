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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static ch.cyberduck.core.AbstractPath.Type.directory;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class BrickListServiceTest extends AbstractBrickTest {

    @Test
    public void testListRoot() throws Exception {
        final AttributedList<Path> list = new BrickListService(session).list(
            new Path("/", EnumSet.of(directory)), new DisabledListProgressListener());
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test
    public void testList() throws Exception {
        final Path directory = new BrickDirectoryFeature(session).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        final AttributedList<Path> list = new BrickListService(session).list(directory, new DisabledListProgressListener());
        assertNotNull(list);
        assertTrue(list.isEmpty());
        new BrickDeleteFeature(session).delete(Collections.singletonList(directory), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}
