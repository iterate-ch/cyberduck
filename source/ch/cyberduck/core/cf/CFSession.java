package ch.cyberduck.core.cf;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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
import ch.cyberduck.core.http.StickyHostConfiguration;
import ch.cyberduck.core.http.HTTPSession;
import ch.cyberduck.core.ssl.IgnoreX509TrustManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.core.ssl.SSLSession;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;

import org.apache.log4j.Logger;
import org.apache.commons.httpclient.HostConfiguration;

import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.text.MessageFormat;

import com.mosso.client.cloudfiles.FilesClient;

/**
 * Mosso Cloud Files Implementation
 *
 * @version $Id$
 */
public class CFSession extends HTTPSession implements SSLSession {
    private static Logger log = Logger.getLogger(CFSession.class);

    static {
        SessionFactory.addFactory(Protocol.MOSSO, new Factory());
    }

    private static class Factory extends SessionFactory {
        protected Session create(Host h) {
            return new CFSession(h);
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

    protected FilesClient CF;

    protected CFSession(Host h) {
        super(h);
        if(Preferences.instance().getBoolean("s3.tls.acceptAnyCertificate")) {
            this.setTrustManager(new IgnoreX509TrustManager());
        }
        else {
            this.setTrustManager(new KeychainX509TrustManager(h.getHostname()));
        }
    }

    protected void connect() throws IOException, ConnectionCanceledException, LoginCanceledException {
        if(this.isConnected()) {
            return;
        }
        this.fireConnectionWillOpenEvent();
        this.message(MessageFormat.format(NSBundle.localizedString("Opening {0} connection to {1}", "Status", ""),
                host.getProtocol().getName(), host.getHostname()));

        // Prompt the login credentials first
        this.login();

        this.message(MessageFormat.format(NSBundle.localizedString("{0} connection opened", "Status", ""),
                host.getProtocol().getName()));
        this.fireConnectionDidOpenEvent();

    }

    protected void login(Credentials credentials) throws IOException {
        this.CF = new FilesClient(credentials.getUsername(), credentials.getPassword(),
                null, this.timeout());
        final HostConfiguration hostConfiguration = new StickyHostConfiguration();
        hostConfiguration.setHost(host.getHostname(), host.getPort(),
                new org.apache.commons.httpclient.protocol.Protocol(host.getProtocol().getScheme(),
                        new CustomTrustSSLProtocolSocketFactory(this.getTrustManager()), host.getPort())
        );
        this.CF.setHostConfiguration(hostConfiguration);
        this.CF.setUserAgent(this.getUserAgent());

        if(!this.CF.login()) {
            this.message(NSBundle.localizedString("Login failed", "Credentials", ""));
            this.login.fail(host.getProtocol(), credentials,
                    NSBundle.localizedString("Login with username and password", "Credentials", ""));
            this.login();
        }
    }

    public void close() {
        try {
            if(this.isConnected()) {
                this.fireConnectionWillCloseEvent();
            }
        }
        finally {
            CF = null;
            this.fireConnectionDidCloseEvent();
        }
    }

    public void interrupt() {
        try {
            super.interrupt();
            if(this.isConnected()) {
                this.fireConnectionWillCloseEvent();
            }
        }
        finally {
            CF = null;
            this.fireConnectionDidCloseEvent();
        }
    }

    public Path workdir() throws IOException {
        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
        if(null == workdir) {
            workdir = PathFactory.createPath(this, Path.DELIMITER, Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        }
        return workdir;
    }

    protected void noop() throws IOException {
        ;
    }

    public void sendCommand(String command) throws IOException {
        throw new UnsupportedOperationException();
    }

    public boolean isConnected() {
        return CF != null;
    }
}
