package ch.cyberduck.cli;

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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.local.FlatTemporaryFileService;
import ch.cyberduck.core.preferences.TemporarySupportDirectoryFinder;
import ch.cyberduck.core.transfer.TransferItem;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

public class UploadTransferItemFinderTest {

    @Test
    public void testResolveFolderToFolder() {
        final Local temp = new TemporarySupportDirectoryFinder().find();
        final Path folder = new Path("/d", EnumSet.of(Path.Type.directory));
        assertEquals(folder, UploadTransferItemFinder.resolve(folder, temp, false).remote);
        assertEquals(temp, UploadTransferItemFinder.resolve(folder, temp, false).local);
        assertEquals(new Path(folder, temp.getName(), EnumSet.of(Path.Type.directory)), UploadTransferItemFinder.resolve(folder, temp, true).remote);
        assertEquals(temp, UploadTransferItemFinder.resolve(folder, temp, true).local);
    }

    @Test
    public void testResolveFileToFile() {
        final Local temp = new FlatTemporaryFileService().create(new AlphanumericRandomStringService().random());
        final Path file = new Path("/f", EnumSet.of(Path.Type.file));
        final TransferItem item = UploadTransferItemFinder.resolve(file, temp, false);
        assertEquals(file, item.remote);
        assertEquals(temp, item.local);
    }

    @Test
    public void testResolveFileToFolder() {
        final Local temp = new FlatTemporaryFileService().create(new AlphanumericRandomStringService().random());
        final Path folder = new Path("/d", EnumSet.of(Path.Type.directory));
        final TransferItem item = UploadTransferItemFinder.resolve(folder, temp, false);
        assertEquals(new Path("/d/" + temp.getName(), EnumSet.of(Path.Type.file)), item.remote);
        assertEquals(temp, item.local);
    }
}