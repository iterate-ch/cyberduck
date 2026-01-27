package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2025 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import com.amazonaws.internal.DelegateSocket;

import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

/**
 * Socket wrapper that enforces proper TCP shutdown sequence to prevent
 * race conditions due to premature socket closure during FTP data connections.
 * <p>
 * This fixes issues where:
 * - macOS: Intermittent "426 Connection closed; transfer aborted" errors due to socket closure before server ACKs
 * - Windows: Intermittent transfer hanging due to socket closure before client sends FIN with last data packet
 * <p>
 * The proper TCP shutdown sequence is:
 * 1. Call shutdownOutput() to send FIN while keeping socket input open for ACKs
 * 2. Drain input to wait for ACKs until we receive server FIN
 * 3. Close the socket to release resources
 */
public class FTPSocket extends DelegateSocket {
    private static final Logger log = LogManager.getLogger(FTPSocket.class);

    private InputStream inputStreamWrapper;
    private CountingOutputStream outputStreamWrapper;

    public FTPSocket(final Socket sock) {
        super(sock);
    }

    @Override
    public synchronized void close() throws IOException {
        if(sock.isClosed()) {
            log.debug("Socket already closed {}", sock);
            return;
        }
        try {
            // Only do full TCP shutdown if we have output bytes, otherwise close socket directly
            if(outputStreamWrapper != null && outputStreamWrapper.getByteCount() > 0) {
                if(sock.isOutputShutdown()) {
                    log.debug("Socket output already closed {}", sock);
                }
                else if(!sock.isConnected()) {
                    log.debug("Socket is already disconnected {}", sock);
                }
                else {
                    // Shutdown output to send FIN, but keep socket open to receive ACKs
                    log.debug("Shutting down output for socket {}", sock);
                    sock.shutdownOutput();

                    log.debug("Waiting for input to close for socket {}", sock);
                    int bytesRead = 0;
                    // Read until EOF (server FIN) or timeout
                    while(sock.getInputStream().read() != -1) {
                        bytesRead++;
                    }
                    if(bytesRead > 0) {
                        log.warn("Drained {} bytes from socket {}", bytesRead, sock);
                    }
                }
            }
        }
        finally {
            log.debug("Closing socket {}", sock);
            // Work around macOS quirk where Java NIO's SocketDispatcher.close0() has a 1,000ms delay
            CompletableFuture.runAsync(() -> {
                try {
                    sock.close();
                }
                catch(IOException e) {
                    log.error("Error closing socket {}: {}", sock, e.getMessage());
                }
            });
        }
    }

    @Override
    public synchronized OutputStream getOutputStream() throws IOException {
        if(outputStreamWrapper == null) {
            outputStreamWrapper = new CountingOutputStream(sock.getOutputStream()) {
                @Override
                public void close() throws IOException {
                    // We can't call super.close() as it would call sock.close()
                    // Therefore, we flush here and close the underlying stream ourselves
                    try {
                        super.flush();
                    }
                    finally {
                        FTPSocket.this.close();
                    }
                }
            };
        }
        return outputStreamWrapper;
    }

    @Override
    public synchronized InputStream getInputStream() throws IOException {
        if(inputStreamWrapper == null) {
            inputStreamWrapper = new ProxyInputStream(sock.getInputStream()) {
                @Override
                public void close() throws IOException {
                    // super.close() will call sock.close(), so override it with ours instead
                    FTPSocket.this.close();
                }
            };
        }
        return inputStreamWrapper;
    }
}
