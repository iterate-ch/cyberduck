package ch.cyberduck.core.gmxcloud;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GmxcloudCopyFeatureTest extends AbstractGmxcloudTest {

    @Test
    public void testCopyFile() throws Exception {
        final GmxcloudResourceIdProvider fileid = new GmxcloudResourceIdProvider(session);
        final Path sourceFolder = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory));
        final Path sourceFile = new Path(sourceFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new GmxcloudDirectoryFeature(session, fileid).mkdir(sourceFolder, new TransferStatus());
        createFile(sourceFile, RandomUtils.nextBytes(1023));
        assertTrue(new GmxcloudFindFeature(session, fileid).find(sourceFile));
        final Path targetFolder = new GmxcloudDirectoryFeature(session, fileid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        final Path targetFile = new Path(targetFolder, sourceFile.getName(), EnumSet.of(AbstractPath.Type.file));
        final Path copy = new GmxcloudCopyFeature(session, fileid).copy(sourceFile, targetFile, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertTrue(new GmxcloudFindFeature(session, fileid).find(sourceFile));
        assertTrue(new DefaultFindFeature(session).find(sourceFile));
        assertTrue(new GmxcloudFindFeature(session, fileid).find(targetFile));
        assertTrue(new DefaultFindFeature(session).find(targetFile));
        assertEquals(copy.attributes(), new GmxcloudAttributesFinderFeature(session, fileid).find(targetFile));
        assertEquals(new GmxcloudAttributesFinderFeature(session, fileid).find(sourceFile).getSize(),
                new GmxcloudAttributesFinderFeature(session, fileid).find(targetFile).getSize());
        assertEquals(new GmxcloudAttributesFinderFeature(session, fileid).find(sourceFile).getETag(),
                new GmxcloudAttributesFinderFeature(session, fileid).find(targetFile).getETag());
        assertNotEquals(new GmxcloudAttributesFinderFeature(session, fileid).find(sourceFile).getFileId(),
                new GmxcloudAttributesFinderFeature(session, fileid).find(targetFile).getFileId());
        new GmxcloudDeleteFeature(session, fileid).delete(Arrays.asList(sourceFolder, targetFolder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyRenameFile() throws Exception {
        final GmxcloudResourceIdProvider fileid = new GmxcloudResourceIdProvider(session);
        final Path sourceFolder = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory));
        final Path sourceFile = new Path(sourceFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new GmxcloudDirectoryFeature(session, fileid).mkdir(sourceFolder, new TransferStatus());
        createFile(sourceFile, RandomUtils.nextBytes(1023));
        assertTrue(new GmxcloudFindFeature(session, fileid).find(sourceFile));
        final Path targetFolder = new GmxcloudDirectoryFeature(session, fileid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        final Path targetFile = new Path(targetFolder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.file));
        final Path copy = new GmxcloudCopyFeature(session, fileid).copy(sourceFile, targetFile, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertTrue(new GmxcloudFindFeature(session, fileid).find(sourceFile));
        assertTrue(new DefaultFindFeature(session).find(sourceFile));
        assertTrue(new GmxcloudFindFeature(session, fileid).find(targetFile));
        assertTrue(new DefaultFindFeature(session).find(targetFile));
        assertEquals(copy.attributes(), new GmxcloudAttributesFinderFeature(session, fileid).find(targetFile));
        assertEquals(new GmxcloudAttributesFinderFeature(session, fileid).find(sourceFile).getSize(),
                new GmxcloudAttributesFinderFeature(session, fileid).find(targetFile).getSize());
        assertEquals(new GmxcloudAttributesFinderFeature(session, fileid).find(sourceFile).getChecksum(),
                new GmxcloudAttributesFinderFeature(session, fileid).find(targetFile).getChecksum());
        assertNotEquals(new GmxcloudAttributesFinderFeature(session, fileid).find(sourceFile).getFileId(),
                new GmxcloudAttributesFinderFeature(session, fileid).find(targetFile).getFileId());
        new GmxcloudDeleteFeature(session, fileid).delete(Arrays.asList(sourceFolder, targetFolder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyToExistingFile() throws Exception {
        final GmxcloudResourceIdProvider fileid = new GmxcloudResourceIdProvider(session);
        final Path folder = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new GmxcloudDirectoryFeature(session, fileid).mkdir(folder, new TransferStatus());
        final Path test = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), test.getName());
        final byte[] random = RandomUtils.nextBytes(2547);
        IOUtils.write(random, local.getOutputStream(false));
        final TransferStatus status = new TransferStatus().withLength(random.length);
        new GmxcloudSingleUploadService(session, fileid, new GmxcloudWriteFeature(session, fileid)).upload(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), status, new DisabledLoginCallback());
        local.delete();
        assertTrue(new GmxcloudFindFeature(session, fileid).find(test));
        final Path copy = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new GmxcloudTouchFeature(session, fileid)
                .touch(copy, new TransferStatus().withLength(0L));
        new GmxcloudCopyFeature(session, fileid).copy(test, copy, new TransferStatus().exists(true), new DisabledConnectionCallback(), new DisabledStreamListener());
        final Find find = new DefaultFindFeature(session);
        assertTrue(find.find(test));
        assertTrue(find.find(copy));
        new GmxcloudDeleteFeature(session, fileid).delete(Arrays.asList(test, copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
