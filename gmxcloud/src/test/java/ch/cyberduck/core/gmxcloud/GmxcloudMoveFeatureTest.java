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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GmxcloudMoveFeatureTest extends AbstractGmxcloudTest {

    @Test
    public void testMoveFile() throws Exception {
        final GmxcloudResourceIdProvider fileid = new GmxcloudResourceIdProvider(session);
        final Path sourceFolder = new GmxcloudDirectoryFeature(session, fileid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        final Path sourceFile = new Path(sourceFolder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        createFile(sourceFile, RandomUtils.nextBytes(541));
        final PathAttributes sourceAttr = new GmxcloudAttributesFinderFeature(session, fileid).find(sourceFile);
        assertTrue(new GmxcloudFindFeature(session, fileid).find(sourceFile));
        final Path targetFolder = new GmxcloudDirectoryFeature(session, fileid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        final Path targetFile = new GmxcloudMoveFeature(session, fileid).move(sourceFile,
                new Path(targetFolder, new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new GmxcloudFindFeature(session, fileid).find(sourceFile));
        assertTrue(new GmxcloudFindFeature(session, fileid).find(targetFile));
        assertFalse(new DefaultFindFeature(session).find(sourceFile));
        assertTrue(new DefaultFindFeature(session).find(targetFile));
        assertEquals(targetFile.attributes(), new GmxcloudAttributesFinderFeature(session, fileid).find(targetFile));
        assertEquals(sourceAttr.getSize(),
                new GmxcloudAttributesFinderFeature(session, fileid).find(targetFile).getSize());
        assertEquals(sourceAttr.getETag(),
                new GmxcloudAttributesFinderFeature(session, fileid).find(targetFile).getETag());
        assertEquals(sourceAttr.getFileId(),
                new GmxcloudAttributesFinderFeature(session, fileid).find(targetFile).getFileId());
        new GmxcloudDeleteFeature(session, fileid).delete(Arrays.asList(sourceFolder, targetFolder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testMoveNotFound() throws Exception {
        final GmxcloudResourceIdProvider fileid = new GmxcloudResourceIdProvider(session);
        final Path test = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new GmxcloudMoveFeature(session, fileid).move(test, new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testMoveInvalidResourceId() throws Exception {
        final GmxcloudResourceIdProvider fileid = new GmxcloudResourceIdProvider(session);
        final Path folder = new GmxcloudDirectoryFeature(session, fileid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(AbstractPath.Type.directory)), new TransferStatus());
        final Path file = new GmxcloudTouchFeature(session, fileid).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String resourceId = file.attributes().getFileId();
        new GmxcloudDeleteFeature(session, fileid, false).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        try {
            new GmxcloudMoveFeature(session, fileid).move(file.withAttributes(new PathAttributes().withFileId(resourceId)),
                    new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
            fail();
        }
        finally {
            new GmxcloudDeleteFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }

    @Test(expected = NotfoundException.class)
    public void testRenameInvalidResourceId() throws Exception {
        final GmxcloudResourceIdProvider fileid = new GmxcloudResourceIdProvider(session);
        final Path file = new GmxcloudTouchFeature(session, fileid).touch(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String resourceId = file.attributes().getFileId();
        new GmxcloudDeleteFeature(session, fileid, false).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new GmxcloudMoveFeature(session, fileid).move(file.withAttributes(new PathAttributes().withFileId(resourceId)),
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
    }
}
