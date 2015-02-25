package ch.cyberduck.core.irods;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.SSLSession;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.log4j.Logger;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.IRODSFileSystemAO;

/**
 * @version $Id$
 */
public class IRODSSession extends SSLSession<IRODSFileSystem> {
    private static final Logger log = Logger.getLogger(IRODSSession.class);

    private IRODSFileSystemAO irodsFileSystemAO;

    public IRODSSession(final Host h) {
        super(h, new DisabledX509TrustManager(), new DefaultX509KeyManager());
    }

    public IRODSSession(final Host h, final X509TrustManager trust, final X509KeyManager key) {
        super(h, trust, key);
    }

    @Override
    protected IRODSFileSystem connect(final HostKeyCallback key) throws BackgroundException {
        try {
            return IRODSFileSystem.instance();
        } catch(JargonException e) {
            throw new IRODSExceptionMappingService().map(e);
        }
    }

    @Override
    public void login(final PasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel, final Cache<Path> cache) throws BackgroundException {
        try {
            final IRODSAccount irodsAccount = IRODSAccount.instance(host.getHostname(), host.getPort(),
                    host.getCredentials().getUsername(), host.getCredentials().getPassword(),
                    getHomeDir(), host.getRegion(), "");

            final AuthResponse authResponse = client.getIRODSAccessObjectFactory().authenticateIRODSAccount(irodsAccount);
            if (authResponse.isSuccessful()) {
                irodsFileSystemAO = client.getIRODSAccessObjectFactory().getIRODSFileSystemAO(authResponse.getAuthenticatedIRODSAccount());
            } else {
                // TODO research auth further
                throw new LoginFailureException(String.format("Login failed for user %s", host.getCredentials().getUsername()));
            }
        } catch(JargonException e) {
            throw new IRODSExceptionMappingService().map(e);
        }
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            client.close();
            irodsFileSystemAO = null;
            client = null;
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
        return super.getFeature(type);
    }

    public final IRODSFileSystemAO getIrodsFileSystemAO() {
        return irodsFileSystemAO;
    }

    public final IRODSAccount getIRODSAccount() {
        return irodsFileSystemAO.getIRODSAccount();
    }

    private String getHomeDir() {
        // TODO extend DefaultHomeFinderService
        return new StringBuilder()
                .append(Path.DELIMITER).append(host.getRegion())
                .append(Path.DELIMITER).append("home")
                .append(Path.DELIMITER).append(host.getCredentials().getUsername())
                .toString();
    }

}
