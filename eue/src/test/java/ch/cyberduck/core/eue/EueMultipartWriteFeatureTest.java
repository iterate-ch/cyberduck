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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.StreamCopier;
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
public class EueMultipartWriteFeatureTest extends AbstractEueSessionTest {

    @Test
    public void testWriteZeroLength() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final EueMultipartWriteFeature feature = new EueMultipartWriteFeature(session, fileid);
        final Path container = new EueDirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final byte[] content = RandomUtils.nextBytes(0);
        final TransferStatus status = new TransferStatus().withLength(-1L);
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final HttpResponseOutputStream<EueWriteFeature.Chunk> out = feature.write(file, status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        assertEquals(content.length, IOUtils.copyLarge(in, out));
        in.close();
        out.close();
        assertTrue(new DefaultFindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new EueReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testLowerThreshold() throws Exception {
        // Uploading a file via the Upload Resource, using the chunked upload method, is only allowed for documents bigger than the chunksize (4MiB)
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final EueMultipartWriteFeature feature = new EueMultipartWriteFeature(session, fileid);
        final Path container = new EueDirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(100072);
        // Multipart
        final TransferStatus status = new TransferStatus().withLength(-1L);
        final Checksum checksum = feature.checksum(file, status).compute(new ByteArrayInputStream(content), new TransferStatus().withLength(content.length));
        final HttpResponseOutputStream<EueWriteFeature.Chunk> out = feature.write(file, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        assertNotNull(out.getStatus());
        assertNotNull(out.getStatus().getCdash64());
        assertEquals(checksum, out.getStatus().getChecksum());
        assertEquals(content.length, out.getStatus().getLength(), 0L);
        final PathAttributes attr = new EueAttributesFinderFeature(session, fileid).find(file);
        assertEquals(attr.getFileId(), out.getStatus().getResourceId());
        assertEquals(content.length, attr.getSize());
        assertTrue(new DefaultFindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new EueReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSmallChunk() throws Exception {
        // Uploading a file via the Upload Resource, using the chunked upload method, is only allowed for documents bigger than the chunksize (4MiB)
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final EueMultipartWriteFeature feature = new EueMultipartWriteFeature(session, fileid);
        final Path container = new EueDirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(512000);
        final TransferStatus status = new TransferStatus().withLength(-1L);
        final Checksum checksum = feature.checksum(file, status).compute(new ByteArrayInputStream(content), new TransferStatus().withLength(content.length));
        final HttpResponseOutputStream<EueWriteFeature.Chunk> out = feature.write(file, status, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        assertNotNull(out.getStatus());
        assertNotNull(out.getStatus().getCdash64());
        assertEquals(checksum, out.getStatus().getChecksum());
        assertEquals(content.length, out.getStatus().getLength(), 0L);
        final PathAttributes attr = new EueAttributesFinderFeature(session, fileid).find(file);
        assertEquals(attr.getFileId(), out.getStatus().getResourceId());
        assertEquals(content.length, attr.getSize());
        final long timestamp = attr.getModificationDate();
        assertEquals(timestamp, new EueListService(session, fileid).list(container, new DisabledListProgressListener()).find(new SimplePathPredicate(file)).attributes().getModificationDate());
        assertTrue(new DefaultFindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new EueReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMultipartWrite() throws Exception {
        final EueResourceIdProvider fileid = new EueResourceIdProvider(session);
        final EueMultipartWriteFeature feature = new EueMultipartWriteFeature(session, fileid);
        final Path container = new EueDirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final long timestamp = System.currentTimeMillis();
        {
            final byte[] content = RandomUtils.nextBytes(8943045);
            final TransferStatus status = new TransferStatus().withLength(-1L).withTimestamp(timestamp);
            final Checksum checksum = feature.checksum(file, status).compute(new ByteArrayInputStream(content), new TransferStatus().withLength(content.length));
            final HttpResponseOutputStream<EueWriteFeature.Chunk> out = feature.write(file, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
            assertNotNull(out.getStatus());
            final PathAttributes attributes = new EueAttributesFinderFeature(session, fileid).find(file);
            assertEquals(timestamp, attributes.getModificationDate());
            assertEquals(attributes.getFileId(), out.getStatus().getResourceId());
            assertNotNull(out.getStatus().getCdash64());
            // Different due to chunking
            assertNotEquals(checksum.hash, out.getStatus().getCdash64());
            assertEquals(content.length, out.getStatus().getLength(), 0L);
            assertTrue(new DefaultFindFeature(session).find(file));
            final byte[] compare = new byte[content.length];
            final InputStream stream = new EueReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
            IOUtils.readFully(stream, compare);
            stream.close();
            assertArrayEquals(content, compare);
        }
        // Override
        {
            final byte[] content = RandomUtils.nextBytes(4943045);
            final TransferStatus status = new TransferStatus().withLength(-1L).exists(true).withTimestamp(timestamp + 1000L);
            final Checksum checksum = feature.checksum(file, status).compute(new ByteArrayInputStream(content), new TransferStatus().withLength(content.length));
            final HttpResponseOutputStream<EueWriteFeature.Chunk> out = feature.write(file, status, new DisabledConnectionCallback());
            assertNotNull(out);
            new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
            assertNotNull(out.getStatus());
            assertNotNull(out.getStatus().getCdash64());
            // Different due to chunking
            assertNotEquals(checksum.hash, out.getStatus().getCdash64());
            assertEquals(content.length, out.getStatus().getLength(), 0L);
            final PathAttributes attributes = new EueAttributesFinderFeature(session, fileid).find(file);
            assertEquals(attributes.getFileId(), out.getStatus().getResourceId());
            assertNotEquals(timestamp, attributes.getModificationDate());
            assertEquals(timestamp + 1000L, attributes.getModificationDate());
            assertTrue(new DefaultFindFeature(session).find(file));
            final byte[] compare = new byte[content.length];
            final InputStream stream = new EueReadFeature(session, fileid).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
            IOUtils.readFully(stream, compare);
            stream.close();
            assertArrayEquals(content, compare);
        }
        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
