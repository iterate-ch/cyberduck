package ch.cyberduck.core.nextcloud;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.dav.DAVPathEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.ocs.OcsCapabilities;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.github.sardine.impl.SardineException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

public class NextcloudChunkedUploadFeatureTest extends AbstractNextcloudTest {

    @Test
    public void testUploadChunks() throws Exception {
        final byte[] content = RandomUtils.nextBytes(12 * 1024 * 1024 + 17);
        final Local local = new Local(Files.createTempFile("chunked", ".bin").toString());
        try (OutputStream out = local.getOutputStream(false)) {
            out.write(content);
        }
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus().setLength(content.length);
        new NextcloudChunkedUploadFeature(session, new OcsCapabilities(), 5L * 1024 * 1024).upload(new NextcloudWriteFeature(session), test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledProgressListener(), new DisabledStreamListener(), status, ConnectionCallback.noop);
        assertTrue(status.isComplete());
        final byte[] read = new byte[content.length];
        try (InputStream in = new NextcloudReadFeature(session).read(test, new TransferStatus().setLength(content.length), ConnectionCallback.noop)) {
            IOUtils.readFully(in, read);
        }
        assertArrayEquals(content, read);
        new NextcloudDeleteFeature(session).delete(Collections.singletonMap(test, new TransferStatus()), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testUploadSingleChunk() throws Exception {
        final byte[] content = RandomUtils.nextBytes(1024);
        final Local local = this.write(content);
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus().setLength(content.length);
        new NextcloudChunkedUploadFeature(session, new OcsCapabilities(), 1024L).upload(new NextcloudWriteFeature(session), test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledProgressListener(), new DisabledStreamListener(), status, ConnectionCallback.noop);
        assertTrue(status.isComplete());
        assertArrayEquals(content, this.read(test, content.length));
        new NextcloudDeleteFeature(session).delete(Collections.singletonMap(test, new TransferStatus()), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    @Test
    public void testUploadChunksModificationDate() throws Exception {
        final byte[] content = RandomUtils.nextBytes(3 * 1024 + 1);
        final Local local = this.write(content);
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final long modified = 1700000000000L;
        final TransferStatus status = new TransferStatus().setLength(content.length).setModified(modified);
        new NextcloudChunkedUploadFeature(session, new OcsCapabilities(), 1024L).upload(new NextcloudWriteFeature(session), test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledProgressListener(), new DisabledStreamListener(), status, ConnectionCallback.noop);
        assertTrue(status.isComplete());
        assertEquals(modified, new NextcloudAttributesFinderFeature(session).find(test).getModificationDate());
        assertArrayEquals(content, this.read(test, content.length));
        new NextcloudDeleteFeature(session).delete(Collections.singletonMap(test, new TransferStatus()), new DisabledLoginCallback(), new Delete.DisabledCallback());
        local.delete();
    }

    /**
     * Assembling fails with missing parent collection of target. Upload collection must be removed.
     */
    @Test
    public void testUploadChunksFailureRemovesUploadCollection() throws Exception {
        final Set<Path> collections = new HashSet<>();
        final byte[] content = RandomUtils.nextBytes(3 * 1024);
        final Local local = this.write(content);
        final Path parent = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(parent, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus().setLength(content.length);
        try {
            new NextcloudChunkedUploadFeature(session, new OcsCapabilities(), 1024L).upload(new NextcloudWriteFeature(session) {
                @Override
                public HttpResponseOutputStream<Void> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                    // Chunks are uploaded to the upload collection
                    collections.add(file.getParent());
                    return super.write(file, status, callback);
                }
            }, test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                    new DisabledProgressListener(), new DisabledStreamListener(), status, ConnectionCallback.noop);
            fail();
        }
        catch(BackgroundException e) {
            // Expected
        }
        assertFalse(status.isComplete());
        assertFalse(collections.isEmpty());
        for(Path collection : collections) {
            try {
                session.getClient().list(new DAVPathEncoder(session).encode(collection), 0);
                fail(String.format("Upload collection %s not removed", collection));
            }
            catch(SardineException e) {
                assertEquals(404, e.getStatusCode());
            }
        }
        local.delete();
    }

    private Local write(final byte[] content) throws Exception {
        final Local local = new Local(Files.createTempFile("chunked", ".bin").toString());
        try (OutputStream out = local.getOutputStream(false)) {
            out.write(content);
        }
        return local;
    }

    private byte[] read(final Path file, final int length) throws Exception {
        final byte[] read = new byte[length];
        try (InputStream in = new NextcloudReadFeature(session).read(file, new TransferStatus().setLength(length), ConnectionCallback.noop)) {
            IOUtils.readFully(in, read);
        }
        return read;
    }
}
