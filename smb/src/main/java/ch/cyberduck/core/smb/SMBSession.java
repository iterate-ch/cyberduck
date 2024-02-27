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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.ConnectionTimeoutFactory;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
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
import ch.cyberduck.core.random.SecureRandomProviderFactory;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.hierynomus.protocol.transport.TransportException;
import com.hierynomus.smbj.SMBClient;
import com.hierynomus.smbj.SmbConfig;
import com.hierynomus.smbj.auth.AuthenticationContext;
import com.hierynomus.smbj.auth.NtlmAuthenticator;
import com.hierynomus.smbj.common.SMBRuntimeException;
import com.hierynomus.smbj.connection.Connection;
import com.hierynomus.smbj.session.Session;
import com.hierynomus.smbj.share.DiskShare;
import com.hierynomus.smbj.share.Share;

public class SMBSession extends ch.cyberduck.core.Session<Connection> {
    private static final Logger log = LogManager.getLogger(SMBSession.class);

    private Session session;
    private SMBRootListService shares;

    private final SMBPathContainerService containerService = new SMBPathContainerService(this);
    /**
     * Disk share pool per share name
     */
    private final Map<String, GenericObjectPool<DiskShareWrapper>> pools = new ConcurrentHashMap<>();

    /**
     * Synchronize access to pools
     */
    private final Lock lock = new ReentrantLock();

    public static final class DiskShareWrapper {
        private final DiskShare share;

        private DiskShareWrapper(DiskShare share) {
            this.share = share;
        }

        public DiskShare get() {
            return share;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("DiskShareWrapper{");
            sb.append("share=").append(share);
            sb.append('}');
            return sb.toString();
        }
    }

    private final class DiskSharePool extends GenericObjectPool<DiskShareWrapper> {
        public DiskSharePool(final String shareName) {
            super(new DiskSharePoolObjectFactory(shareName));
            final GenericObjectPoolConfig<DiskShareWrapper> config = new GenericObjectPoolConfig<>();
            config.setJmxEnabled(false);
            config.setBlockWhenExhausted(true);
            config.setMaxIdle(1);
            config.setMaxTotal(Integer.MAX_VALUE);
            this.setConfig(config);
        }
    }

    private final class DiskSharePoolObjectFactory extends BasePooledObjectFactory<DiskShareWrapper> {
        private final String shareName;

        /**
         * Single lock to access disk share of same name created by this pool
         */
        private final Lock lock = new ReentrantLock();

        public DiskSharePoolObjectFactory(final String shareName) {
            this.shareName = shareName;
        }

        @Override
        public DiskShareWrapper create() throws BackgroundException {
            try {
                // Share returned is cached in tree connect table internally
                final Share share = session.connectShare(shareName);
                if(share instanceof DiskShare) {
                    return new DiskShareWrapper((DiskShare) share);
                }
                throw new UnsupportedException(String.format("Unsupported share %s", share.getSmbPath().getShareName()));
            }
            catch(SMBRuntimeException e) {
                throw new SMBExceptionMappingService().map("Cannot read container configuration", e);
            }
        }

        @Override
        public PooledObject<DiskShareWrapper> wrap(final DiskShareWrapper share) {
            return new DefaultPooledObject<>(share);
        }

        @Override
        public void passivateObject(final PooledObject<DiskShareWrapper> object) throws BackgroundException {
            final DiskShareWrapper share = object.getObject();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Passivate share %s", share));
            }
            try {
                lock.unlock();
            }
            catch(IllegalMonitorStateException e) {
                // Not held by current thread
                log.error(String.format("Lock %s not held by current thread %s", lock, Thread.currentThread().getName()));
                throw new DefaultExceptionMappingService().map(e);
            }
        }

        @Override
        public void activateObject(final PooledObject<DiskShareWrapper> object) {
            final DiskShareWrapper share = object.getObject();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Obtain lock for share %s", share));
            }
            lock.lock();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Obtained lock for share %s", share));
            }
        }
    }

    public SMBSession(final Host h) {
        super(h);
    }

    @Override
    protected Connection connect(final Proxy proxy, final HostKeyCallback key, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        try {
            final SMBClient client = new SMBClient(SmbConfig.builder()
                    .withSocketFactory(new ProxySocketFactory(host))
                    .withTimeout(ConnectionTimeoutFactory.get(new HostPreferences(host)).getTimeout(), TimeUnit.SECONDS)
                    .withSoTimeout(new HostPreferences(host).getLong("smb.socket.timeout"), TimeUnit.SECONDS)
                    .withAuthenticators(new NtlmAuthenticator.Factory())
                    .withDfsEnabled(new HostPreferences(host).getBoolean("smb.dfs.enable"))
                    .withEncryptData(new HostPreferences(host).getBoolean("smb.encrypt.enable"))
                    .withSigningRequired(new HostPreferences(host).getBoolean("smb.signing.required"))
                    .withRandomProvider(SecureRandomProviderFactory.get().provide())
                    .withMultiProtocolNegotiate(new HostPreferences(host).getBoolean("smb.protocol.negotiate.enable"))
                    .build());
            final Connection connection = client.connect(getHost().getHostname(), getHost().getPort());
            if(log.isDebugEnabled()) {
                log.debug(String.format("Connected to %s", connection.getConnectionContext()));
            }
            return connection;
        }
        catch(IOException e) {
            throw new SMBTransportExceptionMappingService().map(e);
        }
    }

    @Override
    public boolean alert(final ConnectionCallback callback) throws BackgroundException {
        if(client.getConnectionContext().supportsEncryption()) {
            return false;
        }
        return super.alert(callback);
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
            if(credentials.getUsername().contains("\\")) {
                domain = StringUtils.substringBefore(credentials.getUsername(), "\\");
                username = StringUtils.substringAfter(credentials.getUsername(), "\\");
            }
            else {
                username = credentials.getUsername();
                domain = new HostPreferences(host).getProperty("smb.domain.default");
            }
            context = new AuthenticationContext(username, credentials.getPassword().toCharArray(), domain);
        }
        try {
            shares = new SMBRootListService(this, prompt, session = client.authenticate(context));
        }
        catch(SMBRuntimeException e) {
            throw new SMBExceptionMappingService().map(LocaleFactory.localizedString("Login failed", "Credentials"), e);
        }
    }

    public DiskShareWrapper openShare(final Path file) throws BackgroundException {
        try {
            final String shareName = containerService.getContainer(file).getName();
            final GenericObjectPool<DiskShareWrapper> pool;
            lock.lock();
            try {
                pool = pools.getOrDefault(shareName, new DiskSharePool(shareName));
                if(pool.getNumIdle() == 0) {
                    log.warn(String.format("No idle share for %s with %d active", shareName, pool.getNumActive()));
                }
                pools.putIfAbsent(shareName, pool);
            }
            finally {
                lock.unlock();
            }
            if(log.isDebugEnabled()) {
                log.debug(String.format("Open share %s in thread %s", shareName, Thread.currentThread().getName()));
            }
            final DiskShareWrapper wrapper = pool.borrowObject();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Opened share %s in thread %s", wrapper, Thread.currentThread().getName()));
            }
            return wrapper;
        }
        catch(BackgroundException e) {
            throw e;
        }
        catch(Exception e) {
            throw new DefaultExceptionMappingService().map(e);
        }
    }

    public void releaseShare(final DiskShareWrapper share) throws BackgroundException {
        final String shareName = share.get().getSmbPath().getShareName();
        lock.lock();
        try {
            final GenericObjectPool<DiskShareWrapper> pool = pools.get(shareName);
            if(null != pool) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Release share %s in thread %s", share, Thread.currentThread().getName()));
                }
                try {
                    pool.returnObject(share);
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Released share %s in thread %s", share, Thread.currentThread().getName()));
                    }
                }
                catch(IllegalStateException e) {
                    log.warn(String.format("Failure %s releasing share %s", e, share));
                    throw new BackgroundException(e);
                }
            }
        }
        finally {
            lock.unlock();
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
        return super.isConnected() && client.isConnected();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == PathContainerService.class) {
            return (T) containerService;
        }
        if(type == Find.class) {
            return (T) new SMBFindFeature(this);
        }
        if(type == ListService.class) {
            return (T) shares;
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
