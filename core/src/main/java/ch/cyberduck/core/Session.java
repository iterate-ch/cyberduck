package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Download;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Search;
import ch.cyberduck.core.features.Share;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesReader;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.shared.*;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.vault.VaultRegistry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Session<C> implements TranscriptListener {
    private static final Logger log = LogManager.getLogger(Session.class);

    /**
     * Append HTTP transcript to logger
     */
    private static final TranscriptListener transcript = new LoggingTranscriptListener();

    /**
     * Encapsulating all the information of the remote host
     */
    protected final Host host;

    private Metrics metrics = new DisabledMetrics();

    /**
     * Connection
     */
    protected C client;
    protected VaultRegistry registry = VaultRegistry.DISABLED;

    private final Set<TranscriptListener> listeners = Collections.newSetFromMap(new ConcurrentHashMap<>());

    /**
     * Connection attempt being made.
     */
    private State state = State.closed;

    public boolean alert(final ConnectionCallback callback) throws BackgroundException {
        if(host.getProtocol().isSecure()) {
            return false;
        }
        if(host.getCredentials().isAnonymousLogin()) {
            return false;
        }
        final PreferencesReader preferences = new HostPreferences(host);
        if(preferences.getBoolean(String.format("connection.unsecure.%s", host.getHostname()))) {
            return false;
        }
        return preferences.getBoolean(
                String.format("connection.unsecure.warning.%s", host.getProtocol().getScheme()));
    }

    public Session<?> withListener(final TranscriptListener listener) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Add listener %s", listener));
        }
        listeners.add(listener);
        return this;
    }

    public Session<?> removeListener(final TranscriptListener listener) {
        listeners.remove(listener);
        return this;
    }

    public Session<?> withRegistry(final VaultRegistry registry) {
        this.registry = registry;
        return this;
    }

    public enum State {
        opening,
        open,
        closing,
        closed
    }

    protected Session(final Host h) {
        this.host = h;
    }

    /**
     * @return The client implementation.
     */
    public C getClient() {
        return client;
    }

    public void enableMetrics() {
        metrics = new CountingMetrics();
    }

    public Metrics getMetrics() {
        return metrics;
    }

    /**
     * Connect to host
     *
     * @param key    Host identity verification callback
     * @param login  Prompt for proxy credentials
     * @param cancel
     * @return Client
     */
    public C open(final ProxyFinder proxy, final HostKeyCallback key, final LoginCallback login, final CancelCallback cancel) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Connection will open to %s", host));
        }
        // Update status flag
        state = State.opening;
        client = this.connect(proxy, key, login, cancel);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Connection did open to %s", host));
        }
        // Update status flag
        state = State.open;
        return client;
    }

    protected abstract C connect(ProxyFinder proxy, HostKeyCallback key, LoginCallback prompt, CancelCallback cancel) throws BackgroundException;

    /**
     * Send the authentication credentials to the server. The connection must be opened first.
     *
     * @param prompt Prompt
     * @param cancel Cancel callback
     */
    public abstract void login(LoginCallback prompt, CancelCallback cancel) throws BackgroundException;

    /**
     * Logout and close client connection
     */
    public void close() throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Connection will close to %s", host));
        }
        try {
            switch(state) {
                case open:
                    state = State.closing;
                    this.logout();
                    this.disconnect();
            }
        }
        finally {
            state = State.closed;
            if(log.isDebugEnabled()) {
                log.debug(String.format("Connection did close to %s", host));
            }
        }
    }

    public void interrupt() throws BackgroundException {
        try {
            switch(state) {
                case open:
                    state = State.closing;
                    this.disconnect();
            }
        }
        finally {
            state = State.closed;
            if(log.isDebugEnabled()) {
                log.debug(String.format("Connection did close to %s", host));
            }
        }
    }

    protected abstract void logout() throws BackgroundException;

    /**
     * Close the connection to the remote host. Subsequent calls to #getClient() must return null.
     */
    protected void disconnect() {
        state = State.closed;
        listeners.clear();
    }

    /**
     * @return the host this session connects to
     */
    public Host getHost() {
        return host;
    }

    /**
     * @return Case sensitivity of the underlying remote file system
     */
    public Protocol.Case getCaseSensitivity() {
        return host.getProtocol().getCaseSensitivity();
    }


    /**
     * @return boolean True if the session has not yet been closed.
     */
    public boolean isConnected() {
        return state == State.open;
    }

    /**
     * @return True if a connection attempt is currently being made. False if the connection
     * has already been established or is closed.
     */
    public State getState() {
        return state;
    }

    /**
     * Log the message to all subscribed transcript listeners
     *
     * @param message Log line
     * @see TranscriptListener
     */
    @Override
    public void log(final Type request, final String message) {
        transcript.log(request, message);
        for(TranscriptListener listener : listeners) {
            listener.log(request, message);
        }
    }

    /**
     * Get feature implementation
     *
     * @param type Feature type
     * @return Feature implementation or null when not supported
     */
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type) {
        metrics.increment(type);
        return this.getFeature(type, this._getFeature(type));
    }

    /**
     * Wrap proxy with cryptographic feature
     *
     * @param type    Feature type
     * @param feature Proxy implementation to wrap with vault features
     * @return Feature implementation or null when not supported
     */
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type, final T feature) {
        return registry.getFeature(this, type, feature);
    }

    /**
     * Get feature implementation
     *
     * @param type Feature type
     * @return Feature implementation or null when not supported
     */
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == Upload.class) {
            return (T) new DefaultUploadFeature(this.getFeature(Write.class));
        }
        if(type == Download.class) {
            return (T) new DefaultDownloadFeature(this.getFeature(Read.class));
        }
        if(type == Move.class) {
            return (T) new DisabledMoveFeature();
        }
        if(type == Copy.class) {
            return (T) new DefaultCopyFeature(this);
        }
        if(type == UrlProvider.class) {
            return (T) new DefaultUrlProvider(host);
        }
        if(type == Share.class) {
            return (T) new DefaultShareFeature(this.getFeature(UrlProvider.class));
        }
        if(type == Find.class) {
            return (T) new DefaultFindFeature(this);
        }
        if(type == AttributesFinder.class) {
            return (T) new DefaultAttributesFinderFeature(this);
        }
        if(type == Search.class) {
            return (T) new DefaultSearchFeature(this);
        }
        if(type == Quota.class) {
            return (T) new DisabledQuotaFeature();
        }
        if(type == Home.class) {
            return (T) new DelegatingHomeFeature(new WorkdirHomeFeature(host), new DefaultPathHomeFeature(host));
        }
        if(type == Versioning.class) {
            switch(host.getProtocol().getVersioningMode()) {
                case custom:
                    return (T) new DefaultVersioningFeature(this);
            }
            return null;
        }
        if(type == Bulk.class) {
            return (T) new DisabledBulkFeature();
        }
        return host.getProtocol().getFeature(type);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Session{");
        sb.append("host=").append(host);
        sb.append(", state=").append(state);
        sb.append('}');
        return sb.toString();
    }
}
