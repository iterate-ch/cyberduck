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

import ch.cyberduck.core.*;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.ssl.IgnoreX509TrustManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.core.ssl.SSLSession;

import org.apache.log4j.Logger;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;

import com.enterprisedt.net.ftp.FTPClient;
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
        if(Preferences.instance().getBoolean("ftp.tls.acceptAnyCertificate")) {
            this.setTrustManager(new IgnoreX509TrustManager());
        }
        else {
            this.setTrustManager(new KeychainX509TrustManager(h.getHostname()));
        }
    }

    /**
     * A trust manager accepting any certificate by default
     */
    private X509TrustManager trustManager;

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

    protected FTPClient getClient() {
        // AUTH command required before login
        auth = true;
        return new FTPSClient(this.getEncoding(), messageListener, this.getTrustManager());
    }

    private boolean auth;

    public void login(final Credentials credentials) throws IOException {
        if(auth) {
            // Only send AUTH before the first login attempt
            try {
                ((FTPSClient) this.FTP).auth();
                auth = false;
            }
            catch(SSLHandshakeException e) {
                throw new ConnectionCanceledException(e.getMessage());
            }
        }
        super.login(credentials);
    }
}