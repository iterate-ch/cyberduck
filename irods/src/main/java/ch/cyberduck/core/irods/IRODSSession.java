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

public class IRODSSession extends SSLSession<IRODSFileSystem> {
    private static final Logger log = Logger.getLogger(IRODSSession.class);

    private IRODSFileSystemAO filesystem;

    public IRODSSession(final Host h) {
        super(h, new DisabledX509TrustManager(), new DefaultX509KeyManager());
    }

    public IRODSSession(final Host h, final X509TrustManager trust, final X509KeyManager key) {
        super(h, trust, key);
    }

    @Override
    protected IRODSFileSystem connect(final HostKeyCallback key) throws BackgroundException {
        try {
            return this.configure(IRODSFileSystem.instance());
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map(e);
        }
    }

    protected IRODSFileSystem configure(final IRODSFileSystem client) {
        final SettableJargonProperties properties = new SettableJargonProperties(client.getJargonProperties());
        properties.setComputeAndVerifyChecksumAfterTransfer(false);
        properties.setEncoding(this.getEncoding());
        properties.setIrodsSocketTimeout(this.timeout());
        properties.setIrodsParallelSocketTimeout(this.timeout());
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
            final String region = this.getRegion();
            final String resource = this.getResource();
            final String user;
            final AuthScheme scheme;
            final Credentials credentials = host.getCredentials();
            if(StringUtils.contains(credentials.getUsername(), ':')) {
                // Support non default auth scheme (PAM)
                user = StringUtils.splitPreserveAllTokens(credentials.getUsername(), ':')[1];
                // Defaults to standard if not found
                scheme = AuthScheme.findTypeByString(StringUtils.splitPreserveAllTokens(credentials.getUsername(), ':')[0]);
            }
            else {
                user = credentials.getUsername();
                // We can default to Standard if not specified
                scheme = AuthScheme.STANDARD;
            }
            final IRODSAccount account;
            try {
                account = new IRODSAccount(host.getHostname(), host.getPort(),
                        user, credentials.getPassword(), new IRODSHomeFinderService(this).find().getAbsolute(), region, resource) {
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
                };
                account.setAuthenticationScheme(scheme);
            }
            catch(IllegalArgumentException e) {
                throw new LoginFailureException(e.getMessage(), e);
            }

            final IRODSAccessObjectFactory factory = client.getIRODSAccessObjectFactory();
            final AuthResponse auth = factory.authenticateIRODSAccount(account);
            if(auth.isSuccessful()) {
                filesystem = factory.getIRODSFileSystemAO(auth.getAuthenticatedIRODSAccount());
            }
            else {
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
            client.close();
            filesystem = null;
            client = null;
        }
        catch(JargonException e) {
            throw new IRODSExceptionMappingService().map(e);
        }
        finally {
            super.logout();
        }
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return new IRODSListService(this).list(directory, listener);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type) {
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
        return super.getFeature(type);
    }

    public final IRODSFileSystemAO filesystem() {
        return filesystem;
    }
}
