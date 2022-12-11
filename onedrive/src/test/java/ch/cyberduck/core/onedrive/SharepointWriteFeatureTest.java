package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.onedrive.features.GraphDeleteFeature;
import ch.cyberduck.core.onedrive.features.GraphDirectoryFeature;
import ch.cyberduck.core.onedrive.features.GraphReadFeature;
import ch.cyberduck.core.onedrive.features.GraphTouchFeature;
import ch.cyberduck.core.onedrive.features.GraphWriteFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.nuxeo.onedrive.client.types.DriveItem;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SharepointWriteFeatureTest extends AbstractSharepointTest {

    @Test
    public void testWrite() throws Exception {
        final GraphWriteFeature feature = new GraphWriteFeature(session, fileid);
        final ListService list = new SharepointListService(session, fileid);
        final AttributedList<Path> drives = list.list(new Path(SharepointListService.DEFAULT_NAME, "Drives", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        final Path container = drives.get(0);
        final Path folder = new GraphDirectoryFeature(session, fileid).mkdir(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final PathAttributes folderAttributes = new GraphAttributesFinderFeature(session, fileid).find(folder);
        final String folderEtag = folderAttributes.getETag();
        final long folderTimestamp = folderAttributes.getModificationDate();
        final byte[] content = RandomUtils.nextBytes(5 * 1024 * 1024);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path file = new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final String id = new GraphTouchFeature(session, fileid).touch(file, new TransferStatus()).attributes().getFileId();
        final StatusOutputStream<DriveItem.Metadata> out = feature.write(file, status, new DisabledConnectionCallback());
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        assertNotNull(out.getStatus());
        assertTrue(status.isComplete());
        assertNotSame(PathAttributes.EMPTY, status.getResponse());
        assertEquals(Protocol.DirectoryTimestamp.explicit, session.getHost().getProtocol().getDirectoryTimestamp());
        assertEquals(folderEtag, new GraphAttributesFinderFeature(session, fileid).find(folder).getETag());
        assertEquals(folderTimestamp, new GraphAttributesFinderFeature(session, fileid).find(folder).getModificationDate());
        assertTrue(new DefaultFindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new GraphReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        final Path copy = new Path(file);
        copy.attributes().setCustom(Collections.emptyMap());
        assertEquals(id, fileid.getFileId(copy));
        // Overwrite
        final StatusOutputStream<DriveItem.Metadata> overwrite = feature.write(file, status.exists(true), new DisabledConnectionCallback());
        assertNotNull(overwrite);
        assertEquals(content.length, IOUtils.copyLarge(new ByteArrayInputStream(content), overwrite));
        overwrite.close();
        assertEquals(new GraphAttributesFinderFeature(session, fileid).toAttributes(overwrite.getStatus()), new GraphAttributesFinderFeature(session, fileid).find(file));
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
