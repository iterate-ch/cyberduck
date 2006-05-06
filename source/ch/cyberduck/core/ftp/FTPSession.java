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

    public boolean isConnected() {
        if(FTP != null) {
            return this.FTP.isConnected();
        }
        return false;
    }

    public void close() {
        synchronized(this) {
            this.activityStarted();
            try {
                if(this.isConnected()) {
                    this.connectionWillClose();
                    FTP.quit();
                    this.connectionDidClose();
                }
            }
            catch(FTPException e) {
                log.error("FTP Error: " + e.getMessage());
            }
            catch(IOException e) {
                log.error("IO Error: " + e.getMessage());
            }
            finally {
                FTP = null;
                this.activityStopped();
            }
        }
    }

    public void interrupt() {
        try {
            if(null == this.FTP) {
                return;
            }
            this.connectionWillClose();
            this.FTP.interrupt();
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
        finally {
            this.connectionDidClose();
        }
    }

    protected void connect() throws IOException, FTPException, LoginCanceledException {
        synchronized(this) {
            SessionPool.instance().add(this);
            this.connectionWillOpen();
            this.message(NSBundle.localizedString("Opening FTP connection to", "Status", "") + " " + host.getHostname() + "...");
            this.log("=====================================");
            this.log(new java.util.Date().toString());
            this.log(host.getIp());
            this.FTP = new FTPClient(host.getEncoding(), new FTPMessageListener() {
                public void logCommand(String cmd) {
                    FTPSession.this.log(cmd);
                }

                public void logReply(String reply) {
                    FTPSession.this.log(reply);
                }
            });
            this.FTP.connect(host.getHostname(), host.getPort(),
                    Preferences.instance().getInteger("connection.timeout"));
            this.FTP.setStrictReturnCodes(true);
            if(Proxy.isSOCKSProxyEnabled()) {
                log.info("Using SOCKS Proxy");
                FTPClient.initSOCKS(Proxy.getSOCKSProxyPort(), Proxy.getSOCKSProxyHost());
            }
            else {
                FTPClient.clearSOCKS();
            }
            this.FTP.setConnectMode(this.host.getFTPConnectMode());
            this.message(NSBundle.localizedString("FTP connection opened", "Status", ""));
            this.login();
            if(Preferences.instance().getBoolean("ftp.sendSystemCommand")) {
                this.host.setIdentification(this.FTP.system());
            }
            this.parser = new DefaultFTPFileEntryParserFactory().createFileEntryParser(this.host.getIdentification());
            this.connectionDidOpen();
        }
    }

    protected void login() throws IOException, LoginCanceledException {
        log.debug("login");
        if(host.getCredentials().check(loginController)) {
            try {
                this.message(NSBundle.localizedString("Authenticating as", "Status", "") + " "
                        + host.getCredentials().getUsername() + "...");
                this.FTP.login(host.getCredentials().getUsername(), host.getCredentials().getPassword());
                host.getCredentials().addInternetPasswordToKeychain();
                this.message(NSBundle.localizedString("Login successful", "Credentials", ""));
            }
            catch(FTPException e) {
                this.message(NSBundle.localizedString("Login failed", "Credentials", ""));
                loginController.promptUser(host.getCredentials(),
                        NSBundle.localizedString("Login failed", "Credentials", ""),
                        e.getMessage());
                if(!host.getCredentials().tryAgain()) {
                    throw new LoginCanceledException();
                }
                this.login();
            }
        }
        else {
            throw new LoginCanceledException();
        }
    }

    public Path workdir() {
        try {
            Path workdir = PathFactory.createPath(this, this.FTP.pwd());
            workdir.attributes.setType(Path.DIRECTORY_TYPE);
            return workdir;
        }
        catch(FTPException e) {
            this.error(e);
        }
        catch(IOException e) {
            this.error(e);
            this.interrupt();
        }
        return null;
    }

    protected void noop() throws IOException {
        synchronized(this) {
            if(this.isConnected()) {
                this.FTP.noop();
            }
        }
    }

    public void sendCommand(String command) {
        try {
            this.FTP.quote(command);
        }
        catch(FTPException e) {
            this.error(e);
        }
        catch(IOException e) {
            this.error(e);
            this.interrupt();
        }
    }
}