/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

package ch.cyberduck.core.ftp;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class FTPClientKeepAliveTest {

    @FunctionalInterface
    private interface Script {
        void run(BufferedReader in, BufferedWriter out) throws Exception;
    }

    private static void reply(final BufferedWriter out, final String line) throws IOException {
        out.write(line + "\r\n");
        out.flush();
    }

    private static void run(final Script script, final ClientAction action) throws Exception {
        final AtomicReference<Throwable> failure = new AtomicReference<>();
        try(final ServerSocket server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            final Thread thread = new Thread(() -> {
                try(final Socket socket = server.accept();
                    final BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                    final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
                    reply(out, "220 Ready");
                    script.run(in, out);
                }
                catch(Throwable t) {
                    failure.set(t);
                }
            });
            thread.start();
            final FTPClient client = new FTPClient(new FTPProtocol(), null, null);
            client.setControlKeepAliveTimeout(Duration.ofMillis(1));
            client.setControlKeepAliveReplyTimeout(Duration.ofMillis(100));
            client.setDefaultTimeout(5000);
            client.connect(InetAddress.getLoopbackAddress(), server.getLocalPort());
            try {
                action.run(client);
            }
            finally {
                client.disconnect();
                thread.join(5000);
            }
        }
        if(failure.get() != null) {
            throw new AssertionError(failure.get());
        }
    }

    @FunctionalInterface
    private interface ClientAction {
        void run(FTPClient client) throws Exception;
    }

    @Test
    public void testReplyReceivedInTime() throws Exception {
        run((in, out) -> {
            assertEquals("NOOP", in.readLine());
            reply(out, "200 NOOP ok");
            reply(out, "226 Transfer complete");
        }, client -> {
            Thread.sleep(5);
            client.keepAlive();
            assertEquals(226, client.getTransferReply());
            assertEquals(226, client.getTransferReplyCode());
        });
    }

    @Test
    public void testSlowReplyDoesNotBlockAndIsSkipped() throws Exception {
        run((in, out) -> {
            assertEquals("NOOP", in.readLine());
            Thread.sleep(600);
            reply(out, "200 NOOP ok");
            reply(out, "226 Transfer complete");
            assertEquals("NOOP", in.readLine());
            reply(out, "200 Next command");
        }, client -> {
            Thread.sleep(5);
            final long start = System.nanoTime();
            client.keepAlive();
            assertTrue("Must not wait for the full reply", Duration.ofNanos(System.nanoTime() - start).toMillis() < 500);
            assertEquals(226, client.getTransferReply());
            assertTrue(client.getTransferReplyString().startsWith("226"));
            // Control channel in sync
            assertEquals(200, client.noop());
            assertTrue(client.getReplyString().contains("Next command"));
        });
    }

    @Test
    public void testTransferReplyBeforeLateKeepAliveReply() throws Exception {
        run((in, out) -> {
            assertEquals("NOOP", in.readLine());
            Thread.sleep(300);
            reply(out, "226 Transfer complete");
            reply(out, "200 NOOP ok");
            assertEquals("NOOP", in.readLine());
            reply(out, "200 Next command");
        }, client -> {
            Thread.sleep(5);
            client.keepAlive();
            assertEquals(226, client.getTransferReply());
            assertTrue(client.getTransferReplyString().startsWith("226"));
            assertEquals(200, client.noop());
            assertTrue(client.getReplyString().contains("Next command"));
        });
    }

    @Test
    public void testFailedTransferReplyReported() throws Exception {
        run((in, out) -> {
            assertEquals("NOOP", in.readLine());
            Thread.sleep(300);
            reply(out, "200 NOOP ok");
            reply(out, "426 Connection closed");
        }, client -> {
            Thread.sleep(5);
            client.keepAlive();
            assertEquals(426, client.getTransferReply());
            assertTrue(client.getTransferReplyString().startsWith("426"));
        });
    }

    @Test
    public void testNoKeepAliveBeforeIdleTimeout() throws Exception {
        run((in, out) -> reply(out, "226 Transfer complete"), client -> {
            client.setControlKeepAliveTimeout(Duration.ofMinutes(5));
            client.keepAlive();
            assertEquals(226, client.getTransferReply());
        });
    }
}
