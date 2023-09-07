package ch.cyberduck.core.b2;

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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import synapticloop.b2.response.B2FileResponse;
import synapticloop.b2.response.B2FinishLargeFileResponse;
import synapticloop.b2.response.BaseB2Response;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2LargeUploadWriteFeatureTest extends AbstractB2Test {

    @Test
    public void testWrite() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final B2LargeUploadWriteFeature feature = new B2LargeUploadWriteFeature(session, fileid);
        final Path container = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        status.setModified(1503654614004L);
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final StatusOutputStream<BaseB2Response> out = feature.write(file, status, new DisabledConnectionCallback());
        final byte[] content = RandomUtils.nextBytes(6 * 1024 * 1024);
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        assertEquals(content.length, IOUtils.copy(in, out));
        in.close();
        out.close();
        assertEquals(content.length, ((B2FinishLargeFileResponse) out.getStatus()).getContentLength(), 0L);
        assertTrue(new B2FindFeature(session, fileid).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new B2ReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        assertEquals(1503654614004L, new B2AttributesFinderFeature(session, fileid).find(file).getModificationDate());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteLowerMinimumSize() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final B2LargeUploadWriteFeature feature = new B2LargeUploadWriteFeature(session, fileid);
        final Path container = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        status.setModified(1503654614004L);
        final Path file = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final StatusOutputStream<BaseB2Response> out = feature.write(file, status, new DisabledConnectionCallback());
        final byte[] content = RandomUtils.nextBytes(2 * 1024 * 1024);
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        assertEquals(content.length, IOUtils.copy(in, out));
        in.close();
        out.close();
        assertEquals(content.length, ((B2FileResponse) out.getStatus()).getContentLength(), 0L);
        assertTrue(new B2FindFeature(session, fileid).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new B2ReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        assertEquals(1503654614004L, new B2AttributesFinderFeature(session, fileid).find(file).getModificationDate());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteZeroLength() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final B2LargeUploadWriteFeature feature = new B2LargeUploadWriteFeature(session, fileid);
        final Path container = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final byte[] content = RandomUtils.nextBytes(0);
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        status.setModified(1503654614004L);
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final StatusOutputStream<BaseB2Response> out = feature.write(file, status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        assertEquals(content.length, IOUtils.copyLarge(in, out));
        in.close();
        out.close();
        assertNotNull(out.getStatus());
        assertEquals(content.length, ((B2FileResponse) out.getStatus()).getContentLength(), 0L);
        assertTrue(new DefaultFindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new B2ReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        assertEquals(1503654614004L, new B2AttributesFinderFeature(session, fileid).find(file).getModificationDate());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
