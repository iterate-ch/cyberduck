package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.EnumSet;

import synapticloop.b2.response.BaseB2Response;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2VersioningFeatureTest extends AbstractB2Test {

    @Test
    public void testRevert() throws Exception {
        final B2VersionIdProvider nodeid = new B2VersionIdProvider(session);
        final Path room = new B2DirectoryFeature(session, nodeid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final B2AttributesFinderFeature f = new B2AttributesFinderFeature(session, nodeid);
        final Path test = new B2TouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final PathAttributes initialAttributes = new PathAttributes(test.attributes());
        final String initialVersion = test.attributes().getVersionId();
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setExists(true);
        final B2WriteFeature writer = new B2WriteFeature(session, nodeid);
        final StatusOutputStream<BaseB2Response> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        assertNotNull(test.attributes().getVersionId());
        assertNotEquals(initialVersion, test.attributes().getVersionId());
        final B2VersioningFeature feature = new B2VersioningFeature(session, nodeid);
        {
            final AttributedList<Path> versions = feature.list(test, new DisabledListProgressListener());
            assertEquals(1, versions.size());
            assertEquals(new Path(test).withAttributes(initialAttributes), versions.get(0));
            assertTrue(new B2FindFeature(session, nodeid).find(versions.get(0)));
            assertEquals(initialVersion, new B2AttributesFinderFeature(session, nodeid).find(versions.get(0)).getVersionId());
        }
        final PathAttributes updated = new B2AttributesFinderFeature(session, nodeid).find(test);
        assertNotEquals(initialVersion, updated.getVersionId());
        feature.revert(new Path(test).withAttributes(initialAttributes));
        assertEquals(2, feature.list(test, new DisabledListProgressListener()).size());
        for(Path version : new B2ListService(session, nodeid).list(room, new DisabledListProgressListener())) {
            new B2DeleteFeature(session, nodeid).delete(Collections.singletonList(version), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
        new B2DeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}