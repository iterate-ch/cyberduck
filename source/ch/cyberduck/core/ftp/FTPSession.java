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
import java.text.MessageFormat;
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
        SessionFactory.addFactory(Protocol.FTP, new Factory());
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

    protected FTPFileEntryParser getFileParser() throws IOException {
        try {
            if(null == parser) {
                String system = null;
                try {
                    system = this.FTP.system();
                }
                catch(FTPException e) {
                    log.warn(this.host.getHostname() + " does not support the SYST command:" + e.getMessage());
                }
                parser = new FTPParserFactory().createFileEntryParser(system);
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
        }
        else {
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

    public String getIdentification() {
        StringBuffer info = new StringBuffer(super.getIdentification() + "\n");
        try {
            info.append(this.FTP.system()).append("\n");
        }
        catch(IOException e) {
            log.warn(this.host.getHostname() + " does not support the SYST command:" + e.getMessage());
        }
        return info.toString();
    }

    public boolean isConnected() {
        if(FTP != null) {
            return this.FTP.isConnected();
        }
        return false;
    }

    public void close() {
        synchronized(this) {
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

    protected FTPClient getClient() {
        return new FTPClient(this.getEncoding(), new FTPMessageListener() {
            public void logCommand(String cmd) {
                FTPSession.this.log(cmd);
            }

            public void logReply(String reply) {
                FTPSession.this.log(reply);
            }
        });
    }

    protected void connect() throws IOException, FTPException, ConnectionCanceledException, LoginCanceledException {
        synchronized(this) {
            if(this.isConnected()) {
                return;
            }
            this.fireConnectionWillOpenEvent();

            this.message(MessageFormat.format(NSBundle.localizedString("Opening {0} connection to {1}", "Status", ""),
                    new Object[]{host.getProtocol().getName(), host.getHostname()}));

            this.FTP = this.getClient();
            this.FTP.setTimeout(this.timeout());
            this.FTP.connect(host.getHostname(true), host.getPort());
            if(!this.isConnected()) {
                throw new ConnectionCanceledException();
            }
            this.FTP.setStrictReturnCodes(true);
            this.FTP.setConnectMode(this.getConnectMode());
            this.message(MessageFormat.format(NSBundle.localizedString("{0} connection opened", "Status", ""),
                    new Object[]{host.getProtocol().getName()}));
            this.login();
            this.fireConnectionDidOpenEvent();
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

    protected void login() throws IOException, LoginCanceledException {
        final Credentials credentials = host.getCredentials();
        login.check(credentials, host.getProtocol(), host.getHostname());
        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
        String failure = null;
        try {
            this.message(MessageFormat.format(NSBundle.localizedString("Authenticating as {0}", "Status", ""),
                    new Object[]{credentials.getUsername()}));

            this.FTP.login(credentials.getUsername(), credentials.getPassword());
            this.message(NSBundle.localizedString("Login successful", "Credentials", ""));
        }
        catch(FTPException e) {
            failure = e.getMessage();
        }
        if(failure != null) {
            this.message(NSBundle.localizedString("Login failed", "Credentials", ""));
            this.login.fail(host.getProtocol(), credentials, failure);
            this.login();
        }
    }

    public Path workdir() throws IOException {
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

    public void setWorkdir(Path workdir) throws IOException {
        if(workdir.equals(this.workdir)) {
            // Do not attempt to change the workdir if the same
            return;
        }
        synchronized(this) {
            if(!this.isConnected()) {
                throw new ConnectionCanceledException();
            }
            this.FTP.chdir(workdir.getAbsolute());
            // Workdir change succeeded
            this.workdir = workdir;
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