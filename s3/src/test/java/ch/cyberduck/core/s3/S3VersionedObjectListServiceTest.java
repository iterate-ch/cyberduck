package ch.cyberduck.core.s3;

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
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DefaultPathPredicate;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.jets3t.service.model.StorageObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3VersionedObjectListServiceTest extends AbstractS3Test {

    @Test
    public void testList() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        file.attributes().withVersionId("null");
        final AttributedList<Path> list = new S3VersionedObjectListService(session, new S3AccessControlListFeature(session)).list(container, new DisabledListProgressListener());
        final Path lookup = list.find(new DefaultPathPredicate(file));
        assertNotNull(lookup);
        assertEquals(file, lookup);
        assertSame(container, lookup.getParent());
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", lookup.attributes().getChecksum().hash);
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", Checksum.parse(lookup.attributes().getETag()).hash);
    }

    @Test
    public void testListVirtualHostStyle() throws Exception {
        final AttributedList<Path> list = new S3VersionedObjectListService(virtualhost, new S3AccessControlListFeature(virtualhost)).list(
                new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), new DisabledListProgressListener());
        for(Path p : list) {
            assertEquals(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), p.getParent());
            if(p.isFile()) {
                assertNotEquals(-1L, p.attributes().getModificationDate());
                assertNotEquals(-1L, p.attributes().getSize());
                assertNotNull(p.attributes().getETag());
                assertNotNull(p.attributes().getStorageClass());
                assertEquals("null", p.attributes().getVersionId());
            }
        }
    }

    @Test
    public void testDirectory() throws Exception {
        final Path bucket = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path directory = new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus().withLength(0L));
        final S3VersionedObjectListService feature = new S3VersionedObjectListService(session, acl);
        assertTrue(feature.list(bucket, new DisabledListProgressListener()).contains(directory));
        assertTrue(feature.list(directory, new DisabledListProgressListener()).isEmpty());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(feature.list(bucket, new DisabledListProgressListener()).contains(directory));
        try {
            feature.list(directory, new DisabledListProgressListener());
            fail();
        }
        catch(NotfoundException e) {

        }
    }

    @Test
    public void testListNotFoundFolder() throws Exception {
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final String name = new AlphanumericRandomStringService().random();
        try {
            new S3VersionedObjectListService(session, new S3AccessControlListFeature(session)).list(new Path(container, name, EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        }
        catch(NotfoundException e) {
            // Expected
        }
        final Path file = new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(new Path(container, String.format("%s-", name), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        try {
            new S3VersionedObjectListService(session, new S3AccessControlListFeature(session)).list(new Path(container, name, EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListFile() throws Exception {
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final String name = new AlphanumericRandomStringService().random();
        final Path file = new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(new Path(container, name, EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        try {
            new S3VersionedObjectListService(session, new S3AccessControlListFeature(session)).list(new Path(container, name, EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testEnableVersioningExistingFiles() throws Exception {
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path bucket = new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl)
                .mkdir(new Path(new AsciiRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus().withLength(0L).withLength(0L));
        assertTrue(new S3FindFeature(session, acl).find(bucket));
        final Path file = new S3TouchFeature(session, acl).touch(new Path(bucket, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L).withLength(0L));
        final S3WriteFeature feature = new S3WriteFeature(session, acl);
        {
            final byte[] content = RandomUtils.nextBytes(1024);
            final TransferStatus status = new TransferStatus().withLength(0L);
            status.setLength(content.length);
            status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
            final HttpResponseOutputStream<StorageObject> out = feature.write(file, status, new DisabledConnectionCallback());
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
            file.withAttributes(new S3AttributesAdapter().toAttributes(out.getStatus()));
            assertEquals(content.length, new S3AttributesFinderFeature(session, acl).find(file).getSize());
            final PathAttributes attr = new S3AttributesFinderFeature(session, acl).find(file);
            assertEquals(content.length, attr.getSize());
            assertNull(new S3AttributesFinderFeature(session, acl).find(file).getVersionId());
        }
        assertNull(new S3AttributesFinderFeature(session, acl).find(file).getVersionId());
        session.getFeature(Versioning.class).setConfiguration(bucket, new DisabledPasswordCallback(),
                new VersioningConfiguration(true));
        {
            final byte[] content = RandomUtils.nextBytes(256);
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length);
            status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
            final HttpResponseOutputStream<StorageObject> out = feature.write(file, status, new DisabledConnectionCallback());
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
            file.withAttributes(new S3AttributesAdapter().toAttributes(out.getStatus()));
            assertEquals(content.length, new S3AttributesFinderFeature(session, acl).find(file).getSize());
            final PathAttributes attr = new S3AttributesFinderFeature(session, acl).find(file);
            assertEquals(content.length, attr.getSize());
            assertNotNull(attr.getVersionId());
        }
        final AttributedList<Path> list = new S3VersionedObjectListService(session, acl).list(bucket, new DisabledListProgressListener()).filter(
                new Filter<Path>() {
                    @Override
                    public boolean accept(final Path f) {
                        return new SimplePathPredicate(file).test(f);
                    }

                    @Override
                    public Pattern toPattern() {
                        return null;
                    }
                }
        );
        assertEquals(2, list.size());
        final AttributedList<Path> versions = new S3VersioningFeature(session, acl).list(file, new DisabledListProgressListener());
        assertEquals(1, versions.size());
        assertEquals(versions.get(0), list.get(1));
        new S3DefaultDeleteFeature(session).delete(Arrays.asList(
                new Path(file).withAttributes(new PathAttributes().withVersionId("null")),
                new Path(file).withAttributes(new DefaultAttributesFinderFeature(session).find(file)), bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListEncodedCharacter() throws Exception {
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final Path placeholder = new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(
                new Path(container, String.format("^<%%%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        assertTrue(new S3VersionedObjectListService(session, new S3AccessControlListFeature(session)).list(container, new DisabledListProgressListener()).contains(placeholder));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListInvisibleCharacter() throws Exception {
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final Path placeholder = new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(
                new Path(container, String.format("test-\u001F-%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        assertTrue(new S3VersionedObjectListService(session, new S3AccessControlListFeature(session)).list(container, new DisabledListProgressListener()).contains(placeholder));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListFilePlusCharacter() throws Exception {
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final Path placeholder = new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(
                new Path(container, String.format("test+%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        assertTrue(new S3VersionedObjectListService(session, new S3AccessControlListFeature(session)).list(container, new DisabledListProgressListener()).contains(placeholder));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListPlaceholderPlusCharacter() throws Exception {
        final Path bucket = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        bucket.attributes().setRegion("us-east-1");
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path directory = new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus().withLength(0L));
        final Path placeholder = new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl).mkdir(
                new Path(directory, String.format("test+%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory)), new TransferStatus().withLength(0L));
        assertTrue(new S3VersionedObjectListService(session, acl).list(directory, new DisabledListProgressListener()).contains(placeholder));
        assertTrue(new S3VersionedObjectListService(session, acl).list(placeholder, new DisabledListProgressListener()).isEmpty());
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    //                                         | directory placeholder without delete marker | directory placeholder with delete marker  | no directory placeholder
    // no child objects                        |               !hidden (1)                   |                    hidden (2)             |          xxx
    // all child objects have a delete marker  |               !hidden (3)                   |                    hidden (4)             |         hidden (5)
    // some child objects have a delete marker |               !hidden (6)                   |                    !hidden (7)            |         !hidden (8)
    //

    /**
     * Testcases (1) and (2)
     */
    @Test
    public void testDirectoyPlaceholderNoChildren() throws Exception {
        final Path bucket = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path directory = new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus().withLength(0L));
        assertFalse(isDuplicate(directory, new S3VersionedObjectListService(session, acl).list(bucket, new DisabledListProgressListener())));
        // Nullify version to add delete marker
        directory.attributes().setVersionId(null);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertTrue(isDuplicate(directory, new S3VersionedObjectListService(session, acl).list(bucket, new DisabledListProgressListener())));
    }

    /**
     * Testcases (3), (4), (6) and (7)
     */
    @Test
    public void testDirectoyPlaceholderWithChildren() throws Exception {
        final Path bucket = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path directory = new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus().withLength(0L));
        assertTrue(new S3VersionedObjectListService(session, acl).list(bucket, new DisabledListProgressListener()).contains(directory));
        final Path child1 = new S3TouchFeature(session, acl).touch(new Path(directory, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        assertTrue(new S3VersionedObjectListService(session, acl).list(directory, new DisabledListProgressListener()).contains(child1));
        // Nullify version to add delete marker
        child1.attributes().setVersionId(null);
        final Path child2 = new S3TouchFeature(session, acl).touch(new Path(directory, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        assertTrue(new S3VersionedObjectListService(session, acl).list(directory, new DisabledListProgressListener()).contains(child2));
        // Nullify version to add delete marker
        child2.attributes().setVersionId(null);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(child1), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertTrue(isDuplicate(child1, new S3VersionedObjectListService(session, acl).list(directory, new DisabledListProgressListener())));
        assertFalse(isDuplicate(directory, new S3VersionedObjectListService(session, acl).list(bucket, new DisabledListProgressListener())));
        // Nullify version to add delete marker
        directory.attributes().setVersionId(null);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        // No placeholder object but child object under this prefix should still be found
        assertFalse(isDuplicate(directory, new S3VersionedObjectListService(session, acl).list(bucket, new DisabledListProgressListener())));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(child2), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertTrue(isDuplicate(child2, new S3VersionedObjectListService(session, acl).list(directory, new DisabledListProgressListener())));
        assertTrue(isDuplicate(directory, new S3VersionedObjectListService(session, acl).list(bucket, new DisabledListProgressListener())));
    }

    /**
     * Testcases (5) and (8)
     */
    @Test
    public void testNoDirectoyPlaceholderWithChildren() throws Exception {
        final Path bucket = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path directory = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path child1 = new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(new Path(directory, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        assertTrue(new S3VersionedObjectListService(session, new S3AccessControlListFeature(session)).list(bucket, new DisabledListProgressListener()).contains(directory));
        assertTrue(new S3VersionedObjectListService(session, new S3AccessControlListFeature(session)).list(directory, new DisabledListProgressListener()).contains(child1));
        // Nullify version to add delete marker
        child1.attributes().setVersionId(null);
        final Path child2 = new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(new Path(directory, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        assertTrue(new S3VersionedObjectListService(session, new S3AccessControlListFeature(session)).list(directory, new DisabledListProgressListener()).contains(child2));
        // Nullify version to add delete marker
        child2.attributes().setVersionId(null);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(child1), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertTrue(isDuplicate(child1, new S3VersionedObjectListService(session, new S3AccessControlListFeature(session)).list(directory, new DisabledListProgressListener())));
        assertFalse(isDuplicate(directory, new S3VersionedObjectListService(session, new S3AccessControlListFeature(session)).list(bucket, new DisabledListProgressListener())));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(child2), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertTrue(isDuplicate(child2, new S3VersionedObjectListService(session, new S3AccessControlListFeature(session)).list(directory, new DisabledListProgressListener())));
        // Prefix with only deleted files must be marked as hidden
        assertTrue(isDuplicate(directory, new S3VersionedObjectListService(session, new S3AccessControlListFeature(session)).list(bucket, new DisabledListProgressListener())));
    }

    private boolean isDuplicate(final Path file, final AttributedList<Path> list) throws BackgroundException {
        final Path found = list.find(new SimplePathPredicate(file));
        if(null == found) {
            fail(MessageFormat.format("Path {0} not found", file));
        }
        if(null == found.attributes().getVersionId()) {
            return found.attributes().isDuplicate();
        }
        final PathAttributes attr = new S3AttributesFinderFeature(session, new S3AccessControlListFeature(session)).find(found);
        assertEquals(found.attributes().getCustom(), attr.getCustom());
        return found.attributes().isDuplicate();
    }

    @Test
    public void testListFileDot() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(
                new Path(container, ".", EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        assertEquals(".", file.getName());
        assertEquals(container, file.getParent());
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        file.withAttributes(new S3AttributesFinderFeature(session, acl).find(file));
        assertNotNull(new S3VersionedObjectListService(session, new S3AccessControlListFeature(session)).list(container, new DisabledListProgressListener())
                .find(new SimplePathPredicate(file)));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListPlaceholderDot() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path placeholder = new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl).mkdir(
                new Path(container, ".", EnumSet.of(Path.Type.directory)), new TransferStatus().withLength(0L));
        assertTrue(new S3VersionedObjectListService(session, new S3AccessControlListFeature(session)).list(container, new DisabledListProgressListener()).contains(placeholder));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
