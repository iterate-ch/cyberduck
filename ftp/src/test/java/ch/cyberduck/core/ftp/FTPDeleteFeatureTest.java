package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.worker.DeleteWorker;

import org.junit.Test;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class FTPDeleteFeatureTest extends AbstractFTPTest {

    @Test
    public void testDeleteNotFound() throws Exception {
        final Path test = new Path(new FTPWorkdirService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        try {
            new FTPDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
            fail();
        }
        catch(NotfoundException | AccessDeniedException e) {
            //
        }
    }

    @Test
    public void testDeleteDirectory() throws Exception {
        final Path test = new Path(new FTPWorkdirService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new FTPDirectoryFeature(session).mkdir(new FTPWriteFeature(session), test, new TransferStatus());
        new FTPDeleteFeature(session).delete(Collections.singletonList(test), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testFeatures() throws Exception {
        final Path test = new Path(new FTPWorkdirService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        // Only ProFTPD with mod_site_misc advertises SITE RMDIR
        assertEquals(server == FTPTestServer.PROFTPD, session.getClient().hasFeature("SITE", "RMDIR"));
        assertEquals(server == FTPTestServer.PROFTPD, new FTPDeleteFeature(session).features(test).contains(Delete.Flags.recursive));
    }

    @Test
    public void testDeleteDirectoryRecursive() throws Exception {
        final Path folder = new Path(new FTPWorkdirService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final FTPDeleteFeature feature = new FTPDeleteFeature(session);
        assumeTrue(feature.features(folder).contains(Delete.Flags.recursive));
        new FTPDirectoryFeature(session).mkdir(new FTPWriteFeature(session), folder, new TransferStatus());
        final Path file = new FTPTouchFeature(session).touch(new FTPWriteFeature(session), new Path(folder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), new TransferStatus());
        // Delete non-empty folder without deleting contents first
        feature.delete(Collections.singletonList(folder), LoginCallback.noop, new Delete.DisabledCallback());
        assertFalse(new FTPFindFeature(session).find(file));
        assertFalse(new FTPFindFeature(session).find(folder));
    }

    @Test
    public void testDeleteDirectoryWithContents() throws Exception {
        final Path folder = new Path(new FTPWorkdirService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new FTPDirectoryFeature(session).mkdir(new FTPWriteFeature(session), folder, new TransferStatus());
        final Path file = new FTPTouchFeature(session).touch(new FTPWriteFeature(session), new Path(folder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new DeleteWorker(LoginCallback.noop, Collections.singletonList(folder), ProgressListener.noop).run(session);
        assertFalse(new FTPFindFeature(session).find(file));
        assertFalse(new FTPFindFeature(session).find(folder));
    }
}
