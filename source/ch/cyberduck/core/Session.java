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
import ch.cyberduck.ui.cocoa.growl.Growl;

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSObject;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * @version $Id$
 */
public abstract class Session extends NSObject
        implements ConnectionListener, ProgressListener, TranscriptListener {

    private static Logger log = Logger.getLogger(Session.class);

    public static final String SFTP = "sftp";
    public static final String FTP = "ftp";
    public static final String FTP_TLS = "ftps";

    public static final String FTP_STRING = NSBundle.localizedString("FTP (File Transfer Protocol)", "");
    public static final String FTP_TLS_STRING = NSBundle.localizedString("FTP-SSL (FTP over TLS/SSL)", "");
    public static final String SFTP_STRING = NSBundle.localizedString("SFTP (SSH Secure File Transfer)", "");

    /**
     * Default port for ftp
     */
    public static final int FTP_PORT = 21;

    /**
     * Default port for ssh
     */
    public static final int SSH_PORT = 22;

    /**
     * Encapsulating all the information of the remote host
     */
    protected Host host = null;

    /**
     * Caching browsed directories
     */
    protected Cache cache = null;

    private List backHistory = new ArrayList();

    private List forwardHistory = new ArrayList();

    /**
     *
     */
    private boolean connected;

    public Session copy() {
        return SessionFactory.createSession(this.host);
    }

    protected Session(Host h) {
        log.debug("Session(" + h + ")");
        this.host = h;
    }

    /**
     * Assert that the connection to the remote host is still alive. Open connection if needed.
     *
     * @throws IOException The connection to the remote host failed.
     * @see Host
     */
    public abstract void check() throws IOException;

    /**
     * @return true if the control channel is either tunneled using TLS or SSH
     */
    public abstract boolean isSecure();

    /**
     * Connect to the remote Host
     * The protocol specific implementation has to  be coded in the subclasses.
     *
     * @see Host
     */
    protected abstract void connect(String encoding) throws IOException;

    protected void connect() throws IOException {
        this.connect(this.host.getEncoding());
    }

    protected LoginController loginController;

    public void setLoginController(LoginController loginController) {
        this.loginController = loginController;
    }

    protected abstract void login() throws IOException;

    /**
     * Connect to the remote host and mount the home directory
     */
    public Path mount() {
        synchronized (this) {
            this.message(NSBundle.localizedString("Mounting", "Status", "") + " " + host.getHostname() + "...");
            try {
                this.check();
                Path home;
                if (host.hasReasonableDefaultPath()) {
                    home = PathFactory.createPath(this, host.getDefaultPath());
                    home.attributes.setType(Path.DIRECTORY_TYPE);
                    if (null == home.list(true)) {
                        // the default path does not exist
                        home = workdir();
                    }
                }
                else {
                    home = workdir();
                }
                Growl.instance().notify(NSBundle.localizedString("Connection opened", "Growl", "Growl Notification"),
                        host.getHostname());
                return home;
            }
            catch (IOException e) {
                this.error("IO " + NSBundle.localizedString("Error", "") + ": " + e.getMessage());
                Growl.instance().notify(NSBundle.localizedString("Connection failed", "Growl", "Growl Notification"),
                        host.getHostname());
                this.close();
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
     *
     * @return the host this session connects to
     */
    public Host getHost() {
        return this.host;
    }

    /**
     * @return The current working directory (pwd)
     */
    public abstract Path workdir();

    /**
     * Send a no operation command
     * @throws IOException
     */
    protected abstract void noop() throws IOException;

    /**
     * Interrupt any running operation by closing the underlying socket
     */
    public abstract void interrupt();

    public abstract void sendCommand(String command);

    /**
     * @return boolean True if the session has not yet been closed.
     */
    public boolean isConnected() {
        return this.connected;
    }

    private Timer keepAliveTimer = null;

    public void setConnected() throws IOException {
        log.debug("setConnected");
        this.connected = true;
        if (Preferences.instance().getBoolean("connection.keepalive")) {
            this.keepAliveTimer = new Timer();
            this.keepAliveTimer.scheduleAtFixedRate(new KeepAliveTask(),
                    Preferences.instance().getInteger("connection.keepalive.interval"),
                    Preferences.instance().getInteger("connection.keepalive.interval"));
        }

        this.connectionDidOpen();
    }

    public void setClosed() {
        log.debug("setClosed");
        this.connected = false;
        this.release();

        this.connectionDidClose();
    }

    public void retain() throws IOException {
        SessionPool.instance().add(this, Preferences.instance().getBoolean("connection.pool.force"));

        this.connectionWillOpen();
    }

    private void release() {
        if (Preferences.instance().getBoolean("connection.keepalive") && this.keepAliveTimer != null) {
            this.keepAliveTimer.cancel();
        }
//        host.getCredentials().setPassword(null);
//        this.cache.clear();
        SessionPool.instance().release(this);
    }

    private Vector connectionListners = new Vector();

    public void addConnectionListener(ConnectionListener listener) {
        connectionListners.add(listener);
    }

    public void removeConnectionListener(ConnectionListener listener) {
        connectionListners.remove(listener);
    }

    public void connectionWillOpen() {
        ConnectionListener[] l = (ConnectionListener[]) connectionListners.toArray(new ConnectionListener[]{});
        for (int i = 0; i < l.length; i++) {
            l[i].connectionWillOpen();
        }
    }

    public void connectionDidOpen() {
        ConnectionListener[] l = (ConnectionListener[]) connectionListners.toArray(new ConnectionListener[]{});
        for (int i = 0; i < l.length; i++) {
            l[i].connectionDidOpen();
        }
    }

    public void connectionWillClose() {
        this.message(NSBundle.localizedString("Disconnecting...", "Status", ""));
        ConnectionListener[] l = (ConnectionListener[]) connectionListners.toArray(new ConnectionListener[]{});
        for (int i = 0; i < l.length; i++) {
            l[i].connectionWillClose();
        }
    }

    public void connectionDidClose() {
        this.message(NSBundle.localizedString("Disconnected", "Status", ""));
        ConnectionListener[] l = (ConnectionListener[]) connectionListners.toArray(new ConnectionListener[]{});
        for (int i = 0; i < l.length; i++) {
            l[i].connectionDidClose();
        }
    }

    public void activityStarted() {
        ConnectionListener[] l = (ConnectionListener[]) connectionListners.toArray(new ConnectionListener[]{});
        for (int i = 0; i < l.length; i++) {
            l[i].activityStarted();
        }
    }

    public void activityStopped() {
        ConnectionListener[] l = (ConnectionListener[]) connectionListners.toArray(new ConnectionListener[]{});
        for (int i = 0; i < l.length; i++) {
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

    public void log(final String message) {
        log.info(message);
        TranscriptListener[] l = (TranscriptListener[]) transcriptListeners.toArray(new TranscriptListener[]{});
        for (int i = 0; i < l.length; i++) {
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

    public void error(final String message) {
        ProgressListener[] l = (ProgressListener[]) progressListeners.toArray(new ProgressListener[]{});
        for (int i = 0; i < l.length; i++) {
            l[i].error(message);
        }
    }

    public void message(final String message) {
        ProgressListener[] l = (ProgressListener[]) progressListeners.toArray(new ProgressListener[]{});
        for (int i = 0; i < l.length; i++) {
            l[i].message(message);
        }
    }

    /**
     * A task to send no operation commands
     */
    private class KeepAliveTask extends TimerTask {
        public void run() {
            try {
                Session.this.noop();
            }
            catch (IOException e) {
                log.error(e.getMessage());
                this.cancel();
            }
        }
    }

    /**
     *
     * @param p
     */
    public void addPathToHistory(Path p) {
        if (backHistory.size() > 0) {
            if (p.equals(backHistory.get(backHistory.size() - 1))) {
                return;
            }
        }
        this.backHistory.add(p);
    }

    /**
     * Moves the returned path to the forward cache
     * @return The previously browsed path or null if there is none
     */
    public Path getPreviousPath() {
        int size = this.backHistory.size();
        if (size > 1) {
            this.forwardHistory.add(this.backHistory.get(size - 1));
            Path p = (Path) this.backHistory.get(size - 2);
            //delete the fetched path - otherwise we produce a loop
            this.backHistory.remove(size - 1);
            this.backHistory.remove(size - 2);
            return p;
        }
        else if (1 == size) {
            this.forwardHistory.add(this.backHistory.get(size - 1));
            return (Path) this.backHistory.get(size - 1);
        }
        return null;
    }

    /**
     *
     * @return
     */
    public Path getForwardPath() {
        int size = this.forwardHistory.size();
        if (size > 0) {
            Path p = (Path) this.forwardHistory.get(size - 1);
            this.forwardHistory.remove(size - 1);
            return p;
        }
        return null;
    }


    public Path[] getBackHistory() {
        return (Path[]) this.backHistory.toArray(new Path[this.backHistory.size()]);
    }

    public Path[] getForwardHistory() {
        return (Path[]) this.forwardHistory.toArray(new Path[this.forwardHistory.size()]);
    }

    public Cache cache() {
        if (null == this.cache) {
            this.cache = new Cache();
        }
        return this.cache;
    }

    protected void finalize() throws java.lang.Throwable {
        log.debug("finalize:" + super.toString());
        super.finalize();
    }
}
