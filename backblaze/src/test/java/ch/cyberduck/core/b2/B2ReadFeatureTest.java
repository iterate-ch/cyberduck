package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.io.SHA1ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultDownloadFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import synapticloop.b2.response.B2FileResponse;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2ReadFeatureTest {

    @Test(expected = NotfoundException.class)
    public void testReadNotFound() throws Exception {
        final B2Session session = new B2Session(
                new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final TransferStatus status = new TransferStatus();
        new B2ReadFeature(session).read(new Path(new B2HomeFinderService(session).find(), "nosuchname", EnumSet.of(Path.Type.file)), status);
    }

    @Test
    public void testReadChunkedTransfer() throws Exception {
        final B2Session session = new B2Session(
                new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                        )));
        final LoginConnectionService service = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener());
        service.connect(session, PathCache.empty());
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(923);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setChecksum(new SHA1ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final HttpResponseOutputStream<B2FileResponse> out = new B2WriteFeature(session).write(file, status);
        IOUtils.write(content, out);
        out.close();
        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        assertEquals(-1L, local.attributes().getSize());
        new DefaultDownloadFeature(session).download(file, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                new DisabledStreamListener(), new TransferStatus() {
                    @Override
                    public void setLength(long length) {
                        assertEquals(923L, length);
                        // Ignore update. As with unknown length for chunked transfer
                    }
                }, new DisabledLoginCallback());
        assertEquals(923L, local.attributes().getSize());
        new B2DeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testReadInterrupt() throws Exception {
        final B2Session session = new B2Session(
                new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                        )));
        final LoginConnectionService service = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new DisabledPasswordStore(), new DisabledProgressListener(), new DisabledTranscriptListener());
        service.connect(session, PathCache.empty());
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final byte[] content = RandomUtils.nextBytes(923);
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setChecksum(new SHA1ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final HttpResponseOutputStream<B2FileResponse> out = new B2WriteFeature(session).write(file, status);
        IOUtils.write(content, out);
        out.close();
        {
            // Unknown length in status
            // Read a single byte
            final InputStream in = new B2ReadFeature(session).read(file, new TransferStatus());
            assertNotNull(in.read());
            in.close();
        }
        {
            final InputStream in = new B2ReadFeature(session).read(file, new TransferStatus());
            assertNotNull(in);
            in.close();
        }
        new B2DeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testReadRange() throws Exception {
        final B2Session session = new B2Session(
                new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());

        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new B2TouchFeature(session).touch(test, new TransferStatus());

        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = RandomStringUtils.random(1000).getBytes();
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        out.close();
        new B2SingleUploadService(session, new B2WriteFeature(session)).upload(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                new TransferStatus().length(content.length),
                new DisabledConnectionCallback());
        final TransferStatus status = new TransferStatus();
        status.setLength(content.length);
        status.setAppend(true);
        status.setOffset(100L);
        final InputStream in = new B2ReadFeature(session).read(test, status.length(content.length - 100));
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new B2DeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testReadRangeUnknownLength() throws Exception {
        final B2Session session = new B2Session(
                new Host(new B2Protocol(), new B2Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("b2.user"), System.getProperties().getProperty("b2.key")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());

        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(bucket, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new B2TouchFeature(session).touch(test, new TransferStatus());

        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = RandomStringUtils.random(1000).getBytes();
        final OutputStream out = local.getOutputStream(false);
        assertNotNull(out);
        IOUtils.write(content, out);
        out.close();
        new B2SingleUploadService(session, new B2WriteFeature(session)).upload(
                test, local, new BandwidthThrottle(BandwidthThrottle.UNLIMITED), new DisabledStreamListener(),
                new TransferStatus().length(content.length),
                new DisabledConnectionCallback());
        final TransferStatus status = new TransferStatus();
        status.setLength(-1L);
        status.setAppend(true);
        status.setOffset(100L);
        final InputStream in = new B2ReadFeature(session).read(test, status);
        assertNotNull(in);
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream(content.length - 100);
        new StreamCopier(status, status).transfer(in, buffer);
        final byte[] reference = new byte[content.length - 100];
        System.arraycopy(content, 100, reference, 0, content.length - 100);
        assertArrayEquals(reference, buffer.toByteArray());
        in.close();
        new B2DeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}