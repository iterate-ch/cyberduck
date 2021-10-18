package ch.cyberduck.core.eue;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class EueTimestampFeatureTest extends AbstractEueSessionTest {

    @Test
    public void testSetTimestampFile() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final Path container = new EueDirectoryFeature(session, fileid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path file = new EueTouchFeature(session, fileid)
                .touch(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        assertNotEquals(PathAttributes.EMPTY, new EueAttributesFinderFeature(session, fileid).find(file));
        final long modified = Instant.now().minusSeconds(5 * 24 * 60 * 60).getEpochSecond() * 1000;
        new EueTimestampFeature(session, fileid).setTimestamp(file, modified);
        assertEquals(modified, new EueAttributesFinderFeature(session, fileid).find(file).getModificationDate());
        assertEquals(modified, new DefaultAttributesFinderFeature(session).find(file).getModificationDate());
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSetTimestampDirectory() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final Path container = new EueDirectoryFeature(session, fileid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new EueDirectoryFeature(session, fileid).mkdir(test, null);
        assertNotNull(new EueAttributesFinderFeature(session, fileid).find(test));
        final long modified = Instant.now().minusSeconds(5 * 24 * 60 * 60).getEpochSecond() * 1000;
        new EueTimestampFeature(session, fileid).setTimestamp(test, modified);
        assertEquals(modified, new EueAttributesFinderFeature(session, fileid).find(test).getModificationDate());
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
