package ch.cyberduck.core.smb;

import ch.cyberduck.core.ConnectionTimeoutFactory;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxySocketFactory;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.hierynomus.protocol.transport.TransportException;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.common.SMBRuntimeException;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

public class SMBSession extends ch.cyberduck.core.Session<SMBClient> {
    private static final Logger log = LogManager.getLogger(SMBSession.class);

    protected Connection connection;
    protected DiskShare share;
    protected Session session;

    public SMBSession(final Host h) {
        super(h);
        SmbConfig config = SmbConfig.builder()
                .withSocketFactory(new ProxySocketFactory(h))
                .withTimeout(ConnectionTimeoutFactory.get(new HostPreferences(h)).getTimeout(), TimeUnit.SECONDS)
                .withSoTimeout(ConnectionTimeoutFactory.get(new HostPreferences(h)).getTimeout(), TimeUnit.SECONDS)
                .build();
        client = new SMBClient(config);
    }

    @Override
    protected SMBClient connect(Proxy proxy, HostKeyCallback key, LoginCallback prompt, CancelCallback cancel)
            throws BackgroundException {

        try {
            this.connection = client.connect(getHost().getHostname(), getHost().getPort());
        }
        catch(IOException e) {
            throw new BackgroundException(e);
        }
        return client;
    }

    @Override
    public void login(Proxy proxy, LoginCallback prompt, CancelCallback cancel) throws BackgroundException {
        if(connection == null) {
            throw new BackgroundException(new IllegalStateException("Connection must be established before login"));
        }
        final String domain, username, shareString;

        String[] parts = host.getCredentials().getUsername().split("/", 0);
        final String domainUsername = parts[0];
        if(parts.length > 1) {
            shareString = parts[1];
        }
        else {
            throw new BackgroundException("Share name missing", "Share name must be specified after /");
        }

        parts = domainUsername.split("@", 0);
        if(parts.length == 0) {
            throw new BackgroundException("Username missing", "Username must be specified");
        }
        else if(parts.length == 1) {
            username = parts[0];
            domain = "WORKGROUP";
        }
        else {
            username = parts[0];
            domain = parts[1];
        }

        final AuthenticationContext ac = new AuthenticationContext(username, host.getCredentials().getPassword().toCharArray(), domain);

        try {
            session = connection.authenticate(ac);
            share = (DiskShare) session.connectShare(shareString);
        }
        catch(SMBRuntimeException e) {
            throw new BackgroundException(e);
        }

    }

    @Override
    protected void logout() throws BackgroundException {
        if(session != null) {
            try {
                session.logoff();
            }
            catch(SMBRuntimeException|TransportException e) {
                throw new BackgroundException(e);
            }
        }
    }

    @Override
    protected void disconnect() {
        try {
            if(connection != null) {
                connection.close();
                connection = null;
            }
            client.close();
        }
        catch(IOException e) {
            log.warn(String.format("Ignore disconnect failure %s", e.getMessage()));
        }
        super.disconnect();

    }

    @Override
    public boolean isConnected() {
        return connection != null && connection.isConnected();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == ListService.class) {
            return (T) new SMBListService(this);
        }
        if(type == Directory.class) {
            return (T) new SMBDirectoryFeature(this);
        }
        if(type == Touch.class) {
            return (T) new SMBTouchFeature(null);
        }
        if(type == Delete.class) {
            return (T) new SMBDeleteFeature(this);
        }
        return super._getFeature(type);
    }

}
