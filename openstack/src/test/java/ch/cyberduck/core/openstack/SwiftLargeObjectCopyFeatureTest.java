package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.DisabledStreamListener;
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SwiftLargeObjectCopyFeatureTest extends AbstractSwiftTest {

    @Test
    public void testCopyManifestSameBucket() throws Exception {
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
        final Path copiedFile = new SwiftDefaultCopyFeature(session, regionService)
            .copy(sourceFile, targetFile, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        // copied file exists
        assertTrue(findFeature.find(copiedFile));

        final List<Path> targetSegments = segmentService.list(targetFile);

        assertTrue(sourceSegments.containsAll(targetSegments) && targetSegments.containsAll(sourceSegments));

        // delete source, without deleting segments
        new SwiftDeleteFeature(session, segmentService, regionService).delete(
            Collections.singletonMap(sourceFile, new TransferStatus()),
            new DisabledPasswordCallback(), new Delete.DisabledCallback(), false);
        assertFalse(findFeature.find(sourceFile));

        assertTrue(targetSegments.stream().allMatch(p -> {
            try {
                return findFeature.find(p);
            }
            catch(BackgroundException e) {
                return false;
            }
        }));

        new SwiftDeleteFeature(session, segmentService, regionService).delete(
            Collections.singletonMap(copiedFile, new TransferStatus()),
            new DisabledPasswordCallback(), new Delete.DisabledCallback(), true);
        assertFalse(findFeature.find(copiedFile));
    }

    @Test
    public void testCopyLargeObjectSameBucket() throws Exception {
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
        final Path copiedFile = new SwiftLargeObjectCopyFeature(session, regionService)
            .copy(sourceFile, targetFile, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        // copied file exists
        assertTrue(findFeature.find(copiedFile));

        final List<Path> targetSegments = segmentService.list(targetFile);

        // delete source, without deleting copied-segments
        new SwiftDeleteFeature(session, segmentService, regionService).delete(
            Collections.singletonMap(sourceFile, new TransferStatus()),
            new DisabledPasswordCallback(), new Delete.DisabledCallback(), true);
        assertFalse(findFeature.find(sourceFile));

        assertTrue(targetSegments.stream().allMatch(p -> {
            try {
                return findFeature.find(p);
            }
            catch(BackgroundException e) {
                return false;
            }
        }));

        new SwiftDeleteFeature(session, segmentService, regionService).delete(
            Collections.singletonMap(copiedFile, new TransferStatus()),
            new DisabledPasswordCallback(), new Delete.DisabledCallback(), true);
        assertFalse(findFeature.find(copiedFile));
    }

    @Test
    public void testCopyManifestDifferentBucket() throws Exception {
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
        targetBucket.attributes().setRegion("IAD");
        final Path targetFolder = new Path(targetBucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path targetFile = new Path(targetFolder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final Path copiedFile = new SwiftDefaultCopyFeature(session, regionService)
            .copy(sourceFile, targetFile, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        // copied file exists
        assertTrue(findFeature.find(copiedFile));

        final List<Path> targetSegments = segmentService.list(targetFile);

        assertTrue(sourceSegments.containsAll(targetSegments) && targetSegments.containsAll(sourceSegments));

        // delete source, without deleting segments
        new SwiftDeleteFeature(session, segmentService, regionService).delete(
            Collections.singletonMap(sourceFile, new TransferStatus()),
            new DisabledPasswordCallback(), new Delete.DisabledCallback(), false);
        assertFalse(findFeature.find(sourceFile));

        assertTrue(targetSegments.stream().allMatch(p -> {
            try {
                return findFeature.find(p);
            }
            catch(BackgroundException e) {
                return false;
            }
        }));

        new SwiftDeleteFeature(session, segmentService, regionService).delete(
            Collections.singletonMap(copiedFile, new TransferStatus()),
            new DisabledPasswordCallback(), new Delete.DisabledCallback(), true);
        assertFalse(findFeature.find(copiedFile));
    }

    @Test
    public void testCopyLargeObjectDifferentBucket() throws Exception {
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path originFolder = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path sourceFile = new Path(originFolder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));

        final SwiftRegionService regionService = new SwiftRegionService(session);
        final SwiftSegmentService segmentService = new SwiftSegmentService(session, ".segments-test/");
        prepareFile(sourceFile, regionService, segmentService);

        final SwiftFindFeature findFeature = new SwiftFindFeature(session);
        assertTrue(findFeature.find(sourceFile));

        final Path targetBucket = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        targetBucket.attributes().setRegion("IAD");
        final Path targetFolder = new Path(targetBucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        final Path targetFile = new Path(targetFolder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final SwiftObjectListService listService = new SwiftObjectListService(session, regionService);
        final Path copiedFile = new SwiftLargeObjectCopyFeature(session, regionService, segmentService)
            .copy(sourceFile, targetFile, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        // copied file exists
        assertTrue(findFeature.find(copiedFile));

        final List<Path> targetSegments = segmentService.list(targetFile);

        // delete source, without deleting copy
        new SwiftDeleteFeature(session, segmentService, regionService).delete(
            Collections.singletonMap(sourceFile, new TransferStatus()),
            new DisabledPasswordCallback(), new Delete.DisabledCallback(), true);
        assertFalse(findFeature.find(sourceFile));

        assertTrue(targetSegments.stream().allMatch(p -> {
            try {
                return findFeature.find(p);
            }
            catch(BackgroundException e) {
                e.printStackTrace();
                return false;
            }
        }));

        new SwiftDeleteFeature(session, segmentService, regionService).delete(
            Collections.singletonMap(copiedFile, new TransferStatus()),
            new DisabledPasswordCallback(), new Delete.DisabledCallback(), true);
        assertFalse(findFeature.find(copiedFile));
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
