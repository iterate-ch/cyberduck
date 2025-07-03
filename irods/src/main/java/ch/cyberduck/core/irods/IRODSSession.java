package ch.cyberduck.core.irods;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.connection.QualifiedUsername;
import org.irods.irods4j.low_level.api.IRODSApi.ConnectionOptions;


/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.ConnectionTimeoutFactory;
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
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.preferences.PreferencesReader;
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


public class IRODSSession extends SSLSession<IRODSConnection> {
    private static final Logger log = LogManager.getLogger(IRODSSession.class);

    public IRODSSession(final Host h) {
        super(h, new DisabledX509TrustManager(), new DefaultX509KeyManager());
    }

    public IRODSSession(final Host h, final X509TrustManager trust, final X509KeyManager key) {
        super(h, trust, key);
    }

    @Override
    protected IRODSConnection connect(final ProxyFinder proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        try {
            final String host = "localhost";
            final int port = 1247;
            final String zone = "tempZone";
            
            ConnectionOptions options=new ConnectionOptions();
         
            
            IRODSConnection conn = new IRODSConnection(options);
            conn.connect(host, port, new QualifiedUsername("rods", zone));
            
            
            return conn;
        } catch (Exception e) {
        	String msg = String.format("exception=[%s], host=[%s], port=[%d], username=[%s], zone=[%s]", e.getMessage(), this.host.getHostname(),this.host.getPort(), this.host.getCredentials().getUsername(),getRegion());
            throw new BackgroundException("Failed to connect to iRODS - "+ msg,e);
        }
    }

    protected ConnectionOptions configure(final ConnectionOptions options) {
    	//TODO update to use configure
        final PreferencesReader preferences = HostPreferencesFactory.get(host);
        options.tcpReceiveBufferSize=preferences.getInteger("connection.chunksize");
        options.tcpSendBufferSize=preferences.getInteger("connection.chunksize");
        
    	return options;
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
            final Credentials credentials = host.getCredentials();
            final String username = credentials.getUsername();
            final String password = credentials.getPassword();
            final String authScheme = StringUtils.defaultIfBlank(
                host.getProtocol().getAuthorization(),
                "native"
            );
            //irods4j authenticate
            client.authenticate(authScheme, password);

            log.debug("Authenticated to iRODS as {}", username);
        }
        catch (Exception e) {
            throw new LoginFailureException(MessageFormat.format(LocaleFactory.localizedString(
                "Login {0} with username and password", "Credentials"),
                BookmarkNameProvider.toString(host)), e);
        }
    }

    @Override
    protected void logout() throws BackgroundException {
    	try {
            if (client != null) {
                client.disconnect();
                client = null;
            }
        } catch (Exception e) {
            throw new BackgroundException("Failed to disconnect from iRODS", e);
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
        if(type == Write.class) {
            return (T) new IRODSWriteFeature(this);
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
        return super._getFeature(type);
    }


}
