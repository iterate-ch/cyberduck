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
import ch.cyberduck.core.cloud.CloudSession;
import ch.cyberduck.core.cloud.Distribution;
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

import com.rackspacecloud.client.cloudfiles.FilesCDNContainer;
import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesException;

/**
 * Rackspace Cloud Files Implementation
 *
 * @version $Id$
 */
public class CFSession extends HTTPSession implements SSLSession, CloudSession {
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
        final HostConfiguration config = new StickyHostConfiguration();
        config.setHost(host.getHostname(), host.getPort(),
                new org.apache.commons.httpclient.protocol.Protocol(host.getProtocol().getScheme(),
                        (ProtocolSocketFactory)new CustomTrustSSLProtocolSocketFactory(this.getTrustManager()), host.getPort())
        );
        final Proxy proxy = ProxyFactory.instance();
        if(proxy.isHTTPSProxyEnabled()) {
            config.setProxy(proxy.getHTTPSProxyHost(), proxy.getHTTPSProxyPort());
        }
        this.CF.setHostConfiguration(config);
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

    /**
     * @param enabled Enable content distribution for the container
     * @param cnames  Currently ignored
     * @param logging
     */
    public void writeDistribution(String container, boolean enabled, String[] cnames, boolean logging) {
        final AbstractX509TrustManager trust = this.getTrustManager();
        try {
            this.check();
            trust.setHostname(URI.create(CF.getCdnManagementURL()).getHost());
            if(enabled) {
                this.message(MessageFormat.format(Locale.localizedString("Enable {0} Distribution", "Status"),
                        Locale.localizedString("Rackspace Cloud Files", "Mosso")));
            }
            else {
                this.message(MessageFormat.format(Locale.localizedString("Disable {0} Distribution", "Status"),
                        Locale.localizedString("Rackspace Cloud Files", "Mosso")));
            }
            if(enabled) {
                try {
                    final FilesCDNContainer info = CF.getCDNContainerInfo(container);
                }
                catch(FilesException e) {
                    log.warn(e.getMessage());
                    // Not found.
                    CF.cdnEnableContainer(container);
                }
            }
            // Toggle content distribution for the container without changing the TTL expiration
            CF.cdnUpdateContainer(container, -1, enabled, logging);
        }
        catch(IOException e) {
            this.error("Cannot write file attributes", e);
        }
        finally {
            trust.setHostname(URI.create(CF.getStorageURL()).getHost());
        }
    }

    public Distribution readDistribution(String container) {
        if(null != container) {
            final AbstractX509TrustManager trust = this.getTrustManager();
            try {
                this.check();
                trust.setHostname(URI.create(CF.getCdnManagementURL()).getHost());
                try {
                    final FilesCDNContainer info = CF.getCDNContainerInfo(container);
                    return new Distribution(info.isEnabled(), info.getCdnURL(),
                            info.isEnabled() ? Locale.localizedString("CDN Enabled", "Mosso") : Locale.localizedString("CDN Disabled", "Mosso"), info.getRetainLogs());
                }
                catch(FilesException e) {
                    log.warn(e.getMessage());
                    // Not found.
                    return new Distribution(false, null, Locale.localizedString("CDN Disabled", "Mosso"));
                }
            }
            catch(IOException e) {
                this.error("Cannot read file attributes", e);
            }
            finally {
                trust.setHostname(URI.create(CF.getStorageURL()).getHost());
            }
        }
        return new Distribution(false, null, null);
    }
}