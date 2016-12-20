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
import ch.cyberduck.core.features.Download;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.features.Search;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultDownloadFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHeadersFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.shared.DefaultSearchFeature;
import ch.cyberduck.core.shared.DefaultTouchFeature;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.shared.DefaultUrlProvider;
import ch.cyberduck.core.shared.DisabledBulkFeature;
import ch.cyberduck.core.shared.DisabledMoveFeature;
import ch.cyberduck.core.shared.DisabledQuotaFeature;
import ch.cyberduck.core.shared.NullFileidProvider;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.vault.DelegatingVaultLookupListener;
import ch.cyberduck.core.vault.VaultFinderListProgressListener;
import ch.cyberduck.core.vault.VaultFinderListService;
import ch.cyberduck.core.vault.VaultLookupListener;

import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public abstract class Session<C> implements ListService, TranscriptListener {
    private static final Logger log = Logger.getLogger(Session.class);

    private static final LoggingTranscriptListener transcript = new LoggingTranscriptListener();

    /**
     * Encapsulating all the information of the remote host
     */
    protected final Host host;

    /**
     * Cryptomator
     */
    protected Vault vault = Vault.DISABLED;

    protected C client;

    private final Set<TranscriptListener> transcriptListeners = new HashSet<>();
    private final Set<VaultLookupListener> vaultListeners = new HashSet<>();

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
        final Preferences preferences = PreferencesFactory.get();
        if(preferences.getBoolean(String.format("connection.unsecure.%s", host.getHostname()))) {
            return false;
        }
        return preferences.getBoolean(
                String.format("connection.unsecure.warning.%s", host.getProtocol().getScheme()));
    }

    public void addListener(final TranscriptListener listener) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Add listener %s", listener));
        }
        transcriptListeners.add(listener);
    }

    public void addListener(final VaultLookupListener listener) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Add listener %s", listener));
        }
        vaultListeners.add(listener);
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

    public Session<C> withVault(final Vault vault) {
        this.vault.close();
        this.vault = vault;
        return this;
    }

    /**
     * Connect to host
     *
     * @param key Host identity verification callback
     * @return Client
     */
    public C open(final HostKeyCallback key) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Connection will open to %s", host));
        }
        // Update status flag
        state = State.opening;
        client = this.connect(key);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Connection did open to %s", host));
        }
        // Update status flag
        state = State.open;
        return client;
    }

    protected abstract C connect(HostKeyCallback key) throws BackgroundException;

    /**
     * Send the authentication credentials to the server. The connection must be opened first.
     *
     * @param keychain Password store
     * @param prompt   Prompt
     * @param cancel   Cancel callback
     * @param cache    Directory listing cache
     */
    public abstract void login(HostPasswordStore keychain, LoginCallback prompt, CancelCallback cancel, Cache<Path> cache) throws BackgroundException;

    /**
     * Logout and close client connection
     */
    public void close() throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Connection will close to %s", host));
        }
        state = State.closing;
        try {
            if(client != null) {
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
        state = State.closing;
        try {
            if(client != null) {
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

    protected void logout() throws BackgroundException {
        // Nullify password on disconnect. Some implementations that do not swap credentials with a token require the password while connected.
        host.getCredentials().setPassword(null);
    }

    /**
     * Close the connection to the remote host. Subsequent calls to #getClient() must return null.
     */
    protected void disconnect() {
        state = State.closed;
        vault.close();
        transcriptListeners.clear();
        vaultListeners.clear();
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
    public Case getCase() {
        return Case.sensitive;
    }

    public enum Case {
        sensitive,
        insensitive
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
        switch(state) {
            case opening:
            case open:
            case closing:
                for(TranscriptListener listener : transcriptListeners) {
                    listener.log(request, message);
                }
                break;
        }
    }

    /**
     * @param directory Directory
     * @param listener  Callback
     */
    @Override
    public abstract AttributedList<Path> list(Path directory, ListProgressListener listener) throws BackgroundException;

    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type) {
        return vault.getFeature(this, type, this._getFeature(type));
    }

    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type, final T feature) {
        return vault.getFeature(this, type, feature);
    }

    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == Upload.class) {
            return (T) new DefaultUploadFeature(this.getFeature(Write.class));
        }
        if(type == Download.class) {
            return (T) new DefaultDownloadFeature(this);
        }
        if(type == Bulk.class) {
            return (T) new DisabledBulkFeature();
        }
        if(type == Touch.class) {
            return (T) new DefaultTouchFeature(this);
        }
        if(type == Move.class) {
            return (T) new DisabledMoveFeature();
        }
        if(type == UrlProvider.class) {
            return (T) new DefaultUrlProvider(host);
        }
        if(type == Find.class) {
            return (T) new DefaultFindFeature(this);
        }
        if(type == AttributesFinder.class) {
            return (T) new DefaultAttributesFinderFeature(this);
        }
        if(type == Home.class) {
            return (T) new DefaultHomeFinderService(this);
        }
        if(type == Search.class) {
            return (T) new DefaultSearchFeature(this);
        }
        if(type == IdProvider.class) {
            return (T) new NullFileidProvider();
        }
        if(type == Quota.class) {
            return (T) new DisabledQuotaFeature();
        }
        if(type == ListService.class) {
            if(PreferencesFactory.get().getBoolean("cryptomator.enable")) {
                return (T) new VaultFinderListService(vault, this, this,
                        new VaultFinderListProgressListener(PasswordStoreFactory.get(), new DelegatingVaultLookupListener(vaultListeners)));
            }
            return (T) this;
        }
        if(type == Headers.class) {
            return (T) new DefaultHeadersFeature();
        }
        if(type == Vault.class) {
            return (T) vault;
        }
        return null;
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