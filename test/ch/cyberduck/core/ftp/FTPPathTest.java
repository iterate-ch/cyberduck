package ch.cyberduck.core.ftp;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
 *
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

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class FTPPathTest extends AbstractTestCase {

    @Test
    public void testFallbackDataConnection() throws Exception {
        final Host host = new Host(Protocol.FTP, "mirror.switch.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        host.setFTPConnectMode(FTPConnectMode.PORT);
        final FTPSession session = new FTPSession(host) {
            protected int timeout() {
                return 2000;
            }
        };
        session.open();
        session.login(new DisabledPasswordStore(), new DisabledLoginController());

        final AtomicInteger count = new AtomicInteger();

        final FTPPath path = new FTPPath(session, "/pub/debian/README.html", Path.FILE_TYPE) {
            @Override
            protected <T> T fallback(final DataConnectionAction<T> action) throws ConnectionCanceledException, IOException {
                count.incrementAndGet();
                return super.fallback(action);
            }
        };
        final TransferStatus status = new TransferStatus();
        final FTPPath.DataConnectionAction<Void> action = new FTPPath.DataConnectionAction<Void>() {
            @Override
            public Void run() throws IOException {
                try {
                    assertNotNull(path.read(status));
                    assertEquals(1, count.get());
                }
                catch(BackgroundException e) {
                    fail();
                }
                return null;
            }
        };
        path.data(action);
    }
}
