package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ctera.model.DirectIO;
import ch.cyberduck.core.dav.DAVDeleteFeature;
import ch.cyberduck.core.dav.DAVReadFeature;
import ch.cyberduck.core.dav.DAVUploadFeature;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.shared.DefaultDownloadFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class CteraDirectIOReadFeatureTest {

    protected CteraSession session;

    @After
    public void disconnect() throws Exception {
        session.close();
    }

    @Before
    public void setup() throws Exception {
        final Host host = new Host(new CteraProtocol(), "dcdirect.ctera.me", new Credentials(
                System.getenv("CTERA_USER"), System.getenv("CTERA_PASSWORD"), null
        ));
        host.setDefaultPath("/ServicesPortal/webdav/My Files");
        session = new CteraSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
    }

    @Test
    public void testTransferInfo() throws Exception {
        final DirectIO directio = new DirectIO();
        final List<DirectIO.Chunk> chunks = new ArrayList<>();
        {
            final DirectIO.Chunk chunk = new DirectIO.Chunk();
            chunk.len = 500;
            chunk.url = "chunk1";
            chunks.add(chunk);
        }
        {
            final DirectIO.Chunk chunk = new DirectIO.Chunk();
            chunk.len = 100;
            chunk.url = "chunk2";
            chunks.add(chunk);
        }
        directio.chunks = chunks;

        final CteraDirectIOReadFeature read = new CteraDirectIOReadFeature(session, new CteraFileIdProvider(session));
        {
            // Full read
            final CteraDirectIOReadFeature.TransferInfo info = read.getTransferInfo(directio, new TransferStatus());
            assertEquals(2, info.chunks.size());
            assertEquals(0, info.offset);
        }
        {
            // Full read with append and explicit values
            final CteraDirectIOReadFeature.TransferInfo info = read.getTransferInfo(directio, new TransferStatus().setAppend(true).setOffset(0).setLength(600));
            assertEquals(2, info.chunks.size());
            assertEquals(0, info.offset);
        }
        {
            // Full read of first chunk
            final CteraDirectIOReadFeature.TransferInfo info = read.getTransferInfo(directio, new TransferStatus().setAppend(true).setOffset(0).setLength(500));
            assertEquals(1, info.chunks.size());
            assertEquals(0, info.offset);
        }
        {
            // First chunk plus a few bytes
            final CteraDirectIOReadFeature.TransferInfo info = read.getTransferInfo(directio, new TransferStatus().setAppend(true).setOffset(0).setLength(510));
            assertEquals(2, info.chunks.size());
            assertEquals(0, info.offset);
        }
        {
            // Within first chunk only with offset
            final CteraDirectIOReadFeature.TransferInfo info = read.getTransferInfo(directio, new TransferStatus().setAppend(true).setOffset(100).setLength(250));
            assertEquals(1, info.chunks.size());
            assertEquals(100, info.offset);
        }
        {
            // Offset from second chunk until end
            final CteraDirectIOReadFeature.TransferInfo info = read.getTransferInfo(directio, new TransferStatus().setAppend(true).setOffset(500).setLength(100));
            assertEquals(1, info.chunks.size());
            assertEquals(0, info.offset);
        }
        {
            // Offset from second chunk until end
            final CteraDirectIOReadFeature.TransferInfo info = read.getTransferInfo(directio, new TransferStatus().setAppend(true).setOffset(510).setLength(90));
            assertEquals(1, info.chunks.size());
            assertEquals(10, info.offset);
        }
    }

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final TransferStatus status = new TransferStatus();
        try {
            new DAVReadFeature(session).read(new Path(new DefaultHomeFinderService(session).find(), "nosuchname", EnumSet.of(Path.Type.file)), status, new DisabledConnectionCallback());
        }
        catch(NotfoundException e) {
            assertTrue(StringUtils.startsWith(e.getDetail(), "Unexpected response"));
            throw e;
        }
    }

    @Test
    public void testReadChunkedTransfer() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final byte[] content = RandomUtils.nextBytes(923);
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        out.close();
        new DAVUploadFeature(session).upload(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledProgressListener(), new DisabledStreamListener(),
                new TransferStatus().setLength(content.length),
                new DisabledConnectionCallback());
        // Unknown length in status
        final TransferStatus status = new TransferStatus() {
            @Override
            public TransferStatus setLength(long length) {
                assertEquals(923L, length);
                // Ignore update. As with unknown length for chunked transfer
                return this;
            }
        };
        new DefaultDownloadFeature(session.getFeature(Read.class)).download(test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), status, new DisabledLoginCallback());
        assertEquals(923L, local.attributes().getSize());
        new DAVDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadAllhunks() throws Exception {
        final Path test = new CteraTouchFeature(session).touch(new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final byte[] content = RandomUtils.nextBytes(10 * 1024 * 1024); // 10MB -> 3 chunks
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        out.close();
        new DAVUploadFeature(session).upload(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledProgressListener(), new DisabledStreamListener(),
                new TransferStatus().setLength(content.length),
                new DisabledConnectionCallback());

        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        final CteraFileIdProvider fileid = new CteraFileIdProvider(session);
        final InputStream in = new CteraDirectIOReadFeature(session, fileid).read(test, status, new DisabledConnectionCallback());
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length);
        new StreamCopier(status, status).transfer(in, buffer);
        in.close();
        assertArrayEquals(content, buffer.toByteArray());
        new CteraDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadInterrupt() throws Exception {
        final byte[] content = RandomUtils.nextBytes(32769);
        final TransferStatus writeStatus = new TransferStatus();
        writeStatus.setLength(content.length);
        final CteraFileIdProvider fileid = new CteraFileIdProvider(session);
        final Path folder = new CteraDirectoryFeature(session).mkdir(
                new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new Path(folder, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final CteraWriteFeature writer = new CteraWriteFeature(session);
        final HttpResponseOutputStream<Void> out = writer.write(test, writeStatus, new DisabledConnectionCallback());
        assertNotNull(out);
        new StreamCopier(writeStatus, writeStatus).transfer(new ByteArrayInputStream(content), out);
        // Unknown length in status
        final TransferStatus readStatus = new TransferStatus();
        // Read a single byte
        {
            final InputStream in = new CteraDirectIOReadFeature(session, fileid).read(test, readStatus, new DisabledConnectionCallback());
            assertNotNull(in.read());
            in.close();
        }
        {
            final InputStream in = new CteraDirectIOReadFeature(session, fileid).read(test, readStatus, new DisabledConnectionCallback());
            assertNotNull(in);
            in.close();
        }
        new CteraDeleteFeature(session).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testReadRange() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Local local = new Local(System.getProperty("java.io.tmpdir"), new AlphanumericRandomStringService().random());
        final byte[] content = RandomUtils.nextBytes(1023);
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        out.close();
        new DAVUploadFeature(session).upload(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledProgressListener(), new DisabledStreamListener(),
                new TransferStatus().setLength(content.length),
                new DisabledConnectionCallback());
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setAppend(true);
        status.setOffset(100L);
        final InputStream in = new CteraDirectIOReadFeature(session, new CteraFileIdProvider(session)).read(test, status.setLength(content.length - 100), new DisabledConnectionCallback());
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new CteraDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

}
