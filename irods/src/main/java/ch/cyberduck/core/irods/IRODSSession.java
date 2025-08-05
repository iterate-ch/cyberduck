package ch.cyberduck.core.irods;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.shared.DefaultPathHomeFeature;
import ch.cyberduck.core.shared.DelegatingHomeFeature;
import ch.cyberduck.core.shared.WorkdirHomeFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.SSLSession;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.connection.QualifiedUsername;
import org.irods.irods4j.low_level.api.IRODSApi;
import org.irods.irods4j.low_level.api.IRODSException;

import java.text.MessageFormat;

public class IRODSSession extends SSLSession<IRODSConnection> {

    static {
        IRODSApi.setApplicationName(PreferencesFactory.get().getProperty("application.name"));
    }

    private static final Logger log = LogManager.getLogger(IRODSSession.class);

    public IRODSSession(final Host h) {
        super(h, new DisabledX509TrustManager(), new DefaultX509KeyManager());
    }

    public IRODSSession(final Host h, final X509TrustManager trust, final X509KeyManager key) {
        super(h, trust, key);
    }

    @Override
    public boolean isConnected() {
        return super.isConnected() && null != client && client.isConnected();
    }

    @Override
    protected IRODSConnection connect(final ProxyFinder proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        final String host = this.host.getHostname();
        final int port = this.host.getPort();
        final String username = this.host.getCredentials().getUsername();
        final String zone = getRegion();

        try {
            log.debug("connecting to iRODS server.");
            log.debug("iRODS server: host=[{}], port=[{}], username=[{}], zone=[{}]", host, port, username, zone);

            IRODSConnection conn = new IRODSConnection(IRODSConnectionUtils.initConnectionOptions(this));
            conn.connect(host, port, new QualifiedUsername(username, zone));
            log.debug("connected to iRODS server successfully.");

            return conn;
        }
        catch(IRODSException e) {
            String msg = String.format("Could not connect to iRODS server at [%s:%d] as [%s#%s]: %s",
                    host, port, username, zone, e.getMessage());
            throw new IRODSExceptionMappingService().map(msg, e);
        }
        catch(Exception e) {
            String msg = String.format("Problem connecting to iRODS server at [%s:%d] as [%s#%s]: %s",
                    host, port, username, zone, e.getMessage());
            throw new DefaultExceptionMappingService().map(msg, e);
        }
    }

    protected String getRegion() {
        if(StringUtils.contains(host.getRegion(), ':')) {
            return StringUtils.splitPreserveAllTokens(host.getRegion(), ':')[0];
        }
        return host.getRegion();
    }

    protected String getResource() {
        if(StringUtils.contains(host.getRegion(), ':')) {
            return StringUtils.splitPreserveAllTokens(host.getRegion(), ':')[1];
        }
        return StringUtils.EMPTY;
    }

    @Override
    public void login(final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        try {
            log.debug("authenticating with iRODS server.");

            final Credentials credentials = host.getCredentials();
            final String password = credentials.getPassword();

            client.authenticate(IRODSConnectionUtils.newAuthPlugin(this), password);
            log.debug("authenticated with iRODS server successfully.");
        }
        catch(Exception e) {
            throw new LoginFailureException(MessageFormat.format(LocaleFactory.localizedString(
                            "Login {0} with username and password", "Credentials"),
                    BookmarkNameProvider.toString(host)), e);
        }
    }

    @Override
    protected void disconnect() {
        try {
            if(null != client) {
                client.disconnect();
                client = null;
                log.debug("disconnected from iRODS server.");
            }
        }
        catch(Exception e) {
            log.error(e.getMessage());
        }

        try {
            super.disconnect();
        }
        catch(BackgroundException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == ListService.class) {
            return (T) new IRODSListService(this);
        }
        if(type == Directory.class) {
            return (T) new IRODSDirectoryFeature(this);
        }
        if(type == Find.class) {
            return (T) new IRODSFindFeature(this);
        }
        if(type == Delete.class) {
            return (T) new IRODSDeleteFeature(this);
        }
        if(type == Read.class) {
            return (T) new IRODSReadFeature(this);
        }
        if(type == Move.class) {
            return (T) new IRODSMoveFeature(this);
        }
        if(type == Upload.class) {
            return (T) new IRODSUploadFeature(this);
        }
        if(type == Touch.class) {
            return (T) new IRODSTouchFeature(this);
        }
        if(type == Copy.class) {
            return (T) new IRODSCopyFeature(this);
        }
        if(type == Home.class) {
            return (T) new DelegatingHomeFeature(new WorkdirHomeFeature(host), new DefaultPathHomeFeature(host), new IRODSHomeFinderService(this));
        }
        if(type == AttributesFinder.class) {
            return (T) new IRODSAttributesFinderFeature(this);
        }
        if(type == Write.class) {
            return (T) new IRODSWriteFeature(this);
        }
        if(type == Timestamp.class) {
            return (T) new IRODSTimestampFeature(this);
        }
        return super._getFeature(type);
    }

}
