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
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.dav.DAVAttributesFinderFeature;
import ch.cyberduck.core.dav.DAVDirectoryFeature;
import ch.cyberduck.core.dav.DAVFindFeature;
import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.dav.DAVUploadFeature;
import ch.cyberduck.core.dav.DAVWriteFeature;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class CopyWorkerTest {

    @Test
    public void testCopyFile() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
            System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path home = new DefaultHomeFinderService(session).find();
        final Path source = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path target = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DefaultTouchFeature<>(new DAVUploadFeature(new DAVWriteFeature(session)),
            new DAVAttributesFinderFeature(session)).touch(source, new TransferStatus());
        assertTrue(new DAVFindFeature(session).find(source, new DisabledListProgressListener()));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new DAVFindFeature(session).find(source, new DisabledListProgressListener()));
        assertTrue(new DAVFindFeature(session).find(target, new DisabledListProgressListener()));
        new DeleteWorker(new DisabledLoginCallback(), Arrays.asList(source, target), new DisabledProgressListener()).run(session);
        session.close();
    }

    @Test
    public void testCopyFileToDirectory() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
            System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path home = new DefaultHomeFinderService(session).find();
        final Path sourceFile = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DefaultTouchFeature<>(new DAVUploadFeature(new DAVWriteFeature(session)),
            new DAVAttributesFinderFeature(session)).touch(sourceFile, new TransferStatus());
        assertTrue(new DAVFindFeature(session).find(sourceFile, new DisabledListProgressListener()));
        final Path targetFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path targetFile = new Path(targetFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new DAVDirectoryFeature(session).mkdir(targetFolder, null, new TransferStatus());
        assertTrue(new DAVFindFeature(session).find(targetFolder, new DisabledListProgressListener()));
        // copy file into vault
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(sourceFile, targetFile), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new DAVFindFeature(session).find(sourceFile, new DisabledListProgressListener()));
        assertTrue(new DAVFindFeature(session).find(targetFile, new DisabledListProgressListener()));
        new DeleteWorker(new DisabledLoginCallback(), Arrays.asList(sourceFile, targetFolder), new DisabledProgressListener()).run(session);
        session.close();
    }

    @Test
    public void testCopyDirectory() throws Exception {
        final Host host = new Host(new DAVProtocol(), "test.cyberduck.ch", new Credentials(
            System.getProperties().getProperty("webdav.user"), System.getProperties().getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path home = new DefaultHomeFinderService(session).find();
        final Path folder = new DAVDirectoryFeature(session).mkdir(new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final Path sourceFile = new DefaultTouchFeature<>(new DAVUploadFeature(new DAVWriteFeature(session)),
            new DAVAttributesFinderFeature(session)).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new DAVFindFeature(session).find(folder, new DisabledListProgressListener()));
        assertTrue(new DAVFindFeature(session).find(sourceFile, new DisabledListProgressListener()));
        final Path targetFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path targetFile = new Path(targetFolder, sourceFile.getName(), EnumSet.of(Path.Type.file));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(folder, targetFolder), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new DAVFindFeature(session).find(targetFolder, new DisabledListProgressListener()));
        assertTrue(new DAVFindFeature(session).find(targetFile, new DisabledListProgressListener()));
        assertTrue(new DAVFindFeature(session).find(folder, new DisabledListProgressListener()));
        assertTrue(new DAVFindFeature(session).find(sourceFile, new DisabledListProgressListener()));
        new DeleteWorker(new DisabledLoginCallback(), Arrays.asList(folder, targetFile), new DisabledProgressListener()).run(session);
        session.close();
    }
}
