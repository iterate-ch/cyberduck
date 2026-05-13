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
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.googlestorage.AbstractGoogleStorageTest;
import ch.cyberduck.core.googlestorage.GoogleStorageDirectoryFeature;
import ch.cyberduck.core.googlestorage.GoogleStorageFindFeature;
import ch.cyberduck.core.googlestorage.GoogleStorageTouchFeature;
import ch.cyberduck.core.googlestorage.GoogleStorageWriteFeature;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class CopyWorkerTest extends AbstractGoogleStorageTest {

    @Test
    public void testCopyFile() throws Exception {
        final Path home = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path source = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path target = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new GoogleStorageTouchFeature(session).touch(new GoogleStorageWriteFeature(session), source, new TransferStatus());
        assertTrue(new GoogleStorageFindFeature(session).find(source));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(session), PathCache.empty(), ProgressListener.noop, ConnectionCallback.noop);
        worker.run(session);
        assertTrue(new GoogleStorageFindFeature(session).find(source));
        assertTrue(new GoogleStorageFindFeature(session).find(target));
        new DeleteWorker(LoginCallback.noop, Arrays.asList(source, target), ProgressListener.noop).run(session);
    }

    @Test
    public void testCopyFileToDirectory() throws Exception {
        final Path home = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path sourceFile = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new GoogleStorageTouchFeature(session).touch(new GoogleStorageWriteFeature(session), sourceFile, new TransferStatus());
        assertTrue(new GoogleStorageFindFeature(session).find(sourceFile));
        final Path targetFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path targetFile = new Path(targetFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new GoogleStorageDirectoryFeature(session).mkdir(new GoogleStorageWriteFeature(session), targetFolder, new TransferStatus());
        assertTrue(new GoogleStorageFindFeature(session).find(targetFolder));
        // copy file into vault
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(sourceFile, targetFile), new SessionPool.SingleSessionPool(session), PathCache.empty(), ProgressListener.noop, ConnectionCallback.noop);
        worker.run(session);
        assertTrue(new GoogleStorageFindFeature(session).find(sourceFile));
        assertTrue(new GoogleStorageFindFeature(session).find(targetFile));
        new DeleteWorker(LoginCallback.noop, Arrays.asList(sourceFile, targetFolder), ProgressListener.noop).run(session);
    }

    @Test
    public void testCopyDirectory() throws Exception {
        final Path home = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path sourceFile = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new GoogleStorageDirectoryFeature(session).mkdir(new GoogleStorageWriteFeature(session), folder, new TransferStatus());
        new GoogleStorageTouchFeature(session).touch(new GoogleStorageWriteFeature(session), sourceFile, new TransferStatus());
        assertTrue(new GoogleStorageFindFeature(session).find(folder));
        assertTrue(new GoogleStorageFindFeature(session).find(sourceFile));
        // move directory into vault
        final Path targetFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path targetFile = new Path(targetFolder, sourceFile.getName(), EnumSet.of(Path.Type.file));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(folder, targetFolder), new SessionPool.SingleSessionPool(session), PathCache.empty(), ProgressListener.noop, ConnectionCallback.noop);
        worker.run(session);
        assertTrue(new GoogleStorageFindFeature(session).find(targetFolder));
        assertTrue(new GoogleStorageFindFeature(session).find(targetFile));
        assertTrue(new GoogleStorageFindFeature(session).find(folder));
        assertTrue(new GoogleStorageFindFeature(session).find(sourceFile));
        new DeleteWorker(LoginCallback.noop, Arrays.asList(folder, targetFolder), ProgressListener.noop).run(session);
    }
}
