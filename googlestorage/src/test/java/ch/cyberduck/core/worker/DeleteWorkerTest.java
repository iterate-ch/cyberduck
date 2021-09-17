package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.googlestorage.AbstractGoogleStorageTest;
import ch.cyberduck.core.googlestorage.GoogleStorageDirectoryFeature;
import ch.cyberduck.core.googlestorage.GoogleStorageFindFeature;
import ch.cyberduck.core.googlestorage.GoogleStorageTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class DeleteWorkerTest extends AbstractGoogleStorageTest {

    @Test
    public void testMoveFolder() throws Exception {
        final Path home = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new GoogleStorageDirectoryFeature(session).mkdir(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new GoogleStorageFindFeature(session).find(folder));
        final Path file = new GoogleStorageTouchFeature(session).touch(
                new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new GoogleStorageFindFeature(session).find(file));
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(folder), PathCache.empty(), new DisabledProgressListener()).run(session);
        assertFalse(new GoogleStorageFindFeature(session).find(file));
        assertFalse(new GoogleStorageFindFeature(session).find(folder));
    }
}
