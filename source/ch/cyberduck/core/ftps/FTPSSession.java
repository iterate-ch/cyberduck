package ch.cyberduck.core.ftps;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Message;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Proxy;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.ftp.FTPSession;

import com.apple.cocoa.foundation.NSBundle;

import org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory;
import org.apache.log4j.Logger;

import javax.net.ssl.X509TrustManager;
import java.io.IOException;

/**
 * @version $Id$
 */
public class FTPSSession extends FTPSession {
    private static Logger log = Logger.getLogger(FTPSSession.class);

    static {
        SessionFactory.addFactory(Session.FTP_TLS, new Factory());
    }

    private static class Factory extends SessionFactory {
        protected Session create(Host h) {
            return new FTPSSession(h);
        }
    }

    protected FTPSSession(Host h) {
        super(h);
    }

    public boolean isSecure() {
        return trustManager.isServerCertificateTrusted();
    }

    public X509TrustManager getTrustManager() {
        return trustManager;
    }

    public void setTrustManager(AbstractX509TrustManager trustManager) {
        this.trustManager = trustManager;
    }

    private AbstractX509TrustManager trustManager = new IgnoreX509TrustManager();

    public synchronized void connect(String encoding) throws IOException, FTPException {
        this.log(Message.PROGRESS, NSBundle.localizedString("Opening FTP-TLS connection to", "Status", "") + " " + host.getIp() + "...");
        this.setConnected();
        this.log(Message.TRANSCRIPT, "=====================================");
        this.log(Message.TRANSCRIPT, new java.util.Date().toString());
        this.log(Message.TRANSCRIPT, host.getIp());
        this.FTP = new FTPSClient(host.getHostname(),
                host.getPort(),
                Preferences.instance().getInteger("connection.timeout"), //timeout
                encoding, new FTPMessageListener() {
            public void logCommand(String cmd) {
                FTPSSession.this.log(Message.TRANSCRIPT, cmd);
            }

            public void logReply(String reply) {
                FTPSSession.this.log(Message.TRANSCRIPT, reply);
            }
        },
                this.trustManager);
        this.FTP.setStrictReturnCodes(true);
        if (Proxy.isSOCKSProxyEnabled()) {
            log.info("Using SOCKS Proxy");
            FTPClient.initSOCKS(Proxy.getSOCKSProxyPort(), Proxy.getSOCKSProxyHost());
        }
        else {
            FTPClient.clearSOCKS();
        }
        this.FTP.setConnectMode(this.host.getFTPConnectMode());
        this.log(Message.PROGRESS, NSBundle.localizedString("FTP connection opened", "Status", ""));
        ((FTPSClient) this.FTP).auth();
        this.login();
        if (Preferences.instance().getBoolean("ftp.sendSystemCommand")) {
            this.host.setIdentification(this.FTP.system());
        }
        this.parser = new DefaultFTPFileEntryParserFactory().createFileEntryParser(this.host.getIdentification());
    }
}
	