package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.analytics.QloudstatAnalyticsProvider;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cdn.DistributionUrlProvider;
import ch.cyberduck.core.cdn.features.DistributionLogging;
import ch.cyberduck.core.cdn.features.Index;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.identity.DefaultCredentialsIdentityConfiguration;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.logging.LoggingConfiguration;
import ch.cyberduck.core.s3.S3BucketListService;
import ch.cyberduck.core.s3.S3ExceptionMappingService;
import ch.cyberduck.core.s3.S3PathContainerService;

import org.apache.commons.lang3.StringUtils;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.GSWebsiteConfig;
import org.jets3t.service.model.WebsiteConfig;

import java.net.URI;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class GoogleStorageWebsiteDistributionConfiguration implements DistributionConfiguration, Index {

    private final GoogleStorageSession session;

    private final PathContainerService containerService
            = new S3PathContainerService();

    public GoogleStorageWebsiteDistributionConfiguration(final GoogleStorageSession session) {
        this.session = session;
    }

    /**
     * Distribution methods supported by this S3 provider.
     *
     * @param container Bucket
     * @return Download and Streaming for AWS.
     */
    @Override
    public List<Distribution.Method> getMethods(final Path container) {
        return Collections.singletonList(Distribution.WEBSITE);
    }

    @Override
    public String getName(Distribution.Method method) {
        return method.toString();
    }

    @Override
    public String getName() {
        return LocaleFactory.localizedString("Website Configuration", "S3");
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        final Distribution distribution = new Distribution(URI.create(String.format("%s://%s.%s",
                Distribution.DOWNLOAD.getScheme(), containerService.getContainer(file).getName(), this.getHostname())),
                Distribution.DOWNLOAD, false);
        distribution.setUrl(URI.create(String.format("%s://%s.%s", Distribution.DOWNLOAD.getScheme(), containerService.getContainer(file).getName(),
                this.getHostname())));
        return new DistributionUrlProvider(distribution).toUrl(file);
    }

    @Override
    public Distribution read(final Path file, final Distribution.Method method, final LoginCallback prompt) throws BackgroundException {
        final Path container = containerService.getContainer(file);
        final URI origin = URI.create(String.format("%s://%s.%s", method.getScheme(), container.getName(), this.getHostname()));
        try {
            final WebsiteConfig configuration = session.getClient().getWebsiteConfigImpl(container.getName());
            final Distribution distribution = new Distribution(
                    origin, method, configuration.isWebsiteConfigActive());
            distribution.setUrl(URI.create(String.format("%s://%s.%s", method.getScheme(), container.getName(), this.getHostname())));
            distribution.setStatus(LocaleFactory.localizedString("Deployed", "S3"));
            distribution.setIndexDocument(configuration.getIndexDocumentSuffix());
            final DistributionLogging logging = this.getFeature(DistributionLogging.class, method);
            if(logging != null) {
                final LoggingConfiguration c = new GoogleStorageLoggingFeature(session).getConfiguration(container);
                distribution.setLogging(c.isEnabled());
                distribution.setLoggingContainer(c.getLoggingTarget());
                distribution.setContainers(new S3BucketListService(session).list(
                        new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)), new DisabledListProgressListener()).toList());
            }
            return distribution;
        }
        catch(ServiceException e) {
            // Not found. Website configuration is disabled.
            final Distribution distribution = new Distribution(origin, method, false);
            distribution.setStatus(e.getErrorMessage());
            distribution.setUrl(URI.create(String.format("%s://%s.%s", method.getScheme(), container.getName(), this.getHostname())));
            return distribution;
        }
    }

    @Override
    public void write(final Path file, final Distribution distribution, final LoginCallback prompt) throws BackgroundException {
        final Path container = containerService.getContainer(file);
        try {
            if(distribution.isEnabled()) {
                String suffix = "index.html";
                if(StringUtils.isNotBlank(distribution.getIndexDocument())) {
                    suffix = PathNormalizer.name(distribution.getIndexDocument());
                }
                // Enable website endpoint
                session.getClient().setWebsiteConfigImpl(container.getName(), new GSWebsiteConfig(suffix));
                final DistributionLogging logging = this.getFeature(DistributionLogging.class, distribution.getMethod());
                if(logging != null) {
                    new GoogleStorageLoggingFeature(session).setConfiguration(container, new LoggingConfiguration(
                            distribution.isEnabled(), distribution.getLoggingContainer()));
                }
            }
            else {
                // Disable website endpoint
                session.getClient().setWebsiteConfigImpl(container.getName(), new GSWebsiteConfig());
            }
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Cannot write website configuration", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type, final Distribution.Method method) {
        if(type == Index.class) {
            return (T) this;
        }
        if(type == DistributionLogging.class) {
            return (T) new GoogleStorageLoggingFeature(session);
        }
        if(type == AnalyticsProvider.class) {
            return (T) new QloudstatAnalyticsProvider();
        }
        if(type == IdentityConfiguration.class) {
            return (T) new DefaultCredentialsIdentityConfiguration(session.getHost());
        }
        return null;
    }

    @Override
    public String getHostname() {
        return new GoogleStorageProtocol().getDefaultHostname();
    }
}
