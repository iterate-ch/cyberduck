package ch.cyberduck.core.cf;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cloud.CloudHTTP4Session;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.ssl.AbstractX509TrustManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;

import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import com.rackspacecloud.client.cloudfiles.FilesCDNContainer;
import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.*;

/**
 * Rackspace Cloud Files Implementation
 *
 * @version $Id$
 */
public class CFSession extends CloudHTTP4Session {
    private static Logger log = Logger.getLogger(CFSession.class);

    private static class Factory extends SessionFactory {
        @Override
        protected Session create(Host h) {
            return new CFSession(h);
        }
    }

    public static SessionFactory factory() {
        return new Factory();
    }

    /**
     * Cloudfiles
     */
    private FilesClient CF;

    /**
     * Limelight
     */
    private DistributionConfiguration cdn;

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
    protected void connect() throws IOException {
        if(this.isConnected()) {
            return;
        }
        this.CF = new FilesClient(this.http(), null, null, null, null, this.timeout());
        this.fireConnectionWillOpenEvent();

        // Configure for authentication URL
        this.configure();

        // Prompt the login credentials first
        this.login();

        this.fireConnectionDidOpenEvent();
    }

    /**
     * Set connection properties
     */
    protected void configure() throws IOException {
        FilesClient client = this.getClient();
        client.setConnectionTimeOut(this.timeout());
        client.setUserAgent(this.getUserAgent());
        // Do not calculate ETag in advance
        client.setUseETag(false);
        client.setAuthenticationURL(this.getAuthenticationUrl());
    }

    private String getAuthenticationUrl() {
        StringBuilder authentication = new StringBuilder(host.getProtocol().getScheme()).append("://");
        if(host.getHostname().equals(Protocol.CLOUDFILES.getDefaultHostname())) {
            // Use default authentication server. Rackspace.
            authentication.append(Preferences.instance().getProperty("cf.authentication.host"));
        }
        else {
            // Use custom authentication server. Swift (OpenStack Object Storage) installation.
            authentication.append(host.getHostname()).append(":").append(host.getPort());
        }
        authentication.append(Preferences.instance().getProperty("cf.authentication.context"));
        if(log.isInfoEnabled()) {
            log.info("Using authentication URL " + authentication.toString());
        }
        return authentication.toString();
    }

    /**
     * Request to CDN URL in progress
     */
    private boolean cdnRequest;

    @Override
    public AbstractX509TrustManager getTrustManager(String hostname) {
        if(!trust.containsKey(hostname)) {
            trust.put(hostname, new KeychainX509TrustManager() {
                /**
                 * Different hostname depending if authentication has completed or not.
                 * @return Authentication or storage hostname.
                 */
                @Override
                public String getHostname() {
                    try {
                        if(CFSession.this.isConnected()) {
                            FilesClient client = CFSession.this.getClient();
                            if(!client.isLoggedin()) {
                                URI url = new URI(client.getAuthenticationURL());
                                return url.getHost();
                            }
                            if(cdnRequest) {
                                URI url = new URI(client.getCdnManagementURL());
                                return url.getHost();
                            }
                            URI url = new URI(client.getStorageURL());
                            return url.getHost();
                        }
                        else {
                            URI url = new URI(CFSession.this.getAuthenticationUrl());
                            return url.getHost();
                        }
                    }
                    catch(URISyntaxException e) {
                        log.error("Failure parsing URI:" + e.getMessage());
                    }
                    catch(ConnectionCanceledException e) {
                        log.warn(e.getMessage());
                    }
                    return null;
                }
            });
        }
        return trust.get(hostname);
    }

    @Override
    protected void login(LoginController controller, Credentials credentials) throws IOException {
        FilesClient client = this.getClient();
        client.setUserName(credentials.getUsername());
        client.setPassword(credentials.getPassword());
        try {
            if(!client.login()) {
                this.message(Locale.localizedString("Login failed", "Credentials"));
                controller.fail(host.getProtocol(), credentials);
                this.login();
            }
        }
        catch(HttpException e) {
            IOException failure = new IOException(e.getMessage());
            failure.initCause(e);
            throw failure;
        }
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
     * Renaming is not currently supported
     *
     * @return Always false
     */
    @Override
    public boolean isRenameSupported(Path file) {
        return false;
    }

    /**
     * Creating files is only possible inside a bucket.
     *
     * @param workdir The workdir to create query
     * @return False if directory is root.
     */
    @Override
    public boolean isCreateFileSupported(Path workdir) {
        return !workdir.isRoot();
    }

    @Override
    public boolean isChecksumSupported() {
        return true;
    }

    @Override
    public boolean isCDNSupported() {
        return host.getHostname().equals(Protocol.CLOUDFILES.getDefaultHostname());
    }

    @Override
    public DistributionConfiguration cdn() {
        if(host.getHostname().equals(Protocol.CLOUDFILES.getDefaultHostname())) {
            if(null == cdn) {
                cdn = new DistributionConfiguration() {
                    /**
                     * Cache distribution status result.
                     */
                    private Map<String, Distribution> distributionStatus
                            = new HashMap<String, Distribution>();


                    public boolean isConfigured(Distribution.Method method) {
                        return !distributionStatus.isEmpty();
                    }

                    public String getOrigin(Distribution.Method method, String container) {
                        return container;
                    }

                    /**
                     * @param enabled Enable content distribution for the container
                     * @param method
                     * @param cnames  Currently ignored
                     * @param logging
                     */
                    public void write(boolean enabled, String origin, Distribution.Method method,
                                      String[] cnames, boolean logging, String defaultRootObject) {
                        try {
                            CFSession.this.check();
                            if(enabled) {
                                CFSession.this.message(MessageFormat.format(Locale.localizedString("Enable {0} Distribution", "Status"),
                                        Locale.localizedString("Rackspace Cloud Files", "Mosso")));
                            }
                            else {
                                CFSession.this.message(MessageFormat.format(Locale.localizedString("Disable {0} Distribution", "Status"),
                                        Locale.localizedString("Rackspace Cloud Files", "Mosso")));
                            }
                            cdnRequest = true;
                            URI url = new URI(CFSession.this.getClient().getCdnManagementURL());
                            if(enabled) {
                                try {
                                    final FilesCDNContainer info = CFSession.this.getClient().getCDNContainerInfo(origin);
                                }
                                catch(FilesException e) {
                                    log.warn(e.getMessage());
                                    // Not found.
                                    CFSession.this.getClient().cdnEnableContainer(origin);
                                }
                            }
                            // Toggle content distribution for the container without changing the TTL expiration
                            CFSession.this.getClient().cdnUpdateContainer(origin, -1, enabled, logging);
                        }
                        catch(IOException e) {
                            CFSession.this.error("Cannot write CDN configuration", e);
                        }
                        catch(URISyntaxException e) {
                            CFSession.this.error("Cannot write CDN configuration", e);
                        }
                        catch(HttpException e) {
                            CFSession.this.error("Cannot write CDN configuration", e);
                        }
                        finally {
                            distributionStatus.clear();
                            cdnRequest = false;
                        }
                    }

                    public Distribution read(String origin, Distribution.Method method) {
                        if(!distributionStatus.containsKey(origin)
                                || !distributionStatus.get(origin).isDeployed()) {
                            try {
                                CFSession.this.check();
                                CFSession.this.message(MessageFormat.format(Locale.localizedString("Reading CDN configuration of {0}", "Status"),
                                        origin));

                                cdnRequest = true;
                                final FilesCDNContainer info = CFSession.this.getClient().getCDNContainerInfo(origin);
                                final Distribution distribution = new Distribution(info.getName(),
                                        new URI(CFSession.this.getClient().getStorageURL()).getHost(),
                                        method, info.isEnabled(), info.getCdnURL(),
                                        info.isEnabled() ? Locale.localizedString("CDN Enabled", "Mosso") : Locale.localizedString("CDN Disabled", "Mosso"),
                                        info.getRetainLogs());
                                distributionStatus.put(origin, distribution);
                            }
                            catch(HttpException e) {
                                log.warn(e.getMessage());
                                // Not found.
                                distributionStatus.put(origin, new Distribution(null, origin, method, false, null, Locale.localizedString("CDN Disabled", "Mosso")));
                            }
                            catch(IOException e) {
                                CFSession.this.error("Cannot read CDN configuration", e);
                            }
                            catch(URISyntaxException e) {
                                CFSession.this.error("Cannot read CDN configuration", e);
                            }
                            finally {
                                cdnRequest = false;
                            }
                        }
                        if(distributionStatus.containsKey(origin)) {
                            return distributionStatus.get(origin);
                        }
                        return new Distribution(origin, method);
                    }

                    public void invalidate(String origin, Distribution.Method method, List<Path> files, boolean recursive) {
                        throw new UnsupportedOperationException();
                    }

                    public boolean isInvalidationSupported(Distribution.Method method) {
                        return false;
                    }

                    public boolean isDefaultRootSupported(Distribution.Method method) {
                        return false;
                    }

                    public boolean isLoggingSupported(Distribution.Method method) {
                        return method.equals(Distribution.DOWNLOAD);
                    }

                    public boolean isCnameSupported(Distribution.Method method) {
                        return false;
                    }

                    public List<Distribution.Method> getMethods() {
                        return Arrays.asList(Distribution.DOWNLOAD);
                    }

                    public String toString() {
                        return Locale.localizedString("Akamai", "Mosso");
                    }

                    public void clear() {
                        distributionStatus.clear();
                    }
                };
            }
        }
        else {
            // Amazon CloudFront custom origin
            cdn = super.cdn();
        }
        return cdn;
    }

    public List<String> getSupportedStorageClasses() {
        return Collections.emptyList();
    }
}