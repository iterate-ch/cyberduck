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
import ch.cyberduck.core.http.StickyHostConfiguration;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.ssl.*;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.log4j.Logger;

import com.rackspacecloud.client.cloudfiles.FilesCDNContainer;
import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesException;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Rackspace Cloud Files Implementation
 *
 * @version $Id$
 */
public class CFSession extends CloudSession implements SSLSession {
    private static Logger log = Logger.getLogger(CFSession.class);

    public static class Factory extends SessionFactory {
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
                trustManager = new IgnoreX509TrustManager();
            }
            else {
                trustManager = new KeychainX509TrustManager(host.getHostname());
            }
        }
        return trustManager;
    }

    private FilesClient CF;

    public CFSession(Host h) {
        super(h);
    }

    @Override
    protected FilesClient getClient() throws ConnectionCanceledException {
        if(null == CF) {
            throw new ConnectionCanceledException();
        }
        return CF;
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

        this.getClient().setConnectionTimeOut(this.timeout());
        final HostConfiguration config = new StickyHostConfiguration();
        config.setHost(host.getHostname(), host.getPort(),
                new org.apache.commons.httpclient.protocol.Protocol(host.getProtocol().getScheme(),
                        (ProtocolSocketFactory) new CustomTrustSSLProtocolSocketFactory(this.getTrustManager()), host.getPort())
        );
        final Proxy proxy = ProxyFactory.instance();
        if(proxy.isHTTPSProxyEnabled()) {
            config.setProxy(proxy.getHTTPSProxyHost(), proxy.getHTTPSProxyPort());
        }
        this.getClient().setHostConfiguration(config);
        this.getClient().setUserAgent(this.getUserAgent());

        // Prompt the login credentials first
        this.login();

        this.message(MessageFormat.format(Locale.localizedString("{0} connection opened", "Status"),
                host.getProtocol().getName()));
        this.fireConnectionDidOpenEvent();

    }

    @Override
    protected void login(Credentials credentials) throws IOException {
        this.getClient().setUserName(credentials.getUsername());
        this.getClient().setPassword(credentials.getPassword());
        this.getTrustManager().setHostname(URI.create(this.getClient().getAuthenticationURL()).getHost());
        if(!this.getClient().login()) {
            this.message(Locale.localizedString("Login failed", "Credentials"));
            this.login.fail(host.getProtocol(), credentials,
                    Locale.localizedString("Login with username and password", "Credentials"));
            this.login();
        }
        this.getTrustManager().setHostname(URI.create(this.getClient().getStorageURL()).getHost());
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
    protected void noop() {
        ;
    }

    @Override
    public void sendCommand(String command) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * @return No Content-Range support
     */
    @Override
    public boolean isDownloadResumable() {
        return false;
    }

    /**
     * @return No Content-Range support
     */
    @Override
    public boolean isUploadResumable() {
        return false;
    }

    /**
     * @param enabled Enable content distribution for the container
     * @param method
     * @param cnames  Currently ignored
     * @param logging
     */
    @Override
    public void writeDistribution(boolean enabled, String container, Distribution.Method method, String[] cnames, boolean logging) {
        final AbstractX509TrustManager trust = this.getTrustManager();
        try {
            this.check();
            trust.setHostname(URI.create(this.getClient().getCdnManagementURL()).getHost());
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
                    final FilesCDNContainer info = this.getClient().getCDNContainerInfo(container);
                }
                catch(FilesException e) {
                    log.warn(e.getMessage());
                    // Not found.
                    this.getClient().cdnEnableContainer(container);
                }
            }
            // Toggle content distribution for the container without changing the TTL expiration
            this.getClient().cdnUpdateContainer(container, -1, enabled, logging);
        }
        catch(IOException e) {
            this.error("Cannot write file attributes", e);
        }
        finally {
            try {
                trust.setHostname(URI.create(this.getClient().getStorageURL()).getHost());
            }
            catch(ConnectionCanceledException e) {
                log.error(e.getMessage());
            }
        }
    }

    @Override
    public Distribution readDistribution(String container, Distribution.Method method) {
        if(null != container) {
            final AbstractX509TrustManager trust = this.getTrustManager();
            try {
                this.check();
                trust.setHostname(URI.create(this.getClient().getCdnManagementURL()).getHost());
                try {
                    final FilesCDNContainer info = this.getClient().getCDNContainerInfo(container);
                    return new Distribution(info.isEnabled(), info.getCdnURL(),
                            info.isEnabled() ? Locale.localizedString("CDN Enabled", "Mosso") : Locale.localizedString("CDN Disabled", "Mosso"),
                            info.getRetainLogs());
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
                try {
                    trust.setHostname(URI.create(this.getClient().getStorageURL()).getHost());
                }
                catch(ConnectionCanceledException e) {
                    log.error(e.getMessage());
                }
            }
        }
        return new Distribution();
    }

    @Override
    public String getDistributionServiceName() {
        return Locale.localizedString("Limelight Content", "Mosso");
    }

    @Override
    public List<Distribution.Method> getSupportedDistributionMethods() {
        return Arrays.asList(Distribution.DOWNLOAD);
    }

    @Override
    public List<String> getSupportedStorageClasses() {
        return Collections.emptyList();
    }
}