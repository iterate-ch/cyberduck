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

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class B2BucketTypeFeatureTest extends AbstractB2Test {

    @Test
    public void getLocation() throws Exception {
        final Path bucket1 = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path bucket2 = new Path(UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final B2FileidProvider fileid = new B2FileidProvider(session).withCache(cache);
        new B2DirectoryFeature(session, fileid).mkdir(bucket1, null, new TransferStatus());
        assertEquals("allPrivate", new B2BucketTypeFeature(session, fileid).getLocation(bucket1).getIdentifier());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(bucket1), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new B2DirectoryFeature(session, fileid).mkdir(bucket2, "allPublic", new TransferStatus());
        assertEquals("allPublic", new B2BucketTypeFeature(session, fileid).getLocation(bucket2).getIdentifier());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(bucket2), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
