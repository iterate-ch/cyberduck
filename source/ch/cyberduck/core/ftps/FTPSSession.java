package ch.cyberduck.core.ftps;

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

import com.enterprisedt.net.ftp.FTPMessageListener;
import org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory;
import org.apache.log4j.Logger;

import ch.cyberduck.core.*;
import ch.cyberduck.core.ftp.FTPSession;

/**
 * @version $Id$
 */
public class FTPSSession extends FTPSession {
    private static Logger log = Logger.getLogger(FTPSSession.class);

    static {
        SessionFactory.addFactory(Session.FTPS, new Factory());
        Protocol ftps = new Protocol("ftps", new SSLProtocolSocketFactory(), 21);
        Protocol.registerProtocol("ftps", ftps);
    }

    private static class Factory extends SessionFactory {
        protected Session create(Host h) {
            return new FTPSSession(h);
        }
    }

    protected FTPSSession(Host h) {
        super(h);
    }

    public synchronized void connect(String encoding) throws IOException {
        this.log("Opening FTP connection to " + host.getIp() + "...", Message.PROGRESS);
        this.setConnected();
        this.log("=====================================", Message.TRANSCRIPT);
        this.log(new java.util.Date().toString(), Message.TRANSCRIPT);
        this.log(host.getIp(), Message.TRANSCRIPT);
        this.FTP = new FTPSClient(host.getHostname(),
                host.getPort(),
                Preferences.instance().getInteger("connection.timeout"), //timeout
                encoding, new FTPMessageListener() {
                    public void logCommand(String cmd) {
                        FTPSSession.this.log(cmd, Message.TRANSCRIPT);
                    }

                    public void logReply(String reply) {
                        FTPSSession.this.log(reply, Message.TRANSCRIPT);
                    }
                });
        this.FTP.setStrictReturnCodes(true);
        if (Proxy.isSOCKSProxyEnabled()) {
            log.info("Using SOCKS Proxy");
            this.FTP.initSOCKS(Proxy.getSOCKSProxyPort(),
                    Proxy.getSOCKSProxyHost());
            if (Proxy.isSOCKSAuthenticationEnabled()) {
                log.info("Using SOCKS Proxy Authentication");
                this.FTP.initSOCKSAuthentication(Proxy.getSOCKSProxyUser(),
                        Proxy.getSOCKSProxyPassword());
            }
        }
        this.FTP.setConnectMode(this.host.getFTPConnectMode());
        this.log("FTP connection opened", Message.PROGRESS);
        ((FTPSClient) this.FTP).auth('c');
        this.login();
        if (Preferences.instance().getBoolean("ftp.sendSystemCommand")) {
            this.host.setIdentification(this.FTP.system());
        }
        this.parser = new DefaultFTPFileEntryParserFactory().createFileEntryParser(this.host.getIdentification());
    }
}
	