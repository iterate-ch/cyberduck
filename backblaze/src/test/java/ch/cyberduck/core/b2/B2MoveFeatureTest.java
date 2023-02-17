package ch.cyberduck.core.b2;

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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2MoveFeatureTest extends AbstractB2Test {

    @Test
    public void testMove() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path container = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = new AlphanumericRandomStringService().random();
        final Path test = new B2TouchFeature(session, fileid).touch(new Path(container, name, EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        assertTrue(new B2FindFeature(session, fileid).find(test));
        final Path target = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new B2MoveFeature(session, fileid).move(test, target, new TransferStatus().withLength(0L), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertNotEquals(test.attributes().getVersionId(), target.attributes().getVersionId());
        assertFalse(new B2FindFeature(session, fileid).find(new Path(container, name, EnumSet.of(Path.Type.file))));
        assertTrue(new B2FindFeature(session, fileid).find(target));
        new B2DeleteFeature(session, fileid).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
