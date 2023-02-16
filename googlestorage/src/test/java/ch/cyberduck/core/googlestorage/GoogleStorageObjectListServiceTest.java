package ch.cyberduck.core.googlestorage;

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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.IndexedListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GoogleStorageObjectListServiceTest extends AbstractGoogleStorageTest {

    @Test
    public void testListObjects() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final AttributedList<Path> list = new GoogleStorageObjectListService(session).list(container, new DisabledListProgressListener());
        for(Path p : list) {
            assertSame(container, p.getParent());
            if(p.isFile()) {
                assertNotEquals(-1L, p.attributes().getModificationDate());
                assertNotEquals(-1L, p.attributes().getSize());
                assertNotNull(p.attributes().getETag());
            }
        }
    }

    @Test
    public void testListLexicographicSortOrderAssumption() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path directory = new GoogleStorageDirectoryFeature(session).mkdir(
                new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new GoogleStorageObjectListService(session).list(directory, new DisabledListProgressListener()).isEmpty());
        final List<String> files = new ArrayList<>(Arrays.asList(
                "Z", "aa", "0a", "a", "AAA", "B", "~$a", ".c"
        ));
        for(String f : files) {
            new GoogleStorageTouchFeature(session).touch(new Path(directory, f, EnumSet.of(Path.Type.file)), new TransferStatus());
        }
        final List<String> folders = Arrays.asList("b", "BB");
        for(String f : folders) {
            new GoogleStorageDirectoryFeature(session).mkdir(new Path(directory, f, EnumSet.of(Path.Type.directory)), new TransferStatus());
        }
        files.addAll(folders);
        files.sort(session.getHost().getProtocol().getListComparator());
        final AttributedList<Path> list = new GoogleStorageObjectListService(session).list(directory, new IndexedListProgressListener() {
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
            new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(list.get(i)), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListEncodedCharacter() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final Path placeholder = new GoogleStorageTouchFeature(session).touch(
                new Path(container, String.format("^<%%%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new GoogleStorageObjectListService(session).list(container, new DisabledListProgressListener()).contains(placeholder));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListInvisibleCharacter() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final Path placeholder = new GoogleStorageTouchFeature(session).touch(
                new Path(container, String.format("test-\u001F-%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new GoogleStorageObjectListService(session).list(container, new DisabledListProgressListener()).contains(placeholder));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListFilePlusCharacter() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final Path placeholder = new GoogleStorageTouchFeature(session).touch(
                new Path(container, String.format("test+%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new GoogleStorageObjectListService(session).list(container, new DisabledListProgressListener()).contains(placeholder));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(placeholder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testListPlaceholderPlusCharacter() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("us-east-1");
        final Path directory = new GoogleStorageDirectoryFeature(session).mkdir(
                new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path placeholder = new GoogleStorageDirectoryFeature(session).mkdir(
                new Path(directory, String.format("test+%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new GoogleStorageObjectListService(session).list(directory, new DisabledListProgressListener()).contains(placeholder));
        assertTrue(new GoogleStorageObjectListService(session).list(placeholder, new DisabledListProgressListener()).isEmpty());
        new GoogleStorageDeleteFeature(session).delete(Arrays.asList(placeholder, directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
