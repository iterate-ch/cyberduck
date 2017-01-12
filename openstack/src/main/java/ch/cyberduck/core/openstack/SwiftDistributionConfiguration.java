package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.analytics.QloudstatAnalyticsProvider;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cdn.DistributionUrlProvider;
import ch.cyberduck.core.cdn.features.DistributionLogging;
import ch.cyberduck.core.cdn.features.Index;
import ch.cyberduck.core.cdn.features.Purge;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.identity.DefaultCredentialsIdentityConfiguration;
import ch.cyberduck.core.identity.IdentityConfiguration;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.exception.NotFoundException;
import ch.iterate.openstack.swift.model.CDNContainer;
import ch.iterate.openstack.swift.model.ContainerMetadata;

public class SwiftDistributionConfiguration implements DistributionConfiguration, Index, DistributionLogging {
    private static final Logger log = Logger.getLogger(SwiftDistributionConfiguration.class);

    private final SwiftSession session;

    private final PathContainerService containerService
            = new PathContainerService();

    private final Map<Path, Distribution> cache
            = new HashMap<Path, Distribution>();

    private final SwiftRegionService regionService;

    public SwiftDistributionConfiguration(final SwiftSession session) {
        this(session, new SwiftRegionService(session));
    }

    public SwiftDistributionConfiguration(final SwiftSession session, final SwiftRegionService regionService) {
        this.session = session;
        this.regionService = regionService;
    }

    @Override
    public void write(final Path file, final Distribution configuration, final LoginCallback prompt) throws BackgroundException {
        final Path container = containerService.getContainer(file);
        try {
            if(StringUtils.isNotBlank(configuration.getIndexDocument())) {
                session.getClient().updateContainerMetadata(regionService.lookup(container),
                        container.getName(), Collections.singletonMap("X-Container-Meta-Web-Index", configuration.getIndexDocument()));
            }
            try {
                final CDNContainer info
                        = session.getClient().getCDNContainerInfo(regionService.lookup(container), container.getName());
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Found existing CDN configuration %s", info));
                }
            }
            catch(NotFoundException e) {
                // Not found.
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Enable CDN configuration for %s", container));
                }
                session.getClient().cdnEnableContainer(regionService.lookup(container), container.getName());
            }
            // Toggle content distribution for the container without changing the TTL expiration
            if(log.isDebugEnabled()) {
                log.debug(String.format("Update CDN configuration for %s", container));
            }
            session.getClient().cdnUpdateContainer(regionService.lookup(container),
                    container.getName(), -1, configuration.isEnabled(), configuration.isLogging());
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Cannot write CDN configuration", e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot write CDN configuration", e);
        }
    }

    @Override
    public Distribution read(final Path file, final Distribution.Method method, final LoginCallback prompt) throws BackgroundException {
        final Path container = containerService.getContainer(file);
        try {
            try {
                final CDNContainer info = session.getClient().getCDNContainerInfo(regionService.lookup(container),
                        container.getName());
                final Distribution distribution = new Distribution(regionService.lookup(container).getStorageUrl(container.getName()),
                        method, info.isEnabled());
                distribution.setId(info.getName());
                distribution.setStatus(info.isEnabled() ? LocaleFactory.localizedString("CDN Enabled", "Mosso") : LocaleFactory.localizedString("CDN Disabled", "Mosso"));
                if(StringUtils.isNotBlank(info.getCdnURL())) {
                    distribution.setUrl(URI.create(info.getCdnURL()));
                }
                if(StringUtils.isNotBlank(info.getSslURL())) {
                    distribution.setSslUrl(URI.create(info.getSslURL()));
                }
                if(StringUtils.isNotBlank(info.getStreamingURL())) {
                    distribution.setStreamingUrl(URI.create(info.getStreamingURL()));
                }
                if(StringUtils.isNotBlank(info.getiOSStreamingURL())) {
                    distribution.setiOSstreamingUrl(URI.create(info.getiOSStreamingURL()));
                }
                distribution.setLogging(info.getRetainLogs());
                distribution.setLoggingContainer(".CDN_ACCESS_LOGS");
                final ContainerMetadata metadata = session.getClient().getContainerMetaData(regionService.lookup(container),
                        container.getName());
                if(metadata.getMetaData().containsKey("X-Container-Meta-Web-Index")) {
                    distribution.setIndexDocument(metadata.getMetaData().get("X-Container-Meta-Web-Index"));
                }
                distribution.setContainers(Collections.singletonList(new Path(".CDN_ACCESS_LOGS", EnumSet.of(Path.Type.volume, Path.Type.directory))));
                cache.put(container, distribution);
                return distribution;
            }
            catch(NotFoundException e) {
                // Not found.
                if(log.isDebugEnabled()) {
                    log.debug(String.format("No CDN configuration for %s", container));
                }
                final Distribution distribution = new Distribution(regionService.lookup(container).getStorageUrl(container.getName()), method, false);
                distribution.setStatus(LocaleFactory.localizedString("CDN Disabled", "Mosso"));
                return distribution;
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Cannot read CDN configuration", e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot read CDN configuration", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type, final Distribution.Method method) {
        if(type == Purge.class) {
            return (T) new SwiftDistributionPurgeFeature(session, regionService);
        }
        if(type == Index.class) {
            return (T) this;
        }
        if(type == DistributionLogging.class) {
            return (T) this;
        }
        if(type == IdentityConfiguration.class) {
            return (T) new DefaultCredentialsIdentityConfiguration(session.getHost());
        }
        if(type == AnalyticsProvider.class) {
            return (T) new QloudstatAnalyticsProvider();
        }
        return null;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        final Path container = containerService.getContainer(file);
        if(cache.containsKey(container)) {
            return new DistributionUrlProvider(cache.get(container)).toUrl(file);
        }
        return DescriptiveUrlBag.empty();
    }

    @Override
    public String getHostname() {
        return session.getHost().getProtocol().getDefaultHostname();
    }

    @Override
    public List<Distribution.Method> getMethods(final Path container) {
        return Collections.singletonList(Distribution.DOWNLOAD);
    }

    @Override
    public String getName() {
        return LocaleFactory.localizedString("Akamai", "Mosso");
    }

    @Override
    public String getName(final Distribution.Method method) {
        return this.getName();
    }
}
