package ch.cyberduck.core.dav;

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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DefaultX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Server ignoring the Range header and responding with 200 and chunked transfer encoding (unknown length).
 * Does not require a testcontainer.
 */
public class DAVReadFeatureIgnoredRangeTest {

    private static final int OFFSET = 100;

    private byte[] read(final byte[] content, final TransferStatus status, final AtomicReference<String> range) throws Exception {
        final HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", exchange -> {
            range.set(exchange.getRequestHeaders().getFirst("Range"));
            // Length 0 selects chunked transfer encoding, ignoring the range
            exchange.sendResponseHeaders(200, 0);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(content);
            }
        });
        server.start();
        try {
            final Host host = new Host(new DAVProtocol(), server.getAddress().getHostString(), server.getAddress().getPort());
            final DAVSession session = new DAVSession(host, new DefaultX509TrustManager(), new DefaultX509KeyManager());
            session.open(new DisabledProxyFinder(), HostKeyCallback.noop, new DisabledLoginCallback(), CancelCallback.noop);
            try (InputStream in = new DAVReadFeature(session).read(
                    new Path("/test", EnumSet.of(Path.Type.file)), status, ConnectionCallback.noop)) {
                return IOUtils.toByteArray(in);
            }
            finally {
                session.close();
            }
        }
        finally {
            server.stop(0);
        }
    }

    @Test
    public void testReadRangeIgnoredUnknownLength() throws Exception {
        final byte[] content = RandomUtils.nextBytes(1023);
        final AtomicReference<String> range = new AtomicReference<>();
        final TransferStatus status = new TransferStatus()
                .setAppend(true)
                .setOffset(OFFSET)
                .setLength(TransferStatus.UNKNOWN_LENGTH);
        final byte[] read = this.read(content, status, range);
        assertEquals("bytes=100-", range.get());
        assertArrayEquals(Arrays.copyOfRange(content, OFFSET, content.length), read);
    }

    @Test
    public void testReadRangeIgnoredKnownLength() throws Exception {
        final byte[] content = RandomUtils.nextBytes(1023);
        final AtomicReference<String> range = new AtomicReference<>();
        final TransferStatus status = new TransferStatus()
                .setAppend(true)
                .setOffset(OFFSET)
                .setLength(content.length - OFFSET);
        final byte[] read = this.read(content, status, range);
        assertArrayEquals(Arrays.copyOfRange(content, OFFSET, content.length), read);
    }

    @Test
    public void testReadOffsetZeroNotSkipped() throws Exception {
        final byte[] content = RandomUtils.nextBytes(1023);
        final TransferStatus status = new TransferStatus()
                .setAppend(true)
                .setOffset(0L)
                .setLength(TransferStatus.UNKNOWN_LENGTH);
        final byte[] read = this.read(content, status, new AtomicReference<>());
        assertArrayEquals(content, read);
    }
}
