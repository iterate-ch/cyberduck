package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractDropboxTest;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class DropboxDirectoryFeatureTest extends AbstractDropboxTest {

    @Test
    public void testDirectory() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path level1 = new DropboxDirectoryFeature(session).mkdir(new Path(home,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null);
        assertTrue(new DefaultFindFeature(session).find(level1));
        assertTrue(new DropboxFindFeature(session).find(level1));
        assertThrows(ConflictException.class, () -> new DropboxDirectoryFeature(session).mkdir(level1, new TransferStatus()));
        final Path level2 = new DropboxDirectoryFeature(session).mkdir(new Path(level1,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null);
        assertTrue(new DefaultFindFeature(session).find(level2));
        assertTrue(new DropboxFindFeature(session).find(level2));
        new DropboxDeleteFeature(session).delete(Arrays.asList(level1), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
