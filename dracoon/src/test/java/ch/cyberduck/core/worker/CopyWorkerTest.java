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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.sds.SDSDirectoryFeature;
import ch.cyberduck.core.sds.SDSFindFeature;
import ch.cyberduck.core.sds.SDSProtocol;
import ch.cyberduck.core.sds.SDSSession;
import ch.cyberduck.core.sds.SDSTouchFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.threading.BackgroundActionState;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultRegistry;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class CopyWorkerTest {

    @Test
    public void testCopyFile() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
                System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new SDSDirectoryFeature(session).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path source = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path target = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session).touch(source, new TransferStatus());
        assertTrue(new SDSFindFeature(session).find(source));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(source, target), new TestSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new SDSFindFeature(session).find(source));
        assertTrue(new SDSFindFeature(session).find(target));
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(room), PathCache.empty(), new DisabledProgressListener()).run(session);
        session.close();
    }

    @Test
    public void testCopyFileToDirectory() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
                System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new SDSDirectoryFeature(session).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path sourceFile = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session).touch(sourceFile, new TransferStatus());
        assertTrue(new SDSFindFeature(session).find(sourceFile));
        final Path targetFolder = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path targetFile = new Path(targetFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSDirectoryFeature(session).mkdir(targetFolder, null, new TransferStatus());
        assertTrue(new SDSFindFeature(session).find(targetFolder));
        // copy file into vault
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(sourceFile, targetFile), new TestSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new SDSFindFeature(session).find(sourceFile));
        assertTrue(new SDSFindFeature(session).find(targetFile));
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(room), PathCache.empty(), new DisabledProgressListener()).run(session);
        session.close();
    }

    @Test
    public void testCopyDirectory() throws Exception {
        final Host host = new Host(new SDSProtocol(), "duck.ssp-europe.eu", new Credentials(
                System.getProperties().getProperty("sds.user"), System.getProperties().getProperty("sds.key")
        ));
        final SDSSession session = new SDSSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path room = new SDSDirectoryFeature(session).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path folder = new SDSDirectoryFeature(session).mkdir(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        final Path sourceFile = new SDSTouchFeature(session).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new SDSFindFeature(session).find(folder));
        assertTrue(new SDSFindFeature(session).find(sourceFile));
        final Path targetFolder = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(folder, targetFolder), new TestSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new SDSFindFeature(session).find(targetFolder));
        final Path targetFile = new Path(targetFolder, sourceFile.getName(), EnumSet.of(Path.Type.file));
        assertTrue(new SDSFindFeature(session).find(targetFile));
        assertTrue(new SDSFindFeature(session).find(folder));
        assertTrue(new SDSFindFeature(session).find(sourceFile));
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(room), PathCache.empty(), new DisabledProgressListener()).run(session);
        session.close();
    }

    private static class TestSessionPool implements SessionPool {
        private final Session<?> session;

        public TestSessionPool(final Session<?> session) {
            this.session = session;
        }

        @Override
        public Session<?> borrow(final BackgroundActionState callback) throws BackgroundException {
            return session;
        }

        @Override
        public void release(final Session<?> session, final BackgroundException failure) {
        }

        @Override
        public void evict() {
        }

        @Override
        public Host getHost() {
            return session.getHost();
        }

        @Override
        public PathCache getCache() {
            return PathCache.empty();
        }

        @Override
        public VaultRegistry getVault() {
            return VaultRegistry.DISABLED;
        }

        @Override
        public Session.State getState() {
            return Session.State.open;
        }

        @Override
        public <T> T getFeature(final Class<T> type) {
            return session.getFeature(type);
        }

        @Override
        public void shutdown() {
        }
    }
}
