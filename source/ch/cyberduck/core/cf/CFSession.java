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

import ch.cyberduck.core.ConnectionCanceledException;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionFactory;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cloud.CloudSession;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.identity.DefaultCredentialsIdentityConfiguration;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.ssl.AbstractX509TrustManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rackspacecloud.client.cloudfiles.FilesCDNContainer;
import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesException;

/**
 * Rackspace Cloud Files Implementation
 *
 * @version $Id$
 */
public class CFSession extends CloudSession implements DistributionConfiguration {
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
    private FilesClient client;

    public CFSession(Host h) {
        super(h);
    }

    @Override
    protected FilesClient getClient() throws ConnectionCanceledException {
        if(null == client) {
            throw new ConnectionCanceledException();
        }
        return client;
    }

    @Override
    protected void connect() throws IOException {
        if(this.isConnected()) {
            return;
        }
        this.client = new FilesClient(this.http(), null, null, null, null, this.timeout());
        this.fireConnectionWillOpenEvent();

        // Configure for authentication URL
        this.configure();

        // Prompt the login credentials first
        this.login();

        this.fireConnectionDidOpenEvent();
    }

    /**
     * Set connection properties
     *
     * @throws java.io.IOException If the connection is already canceled
     */
    protected void configure() throws IOException {
        final FilesClient c = this.getClient();
        c.setConnectionTimeOut(this.timeout());
        c.setUserAgent(this.getUserAgent());
        // Do not calculate ETag in advance
        c.setUseETag(false);
        c.setAuthenticationURL(this.getAuthenticationUrl());
    }

    private String getAuthenticationUrl() {
        StringBuilder authentication = new StringBuilder();
        authentication.append(host.getProtocol().getScheme().toString()).append("://");
        if(host.getHostname().equals("storage.clouddrive.com")) {
            // Legacy bookmarks. Use default authentication server for Rackspace.
            authentication.append("auth.api.rackspacecloud.com");
        }
        else {
            // Use custom authentication server. Swift (OpenStack Object Storage) installation.
            authentication.append(host.getHostname());
        }
        authentication.append(":").append(host.getPort());
        if(StringUtils.isBlank(host.getProtocol().getContext())) {
            authentication.append(Path.normalize(Preferences.instance().getProperty("cf.authentication.context")));
        }
        else {
            authentication.append(Path.normalize(host.getProtocol().getContext()));
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("Using authentication URL %s", authentication.toString()));
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
                            final FilesClient c = CFSession.this.getClient();
                            if(!c.isLoggedin()) {
                                URI url = new URI(c.getAuthenticationURL());
                                return url.getHost();
                            }
                            if(cdnRequest) {
                                URI url = new URI(c.getCdnManagementURL());
                                return url.getHost();
                            }
                            URI url = new URI(c.getStorageURL());
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
            client = null;
            this.fireConnectionDidCloseEvent();
        }
    }

    /**
     * @return No Content-Range support
     */
    @Override
    public boolean isUploadResumable() {
        return false;
    }

    @Override
    public boolean isRenameSupported(Path file) {
        return !file.attributes().isVolume();
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
        try {
            return StringUtils.isNotBlank(this.getClient().getCdnManagementURL());
        }
        catch(ConnectionCanceledException e) {
            return false;
        }
    }

    @Override
    public DistributionConfiguration cdn() {
        return this;
    }

    /**
     * Cache distribution status result.
     */
    private Map<String, Distribution> distributionStatus
            = new HashMap<String, Distribution>();


    @Override
    public boolean isCached(Distribution.Method method) {
        return !distributionStatus.isEmpty();
    }

    @Override
    public String getOrigin(Distribution.Method method, String container) {
        return container;
    }

    @Override
    public void write(boolean enabled, String origin, Distribution.Method method,
                      String[] cnames, boolean logging, String loggingBucket, String defaultRootObject) {
        try {
            this.check();
            if(enabled) {
                this.message(MessageFormat.format(Locale.localizedString("Enable {0} Distribution", "Status"), "CDN"));
            }
            else {
                this.message(MessageFormat.format(Locale.localizedString("Disable {0} Distribution", "Status"), "CDN"));
            }
            cdnRequest = true;
            try {
                this.getClient().getCDNContainerInfo(origin);
            }
            catch(FilesException e) {
                if(404 == e.getHttpStatusCode()) {
                    // Not found.
                    this.getClient().cdnEnableContainer(origin);
                }
            }
            // Toggle content distribution for the container without changing the TTL expiration
            this.getClient().cdnUpdateContainer(origin, -1, enabled, logging);
        }
        catch(IOException e) {
            this.error("Cannot write CDN configuration", e);
        }
        catch(HttpException e) {
            this.error("Cannot write CDN configuration", e);
        }
        finally {
            distributionStatus.clear();
            cdnRequest = false;
        }
    }

    @Override
    public Distribution read(String origin, final Distribution.Method method) {
        if(!distributionStatus.containsKey(origin)) {
            try {
                this.check();
                this.message(MessageFormat.format(Locale.localizedString("Reading CDN configuration of {0}", "Status"),
                        origin));

                cdnRequest = true;
                final FilesCDNContainer info = this.getClient().getCDNContainerInfo(origin);
                final Distribution distribution = new Distribution(info.getName(),
                        new URI(this.getClient().getStorageURL()).getHost(),
                        method, info.isEnabled(), info.getCdnURL(), info.getSSLURL(), info.getStreamingURL(),
                        info.isEnabled() ? Locale.localizedString("CDN Enabled", "Mosso") : Locale.localizedString("CDN Disabled", "Mosso"),
                        info.getRetainLogs()) {
                    @Override
                    public String getLoggingTarget() {
                        return ".CDN_ACCESS_LOGS";
                    }
                };
                distribution.setContainers(Collections.singletonList(".CDN_ACCESS_LOGS"));
                distributionStatus.put(origin, distribution);
            }
            catch(HttpException e) {
                log.warn(e.getMessage());
                // Not found.
                distributionStatus.put(origin, new Distribution(null, origin, method, false, null, Locale.localizedString("CDN Disabled", "Mosso")));
            }
            catch(IOException e) {
                this.error("Cannot read CDN configuration", e);
            }
            catch(URISyntaxException e) {
                this.error("Cannot read CDN configuration", e);
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

    @Override
    public void invalidate(String origin, Distribution.Method method, List<Path> files, boolean recursive) {
        try {
            this.check();
            this.message(MessageFormat.format(Locale.localizedString("Writing CDN configuration of {0}", "Status"),
                    origin));
            cdnRequest = true;
            for(Path file : files) {
                if(file.isContainer()) {
                    this.getClient().purgeCDNContainer(origin, null);
                }
                else {
                    this.getClient().purgeCDNObject(origin, file.getKey(), null);
                }
            }
        }
        catch(IOException e) {
            this.error("Cannot write CDN configuration", e);
        }
        catch(HttpException e) {
            this.error("Cannot write CDN configuration", e);
        }
        finally {
            distributionStatus.clear();
            cdnRequest = false;
        }
    }

    @Override
    public boolean isInvalidationSupported(Distribution.Method method) {
        return true;
    }

    @Override
    public boolean isDefaultRootSupported(Distribution.Method method) {
        return false;
    }

    @Override
    public boolean isLoggingSupported(Distribution.Method method) {
        return method.equals(Distribution.DOWNLOAD);
    }

    @Override
    public boolean isAnalyticsSupported(Distribution.Method method) {
        return this.isLoggingSupported(method);
    }

    @Override
    public boolean isCnameSupported(Distribution.Method method) {
        return false;
    }

    @Override
    public Protocol getProtocol() {
        return getHost().getProtocol();
    }

    @Override
    public List<Distribution.Method> getMethods(final String container) {
        if(!this.isCDNSupported()) {
            return Collections.emptyList();
        }
        return Arrays.asList(Distribution.DOWNLOAD);
    }

    public String toString() {
        return Locale.localizedString("Akamai", "Mosso");
    }

    @Override
    public String toString(Distribution.Method method) {
        return this.toString();
    }

    @Override
    public void clear() {
        distributionStatus.clear();
    }

    @Override
    public IdentityConfiguration iam() {
        return new DefaultCredentialsIdentityConfiguration(host);
    }
}