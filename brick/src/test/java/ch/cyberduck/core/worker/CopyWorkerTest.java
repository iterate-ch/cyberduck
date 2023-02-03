package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.brick.AbstractBrickTest;
import ch.cyberduck.core.brick.BrickDirectoryFeature;
import ch.cyberduck.core.brick.BrickFindFeature;
import ch.cyberduck.core.brick.BrickUploadFeature;
import ch.cyberduck.core.brick.BrickWriteFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class CopyWorkerTest extends AbstractBrickTest {

    @Test
    public void testCopyFile() throws Exception {

        final Path home = new DefaultHomeFinderService(session).find();
        final Path source = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path target = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), source.getName());
        final byte[] random = RandomUtils.nextBytes(3247);
        IOUtils.write(random, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus().withLength(random.length);
        new BrickUploadFeature(session, new BrickWriteFeature(session)).upload(source, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
            new DisabledStreamListener(), status, new DisabledLoginCallback());
        assertTrue(new BrickFindFeature(session).find(source));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(source, target), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new BrickFindFeature(session).find(source));
        assertTrue(new BrickFindFeature(session).find(target));
        new DeleteWorker(new DisabledLoginCallback(), Arrays.asList(source, target), new DisabledProgressListener()).run(session);
        local.delete();
    }

    @Test
    public void testCopyFileToDirectory() throws Exception {

        final Path home = new DefaultHomeFinderService(session).find();
        final Path sourceFile = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), sourceFile.getName());
        final byte[] random = RandomUtils.nextBytes(3247);
        IOUtils.write(random, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus().withLength(random.length);
        new BrickUploadFeature(session, new BrickWriteFeature(session)).upload(sourceFile, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
            new DisabledStreamListener(), status, new DisabledLoginCallback());
        assertTrue(new BrickFindFeature(session).find(sourceFile));
        final Path targetFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path targetFile = new Path(targetFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new BrickDirectoryFeature(session).mkdir(targetFolder, new TransferStatus());
        assertTrue(new BrickFindFeature(session).find(targetFolder));
        // copy file into vault
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(sourceFile, targetFile), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new BrickFindFeature(session).find(sourceFile));
        assertTrue(new BrickFindFeature(session).find(targetFile));
        new DeleteWorker(new DisabledLoginCallback(), Arrays.asList(sourceFile, targetFolder), new DisabledProgressListener()).run(session);
        local.delete();
    }

    @Test
    public void testCopyDirectory() throws Exception {

        final Path home = new DefaultHomeFinderService(session).find();
        final Path folder = new BrickDirectoryFeature(session).mkdir(new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path sourceFile = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), sourceFile.getName());
        final byte[] random = RandomUtils.nextBytes(3247);
        IOUtils.write(random, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus().withLength(random.length);
        new BrickUploadFeature(session, new BrickWriteFeature(session)).upload(sourceFile, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
            new DisabledStreamListener(), status, new DisabledLoginCallback());
        assertTrue(new BrickFindFeature(session).find(folder));
        assertTrue(new BrickFindFeature(session).find(sourceFile));
        final Path targetFolder = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path targetFile = new Path(targetFolder, sourceFile.getName(), EnumSet.of(Path.Type.file));
        final CopyWorker worker = new CopyWorker(Collections.singletonMap(folder, targetFolder), new SessionPool.SingleSessionPool(session), PathCache.empty(), new DisabledProgressListener(), new DisabledConnectionCallback());
        worker.run(session);
        assertTrue(new BrickFindFeature(session).find(targetFolder));
        assertTrue(new BrickFindFeature(session).find(targetFile));
        assertTrue(new BrickFindFeature(session).find(folder));
        assertTrue(new BrickFindFeature(session).find(sourceFile));
        new DeleteWorker(new DisabledLoginCallback(), Arrays.asList(folder, targetFile), new DisabledProgressListener()).run(session);
        local.delete();
    }
}
