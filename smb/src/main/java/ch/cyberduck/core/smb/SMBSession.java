package ch.cyberduck.core.smb;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

public class SMBSession extends ch.cyberduck.core.Session<SMBClient> {

    private SMBClient client;
    private Connection connection;
    private DiskShare share;

    public SMBSession(final Host h, final X509TrustManager trust, final X509KeyManager key) {
        super(h);
        client = new SMBClient();
    }

    private static final Logger log = LogManager.getLogger(SMBSession.class);

    @Override
    protected SMBClient connect(Proxy proxy, HostKeyCallback key, LoginCallback prompt, CancelCallback cancel)
            throws BackgroundException {
        try (Connection connection = client.connect(getHost().getHostname())) {
            this.connection = connection;
            AuthenticationContext ac = new AuthenticationContext(host.getCredentials().getUsername(), host.getCredentials().getPassword().toCharArray(), "EDU");
            Session session = connection.authenticate(ac);

            // Connect to Share
            try (DiskShare share = (DiskShare) session.connectShare("data")) {
                this.share = share;
            }
        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
        return client;
    }

    @Override
    public void login(Proxy proxy, LoginCallback prompt, CancelCallback cancel) throws BackgroundException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'login'");
    }

    @Override
    protected void logout() throws BackgroundException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'logout'");
    }

    @Override
    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

}
