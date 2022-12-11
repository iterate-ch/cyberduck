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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class OneDriveWriteFeatureTest extends AbstractOneDriveTest {

    @Test
    public void testWrite() throws Exception {
        final GraphWriteFeature feature = new GraphWriteFeature(session, fileid);
        final Path container = new OneDriveHomeFinderService().find();
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

    @Test
    public void testWriteUmlaut() throws Exception {
        final GraphWriteFeature feature = new GraphWriteFeature(session, fileid);
        final Path container = new OneDriveHomeFinderService().find();
        final byte[] content = RandomUtils.nextBytes(2048);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path file = new Path(container, String.format("%sä", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        final StatusOutputStream<DriveItem.Metadata> out = feature.write(file, status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        assertEquals(content.length, IOUtils.copyLarge(in, out));
        in.close();
        out.close();
        assertNotNull(out.getStatus());
        assertTrue(new DefaultFindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new GraphReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteUmlautZeroLength() throws Exception {
        final GraphWriteFeature feature = new GraphWriteFeature(session, fileid);
        final Path container = new OneDriveHomeFinderService().find();
        final byte[] content = RandomUtils.nextBytes(0);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path file = new Path(container, String.format("%sä", new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        final StatusOutputStream<DriveItem.Metadata> out = feature.write(file, status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        assertEquals(content.length, IOUtils.copyLarge(in, out));
        in.close();
        out.close();
        assertNull(out.getStatus());
        assertTrue(new DefaultFindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new GraphReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteSingleByte() throws Exception {
        final GraphWriteFeature feature = new GraphWriteFeature(session, fileid);
        final Path container = new OneDriveHomeFinderService().find();
        final byte[] content = RandomUtils.nextBytes(1);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final StatusOutputStream<DriveItem.Metadata> out = feature.write(file, status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        assertEquals(content.length, IOUtils.copyLarge(in, out));
        in.close();
        out.close();
        assertNotNull(out.getStatus());
        assertTrue(new DefaultFindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new GraphReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteZeroLength() throws Exception {
        final GraphWriteFeature feature = new GraphWriteFeature(session, fileid);
        final Path container = new OneDriveHomeFinderService().find();
        final byte[] content = RandomUtils.nextBytes(0);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final StatusOutputStream<DriveItem.Metadata> out = feature.write(file, status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        assertEquals(content.length, IOUtils.copyLarge(in, out));
        in.close();
        out.close();
        assertNull(out.getStatus());
        assertTrue(new DefaultFindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new GraphReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = InteroperabilityException.class)
    public void testWriteUnknownLength() throws Exception {
        final GraphWriteFeature feature = new GraphWriteFeature(session, fileid);
        final Path container = new OneDriveHomeFinderService().find();
        final byte[] content = RandomUtils.nextBytes(5 * 1024 * 1024);
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final StatusOutputStream<DriveItem.Metadata> out = feature.write(file, status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        final byte[] buffer = new byte[1 * 1024];
        try {
            assertEquals(content.length, IOUtils.copyLarge(in, out, buffer));
            out.close();
        }
        catch(IOException e) {
            final BackgroundException failure = new DefaultIOExceptionMappingService().map(e);
            assertTrue(failure.getDetail().contains("Invalid Content-Range header value.")
                    || failure.getDetail().contains("Bad Request. The Content-Range header is missing or malformed."));
            throw failure;
        }
    }
}
