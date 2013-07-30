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

import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cloudfront.CustomOriginCloudFrontDistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.shared.DefaultUploadFeature;
import ch.cyberduck.core.shared.DefaultUrlProvider;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @version $Id$
 */
public abstract class Session<C> implements TranscriptListener, ProgressListener {
    private static final Logger log = Logger.getLogger(Session.class);

    /**
     * Encapsulating all the information of the remote host
     */
    protected Host host;

    protected C client;

    /**
     * Caching files listings of previously listed directories
     */
    private Cache cache = new Cache();

    private Set<ConnectionListener> connectionListeners
            = Collections.synchronizedSet(new HashSet<ConnectionListener>(0));

    private Set<TranscriptListener> transcriptListeners
            = Collections.synchronizedSet(new HashSet<TranscriptListener>(0));

    private Set<ProgressListener> progressListeners
            = Collections.synchronizedSet(new HashSet<ProgressListener>(0));

    /**
     * Connection attempt being made.
     */
    private State state = State.closed;

    public boolean alert() throws BackgroundException {
        if(host.getProtocol().isSecure()) {
            return false;
        }
        if(host.getCredentials().isAnonymousLogin()) {
            return false;
        }
        if(Preferences.instance().getBoolean(String.format("connection.unsecure.%s", host.getHostname()))) {
            return false;
        }
        return Preferences.instance().getBoolean(
                String.format("connection.unsecure.warning.%s", host.getProtocol().getScheme()));
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
     * @return The directory listing cache for this session
     */
    public Cache cache() {
        return cache;
    }

    /**
     * @return The client implementation.
     */
    public C getClient() {
        return client;
    }

    /**
     * Connect to host
     *
     * @param key Host identity verification callback
     * @return Client
     * @throws BackgroundException
     */
    public C open(final HostKeyController key) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Connection will open to %s", host));
        }
        // Update status flag
        state = State.opening;
        for(ConnectionListener listener : connectionListeners.toArray(new ConnectionListener[connectionListeners.size()])) {
            listener.connectionWillOpen();
        }
        client = this.connect(key);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Connection did open to %s", host));
        }
        // Update status flag
        state = State.open;
        for(ConnectionListener listener : connectionListeners.toArray(new ConnectionListener[connectionListeners.size()])) {
            listener.connectionDidOpen();
        }
        return client;
    }

    protected abstract C connect(HostKeyController key) throws BackgroundException;

    /**
     * Send the authentication credentials to the server. The connection must be opened first.
     *
     * @param keychain Password store
     * @param prompt   Prompt
     */
    public abstract void login(PasswordStore keychain, LoginController prompt) throws BackgroundException;

    /**
     * Logout and close client connection
     *
     * @throws BackgroundException
     */
    public void close() throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Connection will close to %s", host));
        }
        state = State.closing;
        try {
            this.logout();
            this.disconnect();
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
            this.disconnect();
        }
        finally {
            state = State.closed;
            if(log.isDebugEnabled()) {
                log.debug(String.format("Connection did close to %s", host));
            }
        }
    }

    /**
     * Close the connection to the remote host. Subsequent calls to #getClient() must return null.
     */
    protected abstract void logout() throws BackgroundException;

    protected void disconnect() {
        //
    }

    /**
     * @return The timeout in milliseconds
     */
    protected int timeout() {
        return Preferences.instance().getInteger("connection.timeout.seconds") * 1000;
    }

    /**
     * @return True if the control channel is either tunneled using TLS or SSH
     */
    public boolean isSecured() {
        if(this.isConnected()) {
            return host.getProtocol().isSecure();
        }
        return false;
    }

    /**
     * @return Home directory
     */
    public Path home() throws BackgroundException {
        if(host.getWorkdir() != null) {
            return host.getWorkdir();
        }
        else if(StringUtils.isNotBlank(host.getDefaultPath())) {
            if(host.getDefaultPath().startsWith(String.valueOf(Path.DELIMITER))) {
                // Mount absolute path
                return new Path(host.getDefaultPath(),
                        host.getDefaultPath().equals(String.valueOf(Path.DELIMITER)) ? Path.VOLUME_TYPE | Path.DIRECTORY_TYPE : Path.DIRECTORY_TYPE);
            }
            else {
                final Path workdir = this.workdir();
                if(host.getDefaultPath().startsWith(Path.HOME)) {
                    // Relative path to the home directory
                    return new Path(workdir, host.getDefaultPath().substring(1), Path.DIRECTORY_TYPE);
                }
                else {
                    // Relative path
                    return new Path(workdir, host.getDefaultPath(), Path.DIRECTORY_TYPE);
                }
            }
        }
        else {
            // No default path configured
            return this.workdir();
        }
    }

    /**
     * @return The current working directory (pwd) or null if it cannot be retrieved for whatever reason
     */
    public Path workdir() throws BackgroundException {
        return new Path(String.valueOf(Path.DELIMITER),
                Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
    }

    /**
     * @return the host this session connects to
     */
    public Host getHost() {
        return host;
    }

    /**
     * @return The custom character encoding specified by the host
     *         of this session or the default encoding if not specified
     * @see Preferences
     * @see Host
     */
    public String getEncoding() {
        if(null == host.getEncoding()) {
            return Preferences.instance().getProperty("browser.charset.encoding");
        }
        return host.getEncoding();
    }

    /**
     * @return The maximum number of concurrent connections allowed or -1 if no limit is set
     */
    public int getMaxConnections() {
        if(null == host.getMaxConnections()) {
            return Preferences.instance().getInteger("connection.host.max");
        }
        return host.getMaxConnections();
    }

    /**
     * Send a 'no operation' command
     */
    public void noop() throws BackgroundException {
        //
    }

    /**
     * @return boolean True if the session has not yet been closed.
     */
    public boolean isConnected() {
        return state == State.open;
    }

    /**
     * @return True if a connection attempt is currently being made. False if the connection
     *         has already been established or is closed.
     */
    public State getState() {
        return state;
    }

    public void addTranscriptListener(final TranscriptListener listener) {
        transcriptListeners.add(listener);
    }

    public void removeTranscriptListener(final TranscriptListener listener) {
        transcriptListeners.remove(listener);
    }

    /**
     * Log the message to all subscribed transcript listeners
     *
     * @param message Log line
     * @see TranscriptListener
     */
    @Override
    public void log(final boolean request, final String message) {
        if(log.isInfoEnabled()) {
            log.info(message);
        }
        for(TranscriptListener listener : transcriptListeners.toArray(new TranscriptListener[transcriptListeners.size()])) {
            listener.log(request, message);
        }
    }

    public void addProgressListener(final ProgressListener listener) {
        progressListeners.add(listener);
    }

    public void removeProgressListener(final ProgressListener listener) {
        progressListeners.remove(listener);
    }

    /**
     * Notifies all progress listeners
     *
     * @param message The message to be displayed in a status field
     * @see ProgressListener
     */
    @Override
    public void message(final String message) {
        if(log.isInfoEnabled()) {
            log.info(message);
        }
        for(ProgressListener listener : progressListeners.toArray(new ProgressListener[progressListeners.size()])) {
            listener.message(message);
        }
    }

    /**
     * Includes both native protocol and HTTP URLs
     *
     * @return A list of URLs pointing to the resource.
     */
    public DescriptiveUrlBag getURLs(final Path file) {
        return new DefaultUrlProvider(host).get(file);
    }

    /**
     * Check for file existence. The default implementation does a directory listing of the parent folder.
     *
     * @return True if the path is cached.
     */
    public boolean exists(final Path path) throws BackgroundException {
        if(path.isRoot()) {
            return true;
        }
        try {
            return this.list(path.getParent(), new DisabledListProgressListener()).contains(path.getReference());
        }
        catch(NotfoundException e) {
            return false;
        }
    }

    /**
     * @param file     Directory
     * @param listener Callback
     */
    public abstract AttributedList<Path> list(Path file, ListProgressListener listener) throws BackgroundException;

    public <T> T getFeature(final Class<T> type, final LoginController prompt) {
        if(type == Upload.class) {
            return (T) new DefaultUploadFeature(this);
        }
        if(type == DistributionConfiguration.class) {
            // Use login context of current session
            return (T) new CustomOriginCloudFrontDistributionConfiguration(host, prompt);
        }
        return null;
    }
}