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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2DeleteFeatureTest extends AbstractB2Test {

    @Test(expected = NotfoundException.class)
    public void testDeleteNotFound() throws Exception {
        final Path bucket = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new B2DeleteFeature(session, new B2VersionIdProvider(session)).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDeleteFileHide() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus().withLength(0L));
        final Path test = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus().withLength(0L);
        new B2TouchFeature(session, fileid).touch(test, status);
        final String versionId = status.getResponse().getVersionId();
        assertNotNull(versionId);
        // Hide
        new B2DeleteFeature(session, new B2VersionIdProvider(session)).delete(Collections.singletonList(test.withAttributes(PathAttributes.EMPTY)),
                new DisabledLoginCallback(), new Delete.DisabledCallback());
        // Double hide
        try {
            new B2DeleteFeature(session, new B2VersionIdProvider(session)).delete(Collections.singletonList(test.withAttributes(PathAttributes.EMPTY)),
                    new DisabledLoginCallback(), new Delete.DisabledCallback());
            fail();
        }
        catch(InteroperabilityException e) {
            //
        }
        new B2DeleteFeature(session, fileid).delete(new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener()).toList(), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new B2DeleteFeature(session, new B2VersionIdProvider(session)).delete(Collections.singletonList(bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDelete() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus().withLength(0L));
        final Path file = new Path(bucket, String.format("%s %s", new AlphanumericRandomStringService().random(), "1"), EnumSet.of(Path.Type.file));
        new B2TouchFeature(session, fileid).touch(file, new TransferStatus().withLength(0L));
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(bucket, file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new B2FindFeature(session, fileid).find(file));
        assertFalse(new B2FindFeature(session, fileid).find(bucket));
    }

    @Test
    public void testHideAlreadyDeleted() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus().withLength(0L));
        final Path file = new Path(bucket, String.format("%s %s", new AlphanumericRandomStringService().random(), "1"), EnumSet.of(Path.Type.file));
        new B2TouchFeature(session, fileid).touch(file, new TransferStatus().withLength(0L));
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new B2FindFeature(session, fileid).find(file));
        try {
            new B2DeleteFeature(session, fileid).delete(Collections.singletonList(file.withAttributes(PathAttributes.EMPTY)), new DisabledLoginCallback(), new Delete.DisabledCallback());
            fail();
        }
        catch(NotfoundException e) {
            //
        }
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testHide() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus().withLength(0L));
        final Path file = new Path(bucket, String.format("%s %s", new AlphanumericRandomStringService().random(), "1"), EnumSet.of(Path.Type.file));
        new B2TouchFeature(session, fileid).touch(file, new TransferStatus().withLength(0L));
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(file.withAttributes(PathAttributes.EMPTY)), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new B2FindFeature(session, fileid).find(file));
        assertFalse(new DefaultFindFeature(session).find(file));
        new B2DeleteFeature(session, fileid).delete(new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener()).toList(), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new B2DeleteFeature(session, new B2VersionIdProvider(session)).delete(Collections.singletonList(bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDeletePlaceholder() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus().withLength(0L));
        final Path directory = new B2DirectoryFeature(session, fileid).mkdir(new Path(bucket, String.format("%s %s", new AlphanumericRandomStringService().random(), "1"), EnumSet.of(Path.Type.directory)), new TransferStatus().withLength(0L));
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(bucket, directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new B2FindFeature(session, fileid).find(directory));
        assertFalse(new B2FindFeature(session, fileid).find(bucket));
    }
}
