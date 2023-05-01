package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.local.FlatTemporaryFileService;
import ch.cyberduck.core.preferences.TemporarySupportDirectoryFinder;
import ch.cyberduck.core.transfer.TransferItem;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class DownloadTransferItemFinderTest {

    @Test
    public void testResolveFolderToFolder() {
        final Local temp = new TemporarySupportDirectoryFinder().find();
        final Path folder = new Path("/d", EnumSet.of(Path.Type.directory));
        final TransferItem item = DownloadTransferItemFinder.resolve(folder, temp);
        assertEquals(folder, item.remote);
        assertEquals(LocalFactory.get(temp, "d"), item.local);
    }

    @Test
    public void testResolveFileToFile() {
        final Local temp = new FlatTemporaryFileService().create(new AlphanumericRandomStringService().random());
        final Path file = new Path("/f", EnumSet.of(Path.Type.file));
        final TransferItem item = DownloadTransferItemFinder.resolve(file, temp);
        assertEquals(file, item.remote);
        assertEquals(temp, item.local);
    }

    @Test
    public void testResolveFileToFolder() {
        final Local temp = new TemporarySupportDirectoryFinder().find();
        final Path file = new Path("/f", EnumSet.of(Path.Type.file));
        final TransferItem item = DownloadTransferItemFinder.resolve(file, temp);
        assertEquals(file, item.remote);
        assertEquals(LocalFactory.get(temp, file.getName()), item.local);
    }

    @Test
    public void testResolveGlobPatternToFolder() {
        final Local temp = new TemporarySupportDirectoryFinder().find();
        final Path file = new Path("/*", EnumSet.of(Path.Type.file));
        final TransferItem item = DownloadTransferItemFinder.resolve(file, temp);
        assertEquals(new Path("/", EnumSet.of(Path.Type.directory)), item.remote);
        assertEquals(temp, item.local);
    }
}