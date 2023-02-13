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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.googledrive.AbstractDriveTest;
import ch.cyberduck.core.googledrive.DriveAttributesFinderFeature;
import ch.cyberduck.core.googledrive.DriveDirectoryFeature;
import ch.cyberduck.core.googledrive.DriveFileIdProvider;
import ch.cyberduck.core.googledrive.DriveFindFeature;
import ch.cyberduck.core.googledrive.DriveHomeFinderService;
import ch.cyberduck.core.googledrive.DriveTouchFeature;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DeleteWorkerTest extends AbstractDriveTest {

    @Test
    public void testDelete() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path folder = new DriveDirectoryFeature(session, fileid).mkdir(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new DriveFindFeature(session, fileid).find(folder));
        final Path file = new DriveTouchFeature(session, fileid).touch(
                new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new DriveFindFeature(session, fileid).find(file));
        final DeleteWorker worker = new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(folder), new DisabledProgressListener(), true);
        int hashCode = worker.hashCode();
        worker.run(session);
        assertEquals(hashCode, worker.hashCode());
        assertTrue(new DriveFindFeature(session, fileid).find(file));
        assertTrue(new DriveAttributesFinderFeature(session, fileid).find(file, new DisabledListProgressListener()).isHidden());
        assertTrue(new DefaultFindFeature(session).find(file));
        assertTrue(new DefaultAttributesFinderFeature(session).find(file, new DisabledListProgressListener()).isHidden());
        assertFalse(new DriveFindFeature(session, fileid).find(file.withAttributes(PathAttributes.EMPTY)));
        assertFalse(new DefaultFindFeature(session).find(file.withAttributes(PathAttributes.EMPTY)));
    }
}
