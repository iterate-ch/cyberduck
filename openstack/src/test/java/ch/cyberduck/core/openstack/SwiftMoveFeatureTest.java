package ch.cyberduck.core.openstack;

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

import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SwiftMoveFeatureTest extends AbstractSwiftTest {

    @Test
    public void testMove() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        test.attributes().setRegion("IAD");
        new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(test, new TransferStatus());
        assertTrue(new SwiftFindFeature(session).find(test));
        final Path target = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        target.attributes().setRegion("IAD");
        new SwiftMoveFeature(session).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new SwiftFindFeature(session).find(test));
        assertTrue(new SwiftFindFeature(session).find(target));
        new SwiftDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveOverride() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        test.attributes().setRegion("IAD");
        new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(test, new TransferStatus());
        final Path target = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        target.attributes().setRegion("IAD");
        new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(target, new TransferStatus());
        new SwiftMoveFeature(session).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new SwiftFindFeature(session).find(test));
        assertTrue(new SwiftFindFeature(session).find(target));
        new SwiftDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testMoveNotFound() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        test.attributes().setRegion("IAD");
        new SwiftMoveFeature(session).move(test, new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
    }

    @Test
    public void testSupport() {
        final Path c = new Path("/c", EnumSet.of(Path.Type.directory));
        assertFalse(new SwiftMoveFeature(session).isSupported(c, c));
        final Path cf = new Path("/c/f", EnumSet.of(Path.Type.directory));
        assertTrue(new SwiftMoveFeature(session).isSupported(cf, cf));
    }

    @Test
    public void testMoveLargeObjectToSameBucket() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path originFolder = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path sourceFile = new Path(originFolder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));

        final SwiftRegionService regionService = new SwiftRegionService(session);
        final SwiftSegmentService segmentService = new SwiftSegmentService(session, ".segments-test/");
        prepareFile(sourceFile, regionService, segmentService);

        final SwiftFindFeature findFeature = new SwiftFindFeature(session);
        assertTrue(findFeature.find(sourceFile));

        final List<Path> sourceSegments = segmentService.list(sourceFile);

        final Path targetFolder = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path targetFile = new Path(targetFolder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Path movedFile = new SwiftMoveFeature(session, regionService).move(sourceFile, targetFile,
            new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        // source file does not exist anymore
        assertFalse(findFeature.find(sourceFile));
        // moved file exists
        assertTrue(findFeature.find(movedFile));

        final List<Path> targetSegments = segmentService.list(targetFile);

        assertTrue(sourceSegments.containsAll(targetSegments) && targetSegments.containsAll(sourceSegments));

        new SwiftDeleteFeature(session, segmentService, regionService).delete(
            Collections.singletonMap(targetFile, new TransferStatus()),
            new DisabledPasswordCallback(), new Delete.DisabledCallback(), true);
        assertFalse(findFeature.find(movedFile));

        assertArrayEquals(new PathAttributes[0], targetSegments.stream().filter(p -> {
            try {
                return findFeature.find(movedFile);
            }
            catch(BackgroundException e) {
                e.printStackTrace();
                return false;
            }
        }).toArray());
    }

    @Test
    public void testMoveLargeObjectToDifferentBucket() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path originFolder = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path sourceFile = new Path(originFolder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));

        final SwiftRegionService regionService = new SwiftRegionService(session);
        final SwiftSegmentService segmentService = new SwiftSegmentService(session, ".segments-test/");
        prepareFile(sourceFile, regionService, segmentService);

        final SwiftFindFeature findFeature = new SwiftFindFeature(session);
        assertTrue(findFeature.find(sourceFile));

        final List<Path> sourceSegments = segmentService.list(sourceFile);

        final Path targetBucket = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        targetBucket.attributes().setRegion("DFW");
        final Path targetFolder = new Path(targetBucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path targetFile = new Path(targetFolder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        assertThrows(BackgroundException.class, () -> new SwiftMoveFeature(session, regionService).move(sourceFile, targetFile,
                new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback()));
    }

    private void prepareFile(final Path path, final SwiftRegionService regionService, final SwiftSegmentService segmentService) throws BackgroundException {
        final SwiftLargeUploadWriteFeature upload = new SwiftLargeUploadWriteFeature(session, regionService, segmentService);
        final OutputStream out = upload.write(path, new TransferStatus(), new DisabledConnectionCallback());
        final byte[] content = RandomUtils.nextBytes(1024 * 1024);
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        final TransferStatus progress = new TransferStatus();
        new StreamCopier(new TransferStatus(), progress).transfer(in, out);
    }
}
