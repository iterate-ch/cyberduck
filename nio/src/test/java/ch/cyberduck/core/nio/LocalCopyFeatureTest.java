package ch.cyberduck.core.nio;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.StaticPermission;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.worker.CopyWorker;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

public class LocalCopyFeatureTest {

    @Test
    public void testCopyFile() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledProxyFinder(), HostKeyCallback.noop, LoginCallback.noop, CancelCallback.noop);
        session.login(LoginCallback.noop, CancelCallback.noop);
        final Path workdir = new LocalHomeFinderFeature().find();
        final Path test = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new LocalTouchFeature(session).touch(new LocalWriteFeature(session), test, new TransferStatus());
        final Path target = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new LocalCopyFeature(session).copy(test, target, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertTrue(new LocalFindFeature(session).find(test));
        assertTrue(new LocalFindFeature(session).find(target));
        new LocalDeleteFeature(session).delete(Arrays.asList(test, target), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testCopyDirectory() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledProxyFinder(), HostKeyCallback.noop, LoginCallback.noop, CancelCallback.noop);
        session.login(LoginCallback.noop, CancelCallback.noop);
        final Path workdir = new LocalHomeFinderFeature().find();
        final Path directory = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new LocalDirectoryFeature(session).mkdir(new LocalWriteFeature(session), directory, new TransferStatus());
        final Path file = new Path(directory, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new LocalTouchFeature(session).touch(new LocalWriteFeature(session), file, new TransferStatus());
        // Files.copy does not copy directory entries. Children must be copied individually by the worker
        assertFalse(new LocalCopyFeature(session).isRecursive(directory, new Path(workdir, "n", EnumSet.of(Path.Type.directory))));
        final Path target = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(directory, target),
                new SessionPool.SingleSessionPool(session), PathCache.empty(), ProgressListener.noop, ConnectionCallback.noop);
        final Map<Path, Path> result = worker.run(session);
        assertEquals(2, result.size());
        final Path copy = new Path(target, file.getName(), EnumSet.of(Path.Type.file));
        assertTrue(new LocalFindFeature(session).find(target));
        assertTrue(new LocalFindFeature(session).find(copy));
        assertTrue(new LocalFindFeature(session).find(directory));
        assertTrue(new LocalFindFeature(session).find(file));
        new LocalDeleteFeature(session).delete(Arrays.asList(file, copy, directory, target), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testCopyDirectoryPermission() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledProxyFinder(), HostKeyCallback.noop, LoginCallback.noop, CancelCallback.noop);
        session.login(LoginCallback.noop, CancelCallback.noop);
        assumeTrue(session.isPosixFilesystem());
        final Path workdir = new LocalHomeFinderFeature().find();
        final Path directory = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new LocalDirectoryFeature(session).mkdir(new LocalWriteFeature(session), directory, new TransferStatus());
        new LocalUnixPermissionFeature(session).setUnixPermission(directory, new StaticPermission(700));
        final Path file = new Path(directory, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new LocalTouchFeature(session).touch(new LocalWriteFeature(session), file, new TransferStatus());
        final Path source = directory.withAttributes(new LocalAttributesFinderFeature(session).find(directory));
        assertEquals("700", source.attributes().getPermission().getMode());
        final Path target = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(source, target),
                new SessionPool.SingleSessionPool(session), PathCache.empty(), ProgressListener.noop, ConnectionCallback.noop);
        worker.run(session);
        // Permissions of source directory must be retained when copied by worker
        assertEquals("700", new LocalAttributesFinderFeature(session).find(target).getPermission().getMode());
        final Path copy = new Path(target, file.getName(), EnumSet.of(Path.Type.file));
        assertTrue(new LocalFindFeature(session).find(copy));
        new LocalDeleteFeature(session).delete(Arrays.asList(file, copy, directory, target), LoginCallback.noop, new Delete.DisabledCallback());
    }

    @Test
    public void testCopyDirectoryToExistingDirectory() throws Exception {
        final LocalSession session = new LocalSession(new Host(new LocalProtocol(), new LocalProtocol().getDefaultHostname()));
        session.open(new DisabledProxyFinder(), HostKeyCallback.noop, LoginCallback.noop, CancelCallback.noop);
        session.login(LoginCallback.noop, CancelCallback.noop);
        final Path workdir = new LocalHomeFinderFeature().find();
        final Path directory = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new LocalDirectoryFeature(session).mkdir(new LocalWriteFeature(session), directory, new TransferStatus());
        final Path file = new Path(directory, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new LocalTouchFeature(session).touch(new LocalWriteFeature(session), file, new TransferStatus());
        // Target directory already exists
        final Path target = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new LocalDirectoryFeature(session).mkdir(new LocalWriteFeature(session), target, new TransferStatus());
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(directory, target),
                new SessionPool.SingleSessionPool(session), PathCache.empty(), ProgressListener.noop, ConnectionCallback.noop);
        final Map<Path, Path> result = worker.run(session);
        assertEquals(2, result.size());
        final Path copy = new Path(target, file.getName(), EnumSet.of(Path.Type.file));
        assertTrue(new LocalFindFeature(session).find(copy));
        new LocalDeleteFeature(session).delete(Arrays.asList(file, copy, directory, target), LoginCallback.noop, new Delete.DisabledCallback());
    }
}
