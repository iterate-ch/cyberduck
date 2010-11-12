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

import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.*;

/**
 * @version $Id$
 */
public abstract class Session implements TranscriptListener {
    private static Logger log = Logger.getLogger(Session.class);

    /**
     * Encapsulating all the information of the remote host
     */
    protected Host host;

    /**
     * Current working directory
     */
    protected Path workdir;

    protected Session(Host h) {
        this.host = h;
    }

    /**
     * @param <C>
     * @return The client implementation.
     * @throws ConnectionCanceledException
     */
    protected abstract <C> C getClient() throws ConnectionCanceledException;

    private final String ua = Preferences.instance().getProperty("application.name") + "/"
            + Preferences.instance().getProperty("application.version")
            + " (" + System.getProperty("os.name") + "/" + System.getProperty("os.version") + ")"
            + " (" + System.getProperty("os.arch") + ")";

    public String getUserAgent() {
        return ua;
    }

    /**
     * Assert that the connection to the remote host is still alive.
     * Open connection if needed.
     *
     * @throws IOException The connection to the remote host failed.
     */
    public void check() throws IOException {
        try {
            try {
                if(!this.isConnected()) {
                    if(StringUtils.isBlank(host.getHostname())) {
                        if(StringUtils.isBlank(host.getProtocol().getDefaultHostname())) {
                            log.warn("No hostname configured:" + host);
                            throw new ConnectionCanceledException();
                        }
                        // If hostname is missing update with default
                        host.setHostname(host.getProtocol().getDefaultHostname());
                    }
                    // If not connected anymore, reconnect the session
                    this.connect();
                }
                else {
                    // The session is still supposed to be connected
                    try {
                        // Send a 'no operation command' to make sure the session is alive
                        this.noop();
                    }
                    catch(IOException e) {
                        // Close the underlying socket first
                        this.interrupt();
                        // Try to reconnect once more
                        this.connect();
                    }
                }
            }
            catch(SocketException e) {
                if(e.getMessage().equals("Software caused connection abort")) {
                    // Do not report as failed if socket opening interrupted
                    log.warn("Supressed socket exception:" + e.getMessage());
                    throw new ConnectionCanceledException();
                }
                if(e.getMessage().equals("Socket closed")) {
                    // Do not report as failed if socket opening interrupted
                    log.warn("Supressed socket exception:" + e.getMessage());
                    throw new ConnectionCanceledException();
                }
                throw e;
            }
            catch(SSLHandshakeException e) {
                log.error("SSL Handshake failed: " + e.getMessage());
                if(e.getCause() instanceof sun.security.validator.ValidatorException) {
                    throw e;
                }
                // Most probably caused by user dismissing ceritifcate. No trusted certificate found.
                throw new ConnectionCanceledException(e.getMessage());
            }
        }
        catch(IOException e) {
            this.interrupt();
            this.error("Connection failed", e);
            throw e;
        }
    }

    /**
     * @return The timeout in milliseconds
     */
    protected int timeout() {
        return (int) Preferences.instance().getDouble("connection.timeout.seconds") * 1000;
    }

    /**
     * No information about the curren state of the connection but only the protocol.
     *
     * @return True if the control channel is either tunneled using TLS or SSH
     */
    public boolean isSecure() {
        if(this.isConnected()) {
            return this.host.getProtocol().isSecure();
        }
        return false;
    }

    /**
     * Opens the TCP connection to the server
     *
     * @throws IOException
     * @throws LoginCanceledException
     */
    protected abstract void connect() throws IOException;

    /**
     * Prompt for username and password if not available.
     *
     * @param login
     * @throws LoginCanceledException
     */
    protected void prompt(LoginController login) throws LoginCanceledException {
        login.check(host, Locale.localizedString("Login with username and password", "Credentials"), null);
    }

    private boolean unsecurewarning =
            Preferences.instance().getBoolean("connection.unsecure.warning");

    public boolean isUnsecurewarning() {
        return unsecurewarning;
    }

    public void setUnsecurewarning(boolean unsecurewarning) {
        this.unsecurewarning = unsecurewarning;
    }

    /**
     * Attempts to login using the credentials provided from the login controller.
     *
     * @throws IOException
     */
    protected void login() throws IOException {
        LoginController login = LoginControllerFactory.instance(this);
        this.prompt(login);

        final Credentials credentials = host.getCredentials();
        this.warn(login, credentials);

        this.message(MessageFormat.format(Locale.localizedString("Authenticating as {0}", "Status"),
                credentials.getUsername()));
        this.login(login, credentials);

        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
        login.success(host);
    }

    /**
     * Warning if credenials are sent plaintext.
     *
     * @param login
     * @param credentials
     * @throws ConnectionCanceledException
     */
    protected void warn(LoginController login, Credentials credentials) throws IOException {
        if(this.isUnsecurewarning()
                && !host.getProtocol().isSecure()
                && !credentials.isAnonymousLogin()
                && !Preferences.instance().getBoolean("connection.unsecure." + host.getHostname())) {
            try {
                login.warn(MessageFormat.format(Locale.localizedString("Unsecured {0} connection", "Credentials"), host.getProtocol().getName()),
                        MessageFormat.format(Locale.localizedString("{0} will be sent in plaintext.", "Credentials"), credentials.getPasswordPlaceholder()),
                        "connection.unsecure." + host.getHostname());
            }
            finally {
                // Do not warn again upon subsequent login
                this.setUnsecurewarning(false);
            }
        }
    }

    /**
     * Send the authentication credentials to the server. The connection must be opened first.
     *
     * @throws IOException
     * @throws LoginCanceledException
     * @see #connect
     */
    protected abstract void login(LoginController controller, Credentials credentials) throws IOException;

    /**
     * Mount the default path of the configured host or the home directory as returned by the server
     * when not given.
     *
     * @return Null if mount fails. Check the error listener for details.
     */
    public Path mount() {
        try {
            if(StringUtils.isNotBlank(host.getWorkdir())) {
                return this.mount(host.getWorkdir());
            }
            if(StringUtils.isNotBlank(host.getDefaultPath())) {
                return this.mount(host.getDefaultPath());
            }
            return this.mount(null);
        }
        catch(IOException e) {
            this.interrupt();
        }
        finally {
            host.setWorkdir(null);
        }
        return null;
    }

    /**
     * Connect to the remote host and mount the home directory
     *
     * @param directory
     * @return null if we fail, the mounted working directory if we succeed
     */
    protected Path mount(String directory) throws IOException {
        this.message(MessageFormat.format(Locale.localizedString("Mounting {0}", "Status"),
                host.getHostname()));
        this.check();
        if(!this.isConnected()) {
            return null;
        }
        Path home;
        if(directory != null) {
            if(directory.startsWith(String.valueOf(Path.DELIMITER)) || directory.equals(this.workdir().getName())) {
                home = PathFactory.createPath(this, directory,
                        directory.equals(String.valueOf(Path.DELIMITER)) ? Path.VOLUME_TYPE | Path.DIRECTORY_TYPE : Path.DIRECTORY_TYPE);
            }
            else if(directory.startsWith(Path.HOME)) {
                // relative path to the home directory
                home = PathFactory.createPath(this,
                        this.workdir().getAbsolute(), directory.substring(1), Path.DIRECTORY_TYPE);
            }
            else {
                // relative path
                home = PathFactory.createPath(this,
                        this.workdir().getAbsolute(), directory, Path.DIRECTORY_TYPE);
            }
            if(!home.children().attributes().isReadable()) {
                // the default path does not exist or is not readable due to permission issues
                home = this.workdir();
            }
        }
        else {
            home = this.workdir();
        }
        return home;
    }

    /**
     * Close the connecion to the remote host. The protocol specific
     * implementation has to be implemented in the subclasses. Subsequent calls to #getClient() must return null.
     *
     * @see #isConnected()
     */
    public abstract void close();

    /**
     * @return the host this session connects to
     */
    public Host getHost() {
        return this.host;
    }

    /**
     * @return The custom character encoding specified by the host
     *         of this session or the default encoding if not specified
     * @see Preferences
     * @see Host
     */
    public String getEncoding() {
        if(null == this.host.getEncoding()) {
            return Preferences.instance().getProperty("browser.charset.encoding");
        }
        return this.host.getEncoding();
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
     * @return The current working directory (pwd) or null if it cannot be retrieved for whatever reason
     * @throws ConnectionCanceledException If the underlying connection has already been closed before
     */
    public Path workdir() throws IOException {
        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
        if(null == workdir) {
            workdir = PathFactory.createPath(this, String.valueOf(Path.DELIMITER), Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        }
        return workdir;
    }

    /**
     * Set the current working directory for this session. Implementations may
     * implmeent a change working directory command.
     *
     * @param workdir The new working directory.
     * @throws IOException
     */
    public void setWorkdir(Path workdir) throws IOException {
        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
        this.workdir = workdir;
    }

    /**
     * @param workdir The workdir to create query
     * @return True if making directories is possible.
     * @see Path#mkdir()
     */
    public boolean isCreateFolderSupported(Path workdir) {
        return true;
    }

    /**
     * @param workdir The workdir to create query
     * @return True if creating an empty file is possible.
     * @see ch.cyberduck.core.Path#touch()
     */
    public boolean isCreateFileSupported(Path workdir) {
        return true;
    }

    public boolean isRenameSupported(Path file) {
        return true;
    }

    /**
     * @return
     * @see ch.cyberduck.core.Path#writeAcl(Acl, boolean)
     * @see Path#readAcl()
     */
    public boolean isAclSupported() {
        return false;
    }

    /**
     * @return True if UNIX permissions can be read and written.
     * @see ch.cyberduck.core.Path#writeUnixPermission(Permission, boolean)
     * @see ch.cyberduck.core.Path#readUnixPermission()
     */
    public boolean isUnixPermissionsSupported() {
        return true;
    }

    /**
     * @return True if timestamp of file can be read and written.
     * @see AbstractPath#writeTimestamp(long,long,long)
     * @see ch.cyberduck.core.Path#readTimestamp()
     */
    public boolean isTimestampSupported() {
        return true;
    }

    /**
     * @return
     */
    public boolean isRevertSupported() {
        return false;
    }

    /**
     * Send a 'no operation' command
     *
     * @throws IOException
     */
    protected void noop() throws IOException {
        ;
    }

    /**
     * Interrupt any running operation asynchroneously by closing the underlying socket.
     * Close the underlying socket regardless of its state; will throw a socket exception
     * on the thread owning the socket
     */
    public void interrupt() {
        this.close();
    }

    /**
     * @return True if command execution if supported by the protocol.
     */
    public boolean isSendCommandSupported() {
        return false;
    }

    /**
     * Sends an arbitrary command to the server
     *
     * @param command
     * @see #isSendCommandSupported()
     */
    public void sendCommand(String command) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @return False
     */
    public boolean isArchiveSupported() {
        return false;
    }

    /**
     * Create ompressed archive.
     *
     * @param archive
     */
    public void archive(final Archive archive, final List<Path> files) {
        try {
            this.check();

            this.sendCommand(archive.getCompressCommand(files));

            // The directory listing is no more current
            for(Path file : files) {
                file.getParent().invalidate();
            }
        }
        catch(IOException e) {
            this.error("Cannot create archive", e);
        }
    }

    /**
     * @return False
     */
    public boolean isUnarchiveSupported() {
        return false;
    }

    /**
     * Unpack compressed archive
     *
     * @param archive
     */
    public void unarchive(final Archive archive, Path file) {
        try {
            this.check();

            this.sendCommand(archive.getDecompressCommand(file));

            // The directory listing is no more current
            file.getParent().invalidate();
        }
        catch(IOException e) {
            this.error("Cannot expand archive", e);
        }
    }

    /**
     * @return boolean True if the session has not yet been closed.
     */
    public boolean isConnected() {
        try {
            this.getClient();
        }
        catch(ConnectionCanceledException e) {
            return false;
        }
        return true;
    }

    private boolean opening;

    /**
     * @return True if a connection attempt is currently being made. False if the connection
     *         has already been established or is closed.
     */
    public boolean isOpening() {
        return opening;
    }

    private Set<ConnectionListener> connectionListeners
            = Collections.synchronizedSet(new HashSet<ConnectionListener>());

    public void addConnectionListener(ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        connectionListeners.remove(listener);
    }

    /**
     * Notifies all connection listeners that an attempt is made to open this session
     *
     * @throws ResolveCanceledException      If the name resolution has been canceled by the user
     * @throws java.net.UnknownHostException If the name resolution failed
     * @see ConnectionListener
     */
    protected void fireConnectionWillOpenEvent() throws ResolveCanceledException, UnknownHostException {
        log.debug("connectionWillOpen");
        ConnectionListener[] l = connectionListeners.toArray(new ConnectionListener[connectionListeners.size()]);
        for(ConnectionListener listener : l) {
            listener.connectionWillOpen();
        }

        // Update status flag
        opening = true;

        // Configuring proxy if any
        ProxyFactory.instance().configure(host);

        Resolver resolver = this.getResolver();
        this.message(MessageFormat.format(Locale.localizedString("Resolving {0}", "Status"),
                host.getHostname()));

        // Try to resolve the hostname first
        resolver.resolve();
        // The IP address could successfully be determined
        this.message(MessageFormat.format(Locale.localizedString("Opening {0} connection to {1}", "Status"),
                host.getProtocol().getName(), host.getHostname()));
    }

    protected Resolver getResolver() {
        return new Resolver(host.getHostname(true));
    }

    /**
     * Starts the <code>KeepAliveTask</code> if <code>connection.keepalive</code> is true
     * Notifies all connection listeners that the connection has been opened successfully
     *
     * @see ConnectionListener
     */
    protected void fireConnectionDidOpenEvent() {
        log.debug("connectionDidOpen");

        for(ConnectionListener listener : connectionListeners.toArray(new ConnectionListener[connectionListeners.size()])) {
            listener.connectionDidOpen();
        }
        this.message(MessageFormat.format(Locale.localizedString("{0} connection opened", "Status"),
                host.getProtocol().getName()));

        // Update last accessed timestamp
        host.setTimestamp(new Date());

        // Update status flag
        opening = false;
    }

    /**
     * Notifes all connection listeners that a connection is about to be closed
     *
     * @see ConnectionListener
     */
    protected void fireConnectionWillCloseEvent() {
        log.debug("connectionWillClose");
        this.message(MessageFormat.format(Locale.localizedString("Disconnecting {0}", "Status"),
                host.getHostname()));

        for(ConnectionListener listener : connectionListeners.toArray(new ConnectionListener[connectionListeners.size()])) {
            listener.connectionWillClose();
        }
    }

    /**
     * Notifes all connection listeners that a connection has been closed
     *
     * @see ConnectionListener
     */
    protected void fireConnectionDidCloseEvent() {
        log.debug("connectionDidClose");

        this.workdir = null;

        for(ConnectionListener listener : connectionListeners.toArray(new ConnectionListener[connectionListeners.size()])) {
            listener.connectionDidClose();
        }

        // Update status flag
        opening = false;
    }

    private Set<TranscriptListener> transcriptListeners
            = Collections.synchronizedSet(new HashSet<TranscriptListener>());

    public void addTranscriptListener(TranscriptListener listener) {
        transcriptListeners.add(listener);
    }

    public void removeTranscriptListener(TranscriptListener listener) {
        transcriptListeners.remove(listener);
    }

    /**
     * Log the message to all subscribed transcript listeners
     *
     * @param message
     * @see TranscriptListener
     */
    public void log(boolean request, final String message) {
        log.info(message);
        for(TranscriptListener listener : transcriptListeners) {
            listener.log(request, message);
        }
    }

    /**
     * Content Range support
     *
     * @return True if skipping is supported
     */
    public boolean isDownloadResumable() {
        return true;
    }

    /**
     * Content Range support
     *
     * @return True if appending is supported
     */
    public boolean isUploadResumable() {
        return true;
    }

    /**
     * @return
     */
    public List<Acl.User> getAvailableAclUsers() {
        return Collections.emptyList();
    }

    public Acl getPrivateAcl(String container) {
        return Acl.EMPTY;
    }

    /**
     * @param container
     * @param readable
     * @param writable  @return
     */
    public Acl getPublicAcl(String container, boolean readable, boolean writable) {
        return Acl.EMPTY;
    }

    /**
     * Roles available for users in a configurable ACL.
     *
     * @return A list of role names.
     */
    public List<Acl.Role> getAvailableAclRoles(List<Path> files) {
        return Collections.emptyList();
    }

    private Set<ProgressListener> progressListeners
            = Collections.synchronizedSet(new HashSet<ProgressListener>());

    public void addProgressListener(ProgressListener listener) {
        progressListeners.add(listener);
    }

    public void removeProgressListener(ProgressListener listener) {
        progressListeners.remove(listener);
    }

    /**
     * Notifies all progress listeners
     *
     * @param message The message to be displayed in a status field
     * @see ProgressListener
     */
    public void message(final String message) {
        log.info(message);
        for(ProgressListener listener : progressListeners.toArray(new ProgressListener[progressListeners.size()])) {
            listener.message(message);
        }
    }

    private Set<ErrorListener> errorListeners
            = Collections.synchronizedSet(new HashSet<ErrorListener>());

    public void addErrorListener(ErrorListener listener) {
        errorListeners.add(listener);
    }

    public void removeErrorListener(ErrorListener listener) {
        errorListeners.remove(listener);
    }

    protected void error(String message, Throwable e) {
        this.error(null, message, e);
    }

    /**
     * Notifies all error listeners of this error without sending this error to Growl
     *
     * @param path    The path related to this error
     * @param message The error message to be displayed in the alert sheet
     * @param e       The cause of the error
     */
    protected void error(Path path, String message, Throwable e) {
        this.error(new BackgroundException(this, path, message, e));
    }

    protected void error(BackgroundException failure) {
        this.message(failure.getMessage());
        for(ErrorListener listener : errorListeners.toArray(new ErrorListener[errorListeners.size()])) {
            listener.error(failure);
        }
    }

    /**
     * Caching files listings of previously listed directories
     */
    private Cache<Path> cache;

    /**
     * @return The directory listing cache for this session
     */
    public Cache<Path> cache() {
        if(null == cache) {
            cache = new Cache<Path>() {
                @Override
                public String toString() {
                    return "Cache for " + Session.this.toString();
                }
            };
        }
        return this.cache;
    }

    /**
     * @param other
     * @return true if the other session denotes the same hostname and protocol
     */
    @Override
    public boolean equals(Object other) {
        if(null == other) {
            return false;
        }
        if(other instanceof Session) {
            return host.getHostname().equals(((Session) other).getHost().getHostname())
                    && host.getProtocol().equals(((Session) other).getHost().getProtocol());
        }
        return super.equals(other);
    }

    public String toString() {
        return "Session " + host.toURL();
    }
}