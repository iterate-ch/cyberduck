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
import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.onedrive.features.GraphDeleteFeature;
import ch.cyberduck.core.onedrive.features.GraphReadFeature;
import ch.cyberduck.core.shared.BufferWriteFeature;
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

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class BufferWriteFeatureTest extends AbstractOneDriveTest {

    @Test
    public void testWrite() throws Exception {
        final BufferWriteFeature feature = new BufferWriteFeature(session);
        final Path container = new OneDriveHomeFinderService().find();
        final byte[] content = RandomUtils.nextBytes(5 * 1024);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final StatusOutputStream<Void> out = feature.write(file, status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        final BytecountStreamListener count = new BytecountStreamListener();
        new StreamCopier(status, status).withListener(count).transfer(in, out);
        assertEquals(content.length, count.getSent());
        assertEquals(content.length, status.getLength());
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
    public void testWriteOverwrite() throws Exception {
        final BufferWriteFeature feature = new BufferWriteFeature(session);
        final Path container = new OneDriveHomeFinderService().find();
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        {
            final byte[] content = RandomUtils.nextBytes(42512);
            final TransferStatus status = new TransferStatus();
            status.setLength(content.length);
            final StatusOutputStream<Void> out = feature.write(file, status, new DisabledConnectionCallback());
            final ByteArrayInputStream in = new ByteArrayInputStream(content);
            new StreamCopier(status, status).transfer(in, out);
            assertNull(out.getStatus());
            assertTrue(new DefaultFindFeature(session).find(file));
            final byte[] compare = new byte[content.length];
            final InputStream stream = new GraphReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
            IOUtils.readFully(stream, compare);
            stream.close();
            assertArrayEquals(content, compare);
        }
        {
            final byte[] content = RandomUtils.nextBytes(33221);
            final TransferStatus status = new TransferStatus().exists(true);
            status.setLength(content.length);
            final StatusOutputStream<Void> out = feature.write(file, status, new DisabledConnectionCallback());
            final ByteArrayInputStream in = new ByteArrayInputStream(content);
            new StreamCopier(status, status).transfer(in, out);
            assertNull(out.getStatus());
            assertTrue(new DefaultFindFeature(session).find(file));
            final byte[] compare = new byte[content.length];
            final InputStream stream = new GraphReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
            IOUtils.readFully(stream, compare);
            stream.close();
            assertArrayEquals(content, compare);
        }
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteUnknownLength() throws Exception {
        final BufferWriteFeature feature = new BufferWriteFeature(session);
        final Path container = new OneDriveHomeFinderService().find();
        final byte[] content = RandomUtils.nextBytes(5 * 1024 * 1024);
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final StatusOutputStream<Void> out = feature.write(file, status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        new StreamCopier(status, status).transfer(in, out);
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
    public void testWriteZeroLength() throws Exception {
        final BufferWriteFeature feature = new BufferWriteFeature(session);
        final Path container = new OneDriveHomeFinderService().find();
        final byte[] content = RandomUtils.nextBytes(0);
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final StatusOutputStream<Void> out = feature.write(file, status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        new StreamCopier(status, status).transfer(in, out);
        assertNull(out.getStatus());
        assertTrue(new DefaultFindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new GraphReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
