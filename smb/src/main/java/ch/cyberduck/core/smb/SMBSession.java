package ch.cyberduck.core.smb;

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

import ch.cyberduck.core.ConnectionTimeoutFactory;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxySocketFactory;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.hierynomus.protocol.transport.TransportException;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.auth.NtlmAuthenticator;
import com.hierynomus.smbj.common.SMBRuntimeException;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;

public class SMBSession extends ch.cyberduck.core.Session<Connection> {
    private static final Logger log = LogManager.getLogger(SMBSession.class);

    protected DiskShare share;
    protected Session session;

    public SMBSession(final Host h) {
        super(h);
    }

    @Override
    protected Connection connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        try {
            final SMBClient client = new SMBClient(SmbConfig.builder()
                    .withSocketFactory(new ProxySocketFactory(host))
                    .withTimeout(ConnectionTimeoutFactory.get(new HostPreferences(host)).getTimeout(), TimeUnit.SECONDS)
                    .withSoTimeout(ConnectionTimeoutFactory.get(new HostPreferences(host)).getTimeout(), TimeUnit.SECONDS)
                    .withAuthenticators(new NtlmAuthenticator.Factory())
                    .withDfsEnabled(true)
                    .build());
            return client.connect(getHost().getHostname(), getHost().getPort());
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final AuthenticationContext context;
        final Credentials credentials = host.getCredentials();
        if(credentials.isAnonymousLogin()) {
            context = AuthenticationContext.guest();
        }
        else {
            final String domain, username;
            if(credentials.getUsername().contains("@")) {
                username = StringUtils.substringBefore(credentials.getUsername(), '@');
                domain = StringUtils.substringAfter(credentials.getUsername(), '@');
            }
            else {
                username = credentials.getUsername();
                domain = new HostPreferences(host).getProperty("smb.domain.default");
            }
            context = new AuthenticationContext(username, credentials.getPassword().toCharArray(), domain);
        }
        try {
            session = client.authenticate(context);
            final String shareName;
            if(StringUtils.isNotBlank(host.getProtocol().getContext())) {
                // Use share name from context in profile
                shareName = host.getProtocol().getContext();
            }
            else {
                shareName = prompt.prompt(host,
                        LocaleFactory.localizedString("Share Name"),
                        LocaleFactory.localizedString("Enter the share name to connect to."),
                        new LoginOptions().icon(host.getProtocol().disk()).keychain(false)).getPassword();
            }
            share = (DiskShare) session.connectShare(shareName);
        }
        catch(SMBRuntimeException e) {
            throw new SMBExceptionMappingService().map(LocaleFactory.localizedString("Login failed", "Credentials"), e);
        }
    }

    @Override
    protected void logout() throws BackgroundException {
        if(session != null) {
            try {
                session.logoff();
            }
            catch(SMBRuntimeException e) {
                throw new SMBExceptionMappingService().map(e);
            }
            catch(TransportException e) {
                throw new BackgroundException(e);
            }
        }
    }

    @Override
    protected void disconnect() {
        try {
            client.close();
        }
        catch(IOException e) {
            log.warn(String.format("Ignore disconnect failure %s", e.getMessage()));
        }
        super.disconnect();

    }

    @Override
    public boolean isConnected() {
        return client != null && client.isConnected() && share.isConnected();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == Find.class) {
            return (T) new SMBFindFeature(this);
        }
        if(type == ListService.class) {
            return (T) new SMBListService(this);
        }
        if(type == Directory.class) {
            return (T) new SMBDirectoryFeature(this);
        }
        if(type == Touch.class) {
            return (T) new SMBTouchFeature(this);
        }
        if(type == Delete.class) {
            return (T) new SMBDeleteFeature(this);
        }
        if(type == Move.class) {
            return (T) new SMBMoveFeature(this);
        }
        if(type == Copy.class) {
            return (T) new SMBCopyFeature(this);
        }
        if(type == Read.class) {
            return (T) new SMBReadFeature(this);
        }
        if(type == Write.class) {
            return (T) new SMBWriteFeature(this);
        }
        if(type == AttributesFinder.class) {
            return (T) new SMBAttributesFinderFeature(this);
        }
        if(type == Timestamp.class) {
            return (T) new SMBTimestampFeature(this);
        }
        if(type == Quota.class) {
            return (T) new SMBQuotaFeature(this);
        }
        return super._getFeature(type);
    }
}
