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
import com.enterprisedt.net.ftp.FTPNullReplyException;

import ch.cyberduck.core.*;
import ch.cyberduck.ui.cocoa.threading.BackgroundException;

import com.apple.cocoa.foundation.NSBundle;

import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.UnknownHostException;

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

    public String getSecurityInformation() {
        String syst = this.getIdentification();
        try {
            if(null == syst) {
                return this.host.getIp();
            }
            return syst+" ("+this.host.getIp()+")";
        }
        catch(UnknownHostException e) {
            return this.host.getHostname();
        }
    }

    public boolean isConnected() {
        if(FTP != null) {
            return this.FTP.isConnected();
        }
        return false;
    }

    public void close() {
        synchronized(this) {
            this.fireActivityStartedEvent();
            try {
                if(this.isConnected()) {
                    this.fireConnectionWillCloseEvent();
                    FTP.quit();
                }
            }
            catch(FTPException e) {
                log.error("FTP Error: " + e.getMessage());
            }
            catch(IOException e) {
                log.error("IO Error: " + e.getMessage());
            }
            finally {
                this.fireConnectionDidCloseEvent();
                this.fireActivityStoppedEvent();
            }
        }
    }

    public void interrupt() {
        try {
            if(null == this.FTP) {
                return;
            }
            this.fireConnectionWillCloseEvent();
            this.FTP.interrupt();
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
        finally {
            this.FTP = null;
            this.fireActivityStoppedEvent();
            this.fireConnectionDidCloseEvent();
        }
    }

    public void check() throws IOException {
        try {
            super.check();
        }
        catch(FTPNullReplyException e) {
            this.interrupt();
            this.connect();
        }
    }

    protected void connect() throws IOException, FTPException, ConnectionCanceledException, LoginCanceledException {
        synchronized(this) {
            if(this.isConnected()) {
                return;
            }
            SessionPool.instance().add(this);
            this.fireConnectionWillOpenEvent();
            this.message(NSBundle.localizedString("Opening FTP connection to", "Status", "") + " " + host.getHostname() + "...");
            this.FTP = new FTPClient(host.getEncoding(), new FTPMessageListener() {
                public void logCommand(String cmd) {
                    FTPSession.this.log(cmd);
                }

                public void logReply(String reply) {
                    FTPSession.this.log(reply);
                }
            });
            try {
                this.FTP.connect(host.getHostname(), host.getPort(),
                    Preferences.instance().getInteger("connection.timeout"));
                if(!this.isConnected()) {
                    return;
                }
                this.FTP.setStrictReturnCodes(true);
                if(Proxy.isSOCKSProxyEnabled()) {
                    log.info("Using SOCKS Proxy");
                    FTPClient.initSOCKS(Proxy.getSOCKSProxyPort(), Proxy.getSOCKSProxyHost());
                }
                else {
                    FTPClient.clearSOCKS();
                }
                this.FTP.setConnectMode(host.getFTPConnectMode());
                this.message(NSBundle.localizedString("FTP connection opened", "Status", ""));
                this.login();
                if(Preferences.instance().getBoolean("ftp.sendSystemCommand")) {
                    this.setIdentification(this.FTP.system());
                }
                this.parser = new DefaultFTPFileEntryParserFactory().createFileEntryParser(this.getIdentification());
                this.fireConnectionDidOpenEvent();
            }
            catch(NullPointerException e) {
                // Because the connection could have been closed using #interrupt and set this.FTP to null; we
                // should find a better way to handle this asynchroneous issue than to catch a null pointer
                throw new ConnectionCanceledException();
            }
        }
    }

    protected void login() throws IOException, ConnectionCanceledException, LoginCanceledException {
        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
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

    protected Path workdir() throws ConnectionCanceledException {
        synchronized(this) {
            if(!this.isConnected()) {
                throw new ConnectionCanceledException();
            }
            Path workdir = null;
            try {
                workdir = PathFactory.createPath(this, this.FTP.pwd());
                workdir.attributes.setType(Path.DIRECTORY_TYPE);
            }
            catch(IOException e) {
                this.error("Connection failed", e);
                this.interrupt();
            }
            return workdir;
        }
    }

    protected void noop() throws IOException {
        synchronized(this) {
            if(this.isConnected()) {
                try {
                    this.FTP.noop();
                }
                catch(IOException e) {
                    this.error("Connection failed", e);
                    this.interrupt();
                    throw e;
                }
            }
        }
    }

    public void sendCommand(String command) throws IOException {
        synchronized(this) {
            if(this.isConnected()) {
                this.FTP.quote(command);
            }
        }
    }
}
