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

import com.apple.cocoa.foundation.NSBundle;

import ch.cyberduck.core.*;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.ssl.IgnoreX509TrustManager;
import ch.cyberduck.core.ssl.SSLSession;

import org.apache.log4j.Logger;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;

import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPMessageListener;

/**
 * @version $Id$
 */
public class FTPSSession extends FTPSession implements SSLSession {
    private static Logger log = Logger.getLogger(FTPSSession.class);

    static {
        SessionFactory.addFactory(Protocol.FTP_TLS, new Factory());
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
     * A trust manager accepting any certificate by default
     */
    private X509TrustManager trustManager
            = new IgnoreX509TrustManager();

    /**
     * @return
     */
    public X509TrustManager getTrustManager() {
        return trustManager;
    }

    /**
     * Override the default ignoring trust manager
     *
     * @param trustManager
     */
    public void setTrustManager(X509TrustManager trustManager) {
        this.trustManager = trustManager;
    }

    /**
     * @return
     */
    public String getSecurityInformation() {
        StringBuffer info = new StringBuffer();
        X509Certificate[] accepted = this.getTrustManager().getAcceptedIssuers();
        for(int i = 0; i < accepted.length; i++) {
            info.append(accepted[i].toString());
        }
        return info.toString();
    }

    protected void connect() throws IOException, FTPException, ConnectionCanceledException, LoginCanceledException {
        synchronized(this) {
            if(this.isConnected()) {
                return;
            }
            this.fireConnectionWillOpenEvent();

            this.message(MessageFormat.format(NSBundle.localizedString("Opening {0} connection to {1}...", "Status", ""),
                    new Object[]{host.getProtocol().getName(), host.getHostname()}));

            this.FTP = new FTPSClient(this.getEncoding(), new FTPMessageListener() {
                public void logCommand(String cmd) {
                    FTPSSession.this.log(cmd);
                }

                public void logReply(String reply) {
                    FTPSSession.this.log(reply);
                }
            }, this.getTrustManager());
            try {
                this.FTP.setTimeout(this.timeout());
                this.FTP.connect(host.getHostname(true), host.getPort());
                if(!this.isConnected()) {
                    throw new ConnectionCanceledException();
                }
                this.FTP.setStrictReturnCodes(true);
                this.FTP.setConnectMode(this.getConnectMode());
                this.message(MessageFormat.format(NSBundle.localizedString("{0} connection opened", "Status", ""),
                        new Object[]{host.getProtocol().getName()}));
                ((FTPSClient) this.FTP).auth();
                this.login();
                try {
                    this.setIdentification(this.FTP.system());
                }
                catch(FTPException e) {
                    log.warn(this.host.getHostname() + " does not support the SYST command:" + e.getMessage());
                }
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