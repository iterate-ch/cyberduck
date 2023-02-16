package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.IndexedListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.SHA1ChecksumCompute;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import synapticloop.b2.response.B2FileResponse;
import synapticloop.b2.response.BaseB2Response;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2ObjectListServiceTest extends AbstractB2Test {

    @Test(expected = NotfoundException.class)
    public void testListNotFoundFolder() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        new B2ObjectListService(session, fileid).list(new Path(bucket, "notfound", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
    }

    @Test
    public void testListEmptyFolder() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new B2DirectoryFeature(session, fileid).mkdir(new Path(bucket, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new B2ObjectListService(session, fileid).list(folder, new DisabledListProgressListener()).isEmpty());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListFolderNameDot() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(
                new Path(String.format("test-%s", new AsciiRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path folder = new B2DirectoryFeature(session, fileid).mkdir(new Path(bucket, ".", EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertEquals(".", folder.getName());
        assertTrue(new B2ObjectListService(session, fileid).list(folder, new DisabledListProgressListener()).isEmpty());
        assertTrue(new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener()).contains(folder));
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(folder, bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListFolderNameDotDot() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(
                new Path(String.format("test-%s", new AsciiRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path folder = new B2DirectoryFeature(session, fileid).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path test = new B2DirectoryFeature(session, fileid).mkdir(new Path(folder, "..", EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertEquals("..", test.getName());
        assertEquals(folder, test.getParent());
        assertTrue(new B2ObjectListService(session, fileid).list(test, new DisabledListProgressListener()).isEmpty());
        assertTrue(new B2ObjectListService(session, fileid).list(folder, new DisabledListProgressListener()).contains(test));
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(test, folder, bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListFileNameDot() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(
                new Path(String.format("test-%s", new AsciiRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path folder = new B2DirectoryFeature(session, fileid).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new B2TouchFeature(session, fileid).touch(new Path(folder, ".", EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(".", file.getName());
        assertEquals(folder, file.getParent());
        assertTrue(new B2ObjectListService(session, fileid).list(folder, new DisabledListProgressListener()).contains(file));
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(file, folder, bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListFileNameDotDot() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(
                new Path(String.format("test-%s", new AsciiRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path file = new B2TouchFeature(session, fileid).touch(new Path(bucket, "..", EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(bucket, file.getParent());
        assertEquals("..", file.getName());
        assertTrue(new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener()).contains(file));
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(file, bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testListNotfoundContainer() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new Path("notfound-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener());
    }

    @Test
    public void testList() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(
            new Path(String.format("test-%s", new AsciiRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path file = new Path(bucket, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus();
        status.setChecksum(Checksum.parse("da39a3ee5e6b4b0d3255bfef95601890afd80709"));
        final HttpResponseOutputStream<BaseB2Response> out = new B2WriteFeature(session, fileid).write(file, status, new DisabledConnectionCallback());
        IOUtils.write(new byte[0], out);
        out.close();
        final AttributedList<Path> list = new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener());
        assertNotNull(list.find(new SimplePathPredicate(file)));
        assertNull(list.find(new SimplePathPredicate(file)).attributes().getRevision());
        assertEquals(0L, list.find(new SimplePathPredicate(file)).attributes().getSize());
        assertSame(bucket, list.find(new SimplePathPredicate(file)).getParent());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener()).contains(file));
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListChunking() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(
            new Path(String.format("test-%s", new AsciiRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path file1 = new B2TouchFeature(session, fileid).touch(new Path(bucket, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path file2 = new B2TouchFeature(session, fileid).touch(new Path(bucket, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final AttributedList<Path> list = new B2ObjectListService(session, fileid, 1).list(bucket, new DisabledListProgressListener());
        assertTrue(list.contains(file1));
        assertTrue(list.contains(file2));
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(bucket, file1, file2), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListRevisions() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(new Path(String.format("test-%s", new AsciiRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final String name = new AsciiRandomStringService().random();
        final Path file = new Path(bucket, name, EnumSet.of(Path.Type.file));
        {
            final byte[] content = RandomUtils.nextBytes(1);
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length);
            status.setChecksum(new SHA1ChecksumCompute().compute(new ByteArrayInputStream(content), status));
            final HttpResponseOutputStream<BaseB2Response> out = new B2WriteFeature(session, fileid).write(file, status, new DisabledConnectionCallback());
            IOUtils.write(content, out);
            out.close();
            final B2FileResponse response = (B2FileResponse) out.getStatus();
            assertEquals(response.getFileId(), file.attributes().getVersionId());
            file.attributes().setVersionId(response.getFileId());
            final AttributedList<Path> list = new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener());
            assertTrue(list.contains(file));
            assertNull(list.find(new SimplePathPredicate(file)).attributes().getRevision());
            assertEquals(content.length, list.find(new SimplePathPredicate(file)).attributes().getSize());
            assertEquals(bucket, list.find(new SimplePathPredicate(file)).getParent());
        }
        // Replace
        {
            final byte[] content = RandomUtils.nextBytes(1);
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length);
            status.setChecksum(new SHA1ChecksumCompute().compute(new ByteArrayInputStream(content), status));
            final HttpResponseOutputStream<BaseB2Response> out = new B2WriteFeature(session, fileid).write(file, status, new DisabledConnectionCallback());
            IOUtils.write(content, out);
            out.close();
            final B2FileResponse response = (B2FileResponse) out.getStatus();
            assertEquals(response.getFileId(), file.attributes().getVersionId());
            final AttributedList<Path> list = new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener());
            assertEquals(2, list.size());
            assertTrue(list.contains(file));
            assertEquals(bucket, list.get(file).getParent());
            assertNull(list.get(file).attributes().getRevision());
            assertEquals(Long.valueOf(1L), list.find(path -> path.attributes().isDuplicate()).attributes().getRevision());
        }
        // Add hide marker
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(file.withAttributes(PathAttributes.EMPTY)), new DisabledLoginCallback(), new Delete.DisabledCallback());
        {
            final AttributedList<Path> list = new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener());
            assertEquals(3, list.size());
            for(Path f : list) {
                assertTrue(f.attributes().isDuplicate());
            }
        }
        assertFalse(new B2FindFeature(session, fileid).find(file));
        assertFalse(new DefaultFindFeature(session).find(file));
        try {
            new B2AttributesFinderFeature(session, fileid).find(file);
            fail();
        }
        catch(NotfoundException e) {
            //
        }
        final AttributedList<Path> list = new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener());
        assertEquals(list, new B2VersioningFeature(session, fileid).list(file, new DisabledListProgressListener()));
        final Path other = new B2TouchFeature(session, fileid).touch(new Path(bucket, name + new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final AttributedList<Path> versions = new B2VersioningFeature(session, fileid).list(file, new DisabledListProgressListener());
        assertEquals(list, versions);
        assertFalse(versions.contains(other));
        for(Path f : list) {
            new B2DeleteFeature(session, fileid).delete(Collections.singletonList(f), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(other, bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListFolder() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(
            new Path(String.format("test-%s", new AsciiRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path folder1 = new B2DirectoryFeature(session, fileid).mkdir(new Path(bucket, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path folder2 = new B2DirectoryFeature(session, fileid).mkdir(new Path(folder1, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file1 = new B2TouchFeature(session, fileid).touch(new Path(folder1, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path file2 = new B2TouchFeature(session, fileid).touch(new Path(folder2, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final AttributedList<Path> list = new B2ObjectListService(session, fileid).list(folder1, new DisabledListProgressListener());
        // Including
        // Path{path='/test-e9287cee-772a-4a69-86f5-05905a23a446/2b47b8c4-0d13-41e8-a76f-45e918dd88d6/.bzEmpty', type=[file]}
        // Path{path='/test-e9287cee-772a-4a69-86f5-05905a23a446/2b47b8c4-0d13-41e8-a76f-45e918dd88d6/b136e277-3ee0-49f0-b19f-4a66eb7d8f38', type=[directory, placeholder]}
        // Path{path='/test-e9287cee-772a-4a69-86f5-05905a23a446/2b47b8c4-0d13-41e8-a76f-45e918dd88d6/c2cbb949-2877-416d-9eb5-0855279adde3', type=[file]}
        assertEquals(2, list.size());
        assertNotNull(list.find(new SimplePathPredicate(file1)));
        assertNotNull(list.find(new SimplePathPredicate(folder2)));
        assertNull(list.find(new SimplePathPredicate(file2)));
        assertNull(list.find(new SimplePathPredicate(folder1)));
        assertSame(folder1, list.find(new SimplePathPredicate(file1)).getParent());
        assertSame(folder1, list.find(new SimplePathPredicate(folder2)).getParent());
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(bucket, folder1, file1, folder2, file2), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDisplayFolderInBucketMissingPlaceholder() throws Exception {
        final Path bucket = new Path(String.format("test-%s", new AsciiRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        new B2DirectoryFeature(session, fileid).mkdir(bucket, new TransferStatus());
        final Path folder1 = new Path(bucket, "1-d", EnumSet.of(Path.Type.directory));
        final Path file1 = new Path(folder1, "2-f", EnumSet.of(Path.Type.file));
        new B2TouchFeature(session, fileid).touch(file1, new TransferStatus());

        final AttributedList<Path> list = new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener());
        assertEquals(1, list.size());
        assertEquals(folder1, list.iterator().next());

        new B2DeleteFeature(session, fileid).delete(Arrays.asList(bucket, file1), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDisplayFolderInFolderMissingPlaceholder() throws Exception {
        final Path bucket = new Path(String.format("test-%s", new AsciiRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        new B2DirectoryFeature(session, fileid).mkdir(bucket, new TransferStatus());
        final Path folder1 = new Path(bucket, "1-d", EnumSet.of(Path.Type.directory));
        final Path folder2 = new Path(folder1, "2-d", EnumSet.of(Path.Type.directory));
        final Path file11 = new Path(folder2, "31-f", EnumSet.of(Path.Type.file));
        final Path file12 = new Path(folder2, "32-f", EnumSet.of(Path.Type.file));
        new B2TouchFeature(session, fileid).touch(file11, new TransferStatus());
        new B2TouchFeature(session, fileid).touch(file12, new TransferStatus());

        final AttributedList<Path> list = new B2ObjectListService(session, fileid).list(folder1, new DisabledListProgressListener());
        assertEquals(1, list.size());
        assertEquals(folder2, list.iterator().next());

        new B2DeleteFeature(session, fileid).delete(Arrays.asList(bucket, file11, file12), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testIdenticalNamingFileFolder() throws Exception {
        final Path bucket = new Path(String.format("test-%s", new AsciiRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        new B2DirectoryFeature(session, fileid).mkdir(bucket, new TransferStatus());
        final String name = new AsciiRandomStringService().random();
        final Path folder1 = new B2DirectoryFeature(session, fileid).mkdir(new Path(bucket, name, EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file1 = new B2TouchFeature(session, fileid).touch(new Path(bucket, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        final AttributedList<Path> list = new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener());
        assertEquals(2, list.size());
        assertTrue(list.contains(file1));
        assertTrue(list.contains(folder1));
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(file1, folder1, bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListLexicographicSortOrderAssumption() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path directory = new B2DirectoryFeature(session, fileid).mkdir(
                new Path(String.format("test-%s", new AsciiRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        assertTrue(new B2ObjectListService(session, fileid).list(directory, new DisabledListProgressListener()).isEmpty());
        final List<String> files = Arrays.asList(
                "Z", "aa", "0a", "a", "AAA", "B", "~$a", ".c"
        );
        for(String f : files) {
            new B2TouchFeature(session, fileid).touch(new Path(directory, f, EnumSet.of(Path.Type.file)), new TransferStatus());
        }
        files.sort(session.getHost().getProtocol().getListComparator());
        final AttributedList<Path> list = new B2ObjectListService(session, fileid).list(directory, new IndexedListProgressListener() {
            @Override
            public void message(final String message) {
                //
            }

            @Override
            public void visit(final AttributedList<Path> list, final int index, final Path file) {
                assertEquals(files.get(index), file.getName());
            }
        });
        for(int i = 0; i < list.size(); i++) {
            assertEquals(files.get(i), list.get(i).getName());
            new B2DeleteFeature(session, fileid).delete(Collections.singletonList(list.get(i)), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
