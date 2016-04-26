package ch.cyberduck.core;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import org.junit.Test;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.SocketException;
import java.security.cert.CertificateException;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class DefaultIOExceptionMappingServiceTest {

    @Test
    public void testMap() throws Exception {
        assertEquals(ConnectionCanceledException.class,
                new DefaultIOExceptionMappingService().map(new SocketException("Software caused connection abort")).getClass());
        assertEquals(ConnectionCanceledException.class,
                new DefaultIOExceptionMappingService().map(new SocketException("Socket closed")).getClass());
    }

    @Test
    public void testSSLHandshakeCertificateDismissed() {
        final SSLHandshakeException c = new SSLHandshakeException("f");
        c.initCause(new CertificateException("c"));
        assertEquals(ConnectionCanceledException.class,
                new DefaultIOExceptionMappingService().map(c).getClass());
    }

    @Test
    public void testPlaceholder() throws Exception {
        final BackgroundException e = new DefaultIOExceptionMappingService().map("{0} message", new SocketException("s"),
                new Path("/n", EnumSet.of(Path.Type.directory, Path.Type.volume)));
        assertEquals("N message.", e.getMessage());
        assertEquals("S. The connection attempt was rejected. The server may be down, or your network may not be properly configured.", e.getDetail());
    }

    @Test
    public void testSameMessageInRootCause() throws Exception {
        assertEquals("S. The connection attempt was rejected. The server may be down, or your network may not be properly configured.", new DefaultIOExceptionMappingService().map(new IOException("s", new SocketException("s")))
                .getDetail());
        assertEquals("The connection attempt was rejected. The server may be down, or your network may not be properly configured.", new DefaultIOExceptionMappingService().map(new IOException("s", new SocketException(null)))
                .getDetail());
        assertEquals("S. The connection attempt was rejected. The server may be down, or your network may not be properly configured.", new DefaultIOExceptionMappingService().map(new IOException(null, new SocketException("s")))
                .getDetail());
    }

    @Test
    public void testMapPathName() throws Exception {
        final DefaultIOExceptionMappingService s = new DefaultIOExceptionMappingService();
        assertEquals("Download n failed.", s.map("Download {0} failed", new SocketException("s"),
                new Path("/n", EnumSet.of(Path.Type.directory, Path.Type.volume))).getMessage());
        assertEquals("Download failed n.", s.map("Download failed {0}", new SocketException("s"),
                new Path("/n", EnumSet.of(Path.Type.directory, Path.Type.volume))).getMessage());
        assertEquals("Download failed (/n).", s.map("Download failed", new SocketException("s"),
                new Path("/n", EnumSet.of(Path.Type.directory, Path.Type.volume))).getMessage());
    }

    @Test
    public void testMapWrappedCause() throws Exception {
        final DefaultIOExceptionMappingService s = new DefaultIOExceptionMappingService();
        final BackgroundException cause = new BackgroundException();
        assertSame(cause, s.map(new IOException(cause)));
    }
}