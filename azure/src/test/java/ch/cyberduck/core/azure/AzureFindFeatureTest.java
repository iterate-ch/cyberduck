package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class AzureFindFeatureTest extends AbstractAzureTest{

    @Test
    public void testFindNotFound() throws Exception {
        assertFalse(new AzureFindFeature(session).find(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testFindHome() throws Exception {
        assertTrue(new AzureFindFeature(session).find(new DefaultHomeFinderService(session).find()));
    }

    @Test
    public void testFindDirectory() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new AzureDirectoryFeature(session).mkdir(
                new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new AzureFindFeature(session).find(folder));
        assertFalse(new AzureFindFeature(session).find(new Path(folder.getAbsolute(), EnumSet.of(Path.Type.file))));
        new AzureDeleteFeature(session).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindFile() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new AzureTouchFeature(session).touch(file, new TransferStatus());
        assertTrue(new AzureFindFeature(session).find(file));
        assertFalse(new AzureFindFeature(session).find(new Path(file.getAbsolute(), EnumSet.of(Path.Type.directory))));
        new AzureDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}