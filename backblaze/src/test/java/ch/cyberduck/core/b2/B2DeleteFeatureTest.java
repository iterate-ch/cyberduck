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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

@Category(IntegrationTest.class)
public class B2DeleteFeatureTest extends AbstractB2Test {

    @Test(expected = NotfoundException.class)
    public void testDeleteNotFound() throws Exception {
        final Path test = new Path(new B2HomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new B2DeleteFeature(session, new B2FileidProvider(session).withCache(cache)).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDelete() throws Exception {
        final Path bucket = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        new B2DirectoryFeature(session, fileid).mkdir(bucket, null, new TransferStatus());
        final Path file = new Path(bucket, String.format("%s %s", UUID.randomUUID().toString(), "1"), EnumSet.of(Path.Type.file));
        new B2TouchFeature(session, fileid).touch(file, new TransferStatus());
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(bucket, file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDeletePlaceholder() throws Exception {
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), null, new TransferStatus());
        final Path directory = new B2DirectoryFeature(session, fileid).mkdir(new Path(bucket, String.format("%s %s", UUID.randomUUID().toString(), "1"), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(bucket, directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
