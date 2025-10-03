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

import ch.cyberduck.core.ConnectionTimeout;
import ch.cyberduck.core.ConnectionTimeoutFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
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
public class FTPSocket extends Socket {
    private static final Logger log = LogManager.getLogger(FTPSocket.class);

    private final Socket delegate;

    private final ConnectionTimeout connectionTimeoutPreferences = ConnectionTimeoutFactory.get();

    public FTPSocket(final Socket delegate) {
        this.delegate = delegate;
    }

    @Override
    public void close() throws IOException {
        if(delegate.isClosed()) {
            log.debug("Socket already closed {}", delegate);
            return;
        }
        try {
            if(delegate.isOutputShutdown()) {
                log.debug("Socket output already closed {}", delegate);
            }
            else if(!delegate.isConnected()) {
                log.debug("Socket is already disconnected {}", delegate);
            }
            else {
                // Shutdown output to send FIN, but keep socket open to receive ACKs
                log.debug("Shutting down output for socket {}", delegate);
                delegate.shutdownOutput();

                // Wait for server FIN by draining any remaining data
                // This ensures all our data packets are ACKed before closing
                log.debug("Draining input for socket {}", delegate);
                InputStream in = delegate.getInputStream();
                byte[] buffer = new byte[8192];
                int bytesRead;
                // Read until EOF (server closes its side) or timeout
                while((bytesRead = in.read(buffer)) != -1) {
                    log.debug("Drained {} bytes from socket {}", bytesRead, delegate);
                }
            }
        }
        catch(IOException e) {
            log.error("Failed to shutdown output for socket {}: {}", delegate, e.getMessage());
        }
        finally {
            log.debug("Closing socket {}", delegate);
            // Work around macOS bug where Java NIO's SocketDispatcher.close0() has a 1,000ms delay
            CompletableFuture.runAsync(() -> {
                try {
                    delegate.close();
                }
                catch(IOException e) {
                    log.error("Error closing socket {}: {}", delegate, e.getMessage());
                }
            });
        }
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        delegate.connect(endpoint);
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        delegate.connect(endpoint, timeout);
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        delegate.bind(bindpoint);
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return delegate.getRemoteSocketAddress();
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return delegate.getLocalSocketAddress();
    }

    @Override
    public SocketChannel getChannel() {
        return delegate.getChannel();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FilterInputStream(delegate.getInputStream()) {
            @Override
            public void close() throws IOException {
                FTPSocket.this.close(); // Call wrapper's close, not delegate's
            }
        };
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return new FilterOutputStream(delegate.getOutputStream()) {
            @Override
            public void close() throws IOException {
                FTPSocket.this.close(); // Call wrapper's close, not delegate's
            }
        };
    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        delegate.setTcpNoDelay(on);
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return delegate.getTcpNoDelay();
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        delegate.setSoLinger(on, linger);
    }

    @Override
    public int getSoLinger() throws SocketException {
        return delegate.getSoLinger();
    }

    @Override
    public void sendUrgentData(int data) throws IOException {
        delegate.sendUrgentData(data);
    }

    @Override
    public void setOOBInline(boolean on) throws SocketException {
        delegate.setOOBInline(on);
    }

    @Override
    public boolean getOOBInline() throws SocketException {
        return delegate.getOOBInline();
    }

    @Override
    public void setSoTimeout(int timeout) throws SocketException {
        delegate.setSoTimeout(timeout);
    }

    @Override
    public int getSoTimeout() throws SocketException {
        return delegate.getSoTimeout();
    }

    @Override
    public void setSendBufferSize(int size) throws SocketException {
        delegate.setSendBufferSize(size);
    }

    @Override
    public int getSendBufferSize() throws SocketException {
        return delegate.getSendBufferSize();
    }

    @Override
    public void setReceiveBufferSize(int size) throws SocketException {
        delegate.setReceiveBufferSize(size);
    }

    @Override
    public int getReceiveBufferSize() throws SocketException {
        return delegate.getReceiveBufferSize();
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        delegate.setKeepAlive(on);
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return delegate.getKeepAlive();
    }

    @Override
    public void setTrafficClass(int tc) throws SocketException {
        delegate.setTrafficClass(tc);
    }

    @Override
    public int getTrafficClass() throws SocketException {
        return delegate.getTrafficClass();
    }

    @Override
    public void setReuseAddress(boolean on) throws SocketException {
        delegate.setReuseAddress(on);
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        return delegate.getReuseAddress();
    }

    @Override
    public void shutdownInput() throws IOException {
        delegate.shutdownInput();
    }

    @Override
    public void shutdownOutput() throws IOException {
        delegate.shutdownOutput();
    }

    @Override
    public boolean isConnected() {
        return delegate.isConnected();
    }

    @Override
    public boolean isBound() {
        return delegate.isBound();
    }

    @Override
    public boolean isClosed() {
        return delegate.isClosed();
    }

    @Override
    public boolean isInputShutdown() {
        return delegate.isInputShutdown();
    }

    @Override
    public boolean isOutputShutdown() {
        return delegate.isOutputShutdown();
    }

    @Override
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        delegate.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    @Override
    public String toString() {
        return "FTPSocket{" + delegate + "}";
    }
}
