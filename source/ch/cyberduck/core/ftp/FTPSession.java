package ch.cyberduck.core.ftp;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import java.io.IOException;

import org.apache.log4j.Logger;

import ch.cyberduck.core.*;
import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;

/**
 * Opens a connection to the remote server via ftp protocol
 *
 * @version $Id$
 */
public class FTPSession extends Session {
    private static Logger log = Logger.getLogger(Session.class);

    static {
        SessionFactory.addFactory(Session.FTP, new Factory());
    }

    private static class Factory extends SessionFactory {
        protected Session create(Host h) {
            return new FTPSession(h);
        }
    }

    protected FTPClient FTP;

    public FTPSession(Host h) {
        super(h);
    }

    public synchronized void close() {
		this.callObservers(new Message(Message.CLOSE, "Closing session."));
        try {
            if (this.FTP != null) {
                this.log("Disconnecting...", Message.PROGRESS);
                this.FTP.quit();
                this.host.getLogin().setPassword(null);
                this.FTP = null;
            }
        }
        catch (FTPException e) {
            this.log("FTP Error: " + e.getMessage(), Message.ERROR);
        }
        catch (IOException e) {
            this.log("IO Error: " + e.getMessage(), Message.ERROR);
        }
        finally {
            this.log("Disconnected", Message.PROGRESS);
            this.setConnected(false);
        }
    }

    public synchronized void connect() throws IOException {
        this.callObservers(new Message(Message.OPEN, "Opening session."));
        this.log("Opening FTP connection to " + host.getIp() + "...", Message.PROGRESS);
        this.log(new java.util.Date().toString(), Message.TRANSCRIPT);
        this.log(host.getIp(), Message.TRANSCRIPT);
        this.FTP = new FTPClient();
        if (Preferences.instance().getProperty("ftp.connectmode").equals("active")) {
            this.FTP.setConnectMode(FTPConnectMode.ACTIVE);
        }
        else {
            this.FTP.setConnectMode(FTPConnectMode.PASV);
        }
        this.FTP.connect(host.getHostname(), host.getPort());
        this.log("FTP connection opened", Message.PROGRESS);
        this.login();
        if (Preferences.instance().getProperty("ftp.sendSystemCommand").equals("true")) {
            this.host.setIdentification(this.FTP.system());
        }
        this.setConnected(true);
    }

    private synchronized void login() throws IOException {
        log.debug("login");
        try {
            Login credentials = host.getLogin();
            if (credentials.check()) {
                this.log("Authenticating as " + host.getLogin().getUsername() + "...", Message.PROGRESS);
                this.FTP.login(credentials.getUsername(), credentials.getPassword());
                credentials.addPasswordToKeychain();
                this.log("Login successful", Message.PROGRESS);
            }
        }
        catch (FTPException e) {
            this.log("Login failed", Message.PROGRESS);
            if (host.getLogin().promptUser("Authentication for user " + host.getLogin().getUsername() + " failed. The server response is: " + e.getMessage())) {
                // let's try again with the new values
                this.login();
            }
            else {
                throw new FTPException("Login as user " + host.getLogin().getUsername() + " failed.");
            }
        }
    }

    public synchronized Path workdir() {
        try {
            this.check();
            return PathFactory.createPath(this, this.FTP.pwd());
        }
        catch (FTPException e) {
            this.log("FTP Error: " + e.getMessage(), Message.ERROR);
        }
        catch (IOException e) {
            this.log("IO Error: " + e.getMessage(), Message.ERROR);
        }
        return null;
    }
	
    public synchronized void check() throws IOException {
        log.debug(this.toString() + ":check");
        this.log("Working", Message.START);
//		this.log("Checking connection...", Message.PROGRESS);
        if (null == this.FTP || !this.FTP.isAlive()) {
            this.setConnected(false);
            this.connect();
        }
    }
}