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

import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.io.output.ProxyOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final AtomicInteger outputBytesCount = new AtomicInteger(0);

    private InputStream inputStreamWrapper;
    private OutputStream outputStreamWrapper;

    public FTPSocket(final Socket delegate) {
        this.delegate = delegate;
    }

    @Override
    public synchronized void close() throws IOException {
        if(delegate.isClosed()) {
            log.debug("Socket already closed {}", delegate);
            return;
        }
        try {
            // Only do full TCP shutdown if we have output bytes, otherwise close socket directly
            if(outputBytesCount.get() > 0) {
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

                    log.debug("Waiting for input to close for socket {}", delegate);
                    int bytesRead = 0;
                    // Read until EOF (server FIN) or timeout
                    while(delegate.getInputStream().read() != -1) {
                        bytesRead++;
                    }
                    if(bytesRead > 0) {
                        log.debug("Drained {} bytes from socket {}", bytesRead, delegate);
                    }
                }
            }
        }
        finally {
            log.debug("Closing socket {}", delegate);
            // Work around macOS quirk where Java NIO's SocketDispatcher.close0() has a 1,000ms delay
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
    public synchronized OutputStream getOutputStream() throws IOException {
        if(outputStreamWrapper == null) {
            outputStreamWrapper = new ProxyOutputStream(delegate.getOutputStream()) {
                @Override
                public void close() throws IOException {
                    // We can't call super.close() as it would call delegate.close()
                    // Therefore, we flush here and close the underlying stream ourselves
                    try {
                        super.flush();
                    }
                    finally {
                        FTPSocket.this.close();
                    }
                }

                @Override
                protected void afterWrite(final int n) {
                    outputBytesCount.addAndGet(n);
                }
            };
        }
        return outputStreamWrapper;
    }

    @Override
    public synchronized InputStream getInputStream() throws IOException {
        if(inputStreamWrapper == null) {
            inputStreamWrapper = new ProxyInputStream(delegate.getInputStream()) {
                @Override
                public void close() throws IOException {
                    // super.close() will call delegate.close(), so override it with ours instead
                    FTPSocket.this.close();
                }
            };
        }
        return inputStreamWrapper;
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
