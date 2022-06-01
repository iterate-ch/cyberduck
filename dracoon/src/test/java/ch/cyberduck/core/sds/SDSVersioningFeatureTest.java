package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DefaultPathPredicate;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SDSVersioningFeatureTest extends AbstractSDSTest {

    @Test
    public void testRevert() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final SDSAttributesFinderFeature f = new SDSAttributesFinderFeature(session, nodeid);
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final PathAttributes initialAttributes = new PathAttributes(test.attributes());
        final String initialVersion = test.attributes().getVersionId();
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setExists(true);
        final SDSDirectS3MultipartWriteFeature writer = new SDSDirectS3MultipartWriteFeature(session, nodeid);
        final StatusOutputStream<Node> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        assertNotNull(test.attributes().getVersionId());
        assertNotEquals(initialVersion, test.attributes().getVersionId());
        final SDSVersioningFeature feature = new SDSVersioningFeature(session, nodeid);
        final AttributedList<Path> versions = feature.list(test, new DisabledListProgressListener());
        assertEquals(1, versions.size());
        assertEquals(new Path(test).withAttributes(initialAttributes), versions.get(0));
        assertTrue(new SDSFindFeature(session, nodeid).find(versions.get(0)));
        assertEquals(initialVersion, new SDSAttributesFinderFeature(session, nodeid).find(versions.get(0)).getVersionId());
        feature.revert(new Path(test).withAttributes(initialAttributes));
        final Path reverted = new SDSListService(session, nodeid).list(room, new DisabledListProgressListener()).find(new DefaultPathPredicate(new Path(test).withAttributes(initialAttributes)));
        assertEquals(initialVersion, reverted.attributes().getVersionId());
        // Restored file is no longer in list of deleted items
        assertEquals(1, feature.list(reverted, new DisabledListProgressListener()).size());
        assertEquals(new Path(test).withAttributes(status.getResponse()), feature.list(reverted, new DisabledListProgressListener()).get(0));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
