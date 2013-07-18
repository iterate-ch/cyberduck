package ch.cyberduck.core.openstack;

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

import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.analytics.QloudstatAnalyticsProvider;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cdn.features.Index;
import ch.cyberduck.core.cdn.features.Logging;
import ch.cyberduck.core.cdn.features.Purge;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.identity.DefaultCredentialsIdentityConfiguration;
import ch.cyberduck.core.identity.IdentityConfiguration;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.iterate.openstack.swift.exception.GenericException;
import ch.iterate.openstack.swift.exception.NotFoundException;
import ch.iterate.openstack.swift.model.CDNContainer;
import ch.iterate.openstack.swift.model.ContainerMetadata;

/**
 * @version $Id$
 */
public class SwiftDistributionConfiguration implements DistributionConfiguration, Purge {
    private static final Logger log = Logger.getLogger(SwiftDistributionConfiguration.class);

    private SwiftSession session;

    public SwiftDistributionConfiguration(final SwiftSession session) {
        this.session = session;
    }

    @Override
    public void write(final Path container, final Distribution configuration) throws BackgroundException {
        try {
            if(StringUtils.isNotBlank(configuration.getIndexDocument())) {
                session.getClient().updateContainerMetadata(session.getRegion(container),
                        container.getName(), Collections.singletonMap("X-Container-Meta-Web-Index", configuration.getIndexDocument()));
            }
            try {
                final CDNContainer info
                        = session.getClient().getCDNContainerInfo(session.getRegion(container), container.getName());
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Found existing CDN configuration %s", info));
                }
            }
            catch(NotFoundException e) {
                // Not found.
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Enable CDN configuration for %s", container));
                }
                session.getClient().cdnEnableContainer(session.getRegion(container), container.getName());
            }
            // Toggle content distribution for the container without changing the TTL expiration
            if(log.isDebugEnabled()) {
                log.debug(String.format("Update CDN configuration for %s", container));
            }
            session.getClient().cdnUpdateContainer(session.getRegion(container),
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
    public Distribution read(final Path container, final Distribution.Method method) throws BackgroundException {
        try {
            try {
                final CDNContainer info = session.getClient().getCDNContainerInfo(session.getRegion(container),
                        container.getName());
                final Distribution distribution = new Distribution(
                        session.getRegion(container).getStorageUrl().getHost(),
                        method, info.isEnabled()
                );
                distribution.setId(info.getName());
                distribution.setStatus(info.isEnabled() ? Locale.localizedString("CDN Enabled", "Mosso") : Locale.localizedString("CDN Disabled", "Mosso"));
                distribution.setUrl(info.getCdnURL());
                distribution.setSslUrl(info.getSslURL());
                distribution.setStreamingUrl(info.getStreamingURL());
                distribution.setLogging(info.getRetainLogs());
                distribution.setLoggingContainer(".CDN_ACCESS_LOGS");
                final ContainerMetadata metadata = session.getClient().getContainerMetaData(session.getRegion(container),
                        container.getName());
                if(metadata.getMetaData().containsKey("X-Container-Meta-Web-Index")) {
                    distribution.setIndexDocument(metadata.getMetaData().get("X-Container-Meta-Web-Index"));
                }
                distribution.setContainers(Collections.<Path>singletonList(new Path(".CDN_ACCESS_LOGS", Path.VOLUME_TYPE | Path.DIRECTORY_TYPE)));
                return distribution;
            }
            catch(NotFoundException e) {
                // Not found.
                if(log.isDebugEnabled()) {
                    log.debug(String.format("No CDN configuration for %s", container));
                }
                final Distribution distribution = new Distribution(
                        session.getRegion(container).getStorageUrl().getHost(), method, false);
                distribution.setStatus(Locale.localizedString("CDN Disabled", "Mosso"));
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
    public void invalidate(final Path container, final Distribution.Method method, final List<Path> files, final boolean recursive) throws BackgroundException {
        try {
            final PathContainerService containerService = new PathContainerService();
            for(Path file : files) {
                if(containerService.isContainer(file)) {
                    session.getClient().purgeCDNContainer(session.getRegion(containerService.getContainer(file)),
                            container.getName(), null);
                }
                else {
                    session.getClient().purgeCDNObject(session.getRegion(containerService.getContainer(file)),
                            container.getName(), containerService.getKey(file), null);
                }
            }
        }
        catch(GenericException e) {
            throw new SwiftExceptionMappingService().map("Cannot write CDN configuration", e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot write CDN configuration", e);
        }
    }

    @Override
    public <T> T getFeature(final Class<T> type, final Distribution.Method method, final LoginController prompt) {
        if(type == Purge.class) {
            return (T) this;
        }
        if(type == Index.class) {
            return (T) this;
        }
        if(type == Logging.class) {
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
    public Protocol getProtocol() {
        return session.getHost().getProtocol();
    }

    @Override
    public List<Distribution.Method> getMethods(final Path container) {
        return Arrays.asList(Distribution.DOWNLOAD);
    }

    @Override
    public String getName() {
        return Locale.localizedString("Akamai", "Mosso");
    }

    @Override
    public String getName(final Distribution.Method method) {
        return this.getName();
    }
}
