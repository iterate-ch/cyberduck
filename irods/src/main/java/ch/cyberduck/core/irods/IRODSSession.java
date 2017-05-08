package ch.cyberduck.core.irods;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.SSLSession;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.irods.jargon.core.connection.AuthScheme;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.IRODSFileSystemAO;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

public class IRODSSession extends SSLSession<IRODSFileSystemAO> {
    private static final Logger log = Logger.getLogger(IRODSSession.class);

    private final Preferences preferences
            = PreferencesFactory.get();

    public IRODSSession(final Host h) {
        super(h, new DisabledX509TrustManager(), new DefaultX509KeyManager());
    }

    public IRODSSession(final Host h, final X509TrustManager trust, final X509KeyManager key) {
        super(h, trust, key);
    }

    @Override
    protected IRODSFileSystemAO connect(final HostKeyCallback key) throws BackgroundException {
        try {
            final IRODSFileSystem fs = this.configure(IRODSFileSystem.instance());
            final IRODSAccessObjectFactory factory = fs.getIRODSAccessObjectFactory();
            final String region = this.getRegion();
            final String resource = this.getResource();
            final Credentials credentials = host.getCredentials();
            try {
                return factory.getIRODSFileSystemAO(new URIEncodingIRODSAccount(credentials.getUsername(), credentials.getPassword(),
                        new IRODSHomeFinderService(IRODSSession.this).find().getAbsolute(), region, resource));
            }
            catch(IllegalArgumentException e) {
                throw new LoginFailureException(e.getMessage(), e);
            }
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map(e);
        }
    }

    protected IRODSFileSystem configure(final IRODSFileSystem client) {
        final SettableJargonProperties properties = new SettableJargonProperties(client.getJargonProperties());
        properties.setEncoding(host.getEncoding());
        final int timeout = preferences.getInteger("connection.timeout.seconds") * 1000;
        properties.setIrodsSocketTimeout(timeout);
        properties.setIrodsParallelSocketTimeout(timeout);
        properties.setGetBufferSize(PreferencesFactory.get().getInteger("connection.chunksize"));
        properties.setPutBufferSize(PreferencesFactory.get().getInteger("connection.chunksize"));
        if(log.isDebugEnabled()) {
            log.debug(String.format("Configure client %s with properties %s", client, properties));
        }
        client.getIrodsSession().setJargonProperties(properties);
        client.getIrodsSession().setX509TrustManager(trust);
        return client;
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
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel,
                      final Cache<Path> cache) throws BackgroundException {
        try {
            final IRODSAccount account = client.getIRODSAccount();
            final Credentials credentials = host.getCredentials();
            account.setUserName(credentials.getUsername());
            account.setPassword(credentials.getPassword());
            final AuthResponse response = client.getIRODSAccessObjectFactory().authenticateIRODSAccount(account);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Connected to %s", response.getStartupResponse()));
            }
            if(!response.isSuccessful()) {
                throw new LoginFailureException(MessageFormat.format(LocaleFactory.localizedString(
                        "Login {0} with username and password", "Credentials"), host.getHostname()));
            }
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map(e);
        }
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            client.getIRODSSession().closeSession();
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map(e);
        }
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return new IRODSListService(this).list(directory, listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
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
            return (T) new IRODSHomeFinderService(this);
        }
        return super._getFeature(type);
    }

    private final class URIEncodingIRODSAccount extends IRODSAccount {
        public URIEncodingIRODSAccount(final String user, final String password, final String home, final String region, final String resource) {
            super(host.getHostname(), host.getPort(), StringUtils.isBlank(user) ? StringUtils.EMPTY : user, password, home, region, resource);
            this.setUserName(user);
        }

        @Override
        public URI toURI(final boolean includePassword) throws JargonException {
            try {
                return new URI(String.format("irods://%s.%s%s@%s:%d%s",
                        this.getUserName(),
                        this.getZone(),
                        includePassword ? String.format(":%s", this.getPassword()) : StringUtils.EMPTY,
                        this.getHost(),
                        this.getPort(),
                        URIEncoder.encode(this.getHomeDirectory())));
            }
            catch(URISyntaxException e) {
                throw new JargonException(e.getMessage());
            }
        }

        @Override
        public void setUserName(final String input) {
            final String user;
            final AuthScheme scheme;
            if(StringUtils.contains(input, ':')) {
                // Support non default auth scheme (PAM)
                user = StringUtils.splitPreserveAllTokens(input, ':')[1];
                // Defaults to standard if not found
                scheme = AuthScheme.findTypeByString(StringUtils.splitPreserveAllTokens(input, ':')[0]);
            }
            else {
                user = input;
                if(StringUtils.isNotBlank(host.getProtocol().getAuthorization())) {
                    scheme = AuthScheme.findTypeByString(host.getProtocol().getAuthorization());
                }
                else {
                    // We can default to Standard if not specified
                    scheme = AuthScheme.STANDARD;
                }
            }
            super.setUserName(user);
            this.setAuthenticationScheme(scheme);
        }
    }
}
