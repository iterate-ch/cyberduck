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
import ch.cyberduck.core.DefaultPathPredicate;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.StreamCopier;
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
            new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume, Path.Type.triplecrypt)), null, new TransferStatus());
        final SDSAttributesFinderFeature f = new SDSAttributesFinderFeature(session, nodeid);
        final Path test = new SDSTouchFeature(session, nodeid).touch(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final SDSMultipartWriteFeature writer = new SDSMultipartWriteFeature(session, nodeid);
        final HttpResponseOutputStream<VersionId> out = writer.write(test, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        final VersionId version = out.getStatus();
        assertNotNull(version);
        assertNotEquals(version.id, test.attributes().getVersionId());
        final PathAttributes updated = new SDSAttributesFinderFeature(session, nodeid, true).find(
            new Path(test.getParent(), test.getName(), test.getType(), new PathAttributes().withVersionId(version.id)));
        assertFalse(updated.getVersions().isEmpty());
        assertEquals(1, updated.getVersions().size());
        assertEquals(test, updated.getVersions().get(0));
        assertTrue(new SDSFindFeature(nodeid).find(updated.getVersions().get(0)));
        assertEquals(test.attributes().getVersionId(), new SDSAttributesFinderFeature(session, nodeid).find(updated.getVersions().get(0)).getVersionId());
        new SDSVersioningFeature(session, nodeid).revert(test);
        final Path reverted = new SDSListService(session, nodeid, true).list(room, new DisabledListProgressListener()).find(new DefaultPathPredicate(test));
        assertEquals(test.attributes().getVersionId(), reverted.attributes().getVersionId());
        assertEquals(1, reverted.attributes().getVersions().size());
        assertEquals(new Path(test.getParent(), test.getName(), test.getType(), new PathAttributes().withVersionId(version.id)), reverted.attributes().getVersions().get(0));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
