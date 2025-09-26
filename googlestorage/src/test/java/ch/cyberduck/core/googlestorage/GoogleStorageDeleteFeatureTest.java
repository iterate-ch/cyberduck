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
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GoogleStorageDeleteFeatureTest extends AbstractGoogleStorageTest {

    @Test(expected = NotfoundException.class)
    public void testDeleteNotFoundKey() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new GoogleStorageDeleteFeature(session, new GoogleStorageVersioningFeature(session)).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDeleteContainer() throws Exception {
        final Path container = new Path(new AsciiRandomStringService().random().toLowerCase(Locale.ROOT), EnumSet.of(Path.Type.volume, Path.Type.directory));
        final GoogleStorageVersioningFeature versioning = new GoogleStorageVersioningFeature(session);
        new GoogleStorageDirectoryFeature(session, versioning).mkdir(new GoogleStorageWriteFeature(session, versioning), container, new TransferStatus().setRegion("us"));
        assertTrue(new GoogleStorageFindFeature(session, versioning).find(container));
        new GoogleStorageDeleteFeature(session, versioning).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new GoogleStorageFindFeature(session, versioning).find(container));
    }

    @Test
    public void testDeletedWithMarker() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final GoogleStorageVersioningFeature versioning = new GoogleStorageVersioningFeature(session);
        final Path directory = new GoogleStorageDirectoryFeature(session, versioning).mkdir(new GoogleStorageWriteFeature(session, versioning), new Path(container,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path test = new GoogleStorageTouchFeature(session).touch(new GoogleStorageWriteFeature(session, versioning), new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(test.attributes().getVersionId());
        assertNotEquals(PathAttributes.EMPTY, new GoogleStorageAttributesFinderFeature(session, versioning).find(test));
        // Add delete marker
        new GoogleStorageDeleteFeature(session, versioning).delete(Collections.singletonList(new Path(test).withAttributes(PathAttributes.EMPTY)), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertTrue(new GoogleStorageAttributesFinderFeature(session, versioning).find(test).isDuplicate());
        assertFalse(new GoogleStorageFindFeature(session, versioning).find(new Path(test).withAttributes(PathAttributes.EMPTY)));
        assertFalse(new DefaultFindFeature(session).find(new Path(test).withAttributes(PathAttributes.EMPTY)));
        // Test reading delete marker itself
        final Path marker = new GoogleStorageObjectListService(session, versioning).list(directory, new DisabledListProgressListener()).find(new SimplePathPredicate(test));
        assertTrue(marker.attributes().isDuplicate());
        assertTrue(new GoogleStorageAttributesFinderFeature(session, versioning).find(marker).isDuplicate());
        assertTrue(new DefaultAttributesFinderFeature(session).find(marker).isDuplicate());
        assertTrue(new GoogleStorageFindFeature(session, versioning).find(marker));
        new GoogleStorageDeleteFeature(session, versioning).delete(Collections.singletonList(new Path(directory).withAttributes(PathAttributes.EMPTY)), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}
