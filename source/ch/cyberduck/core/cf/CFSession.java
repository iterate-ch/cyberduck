package ch.cyberduck.core.cf;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyController;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.analytics.QloudstatAnalyticsProvider;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.FilesExceptionMappingService;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.identity.DefaultCredentialsIdentityConfiguration;
import ch.cyberduck.core.identity.IdentityConfiguration;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.rackspacecloud.client.cloudfiles.FilesAuthenticationResponse;
import com.rackspacecloud.client.cloudfiles.FilesClient;
import com.rackspacecloud.client.cloudfiles.FilesException;
import com.rackspacecloud.client.cloudfiles.FilesRegion;

/**
 * Rackspace Cloud Files Implementation
 *
 * @version $Id$
 */
public class CFSession extends HttpSession<FilesClient> {
    private static final Logger log = Logger.getLogger(CFSession.class);

    private Map<String, FilesRegion> regions
            = new HashMap<String, FilesRegion>();

    private Map<Path, Distribution> distributions
            = new HashMap<Path, Distribution>();

    public CFSession(Host h) {
        super(h);
    }

    @Override
    public FilesClient connect(final HostKeyController key) throws BackgroundException {
        return new FilesClient(this.http());
    }

    protected FilesRegion getRegion(final Path container) throws BackgroundException {
        final String location = container.attributes().getRegion();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Lookup region for container %s in region %s", container, location));
        }
        if(regions.containsKey(location)) {
            return regions.get(location);
        }
        log.warn(String.format("Unknown region %s in authentication context", location));
        if(regions.containsKey(null)) {
            final FilesRegion region = regions.get(null);
            log.info(String.format("Use default region %s", region));
            return region;
        }
        if(regions.isEmpty()) {
            throw new ConnectionCanceledException("No default region in authentication context");
        }
        final FilesRegion region = regions.values().iterator().next();
        log.warn(String.format("Fallback to first region found %s", region));
        return region;
    }

    @Override
    public void login(final PasswordStore keychain, final LoginController prompt) throws BackgroundException {
        try {
            final FilesAuthenticationResponse authentication = client.authenticate(
                    new SwiftAuthenticationService().getRequest(host, prompt));
            for(FilesRegion region : authentication.getRegions()) {
                regions.put(region.getRegionId(), region);
            }
        }
        catch(FilesException e) {
            throw new FilesExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    protected void logout() throws BackgroundException {
        super.logout();
        regions.clear();
        distributions.clear();
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

    @Override
    public boolean isCreateFileSupported(final Path workdir) {
        // Creating files is only possible inside a container.
        return !workdir.isRoot();
    }

    /**
     * @return Publicy accessible URL of given object
     */
    @Override
    public String toHttpURL(final Path path) {
        if(distributions.containsKey(path.getContainer())) {
            return distributions.get(path.getContainer()).getURL(path);
        }
        return null;
    }

    @Override
    public Set<DescriptiveUrl> getURLs(final Path path) {
        // Storage URL is not accessible
        return this.getHttpURLs(path);
    }

    @Override
    public <T> T getFeature(final Class<T> type, final LoginController prompt) {
        if(type == Headers.class) {
            return (T) new SwiftHeadersFeature(this);
        }
        if(type == Location.class) {
            return (T) new Location() {
                @Override
                public String getLocation(final Path container) throws BackgroundException {
                    return container.attributes().getRegion();
                }
            };
        }
        if(type == AnalyticsProvider.class) {
            return (T) new QloudstatAnalyticsProvider();
        }
        if(type == IdentityConfiguration.class) {
            return (T) new DefaultCredentialsIdentityConfiguration(host);
        }
        if(type == DistributionConfiguration.class) {
            for(FilesRegion region : client.getRegions()) {
                if(null != region.getCDNManagementUrl()) {
                    return (T) new SwiftDistributionConfiguration(this) {
                        @Override
                        public Distribution read(final Path container, final Distribution.Method method) throws BackgroundException {
                            final Distribution distribution = super.read(container, method);
                            distributions.put(container, distribution);
                            return distribution;
                        }
                    };
                }
            }
            return null;
        }
        return null;
    }
}