package ch.cyberduck.core.ftp;

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

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPMessageListener;

import ch.cyberduck.core.*;

import com.apple.cocoa.foundation.NSBundle;

import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory;
import org.apache.log4j.Logger;

import java.io.IOException;

/**
 * Opens a connection to the remote server via ftp protocol
 *
 * @version $Id$
 */
public class FTPSession extends Session {
    private static Logger log = Logger.getLogger(FTPSession.class);

    static {
        SessionFactory.addFactory(Session.FTP, new Factory());
    }

    private static class Factory extends SessionFactory {
        protected Session create(Host h) {
            return new FTPSession(h);
        }
    }

    protected FTPClient FTP;
    protected FTPFileEntryParser parser;

    protected FTPSession(Host h) {
        super(h);
    }

    public boolean isSecure() {
        return false;
    }

    public void close() {
        synchronized(this) {
            this.activityStarted();
            this.connectionWillClose();
            try {
                if (FTP != null) {
                    FTP.quit();
                    host.getCredentials().setPassword(null);
                    FTP = null;
                }
            }
            catch (FTPException e) {
                log.error("FTP Error: " + e.getMessage());
            }
            catch (IOException e) {
                log.error("IO Error: " + e.getMessage());
            }
            finally {
                this.activityStopped();
                this.setClosed();
            }
        }
    }

    public void interrupt() {
        try {
            if (null == this.FTP) {
                return;
            }
            this.FTP.interrupt();
        }
        catch (FTPException e) {
            this.error("FTP " + NSBundle.localizedString("Error", "") + ": " + e.getMessage());
        }
        catch (IOException e) {
            this.error("IO " + NSBundle.localizedString("Error", "") + ": " + e.getMessage());
        }
    }

    public void connect(String encoding) throws IOException, FTPException {
        try {
            synchronized(this) {
                this.retain();
                this.message(NSBundle.localizedString("Opening FTP connection to", "Status", "") + " " + host.getIp() + "...");
                this.log("=====================================");
                this.log(new java.util.Date().toString());
                this.log(host.getIp());
                this.FTP = new FTPClient(host.getHostname(),
                        host.getPort(),
                        Preferences.instance().getInteger("connection.timeout"), //timeout
                        encoding, new FTPMessageListener() {
                    public void logCommand(String cmd) {
                        FTPSession.this.log(cmd);
                    }

                    public void logReply(String reply) {
                        FTPSession.this.log(reply);
                    }
                });
                this.FTP.setStrictReturnCodes(true);
                if (Proxy.isSOCKSProxyEnabled()) {
                    log.info("Using SOCKS Proxy");
                    FTPClient.initSOCKS(Proxy.getSOCKSProxyPort(), Proxy.getSOCKSProxyHost());
                }
                else {
                    FTPClient.clearSOCKS();
                }
                this.FTP.setConnectMode(this.host.getFTPConnectMode());
                this.message(NSBundle.localizedString("FTP connection opened", "Status", ""));
                this.login();
                if (Preferences.instance().getBoolean("ftp.sendSystemCommand")) {
                    this.host.setIdentification(this.FTP.system());
                }
                this.parser = new DefaultFTPFileEntryParserFactory().createFileEntryParser(this.host.getIdentification());
                this.setConnected();
            }
        }
        catch (FTPException e) {
            this.error("FTP " + NSBundle.localizedString("Error", "") + ": " + e.getMessage());
            throw e;
        }
        catch (IOException e) {
            this.error("IO " + NSBundle.localizedString("Error", "") + ": " + e.getMessage());
            throw e;
        }
    }

    protected void login() throws IOException {
        log.debug("login");
        Login credentials = host.getCredentials();
        if (credentials.check(loginController)) {
            try {
                this.message(NSBundle.localizedString("Authenticating as", "Status", "") + " " + host.getCredentials().getUsername() + "...");
                this.FTP.login(credentials.getUsername(), credentials.getPassword());
                credentials.addInternetPasswordToKeychain();
                this.message(NSBundle.localizedString("Login successful", "Status", ""));
            }
            catch (FTPException e) {
                this.message(NSBundle.localizedString("Login failed", "Status", ""));
                host.setCredentials(credentials.promptUser("Authentication for user " 
                        + credentials.getUsername() + " failed. The server response is: " + e.getMessage(),
                        this.loginController));
                if (host.getCredentials().tryAgain()) {
                    this.login();
                }
                else {
                    throw new FTPException("Login as user " + credentials.getUsername() + " canceled.");
                }
            }
        }
        else {
            throw new FTPException("Login as user " + host.getCredentials().getUsername() + " failed.");
        }
    }

    public Path workdir() {
        try {
            this.check();
            Path workdir = PathFactory.createPath(this, this.FTP.pwd());
            workdir.attributes.setType(Path.DIRECTORY_TYPE);
            return workdir;
        }
        catch (FTPException e) {
            this.error("FTP " + NSBundle.localizedString("Error", "") + ": " + e.getMessage());
        }
        catch (IOException e) {
            this.error("IO " + NSBundle.localizedString("Error", "") + ": " + e.getMessage());
            this.close();
        }
        return null;
    }

    protected void noop() throws IOException {
        synchronized(this) {
            if (this.isConnected()) {
                this.FTP.noop();
            }
        }
    }

    public void sendCommand(String command) {
        try {
            this.FTP.quote(command);
        }
        catch (FTPException e) {
            this.error("FTP " + NSBundle.localizedString("Error", "") + ": " + e.getMessage());
        }
        catch (IOException e) {
            this.error("IO " + NSBundle.localizedString("Error", "") + ": " + e.getMessage());
            this.close();
        }
    }

    public void check() throws IOException {
        this.activityStarted();
        if (null == this.FTP) {
            this.connect();
            return;
        }
        this.host.getIp();
        if (!this.FTP.isAlive()) {
            this.close();
            this.connect();
        }
    }
}