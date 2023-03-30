package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class B2TimestampFeatureTest extends AbstractB2Test {

    @Test
    public void testSetTimestampFile() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new B2TouchFeature(session, fileid).touch(new Path(bucket,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final TransferStatus status = new TransferStatus().withTimestamp(5000L);
        new B2TimestampFeature(session, fileid).setTimestamp(file, status);
        final PathAttributes attr = new B2AttributesFinderFeature(session, fileid).find(file);
        assertEquals(5000L, attr.getModificationDate());
        assertEquals(attr, status.getResponse());
        assertEquals(5000L, new DefaultAttributesFinderFeature(session).find(file).getModificationDate());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSetTimestampDirectory() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new B2DirectoryFeature(session, fileid).mkdir(new Path(bucket,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new B2TimestampFeature(session, fileid).setTimestamp(file, 5000L);
        assertEquals(5000L, new B2AttributesFinderFeature(session, fileid).find(file).getModificationDate());
        assertEquals(5000L, new DefaultAttributesFinderFeature(session).find(file).getModificationDate());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}