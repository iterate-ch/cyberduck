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

import com.apple.cocoa.foundation.NSBundle;

import ch.cyberduck.core.*;
import ch.cyberduck.core.ftp.parser.CompositeFileEntryParser;
import ch.cyberduck.core.ftp.parser.LaxUnixFTPEntryParser;
import ch.cyberduck.core.ftp.parser.RumpusFTPEntryParser;

import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.parser.NetwareFTPEntryParser;
import org.apache.commons.net.ftp.parser.ParserInitializationException;
import org.apache.commons.net.ftp.parser.UnixFTPEntryParser;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.enterprisedt.net.ftp.*;

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
            return syst + " (" + this.host.getIp() + ")";
        }
        catch(UnknownHostException e) {
            return this.host.getHostname();
        }
    }

    protected FTPFileEntryParser getFileParser() throws IOException {
        try {
            if(null == parser) {
                parser = new FTPParserFactory().createFileEntryParser(this.getIdentification());
            }
            return parser;
        }
        catch(ParserInitializationException e) {
            throw new IOException(e.getMessage());
        }
    }

    private Map parsers = new HashMap(1);

    /**
     * @param p
     * @return True if the parser will read the file permissions
     */
    protected boolean isPermissionSupported(final FTPFileEntryParser p) {
        FTPFileEntryParser delegate;
        if(p instanceof CompositeFileEntryParser) {
            // Get the actual parser
            delegate = ((CompositeFileEntryParser) p).getCachedFtpFileEntryParser();
            if(null == delegate) {
                log.warn("Composite FTP parser has no cached delegate yet");
                return false;
            }
        } else {
            // Not a composite parser
            delegate = p;
        }
        if(null == parsers.get(delegate)) {
            // Cache the value as it might get queried frequently
            parsers.put(delegate, Boolean.valueOf(delegate instanceof UnixFTPEntryParser
                    || delegate instanceof LaxUnixFTPEntryParser
                    || delegate instanceof NetwareFTPEntryParser
                    || delegate instanceof RumpusFTPEntryParser)
            );
        }
        return ((Boolean) parsers.get(delegate)).booleanValue();
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
            super.interrupt();
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
        catch(FTPException e) {
            log.debug(e.getMessage());
            this.interrupt();
            this.connect();
        }
        catch(FTPNullReplyException e) {
            log.debug(e.getMessage());
            this.interrupt();
            this.connect();
        }
    }

    protected void connect() throws IOException, FTPException, ConnectionCanceledException, LoginCanceledException {
        synchronized(this) {
            if(this.isConnected()) {
                return;
            }
            this.fireConnectionWillOpenEvent();
            this.message(NSBundle.localizedString("Opening FTP connection to", "Status", "") + " " + host.getHostname() + "...");
            this.FTP = new FTPClient(this.getEncoding(), new FTPMessageListener() {
                public void logCommand(String cmd) {
                    FTPSession.this.log(cmd);
                }

                public void logReply(String reply) {
                    FTPSession.this.log(reply);
                }
            });
            try {
                this.FTP.setTimeout(this.timeout());
                this.FTP.connect(host.getHostname(true), host.getPort());
                if(!this.isConnected()) {
                    throw new ConnectionCanceledException();
                }
                this.FTP.setStrictReturnCodes(true);
                this.FTP.setConnectMode(this.getConnectMode());
                this.message(NSBundle.localizedString("FTP connection opened", "Status", ""));
                this.login();
                try {
                    this.setIdentification(this.FTP.system());
                }
                catch(FTPException e) {
                    log.warn(this.host.getHostname() + " does not support the SYST command:" + e.getMessage());
                }
                this.fireConnectionDidOpenEvent();
            }
            catch(NullPointerException e) {
                // Because the connection could have been closed using #interrupt and set this.FTP to null; we
                // should find a better way to handle this asynchroneous issue than to catch a null pointer
                throw new ConnectionCanceledException();
            }
        }
    }

    /**
     * @return The custom encoding specified in the host of this session
     *         or the default encoding if no cusdtom encoding is set
     * @see Preferences
     * @see Host
     */
    protected FTPConnectMode getConnectMode() {
        if(null == this.host.getFTPConnectMode()) {
            if(Proxy.usePassiveFTP()) {
                return FTPConnectMode.PASV;
            }
            return FTPConnectMode.ACTIVE;
        }
        return this.host.getFTPConnectMode();

    }

    protected void login() throws IOException, ConnectionCanceledException, LoginCanceledException {
        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
        if(host.getCredentials().check(this.loginController, host.getProtocol(), host.getHostname())) {
            String failure = null;
            try {
                this.message(NSBundle.localizedString("Authenticating as", "Status", "") + " "
                        + host.getCredentials().getUsername() + "...");
                this.FTP.login(host.getCredentials().getUsername(), host.getCredentials().getPassword());
                this.message(NSBundle.localizedString("Login successful", "Credentials", ""));
                host.getCredentials().addInternetPasswordToKeychain(host.getProtocol(),
                        host.getHostname(), host.getPort());
            }
            catch(FTPException e) {
                failure = e.getMessage();
            }
            catch(FTPNullReplyException e) {
                failure = e.getMessage();
            }
            if(failure != null) {
                this.message(NSBundle.localizedString("Login failed", "Credentials", ""));
                loginController.promptUser(host.getProtocol(), host.getCredentials(),
                        NSBundle.localizedString("Login failed", "Credentials", ""),
                        failure);
                if(!host.getCredentials().tryAgain()) {
                    throw new LoginCanceledException();
                }
                this.login();
            }
        } else {
            throw new LoginCanceledException();
        }
    }

    protected Path workdir() throws IOException {
        synchronized(this) {
            if(!this.isConnected()) {
                throw new ConnectionCanceledException();
            }
            if(null == workdir) {
                workdir = PathFactory.createPath(this, this.FTP.pwd(), Path.DIRECTORY_TYPE);
            }
            return workdir;
        }
    }

    protected void setWorkdir(Path workdir) throws IOException {
        if(workdir.equals(this.workdir)) {
            // Do not attempt to change the workdir if the same
            return;
        }
        synchronized(this) {
            if(!this.isConnected()) {
                throw new ConnectionCanceledException();
            }
            try {
                this.FTP.chdir(workdir.getAbsolute());
                // Workdir change succeeded
                this.workdir = workdir;
            }
            catch(IOException e) {
                throw e;
            }
        }
    }

    protected void noop() throws IOException {
        synchronized(this) {
            if(this.isConnected()) {
                this.FTP.noop();
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