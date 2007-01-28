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

import ch.cyberduck.core.*;
import ch.cyberduck.core.ftp.FTPSession;

import com.apple.cocoa.foundation.NSBundle;

import org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory;
import org.apache.log4j.Logger;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.CertificateExpiredException;

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
        return this.isConnected();
    }

    /**
     * @return
     */
    public String getSecurityInformation() {
        StringBuffer info = new StringBuffer();
        X509Certificate[] accepted = this.trustManager.getAcceptedIssuers();
        for(int i = 0; i < accepted.length; i++) {
            info.append(accepted[i].toString());
        }
        return info.toString();
    }

    public X509TrustManager getTrustManager() {
        return trustManager;
    }

    public void setTrustManager(X509TrustManager trustManager) {
        this.trustManager = trustManager;
    }

    private X509TrustManager trustManager = new IgnoreX509TrustManager();

    protected void connect() throws IOException, FTPException, ConnectionCanceledException, LoginCanceledException {
        synchronized(this) {
            if(this.isConnected()) {
                return;
            }
            this.fireConnectionWillOpenEvent();
            this.message(NSBundle.localizedString("Opening FTP-TLS connection to", "Status", "") + " " + host.getHostname() + "...");
            this.FTP = new FTPSClient(this.getEncoding(), new FTPMessageListener() {
                public void logCommand(String cmd) {
                    FTPSSession.this.log(cmd);
                }

                public void logReply(String reply) {
                    FTPSSession.this.log(reply);
                }
            }, this.trustManager);
            try {
                this.FTP.setTimeout(Preferences.instance().getInteger("connection.timeout"));
                if(Proxy.isSOCKSProxyEnabled()) {
                    log.info("Using SOCKS Proxy");
                    FTPClient.initSOCKS(Proxy.getSOCKSProxyPort(), Proxy.getSOCKSProxyHost());
                }
                else {
                    FTPClient.clearSOCKS();
                }
                this.FTP.connect(host.getHostname(), host.getPort());
                if(!this.isConnected()) {
                    return;
                }
                this.FTP.setStrictReturnCodes(true);
                this.FTP.setConnectMode(this.getConnectMode());
                this.message(NSBundle.localizedString("FTP connection opened", "Status", ""));
                ((FTPSClient) this.FTP).auth();
                this.login();
                if(Preferences.instance().getBoolean("ftp.sendSystemCommand")) {
                    this.setIdentification(this.FTP.system());
                }
                this.parser = new DefaultFTPFileEntryParserFactory().createFileEntryParser(this.getIdentification());
                this.fireConnectionDidOpenEvent();
            }
            catch(SSLHandshakeException e) {
                this.close();
            }
            catch(NullPointerException e) {
                // Because the connection could have been closed using #interrupt and set this.FTP to null; we
                // should find a better way to handle this asynchroneous issue than to catch a null pointer
                throw new ConnectionCanceledException();
            }
        }
    }
}