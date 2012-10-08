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

import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.analytics.QloudstatAnalyticsProvider;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cloudfront.CloudFrontDistributionConfiguration;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.identity.DefaultCDNCredentialsIdentityConfiguration;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
     * Caching files listings of previously listed directories
     */
    private Cache cache = new Cache() {
        @Override
        public String toString() {
            return String.format("Cache for %s", Session.this.toString());
        }
    };

    private UseragentProvider ua = new PreferencesUseragentProvider();

    private boolean unsecurewarning =
            Preferences.instance().getBoolean("connection.unsecure.warning");

    private Set<ConnectionListener> connectionListeners
            = Collections.synchronizedSet(new HashSet<ConnectionListener>());

    private Set<TranscriptListener> transcriptListeners
            = Collections.synchronizedSet(new HashSet<TranscriptListener>());

    private Set<ProgressListener> progressListeners
            = Collections.synchronizedSet(new HashSet<ProgressListener>());

    private Set<ErrorListener> errorListeners
            = Collections.synchronizedSet(new HashSet<ErrorListener>());

    /**
     * Connection attempt being made.
     */
    private boolean opening;

    protected Session(Host h) {
        this.host = h;
    }

    /**
     * @param <C> Native client type
     * @return The client implementation.
     * @throws ConnectionCanceledException If the connection is alreay closed
     */
    protected abstract <C> C getClient() throws ConnectionCanceledException;

    public String getUserAgent() {
        return ua.get();
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
                    throw new ConnectionCanceledException(e.getMessage(), e);
                }
                if(e.getMessage().equals("Socket closed")) {
                    // Do not report as failed if socket opening interrupted
                    log.warn("Supressed socket exception:" + e.getMessage());
                    throw new ConnectionCanceledException(e.getMessage(), e);
                }
                throw e;
            }
            catch(SSLHandshakeException e) {
                log.error("SSL Handshake failed: " + e.getMessage());
                if(e.getCause() instanceof sun.security.validator.ValidatorException) {
                    throw e;
                }
                // Most probably caused by user dismissing ceritifcate. No trusted certificate found.
                throw new ConnectionCanceledException(e.getMessage(), e);
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
        return Preferences.instance().getInteger("connection.timeout.seconds") * 1000;
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
     * @throws IOException            I/O failure
     * @throws LoginCanceledException Login prompt dismissed with cancel
     */
    protected abstract void connect() throws IOException;

    /**
     * Prompt for username and password if not available.
     *
     * @param controller Prompt
     * @throws LoginCanceledException Login prompt dismissed with cancel
     */
    protected void prompt(LoginController controller) throws IOException {
        String username = host.getCredentials().getUsername();
        controller.check(host, Locale.localizedString("Login with username and password", "Credentials"), null);
        if(!StringUtils.equals(username, host.getCredentials().getUsername())) {
            // Changed login credentials
            if(BookmarkCollection.defaultCollection().contains(host)) {
                BookmarkCollection.defaultCollection().collectionItemChanged(host);
            }
        }
    }

    public boolean isUnsecurewarning() {
        return unsecurewarning;
    }

    public void setUnsecurewarning(boolean unsecurewarning) {
        this.unsecurewarning = unsecurewarning;
    }

    /**
     * Attempts to login using the credentials provided from the login controller.
     *
     * @throws IOException I/O failure
     */
    protected void login() throws IOException {
        this.login(LoginControllerFactory.instance(this));
    }

    /**
     * @param controller Prompt
     * @throws IOException I/O failure
     */
    protected void login(LoginController controller) throws IOException {
        this.prompt(controller);

        final Credentials credentials = host.getCredentials();
        this.warn(controller, credentials);

        this.message(MessageFormat.format(Locale.localizedString("Authenticating as {0}", "Status"),
                credentials.getUsername()));
        this.login(controller, credentials);

        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
        controller.success(host);
    }

    /**
     * Warning if credenials are sent plaintext.
     *
     * @param login       Prompt
     * @param credentials Login credentials
     * @throws ConnectionCanceledException If connection should be dropped
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
     * @param controller  Prompt
     * @param credentials Login credentials
     * @throws IOException            I/O failure
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
        this.message(MessageFormat.format(Locale.localizedString("Mounting {0}", "Status"),
                host.getHostname()));
        try {
            this.check();
            if(!this.isConnected()) {
                return null;
            }
            Path directory = this.home();
            // Retrieve direcotry listing of default path
            if(!directory.children().attributes().isReadable()) {
                // The default path does not exist or is not readable due to possible permission issues
                // Fallback to default working directory
                directory = this.workdir();
            }
            return directory;
        }
        catch(IOException e) {
            log.warn("Connection failed:" + e.getMessage());
            return null;
        }
        finally {
            // Reset current working directory in bookmark
            host.setWorkdir(null);
        }
    }

    /**
     * @return Home directory
     * @throws IOException I/O failure
     */
    public Path home() throws IOException {
        final String directory;
        if(StringUtils.isNotBlank(host.getWorkdir())) {
            directory = host.getWorkdir();
        }
        else if(StringUtils.isNotBlank(host.getDefaultPath())) {
            if(host.getDefaultPath().startsWith(String.valueOf(Path.DELIMITER))) {
                // Mount absolute path
                directory = host.getDefaultPath();
            }
            else {
                Path workdir = this.workdir();
                if(host.getDefaultPath().startsWith(Path.HOME)) {
                    // Relative path to the home directory
                    return PathFactory.createPath(this, workdir.getAbsolute(), host.getDefaultPath().substring(1), Path.DIRECTORY_TYPE);
                }
                else {
                    // Relative path
                    return PathFactory.createPath(this, workdir.getAbsolute(), host.getDefaultPath(), Path.DIRECTORY_TYPE);
                }
            }
        }
        else {
            // No default path configured
            return this.workdir();
        }
        return PathFactory.createPath(this, directory,
                directory.equals(String.valueOf(Path.DELIMITER)) ? Path.VOLUME_TYPE | Path.DIRECTORY_TYPE : Path.DIRECTORY_TYPE);
    }

    /**
     * @return The current working directory (pwd) or null if it cannot be retrieved for whatever reason
     * @throws ConnectionCanceledException If the underlying connection has already been closed before
     */
    public Path workdir() throws IOException {
        return PathFactory.createPath(this, String.valueOf(Path.DELIMITER),
                Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
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
     * @return True if ACLs are supported
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
     * @return True if timestamp of file can be read.
     * @see AbstractPath#writeTimestamp(long, long, long)
     * @see ch.cyberduck.core.Path#readTimestamp()
     */
    public boolean isReadTimestampSupported() {
        return true;
    }

    /**
     * @return True if timestamp of file can be read and written.
     * @see AbstractPath#writeTimestamp(long, long, long)
     * @see ch.cyberduck.core.Path#readTimestamp()
     */
    public boolean isWriteTimestampSupported() {
        return true;
    }

    /**
     * @return MD5/ETag available for files.
     */
    public boolean isChecksumSupported() {
        return false;
    }

    /**
     * @return True if files can be reverted
     */
    public boolean isRevertSupported() {
        return false;
    }

    /**
     * Send a 'no operation' command
     *
     * @throws IOException I/O failure
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
     * @param command Command to send
     * @throws IOException I/O failure
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
     * @param archive Archive format description
     * @param files   List of files to archive
     */
    public void archive(final Archive archive, final List<Path> files) {
        try {
            this.check();

            this.sendCommand(archive.getCompressCommand(files));
        }
        catch(IOException e) {
            this.error("Cannot create archive", e);
        }
    }

    /**
     * @return True if archiving is supported. Always false
     */
    public boolean isUnarchiveSupported() {
        return false;
    }

    /**
     * Unpack compressed archive
     *
     * @param archive Archive format description
     * @param file    File to decompress
     */
    public void unarchive(final Archive archive, final Path file) {
        try {
            this.check();

            this.sendCommand(archive.getDecompressCommand(file));
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

    /**
     * @return True if a connection attempt is currently being made. False if the connection
     *         has already been established or is closed.
     */
    public boolean isOpening() {
        return opening;
    }

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

        final Resolver resolver = this.getResolver();
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
        // Update last accessed timestamp
        host.setTimestamp(new Date());
        // Update status flag
        opening = false;

        final HistoryCollection history = HistoryCollection.defaultCollection();
        history.add(new Host(host.getAsDictionary()));

        for(ConnectionListener listener : connectionListeners.toArray(new ConnectionListener[connectionListeners.size()])) {
            listener.connectionDidOpen();
        }
        this.message(MessageFormat.format(Locale.localizedString("{0} connection opened", "Status"),
                host.getProtocol().getName()));
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

        this.cdn().clear();

        for(ConnectionListener listener : connectionListeners.toArray(new ConnectionListener[connectionListeners.size()])) {
            listener.connectionDidClose();
        }

        // Update status flag
        opening = false;
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
     * @return True if symbolic links are supported on UNIX filesystems
     */
    public boolean isCreateSymlinkSupported() {
        return false;
    }

    /**
     * @return List of known ACL users
     */
    public List<Acl.User> getAvailableAclUsers() {
        return Collections.emptyList();
    }

    /**
     * @return If metadata for files are supported
     */
    public boolean isMetadataSupported() {
        return false;
    }

    /**
     * @return If CDN distribution configuration is supported
     * @see #cdn()
     */
    public boolean isCDNSupported() {
        return true;
    }

    public IdentityConfiguration iam() {
        return new DefaultCDNCredentialsIdentityConfiguration(host);
    }

    public boolean isAnalyticsSupported() {
        return this.isCDNSupported();
    }

    public AnalyticsProvider analytics() {
        return new QloudstatAnalyticsProvider();
    }

    /**
     * Delegating CloudFront requests.
     */
    private DistributionConfiguration cf;

    public DistributionConfiguration cdn() {
        if(null == cf) {
            cf = new CloudFrontDistributionConfiguration(LoginControllerFactory.instance(this),
                    host.getCdnCredentials(),
                    new ErrorListener() {
                        @Override
                        public void error(BackgroundException exception) {
                            Session.this.error(exception);
                        }
                    },
                    new ProgressListener() {
                        @Override
                        public void message(String message) {
                            Session.this.message(message);
                        }
                    },
                    new TranscriptListener() {
                        @Override
                        public void log(boolean request, String message) {
                            Session.this.log(request, message);
                        }
                    }
            ) {

                @Override
                protected void fireConnectionDidOpenEvent() {
                    // Save CloudFront access credentials to bookmark
                    if(BookmarkCollection.defaultCollection().contains(Session.this.getHost())) {
                        BookmarkCollection.defaultCollection().collectionItemChanged(Session.this.getHost());
                    }
                    super.fireConnectionDidOpenEvent();
                }

                /**
                 * @return Service name of the CDN
                 */
                public String toString() {
                    if(this.isCDNSupported()) {
                        return super.toString();
                    }
                    return Locale.localizedString("None");
                }

                @Override
                public List<Distribution.Method> getMethods(final String container) {
                    if(this.isCDNSupported()) {
                        return Arrays.asList(Distribution.CUSTOM);
                    }
                    return Collections.emptyList();
                }

                @Override
                public String getOrigin(Distribution.Method method, String container) {
                    if(Distribution.CUSTOM.equals(method)) {
                        try {
                            return new URI(Session.this.getHost().getWebURL()).getHost();
                        }
                        catch(URISyntaxException e) {
                            log.error("Failure parsing URI:" + e.getMessage());
                        }
                    }
                    return super.getOrigin(method, container);
                }
            };
        }
        return cf;
    }

    /**
     * Roles available for users in a configurable ACL.
     *
     * @param files List of files
     * @return A list of role names.
     */
    public List<Acl.Role> getAvailableAclRoles(final List<Path> files) {
        return Collections.emptyList();
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
    public void message(final String message) {
        if(log.isInfoEnabled()) {
            log.info(message);
        }
        for(ProgressListener listener : progressListeners.toArray(new ProgressListener[progressListeners.size()])) {
            listener.message(message);
        }
    }

    public void addErrorListener(final ErrorListener listener) {
        errorListeners.add(listener);
    }

    public void removeErrorListener(final ErrorListener listener) {
        errorListeners.remove(listener);
    }

    public void error(String message, Throwable e) {
        this.error(null, message, e);
    }

    /**
     * Notifies all error listeners of this error without sending this error to Growl
     *
     * @param path    The path related to this error
     * @param message The error message to be displayed in the alert sheet
     * @param e       The cause of the error
     */
    public void error(final Path path, final String message, final Throwable e) {
        this.error(new BackgroundException(this.getHost(), path, message, e));
    }

    public void error(final BackgroundException failure) {
        this.message(failure.getMessage());
        for(ErrorListener listener : errorListeners.toArray(new ErrorListener[errorListeners.size()])) {
            listener.error(failure);
        }
    }

    /**
     * @return The directory listing cache for this session
     */
    public Cache cache() {
        return this.cache;
    }

    /**
     * @param other Session instance
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

    @Override
    public int hashCode() {
        int result = host.getHostname() != null ? host.getHostname().hashCode() : 0;
        result = 31 * result + (host.getProtocol() != null ? host.getProtocol().hashCode() : 0);
        return result;
    }

    public String toString() {
        return String.format("Session %s", host.toURL());
    }
}