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
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cloud.CloudSession;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.identity.DefaultCredentialsIdentityConfiguration;
import ch.cyberduck.core.identity.IdentityConfiguration;

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
import com.rackspacecloud.client.cloudfiles.FilesContainerMetaData;
import com.rackspacecloud.client.cloudfiles.FilesException;

/**
 * Rackspace Cloud Files Implementation
 *
 * @version $Id$
 */
public class CFSession extends CloudSession implements DistributionConfiguration {
    private static final Logger log = Logger.getLogger(CFSession.class);

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
        final StringBuilder authentication = new StringBuilder();
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
            authentication.append(PathNormalizer.normalize(Preferences.instance().getProperty("cf.authentication.context")));
        }
        else {
            authentication.append(PathNormalizer.normalize(host.getProtocol().getContext()));
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("Using authentication URL %s", authentication.toString()));
        }
        return authentication.toString();
    }

    @Override
    protected void login(final LoginController controller, final Credentials credentials) throws IOException {
        final FilesClient client = this.getClient();
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
    public boolean isRenameSupported(final Path file) {
        return !file.attributes().isVolume();
    }

    /**
     * Creating files is only possible inside a bucket.
     *
     * @param workdir The workdir to create query
     * @return False if directory is root.
     */
    @Override
    public boolean isCreateFileSupported(final Path workdir) {
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
    public void write(final boolean enabled, final String origin, final Distribution.Method method,
                      final String[] cnames, final boolean logging, final String loggingBucket, final String defaultRootObject) {
        try {
            this.check();
            if(enabled) {
                this.message(MessageFormat.format(Locale.localizedString("Enable {0} Distribution", "Status"), "CDN"));
            }
            else {
                this.message(MessageFormat.format(Locale.localizedString("Disable {0} Distribution", "Status"), "CDN"));
            }
            if(StringUtils.isNotBlank(defaultRootObject)) {
                this.getClient().updateContainerMetadata(origin, Collections.singletonMap("Web-Index", defaultRootObject));
            }
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
        }
    }

    @Override
    public Distribution read(final String origin, final Distribution.Method method) {
        if(!distributionStatus.containsKey(origin)) {
            try {
                this.check();
                this.message(MessageFormat.format(Locale.localizedString("Reading CDN configuration of {0}", "Status"),
                        origin));

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
                final FilesContainerMetaData metadata = this.getClient().getContainerMetaData(origin);
                if(metadata.getMetaData().containsKey("Web-Index")) {
                    distribution.setDefaultRootObject(metadata.getMetaData().get("Web-Index"));
                }
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
        }
        if(distributionStatus.containsKey(origin)) {
            return distributionStatus.get(origin);
        }
        return new Distribution(origin, method);
    }

    @Override
    public void invalidate(final String origin, final Distribution.Method method, final List<Path> files, final boolean recursive) {
        try {
            this.check();
            this.message(MessageFormat.format(Locale.localizedString("Writing CDN configuration of {0}", "Status"),
                    origin));
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
        }
    }

    @Override
    public boolean isInvalidationSupported(final Distribution.Method method) {
        return true;
    }

    @Override
    public boolean isDefaultRootSupported(final Distribution.Method method) {
        return true;
    }

    @Override
    public boolean isLoggingSupported(final Distribution.Method method) {
        return method.equals(Distribution.DOWNLOAD);
    }

    @Override
    public boolean isAnalyticsSupported(final Distribution.Method method) {
        return this.isLoggingSupported(method);
    }

    @Override
    public boolean isCnameSupported(final Distribution.Method method) {
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

    public String getName() {
        return Locale.localizedString("Akamai", "Mosso");
    }

    @Override
    public String getName(final Distribution.Method method) {
        return this.getName();
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