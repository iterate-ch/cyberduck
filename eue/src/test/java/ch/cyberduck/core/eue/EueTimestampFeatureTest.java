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
import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
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
        final PathAttributes attr = new EueAttributesFinderFeature(session, fileid).find(file);
        assertNotEquals(PathAttributes.EMPTY, attr);
        assertNotNull(attr.getETag());
        final long modified = Instant.now().minusSeconds(5 * 24 * 60 * 60).getEpochSecond() * 1000;
        new EueTimestampFeature(session, fileid).setTimestamp(file, modified);
        final PathAttributes updated = new EueAttributesFinderFeature(session, fileid).find(file);
        assertEquals(modified, updated.getModificationDate());
        assertNotEquals(attr.getETag(), updated.getETag());
        assertEquals(attr.getChecksum(), updated.getChecksum());
        assertEquals(modified, new DefaultAttributesFinderFeature(session).find(file).getModificationDate());
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSetTimestampDirectory() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final Path container = new EueDirectoryFeature(session, fileid).mkdir(new Path(
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final long containerModification = new EueAttributesFinderFeature(session, fileid).find(container).getModificationDate();
        final Path folder = new EueDirectoryFeature(session, fileid).mkdir(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), null);
        final long folderModification = new EueAttributesFinderFeature(session, fileid).find(folder).getModificationDate();
        assertNotNull(new EueAttributesFinderFeature(session, fileid).find(folder));
        final long modified = Instant.now().minusSeconds(5 * 24 * 60 * 60).getEpochSecond() * 1000;
        new EueTimestampFeature(session, fileid).setTimestamp(folder, modified);
        assertEquals(modified, new EueAttributesFinderFeature(session, fileid).find(folder).getModificationDate());
        // Write file to directory and see if timestamp changes
        final Path file = new EueTouchFeature(session, fileid).touch(new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(modified, new EueAttributesFinderFeature(session, fileid).find(folder).getModificationDate());
        assertEquals(containerModification, new EueAttributesFinderFeature(session, fileid).find(container).getModificationDate());
        final byte[] content = RandomUtils.nextBytes(8235);
        final long ts = System.currentTimeMillis();
        final TransferStatus status = new TransferStatus().withLength(content.length).withModified(ts);
        final Checksum checksum = new EueWriteFeature(session, fileid).checksum(file, status).compute(new ByteArrayInputStream(content), new TransferStatus().withLength(content.length));
        status.withChecksum(checksum);
        final HttpResponseOutputStream<EueWriteFeature.Chunk> out = new EueWriteFeature(session, fileid).write(file, status.exists(true), new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        final TransferStatus progress = new TransferStatus();
        final BytecountStreamListener count = new BytecountStreamListener();
        new StreamCopier(new TransferStatus(), progress).withListener(count).transfer(in, out);
        assertEquals(containerModification, new EueAttributesFinderFeature(session, fileid).find(container).getModificationDate());
        assertNotEquals(folderModification, new EueAttributesFinderFeature(session, fileid).find(folder).getModificationDate());
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
