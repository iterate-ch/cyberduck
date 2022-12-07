package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class DataConnectionActionExecutorTest extends AbstractFTPTest {

    @Test
    @Ignore
    public void testFallbackDataConnectionSocketTimeout() throws Exception {
        final Host host = new Host(new FTPProtocol(), "mirror.switch.ch", new Credentials(
            PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        host.setFTPConnectMode(FTPConnectMode.active);

        final AtomicInteger count = new AtomicInteger();

        final FTPSession session = new FTPSession(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.getClient().setDefaultTimeout(2000);
        session.getClient().setConnectTimeout(2000);
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path file = new Path("/pub/debian/README.html", EnumSet.of(Path.Type.file));
        final TransferStatus status = new TransferStatus();
        final DataConnectionAction<InputStream> action = new DataConnectionAction<InputStream>() {
            @Override
            public InputStream execute() throws BackgroundException {
                try {
                    final InputStream in = session.getClient().retrieveFileStream(file.getAbsolute());
                    if(count.get() == 0) {
                        throw new FTPExceptionMappingService().map(new SocketTimeoutException());
                    }
                    return in;
                }
                catch(IOException e) {
                    throw new FTPExceptionMappingService().map(e);
                }
            }
        };
        final DataConnectionActionExecutor f = new DataConnectionActionExecutor(session, true) {
            @Override
            protected <T> T fallback(final DataConnectionAction<T> action) throws BackgroundException {
                count.incrementAndGet();
                return super.fallback(action);
            }
        };
        f.data(action, new DisabledProgressListener());
        assertEquals(1, count.get());
    }

    @Test
    public void testFallbackDataConnection500Error() throws Exception {
        session.getHost().setFTPConnectMode(FTPConnectMode.active);
        final AtomicInteger count = new AtomicInteger();
        final DataConnectionAction<Void> action = new DataConnectionAction<Void>() {
            @Override
            public Void execute() throws BackgroundException {
                if(count.get() == 0) {
                    throw new FTPExceptionMappingService().map(new FTPException(500, "m"));
                }
                return null;
            }
        };
        final DataConnectionActionExecutor f = new DataConnectionActionExecutor(session, true) {
            @Override
            protected <T> T fallback(final DataConnectionAction<T> action) throws BackgroundException {
                count.incrementAndGet();
                return super.fallback(action);
            }
        };
        f.data(action, new DisabledProgressListener());
        assertEquals(1, count.get());
    }
}
