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
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.ftp.AbstractFTPTest;
import ch.cyberduck.core.ftp.FTPDirectoryFeature;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.ftp.FTPTouchFeature;
import ch.cyberduck.core.ftp.FTPWriteFeature;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class CopyWorkerTest extends AbstractFTPTest {

    @Test
    public void testCopyFile() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path source = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path target = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new FTPTouchFeature(session).touch(new FTPWriteFeature(session), source, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(source));
        final FTPSession copySession = new FTPSession(new Host(session.getHost()).setCredentials(new Credentials("test", "test")));
        copySession.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), CancelCallback.noop);
        copySession.login(new DisabledLoginCallback(), CancelCallback.noop);
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(copySession), PathCache.empty(), ProgressListener.noop, ConnectionCallback.noop);
        worker.run(session);
        assertTrue(new DefaultFindFeature(session).find(source));
        assertTrue(new DefaultFindFeature(session).find(target));
        new DeleteWorker(new DisabledLoginCallback(), Arrays.asList(source, target), ProgressListener.noop).run(session);
    }

    @Test
    public void testCopyFileToDirectory() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path sourceFile = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new FTPTouchFeature(session).touch(new FTPWriteFeature(session), sourceFile, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(sourceFile));
        final Path targetFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path targetFile = new Path(targetFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new FTPDirectoryFeature(session).mkdir(new FTPWriteFeature(session), targetFolder, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(targetFolder));
        // copy file into vault
        final FTPSession copySession = new FTPSession(new Host(session.getHost()).setCredentials(new Credentials("test", "test")));
        copySession.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), CancelCallback.noop);
        copySession.login(new DisabledLoginCallback(), CancelCallback.noop);
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(sourceFile, targetFile), new SessionPool.SingleSessionPool(copySession), PathCache.empty(), ProgressListener.noop, ConnectionCallback.noop);
        worker.run(session);
        assertTrue(new DefaultFindFeature(session).find(sourceFile));
        assertTrue(new DefaultFindFeature(session).find(targetFile));
        new DeleteWorker(new DisabledLoginCallback(), Arrays.asList(sourceFile, targetFolder), ProgressListener.noop).run(session);
    }

    @Test
    public void testCopyDirectory() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path folder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path sourceFile = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new FTPDirectoryFeature(session).mkdir(new FTPWriteFeature(session), folder, new TransferStatus());
        new FTPTouchFeature(session).touch(new FTPWriteFeature(session), sourceFile, new TransferStatus());
        assertTrue(new DefaultFindFeature(session).find(folder));
        assertTrue(new DefaultFindFeature(session).find(sourceFile));
        // move directory into vault
        final Path targetFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path targetFile = new Path(targetFolder, sourceFile.getName(), EnumSet.of(Path.Type.file));
        final FTPSession copySession = new FTPSession(new Host(session.getHost()).setCredentials(new Credentials("test", "test")));
        copySession.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), CancelCallback.noop);
        copySession.login(new DisabledLoginCallback(), CancelCallback.noop);
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(folder, targetFolder), new SessionPool.SingleSessionPool(copySession), PathCache.empty(), ProgressListener.noop, ConnectionCallback.noop);
        worker.run(session);
        assertTrue(new DefaultFindFeature(session).find(targetFolder));
        assertTrue(new DefaultFindFeature(session).find(targetFile));
        assertTrue(new DefaultFindFeature(session).find(folder));
        assertTrue(new DefaultFindFeature(session).find(sourceFile));
        new DeleteWorker(new DisabledLoginCallback(), Arrays.asList(folder, targetFolder), ProgressListener.noop).run(session);
    }
}
