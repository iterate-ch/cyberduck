package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.googledrive.AbstractDriveTest;
import ch.cyberduck.core.googledrive.DriveDeleteFeature;
import ch.cyberduck.core.googledrive.DriveDirectoryFeature;
import ch.cyberduck.core.googledrive.DriveFileIdProvider;
import ch.cyberduck.core.googledrive.DriveHomeFinderService;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;
import ch.cyberduck.ui.browser.DefaultBrowserFilter;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import com.google.api.services.drive.model.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class ListWorkerTest extends AbstractDriveTest {

    @Test
    public void testRun() throws Exception {
        final String f1 = new AlphanumericRandomStringService().random();
        final String f2 = new AlphanumericRandomStringService().random();
        final DriveFileIdProvider fileidProvider = new DriveFileIdProvider(session);
        final Path parent = new DriveDirectoryFeature(session, fileidProvider).mkdir(
            new Path(DriveHomeFinderService.MYDRIVE_FOLDER, f1, EnumSet.of(Path.Type.directory)), new TransferStatus());
        Path folder = new DriveDirectoryFeature(session, fileidProvider).mkdir(
            new Path(parent, f2, EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(folder));
        {
            // trash folder and recreate it
            final String fileid = fileidProvider.getFileId(folder);
            final File body = new File();
            body.set("trashed", true);
            session.getClient().files().update(fileid, body).execute();
            folder = new DriveDirectoryFeature(session, fileidProvider).mkdir(folder, new TransferStatus());
            final PathCache cache = new PathCache(10);
            final SessionListWorker worker = new SessionListWorker(cache, parent, new DisabledListProgressListener());
            final AttributedList<Path> list = worker.run(session);
            assertEquals(2, list.size());
            worker.cleanup(list);
            assertTrue(cache.containsKey(parent));
            final AttributedList<Path> l = cache.get(parent);
            assertEquals(1, l.filter(new DefaultBrowserFilter()).size());
            assertEquals(2, l.size());
        }
        {
            // trash recreated folder
            final String fileid = fileidProvider.getFileId(folder);
            final File body = new File();
            body.set("trashed", true);
            session.getClient().files().update(fileid, body).execute();
            final PathCache cache = new PathCache(10);
            final SessionListWorker worker = new SessionListWorker(cache, parent, new DisabledListProgressListener());
            final AttributedList<Path> list = worker.run(session);
            assertEquals(2, list.size());
            worker.cleanup(list);
            assertTrue(cache.containsKey(parent));
            final AttributedList<Path> l = cache.get(parent);
            assertEquals(0, l.filter(new DefaultBrowserFilter()).size());
            assertEquals(2, l.size());
        }
        new DriveDeleteFeature(session, fileidProvider).delete(Collections.singletonList(parent), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
