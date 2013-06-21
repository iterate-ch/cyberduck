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
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cloudfront.CustomOriginCloudFrontDistributionConfiguration;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.identity.AbstractIdentityConfiguration;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @version $Id$
 */
public abstract class Session<C> implements TranscriptListener {
    private static final Logger log = Logger.getLogger(Session.class);

    /**
     * Encapsulating all the information of the remote host
     */
    protected Host host;

    private C client;

    /**
     * Caching files listings of previously listed directories
     */
    private Cache cache = new Cache() {
        @Override
        public String toString() {
            return String.format("Cache for %s", Session.this.toString());
        }
    };

    private boolean unsecurewarning =
            Preferences.instance().getBoolean("connection.unsecure.warning");

    private Set<ConnectionListener> connectionListeners
            = Collections.synchronizedSet(new HashSet<ConnectionListener>());

    private Set<TranscriptListener> transcriptListeners
            = Collections.synchronizedSet(new HashSet<TranscriptListener>());

    private Set<ProgressListener> progressListeners
            = Collections.synchronizedSet(new HashSet<ProgressListener>());

    /**
     * Connection attempt being made.
     */
    private boolean opening;

    protected Session(final Host h) {
        this.host = h;
    }

    /**
     * @return The client implementation.
     */
    public C getClient() {
        return client;
    }

    public abstract C connect() throws BackgroundException;

    public C open() throws BackgroundException {
        this.fireConnectionWillOpenEvent();
        client = this.connect();
        this.fireConnectionDidOpenEvent();
        return client;
    }

    /**
     * Send the authentication credentials to the server. The connection must be opened first.
     *
     * @param prompt Prompt
     */
    public abstract void login(LoginController prompt) throws BackgroundException;

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
     * Prompt for username and password if not available.
     *
     * @param controller Prompt
     * @throws LoginCanceledException Login prompt dismissed with cancel
     */
    protected void prompt(final LoginController controller) throws BackgroundException {
        controller.check(host, Locale.localizedString("Login with username and password", "Credentials"), null);
    }

    public boolean isUnsecurewarning() {
        return unsecurewarning;
    }

    public void setUnsecurewarning(boolean unsecurewarning) {
        this.unsecurewarning = unsecurewarning;
    }

    /**
     * Warning if credentials are sent plaintext.
     *
     * @param login Prompt
     * @throws LoginCanceledException If connection should be dropped
     */
    protected void warn(final LoginController login) throws BackgroundException {
        if(this.isUnsecurewarning()
                && !host.getProtocol().isSecure()
                && !host.getCredentials().isAnonymousLogin()
                && !Preferences.instance().getBoolean("connection.unsecure." + host.getHostname())) {
            try {
                login.warn(MessageFormat.format(Locale.localizedString("Unsecured {0} connection", "Credentials"), host.getProtocol().getName()),
                        MessageFormat.format(Locale.localizedString("{0} will be sent in plaintext.", "Credentials"), host.getCredentials().getPasswordPlaceholder()),
                        "connection.unsecure." + host.getHostname());
            }
            finally {
                // Do not warn again upon subsequent login
                this.setUnsecurewarning(false);
            }
        }
    }

    /**
     * Mount the default path of the configured host or the home directory as returned by the server
     * when not given.
     */
    public Path mount() throws BackgroundException {
        this.message(MessageFormat.format(Locale.localizedString("Mounting {0}", "Status"),
                host.getHostname()));
        try {
            final Path home = this.home();
            // Retrieve directory listing of default path
            this.cache().put(home.getReference(), home.list());
            return home;
        }
        catch(BackgroundException e) {
            // The default path does not exist or is not readable due to possible permission issues
            // Fallback to default working directory
            final Path workdir = this.workdir();
            this.cache().put(workdir.getReference(), workdir.list());
            return workdir;
        }
        finally {
            // Reset current working directory in bookmark
            host.setWorkdir(null);
        }
    }

    /**
     * @return Home directory
     */
    public Path home() throws BackgroundException {
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
                final Path workdir = this.workdir();
                if(host.getDefaultPath().startsWith(Path.HOME)) {
                    // Relative path to the home directory
                    return PathFactory.createPath(this, workdir, host.getDefaultPath().substring(1), Path.DIRECTORY_TYPE);
                }
                else {
                    // Relative path
                    return PathFactory.createPath(this, workdir, host.getDefaultPath(), Path.DIRECTORY_TYPE);
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
     */
    public Path workdir() throws BackgroundException {
        return PathFactory.createPath(this, String.valueOf(Path.DELIMITER),
                Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
    }

    /**
     * Logout and close client connection
     *
     * @throws BackgroundException
     */
    public void close() throws BackgroundException {
        this.fireConnectionWillCloseEvent();
        this.logout();
        client = null;
        this.fireConnectionDidCloseEvent();
    }

    /**
     * Close the connecion to the remote host. The protocol specific
     * implementation has to be implemented in the subclasses. Subsequent calls to #getClient() must return null.
     *
     * @see #isConnected()
     */
    public abstract void logout() throws BackgroundException;

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
    public boolean isCreateFolderSupported(final Path workdir) {
        return true;
    }

    /**
     * @param workdir The workdir to create query
     * @return True if creating an empty file is possible.
     * @see ch.cyberduck.core.Path#touch()
     */
    public boolean isCreateFileSupported(final Path workdir) {
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
     */
    public void noop() throws BackgroundException {
        //
    }

    /**
     * Interrupt any running operation asynchroneously by closing the underlying socket.
     * Close the underlying socket regardless of its state; will throw a socket exception
     * on the thread owning the socket
     */
    public void interrupt() throws BackgroundException {
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
     * @see #isSendCommandSupported()
     */
    public void sendCommand(String command) throws BackgroundException {
        throw new BackgroundException("Not supported");
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
    public void archive(final Archive archive, final List<Path> files) throws BackgroundException {
        this.sendCommand(archive.getCompressCommand(files));
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
    public void unarchive(final Archive archive, final Path file) throws BackgroundException {
        this.sendCommand(archive.getDecompressCommand(file));
    }

    /**
     * @return boolean True if the session has not yet been closed.
     */
    public boolean isConnected() {
        return client != null;
    }

    /**
     * @return True if a connection attempt is currently being made. False if the connection
     *         has already been established or is closed.
     */
    public boolean isOpening() {
        return opening;
    }

    public void addConnectionListener(final ConnectionListener listener) {
        connectionListeners.add(listener);
    }

    public void removeConnectionListener(final ConnectionListener listener) {
        connectionListeners.remove(listener);
    }

    /**
     * Notifies all connection listeners that an attempt is made to open this session
     *
     * @throws BackgroundException If the name resolution has been canceled by the user
     * @see ConnectionListener
     */
    protected void fireConnectionWillOpenEvent() throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Connection will open to %s", host));
        }
        ConnectionListener[] l = connectionListeners.toArray(new ConnectionListener[connectionListeners.size()]);
        for(ConnectionListener listener : l) {
            listener.connectionWillOpen();
        }

        // Update status flag
        opening = true;

        // Configuring proxy if any
        ProxyFactory.get().configure(host);

        final Resolver resolver = new Resolver(HostnameConfiguratorFactory.get(host.getProtocol()).lookup(host.getHostname()));
        this.message(MessageFormat.format(Locale.localizedString("Resolving {0}", "Status"),
                host.getHostname()));

        // Try to resolve the hostname first
        try {
            resolver.resolve();
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        // The IP address could successfully be determined
        this.message(MessageFormat.format(Locale.localizedString("Opening {0} connection to {1}", "Status"),
                host.getProtocol().getName(), host.getHostname()));
    }

    /**
     * Starts the <code>KeepAliveTask</code> if <code>connection.keepalive</code> is true
     * Notifies all connection listeners that the connection has been opened successfully
     *
     * @see ConnectionListener
     */
    protected void fireConnectionDidOpenEvent() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Connection did open to %s", host));
        }
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
    public void fireConnectionWillCloseEvent() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Connection will close to %s", host));
        }
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
    public void fireConnectionDidCloseEvent() {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Connection did close to %s", host));
        }
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
     * @see #cdn(LoginController)
     */
    public boolean isCDNSupported() {
        return true;
    }

    public IdentityConfiguration iam() {
        return new AbstractIdentityConfiguration() {
            @Override
            public Credentials getUserCredentials(final String username) {
                return host.getCdnCredentials();
            }
        };
    }

    public boolean isAnalyticsSupported() {
        return this.isCDNSupported();
    }

    public AnalyticsProvider analytics() {
        return new QloudstatAnalyticsProvider();
    }

    public DistributionConfiguration cdn(final LoginController prompt) {
        return new CustomOriginCloudFrontDistributionConfiguration(new S3Session(
                // Configure with the same host as S3 to get the same credentials from the keychain.
                new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(), host.getCdnCredentials())),
                // Use login context of current session
                prompt);
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
        return false;
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