/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

package ch.cyberduck.core.openstack;

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
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

import ch.iterate.openstack.swift.model.StorageObject;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class SwiftLargeUploadWriteFeatureTest extends AbstractSwiftTest {

    @Test
    public void testWriteUploadLargeBuffer() throws Exception {
        final SwiftRegionService regionService = new SwiftRegionService(session);
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        {
            final TransferStatus status = new TransferStatus();
            status.setLength(-1L);
            final HttpResponseOutputStream<StorageObject> out = new SwiftLargeUploadWriteFeature(session, regionService,
                    new SwiftSegmentService(session, ".segments-test/")).write(file, status, new DisabledConnectionCallback());
            final byte[] content = RandomUtils.nextBytes(6 * 1024 * 1024);
            final ByteArrayInputStream in = new ByteArrayInputStream(content);
            final TransferStatus progress = new TransferStatus();
            final BytecountStreamListener count = new BytecountStreamListener();
            new StreamCopier(new TransferStatus(), progress).withListener(count).transfer(in, out);
            assertEquals(content.length, count.getSent());
            assertEquals(content.length, out.getStatus().getSize(), 0L);
            assertTrue(new SwiftFindFeature(session).find(file));
            final byte[] compare = new byte[content.length];
            final InputStream stream = new SwiftReadFeature(session, regionService).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
            IOUtils.readFully(stream, compare);
            stream.close();
            assertArrayEquals(content, compare);
        }
        {
            final TransferStatus status = new TransferStatus();
            status.setLength(-1L);
            final HttpResponseOutputStream<StorageObject> out = new SwiftLargeUploadWriteFeature(session, regionService,
                    new SwiftSegmentService(session, ".segments-test/")).write(file, status, new DisabledConnectionCallback());
            final byte[] content = RandomUtils.nextBytes(6 * 1024 * 1024);
            final ByteArrayInputStream in = new ByteArrayInputStream(content);
            final TransferStatus progress = new TransferStatus();
            final BytecountStreamListener count = new BytecountStreamListener();
            new StreamCopier(new TransferStatus(), progress).withListener(count).transfer(in, out);
            assertEquals(content.length, count.getSent());
            assertEquals(content.length, out.getStatus().getSize(), 0L);
            assertTrue(new SwiftFindFeature(session).find(file));
            final byte[] compare = new byte[content.length];
            final InputStream stream = new SwiftReadFeature(session, regionService).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
            IOUtils.readFully(stream, compare);
            stream.close();
            assertArrayEquals(content, compare);
        }
        new SwiftDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWriteZeroLength() throws Exception {
        final SwiftRegionService regionService = new SwiftRegionService(session);
        final SwiftLargeUploadWriteFeature feature = new SwiftLargeUploadWriteFeature(session, regionService,
                new SwiftSegmentService(session, ".segments-test/"));
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final byte[] content = RandomUtils.nextBytes(0);
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final HttpResponseOutputStream<StorageObject> out = feature.write(file, status, new DisabledConnectionCallback());
        final ByteArrayInputStream in = new ByteArrayInputStream(content);
        assertEquals(content.length, IOUtils.copyLarge(in, out));
        in.close();
        out.close();
        assertNotNull(out.getStatus());
        assertEquals(0L, out.getStatus().getSize(), 0L);
        assertTrue(new DefaultFindFeature(session).find(file));
        final byte[] compare = new byte[content.length];
        final InputStream stream = new SwiftReadFeature(session, regionService).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
        IOUtils.readFully(stream, compare);
        stream.close();
        assertArrayEquals(content, compare);
        new SwiftDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testTruncate() throws Exception {
        final SwiftRegionService regionService = new SwiftRegionService(session);
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final SwiftLargeUploadWriteFeature feature = new SwiftLargeUploadWriteFeature(session, regionService,
                new SwiftSegmentService(session, ".segments-test/"));
        {
            final TransferStatus status = new TransferStatus();
            status.setLength(-1L);
            final HttpResponseOutputStream<StorageObject> out = feature.write(file, status, new DisabledConnectionCallback());
            final byte[] content = RandomUtils.nextBytes(6 * 1024 * 1024);
            final ByteArrayInputStream in = new ByteArrayInputStream(content);
            final TransferStatus progress = new TransferStatus();
            final BytecountStreamListener count = new BytecountStreamListener();
            new StreamCopier(new TransferStatus(), progress).withListener(count).transfer(in, out);
            assertEquals(content.length, count.getSent());
            assertEquals(content.length, out.getStatus().getSize(), 0L);
            assertTrue(new SwiftFindFeature(session).find(file));
            final byte[] compare = new byte[content.length];
            final InputStream stream = new SwiftReadFeature(session, regionService).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
            IOUtils.readFully(stream, compare);
            stream.close();
            assertArrayEquals(content, compare);
        }
        {
            final byte[] content = RandomUtils.nextBytes(0);
            final TransferStatus status = new TransferStatus();
            status.setLength(-1L);
            status.setExists(true);
            status.setMetadata(new SwiftAttributesFinderFeature(session, regionService).find(file).getMetadata());
            final HttpResponseOutputStream<StorageObject> out = feature.write(file, status, new DisabledConnectionCallback());
            final ByteArrayInputStream in = new ByteArrayInputStream(content);
            assertEquals(content.length, IOUtils.copyLarge(in, out));
            in.close();
            out.close();
            assertNotNull(out.getStatus());
            assertEquals(0L, out.getStatus().getSize(), 0L);
            assertTrue(new DefaultFindFeature(session).find(file));
            final byte[] compare = new byte[content.length];
            final InputStream stream = new SwiftReadFeature(session, regionService).read(file, new TransferStatus().withLength(content.length), new DisabledConnectionCallback());
            IOUtils.readFully(stream, compare);
            stream.close();
            assertArrayEquals(content, compare);
            new SwiftDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }
}
