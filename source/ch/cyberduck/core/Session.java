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
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.IOResumeException;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.ThrottledInputStream;
import ch.cyberduck.core.io.ThrottledOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
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
     * Mount the default path of the configured host or the home directory as returned by the server
     * when not given.
     */
    public Path mount(final ListProgressListener listener) throws BackgroundException {
        try {
            final Path home = this.home();
            // Retrieve directory listing of default path
            cache.put(home.getReference(), this.list(home, listener));
            return home;
        }
        catch(NotfoundException e) {
            // The default path does not exist or is not readable due to possible permission issues
            // Fallback to default working directory
            final Path workdir = this.workdir();
            cache.put(workdir.getReference(), this.list(workdir, listener));
            return workdir;
        }
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
     * @param workdir The workdir to create query
     * @return True if creating an empty file is possible.
     */
    public boolean isCreateFileSupported(final Path workdir) {
        return true;
    }

    public boolean isRenameSupported(final Path file) {
        return true;
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
     * @return List of known ACL users
     */
    public List<Acl.User> getAvailableAclUsers() {
        return Collections.emptyList();
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
        return String.format("Session %s", host);
    }

    /**
     * URL pointing to the resource using the protocol of the current session.
     *
     * @return Null if there is a encoding failure
     */
    public String toURL(final Path path) {
        return this.toURL(path, true);
    }

    /**
     * @param credentials Include username
     * @return Null if there is a encoding failure
     */
    public String toURL(final Path path, final boolean credentials) {
        return String.format("%s%s", host.toURL(credentials), URIEncoder.encode(path.getAbsolute()));
    }

    /**
     * @return The URL accessible with HTTP using the
     *         hostname configuration from the bookmark
     */
    public String toHttpURL(final Path path) {
        return URI.create(host.getWebURL() + URIEncoder.encode(PathRelativizer.relativize(
                PathNormalizer.normalize(host.getDefaultPath(), true),
                path.getAbsolute()))).normalize().toString();
    }

    /**
     * Includes both native protocol and HTTP URLs
     *
     * @return A list of URLs pointing to the resource.
     * @see #getHttpURLs(Path)
     */
    public Set<DescriptiveUrl> getURLs(final Path path) {
        Set<DescriptiveUrl> list = new LinkedHashSet<DescriptiveUrl>();
        list.add(new DescriptiveUrl(this.toURL(path), MessageFormat.format(Locale.localizedString("{0} URL"),
                host.getProtocol().getScheme().toString().toUpperCase(java.util.Locale.ENGLISH))));
        list.addAll(this.getHttpURLs(path));
        return list;
    }

    /**
     * URLs to open in web browser.
     * Including URLs to CDN.
     *
     * @return All possible URLs to the same resource that can be opened in a web browser.
     */
    public Set<DescriptiveUrl> getHttpURLs(final Path path) {
        final Set<DescriptiveUrl> urls = new LinkedHashSet<DescriptiveUrl>();
        // Include default Web URL
        final String http = this.toHttpURL(path);
        if(StringUtils.isNotBlank(http)) {
            urls.add(new DescriptiveUrl(http, MessageFormat.format(Locale.localizedString("{0} URL"), "HTTP")));
        }
        return urls;
    }

    /**
     * URL that requires authentication in the web browser.
     *
     * @return Empty.
     */
    public DescriptiveUrl toAuthenticatedUrl(final Path path) {
        return DescriptiveUrl.EMPTY;
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

    public abstract void mkdir(Path file, String region) throws BackgroundException;

    public abstract AttributedList<Path> list(Path file, ListProgressListener listener) throws BackgroundException;

    /**
     * @param renamed Must be an absolute path
     */
    public abstract void rename(Path file, Path renamed) throws BackgroundException;

    /**
     * Remove this file from the remote host. Does not affect any corresponding local file
     *
     * @param prompt Login prompt for multi factor authentication
     */
    public void delete(Path file, final LoginController prompt) throws BackgroundException {
        this.getFeature(Delete.class, prompt).delete(Collections.singletonList(file));
    }

    /**
     * @param status Transfer status
     * @return Stream to read from to download file
     */
    public abstract InputStream read(Path file, TransferStatus status) throws BackgroundException;

    public void download(final Path file, final BandwidthThrottle throttle, final StreamListener listener,
                         final TransferStatus status) throws BackgroundException {
        try {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = read(file, status);
                out = file.getLocal().getOutputStream(status.isResume());
                this.download(in, out, throttle, listener, status);
            }
            finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download failed", e, file);
        }
    }

    /**
     * @param status Transfer status
     * @return Stream to write to for upload
     */
    public abstract OutputStream write(Path file, TransferStatus status) throws BackgroundException;

    public void upload(final Path file, final BandwidthThrottle throttle, final StreamListener listener,
                       final TransferStatus status) throws BackgroundException {
        try {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = file.getLocal().getInputStream();
                out = write(file, status);
                this.upload(out, in, throttle, listener, status);
            }
            finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload failed", e, file);
        }
    }

    /**
     * @param out      Remote stream
     * @param in       Local stream
     * @param throttle The bandwidth limit
     * @param l        Listener for bytes sent
     * @param status   Transfer status
     * @throws java.io.IOException Write not completed due to a I/O problem
     */
    public void upload(final OutputStream out, final InputStream in,
                       final BandwidthThrottle throttle,
                       final StreamListener l, final TransferStatus status) throws IOException, ConnectionCanceledException {
        this.upload(out, in, throttle, l, status.getCurrent(), -1, status);
    }

    /**
     * Will copy from in to out. Will attempt to skip Status#getCurrent
     * from the inputstream but not from the outputstream. The outputstream
     * is asssumed to append to a already existing file if
     * Status#getCurrent > 0
     *
     * @param out      The stream to write to
     * @param in       The stream to read from
     * @param throttle The bandwidth limit
     * @param l        The stream listener to notify about bytes received and sent
     * @param offset   Start reading at offset in file
     * @param limit    Transfer only up to this length
     * @param status   Transfer status
     * @throws ch.cyberduck.core.io.IOResumeException
     *                     If the input stream fails to skip the appropriate
     *                     number of bytes
     * @throws IOException Write not completed due to a I/O problem
     */
    public void upload(final OutputStream out, final InputStream in, final BandwidthThrottle throttle,
                       final StreamListener l, long offset, final long limit, final TransferStatus status) throws IOException, ConnectionCanceledException {
        if(log.isDebugEnabled()) {
            log.debug("upload(" + out.toString() + ", " + in.toString());
        }
        if(offset > 0) {
            long skipped = in.skip(offset);
            if(log.isInfoEnabled()) {
                log.info(String.format("Skipping %d bytes", skipped));
            }
            if(skipped < status.getCurrent()) {
                throw new IOResumeException(String.format("Skipped %d bytes instead of %d",
                        skipped, status.getCurrent()));
            }
        }
        new StreamCopier().transfer(in, new ThrottledOutputStream(out, throttle), l, limit, status);
    }

    /**
     * Will copy from in to out. Does not attempt to skip any bytes from the streams.
     *
     * @param in       The stream to read from
     * @param out      The stream to write to
     * @param throttle The bandwidth limit
     * @param l        The stream listener to notify about bytes received and sent
     * @param status   Transfer status
     * @throws IOException Write not completed due to a I/O problem
     */
    public void download(final InputStream in, final OutputStream out, final BandwidthThrottle throttle,
                         final StreamListener l, final TransferStatus status) throws IOException, ConnectionCanceledException {
        if(log.isDebugEnabled()) {
            log.debug("download(" + in.toString() + ", " + out.toString());
        }
        new StreamCopier().transfer(new ThrottledInputStream(in, throttle), out, l, -1, status);
    }

    public <T> T getFeature(final Class<T> type, final LoginController prompt) {
        if(type == Touch.class) {
            // Use login context of current session
            return (T) new DefaultTouchFeature(this);
        }
        if(type == DistributionConfiguration.class) {
            // Use login context of current session
            return (T) new CustomOriginCloudFrontDistributionConfiguration(host, prompt);
        }
        return null;
    }
}