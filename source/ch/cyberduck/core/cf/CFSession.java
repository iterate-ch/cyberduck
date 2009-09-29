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

import ch.cyberduck.core.*;
import ch.cyberduck.core.http.HTTPSession;
import ch.cyberduck.core.http.StickyHostConfiguration;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.ssl.*;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
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
        @Override
        protected Session create(Host h) {
            return new CFSession(h);
        }
    }

    private AbstractX509TrustManager trustManager;

    /**
     * @return
     */
    public AbstractX509TrustManager getTrustManager() {
        if(null == trustManager) {
            if(Preferences.instance().getBoolean("cf.tls.acceptAnyCertificate")) {
                this.setTrustManager(new IgnoreX509TrustManager());
            }
            else {
                this.setTrustManager(new KeychainX509TrustManager(host.getHostname()));
            }
        }
        return trustManager;
    }

    /**
     * Override the default ignoring trust manager
     *
     * @param trustManager
     */
    private void setTrustManager(AbstractX509TrustManager trustManager) {
        this.trustManager = trustManager;
    }

    protected FilesClient CF;

    protected CFSession(Host h) {
        super(h);
    }

    @Override
    protected void connect() throws IOException, ConnectionCanceledException, LoginCanceledException {
        if(this.isConnected()) {
            return;
        }
        this.CF = new FilesClient();
        this.fireConnectionWillOpenEvent();
        this.message(MessageFormat.format(Locale.localizedString("Opening {0} connection to {1}", "Status"),
                host.getProtocol().getName(), host.getHostname()));

        this.CF.setConnectionTimeOut(this.timeout());
        final HostConfiguration hostConfiguration = new StickyHostConfiguration();
        hostConfiguration.setHost(host.getHostname(), host.getPort(),
                new org.apache.commons.httpclient.protocol.Protocol(host.getProtocol().getScheme(),
                        (ProtocolSocketFactory)new CustomTrustSSLProtocolSocketFactory(this.getTrustManager()), host.getPort())
        );
        this.CF.setHostConfiguration(hostConfiguration);
        this.CF.setUserAgent(this.getUserAgent());

        // Prompt the login credentials first
        this.login();

        this.message(MessageFormat.format(Locale.localizedString("{0} connection opened", "Status"),
                host.getProtocol().getName()));
        this.fireConnectionDidOpenEvent();

    }

    @Override
    protected void login(Credentials credentials) throws IOException {
        this.CF.setUserName(credentials.getUsername());
        this.CF.setPassword(credentials.getPassword());
        this.getTrustManager().setHostname(URI.create(CF.getAuthenticationURL()).getHost());
        if(!this.CF.login()) {
            this.message(Locale.localizedString("Login failed", "Credentials"));
            this.login.fail(host,
                    Locale.localizedString("Login with username and password", "Credentials"));
            this.login();
        }
        this.getTrustManager().setHostname(URI.create(CF.getStorageURL()).getHost());
    }

    @Override
    public void close() {
        try {
            if(this.isConnected()) {
                this.fireConnectionWillCloseEvent();
            }
        }
        finally {
            // No logout required
            CF = null;
            this.fireConnectionDidCloseEvent();
        }
    }

    @Override
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

    @Override
    protected void noop() {
        ;
    }

    @Override
    public void sendCommand(String command) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isConnected() {
        return CF != null;
    }
}