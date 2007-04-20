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

import ch.cyberduck.ui.LoginController;
import ch.cyberduck.ui.cocoa.threading.BackgroundException;

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSObject;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * @version $Id$
 */
public abstract class Session extends NSObject {
    private static Logger log = Logger.getLogger(Session.class);

    public static final String SFTP = "sftp";
    public static final String SCP = "scp";
    public static final String FTP = "ftp";
    public static final String FTP_TLS = "ftps";

    public static final String FTP_STRING = NSBundle.localizedString("FTP (File Transfer Protocol)", "");
    public static final String FTP_TLS_STRING = NSBundle.localizedString("FTP-SSL (FTP over TLS/SSL)", "");
    public static final String SFTP_STRING = NSBundle.localizedString("SFTP (SSH Secure File Transfer)", "");
    public static final String SCP_STRING = NSBundle.localizedString("SCP (Secure Copy)", "");

    /**
     * Default port for FTP
     */
    public static final int FTP_PORT = 21;

    /**
     * Default port for SSH
     */
    public static final int SSH_PORT = 22;

    /**
     * Encapsulating all the information of the remote host
     */
    protected Host host = null;

    public Object clone() {
        return SessionFactory.createSession((Host) this.host.clone());
    }

    protected Session(Host h) {
        this.host = h;
    }

    private String identification;

    /**
     * @return The remote host identification such as the response to the SYST command in FTP
     */
    public String getIdentification() {
        return this.identification;
    }

    /**
     * @param id
     */
    public void setIdentification(String id) {
        this.identification = id;
    }

    /**
     * Used for the hostname resolution in the background
     */
    private Resolver resolver;

    /**
     * Assert that the connection to the remote host is still alive. Open connection if needed.
     *
     * @throws IOException The connection to the remote host failed.
     */
    public void check() throws IOException {
        this.fireActivityStartedEvent();
        if(!this.isConnected()) {
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
                try {
                    // Close the underlying socket first
                    this.interrupt();
                    // Try to reconnect once more
                    this.connect();
                    // Do not throw exception as we succeeded on second attempt
                }
                catch(IOException i) {
                    throw i;
                }
            }
        }
    }

    /**
     * @return The timeout in milliseconds
     */
    protected int timeout() {
        return (int)Preferences.instance().getDouble("connection.timeout.seconds")*1000;
    }

    /**
     * @return true if the control channel is either tunneled using TLS or SSH
     */
    public abstract boolean isSecure();

    /**
     *
     * @return
     */
    public abstract String getSecurityInformation();

    /**
     * Opens the TCP connection to the server
     * @throws IOException
     * @throws LoginCanceledException
     */
    protected abstract void connect() throws IOException, ConnectionCanceledException, LoginCanceledException;

    protected LoginController loginController;

    /**
     * Sets the callback to ask for login credentials
     * @param loginController
     * @see #login
     */
    public void setLoginController(LoginController loginController) {
        this.loginController = loginController;
    }

    /**
     * Send the authentication credentials to the server. The connection must be opened first.
     * @see #connect
     * @throws IOException
     * @throws LoginCanceledException
     */
    protected abstract void login() throws IOException, ConnectionCanceledException, LoginCanceledException;

    /**
     * Connect to the remote host and mount the home directory
     *
     * @return null if we fail, the mounted working directory if we succeed
     */
    public Path mount() {
        synchronized(this) {
            this.message(NSBundle.localizedString("Mounting", "Status", "") + " " + host.getHostname() + "...");
            try {
                this.check();
                if(!this.isConnected()) {
                    return null;
                }
                Path home;
                if(host.hasReasonableDefaultPath()) {
                    home = PathFactory.createPath(this, host.getDefaultPath());
                    home.attributes.setType(Path.DIRECTORY_TYPE);
                    if(!home.childs().attributes().isReadable()) {
                        // the default path does not exist or is not readable due to permission issues
                        home = workdir();
                    }
                }
                else {
                    home = workdir();
                }
                return home;
            }
            catch(IOException e) {
                this.error(null, "Connection failed", e);
                this.interrupt();
            }
            return null;
        }
    }

    /**
     * Close the connecion to the remote host.
     * The protocol specific implementation has to  be coded in the subclasses.
     *
     * @see Host
     */
    public abstract void close();

    /**
     * @return the host this session connects to
     */
    public Host getHost() {
        return this.host;
    }

    /**
     *
     * @return The custom character encoding specified by the host
     * of this session or the default encoding if not specified
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
     *
     * @return The maximum number of concurrent connections allowed or -1 if no limit is set
     */
    public int getMaxConnections() {
        if(null == host.getMaxConnections()) {
            return Preferences.instance().getInteger("connection.host.max");
        }
        return host.getMaxConnections().intValue();
    }

    /**
     * @return The current working directory (pwd) or null if it cannot be retrieved for whatever reason
     * @throws ConnectionCanceledException If the underlying connection has already been closed before
     */
    protected abstract Path workdir() throws ConnectionCanceledException;

    /**
     * Send a 'no operation' command
     * @throws IOException
     */
    protected abstract void noop() throws IOException;

    /**
     * Interrupt any running operation asynchroneously by closing the underlying socket.
     * Close the underlying socket regardless of its state; will throw a socket exception
     * on the thread owning the socket
     */
    public void interrupt() {
        if(null == this.resolver) {
            return;
        }
        this.resolver.cancel();
    }

    /**
     * Sends an arbitrary command to the server
     * @param command
     */
    public abstract void sendCommand(String command) throws IOException;

    /**
     * @return boolean True if the session has not yet been closed.
     */
    public abstract boolean isConnected();

    /**
     * A background task to keep a idling connection alive with the server
     */
    private Timer keepAliveTimer = null;

    private Vector listeners = new Vector();

    public void addConnectionListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifies all connection listeners that an attempt is made to open this session
     * @see ConnectionListener
     * @throws ResolveCanceledException If the name resolution has been canceled by the user
     * @throws UnknownHostException If the name resolution failed
     */
    protected void fireConnectionWillOpenEvent() throws ResolveCanceledException, UnknownHostException {
        log.debug("connectionWillOpen");
        ConnectionListener[] l = (ConnectionListener[]) listeners.toArray(
                new ConnectionListener[listeners.size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].connectionWillOpen();
        }
        this.resolver = new Resolver(this.host.getHostname());
        this.message(NSBundle.localizedString("Resolving", "Status", "") + " " + host.getHostname() + "...");
        // Try to resolve the hostname first
        this.resolver.resolve();
        // The IP address could successfully be determined
    }

    /**
     * Starts the <code>KeepAliveTask</code> if <code>connection.keepalive</code> is true
     * Notifies all connection listeners that the connection has been opened successfully
     * @see ConnectionListener
     */
    protected void fireConnectionDidOpenEvent() {
        log.debug("connectionDidOpen");
        if(Preferences.instance().getBoolean("connection.keepalive")) {
            this.keepAliveTimer = new Timer();
            this.keepAliveTimer.scheduleAtFixedRate(new KeepAliveTask(),
                    Preferences.instance().getInteger("connection.keepalive.interval"),
                    Preferences.instance().getInteger("connection.keepalive.interval"));
        }

        ConnectionListener[] l = (ConnectionListener[]) listeners.toArray(
                new ConnectionListener[listeners.size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].connectionDidOpen();
        }
    }

    /**
     * Notifes all connection listeners that a connection is about to be closed
     * @see ConnectionListener
     */
    protected void fireConnectionWillCloseEvent() {
        log.debug("connectionWillClose");
        this.message(NSBundle.localizedString("Disconnecting...", "Status", ""));
        ConnectionListener[] l = (ConnectionListener[]) listeners.toArray(
                new ConnectionListener[listeners.size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].connectionWillClose();
        }
    }

    /**
     * Notifes all connection listeners that a connection has been closed
     * @see ConnectionListener
     */
    protected void fireConnectionDidCloseEvent() {
        log.debug("connectionDidClose");
        if(this.keepAliveTimer != null) {
            this.keepAliveTimer.cancel();
        }
        this.message(NSBundle.localizedString("Disconnected", "Status", ""));
        ConnectionListener[] l = (ConnectionListener[]) listeners.toArray(
                new ConnectionListener[listeners.size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].connectionDidClose();
        }
    }

    /**
     * The caller must call #fireActivityStoppedEvent before
     * @see ConnectionListener#activityStarted
     */
    public void fireActivityStartedEvent() {
        log.debug("activityStarted");
        ConnectionListener[] l = (ConnectionListener[]) listeners.toArray(
                new ConnectionListener[listeners.size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].activityStarted();
        }
    }

    /**
     * The caller must call #fireActivityStartedEvent before
     * @see ConnectionListener#activityStopped
     */
    public void fireActivityStoppedEvent() {
        log.debug("activityStopped");
        ConnectionListener[] l = (ConnectionListener[]) listeners.toArray(
                new ConnectionListener[listeners.size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].activityStopped();
        }
    }

    private Vector transcriptListeners = new Vector();

    public void addTranscriptListener(TranscriptListener listener) {
        transcriptListeners.add(listener);
    }

    public void removeTranscriptListener(TranscriptListener listener) {
        transcriptListeners.remove(listener);
    }

    /**
     * Log the message to all subscribed transcript listeners
     * @see TranscriptListener
     * @param message
     */
    protected void log(final String message) {
        log.info(message);
        TranscriptListener[] l = (TranscriptListener[]) transcriptListeners.toArray(
                new TranscriptListener[transcriptListeners.size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].log(message);
        }
    }

    private Vector progressListeners = new Vector();

    public void addProgressListener(ProgressListener listener) {
        progressListeners.add(listener);
    }

    public void removeProgressListener(ProgressListener listener) {
        progressListeners.remove(listener);
    }

    /**
     * Notifies all progress listeners
     * @param message The message to be displayed in a status field
     * @see ProgressListener
     */
    public void message(final String message) {
        log.info(message);
        ProgressListener[] l = (ProgressListener[]) progressListeners.toArray(
                new ProgressListener[progressListeners.size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].message(message);
        }
    }

    private Vector errorListeners = new Vector();

    public void addErrorListener(ErrorListener listener) {
        errorListeners.add(listener);
    }

    public void removeErrorListener(ErrorListener listener) {
        errorListeners.remove(listener);
    }

    /**
     * Notifies all error listeners of this error without sending this error to Growl
     * @param path The path related to this error
     * @param message The error message to be displayed in the alert sheet
     * @param e The cause of the error
     */
    public void error(Path path, String message, Throwable e) {
        log.info(e.getMessage());
        BackgroundException failure = new BackgroundException(this, path, message, e);
        ErrorListener[] l = (ErrorListener[]) errorListeners.toArray(
                new ErrorListener[errorListeners.size()]);
        for(int i = 0; i < l.length; i++) {
            l[i].error(failure);
        }
    }

    /**
     * A task to send NOOP commands
     */
    private class KeepAliveTask extends TimerTask {
        public void run() {
            try {
                log.info("Sending NOOP to keep connection alive");
                Session.this.noop();
            }
            catch(IOException e) {
                log.warn("Keep alive task failed. Connection closed.");
                Session.this.interrupt();
                this.cancel();
            }
        }
    }

    /**
     * Keeps a ordered backward history of previously visited paths
     */
    private List backHistory = new Collection();

    /**
     * Keeps a ordered forward history of previously visited paths
     */
    private List forwardHistory = new Collection();

    /**
     * @param p
     */
    public void addPathToHistory(Path p) {
        if(backHistory.size() > 0) {
            if(p.equals(backHistory.get(backHistory.size() - 1))) {
                return;
            }
        }
        this.backHistory.add(p);
    }

    /**
     * Returns the prevously browsed path and moves it to the forward history
     * @return The previously browsed path or null if there is none
     */
    public Path getPreviousPath() {
        int size = this.backHistory.size();
        if(size > 1) {
            this.forwardHistory.add(this.backHistory.get(size - 1));
            Path p = (Path) this.backHistory.get(size - 2);
            //delete the fetched path - otherwise we produce a loop
            this.backHistory.remove(size - 1);
            this.backHistory.remove(size - 2);
            return p;
        }
        else if(1 == size) {
            this.forwardHistory.add(this.backHistory.get(size - 1));
            return (Path) this.backHistory.get(size - 1);
        }
        return null;
    }

    /**
     * 
     * @return The last path browsed before #getPrevoiusPath was called
     * @see #getPreviousPath()
     */
    public Path getForwardPath() {
        int size = this.forwardHistory.size();
        if(size > 0) {
            Path p = (Path) this.forwardHistory.get(size - 1);
            this.forwardHistory.remove(size - 1);
            return p;
        }
        return null;
    }

    /**
     *
     * @return The ordered array of prevoiusly visited directories
     */
    public Path[] getBackHistory() {
        return (Path[]) this.backHistory.toArray(new Path[this.backHistory.size()]);
    }

    /**
     *
     * @return The ordered array of prevoiusly visited directories
     */
    public Path[] getForwardHistory() {
        return (Path[]) this.forwardHistory.toArray(new Path[this.forwardHistory.size()]);
    }

    /**
     * Caching files listings of previously visited directories
     */
    private Cache cache = new Cache();

    /**
     *
     * @return The directory listing cache
     */
    public Cache cache() {
        return this.cache;
    }

    /**
     *
     * @param other
     * @return true if the other session denotes the same hostname and protocol
     */
    public boolean equals(Object other) {
        if (null == other) {
            return false;
        }
        if (other instanceof Session) {
            return this.getHost().getHostname().equals(((Session)other).getHost().getHostname())
                    && this.getHost().getProtocol().equals(((Session)other).getHost().getProtocol());
        }
        return false;
    }

    protected void finalize() throws java.lang.Throwable {
        log.debug("finalize:" + super.toString());
        super.finalize();
    }
}
