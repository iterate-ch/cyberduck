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

package ch.cyberduck.core.gmxcloud;

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class GmxcloudTimestampFeatureTest extends AbstractGmxcloudTest {

    @Test
    public void testSetTimestampFile() throws Exception {
        final GmxcloudIdProvider fileid = new GmxcloudIdProvider(session);
        final Path container = new GmxcloudDirectoryFeature(session, fileid).mkdir(new Path(
            String.format("/My files/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = "This is simple test data".getBytes(StandardCharsets.UTF_8);
        createFile(file, content);
        assertNotNull(new GmxcloudAttributesFinderFeature(session, fileid).find(file));
        final long modified = Instant.now().minusSeconds(5 * 24 * 60 * 60).getEpochSecond() * 1000;
        new GmxcloudTimestampFeature(session, fileid).setTimestamp(file, modified);
        assertEquals(modified, new GmxcloudAttributesFinderFeature(session, fileid).find(file).getModificationDate());
        assertEquals(modified, new DefaultAttributesFinderFeature(session).find(file).getModificationDate());
        new GmxcloudDeleteFeature(session, fileid).delete(Collections.singletonList(new Path("/My files", EnumSet.of(AbstractPath.Type.directory))), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSetTimestampDirectory() throws Exception {
        final GmxcloudIdProvider fileid = new GmxcloudIdProvider(session);
        final Path room = new GmxcloudDirectoryFeature(session, fileid).mkdir(new Path(
            String.format("/My files/%s", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new GmxcloudDirectoryFeature(session, fileid).mkdir(test, null);
        assertNotNull(new GmxcloudAttributesFinderFeature(session, fileid).find(test));
        final long modified = Instant.now().minusSeconds(5 * 24 * 60 * 60).getEpochSecond() * 1000;
        new GmxcloudTimestampFeature(session, fileid).setTimestamp(test, modified);
        assertEquals(modified, new GmxcloudAttributesFinderFeature(session, fileid).find(test).getModificationDate());
        new GmxcloudDeleteFeature(session, fileid).delete(Collections.singletonList(new Path("/My files", EnumSet.of(AbstractPath.Type.directory))), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
