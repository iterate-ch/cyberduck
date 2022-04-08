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

import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;
import ch.cyberduck.ui.browser.SearchFilter;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GoogleStorageCopyFeatureTest extends AbstractGoogleStorageTest {

    @Test
    public void testCopyFileZeroLength() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        test.attributes().setSize(0L);
        new GoogleStorageTouchFeature(session).touch(test, new TransferStatus().withMime("application/cyberduck").withMetadata(Collections.singletonMap("cyberduck", "set")));
        final Path copy = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        new GoogleStorageCopyFeature(session).copy(test, copy, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertTrue(new GoogleStorageFindFeature(session).find(test));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertTrue(new GoogleStorageFindFeature(session).find(copy));
        assertEquals("set",
            new GoogleStorageMetadataFeature(session).getMetadata(copy).get("cyberduck"));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyFile() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final TransferStatus status = new TransferStatus();
        status.setMetadata(Collections.singletonMap("cyberduck", "m"));
        final Path test = new GoogleStorageTouchFeature(session).touch(new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), status);
        assertNotNull(test.attributes().getVersionId());
        final Path copy = new GoogleStorageCopyFeature(session).copy(test,
                new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertNotEquals(test.attributes().getVersionId(), copy.attributes().getVersionId());
        assertTrue(new GoogleStorageFindFeature(session).find(test));
        assertEquals("m", new GoogleStorageMetadataFeature(session).getMetadata(copy).get("cyberduck"));
        assertEquals(1, new GoogleStorageObjectListService(session).list(container, new DisabledListProgressListener())
                .filter(new SearchFilter(copy.getName())).size());
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertTrue(new GoogleStorageFindFeature(session).find(copy));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
