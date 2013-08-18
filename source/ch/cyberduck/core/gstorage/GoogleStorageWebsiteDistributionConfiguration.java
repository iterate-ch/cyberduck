package ch.cyberduck.core.gstorage;

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

import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.analytics.QloudstatAnalyticsProvider;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cdn.DistributionUrlProvider;
import ch.cyberduck.core.cdn.features.Index;
import ch.cyberduck.core.cdn.features.Logging;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.s3.ServiceExceptionMappingService;

import org.apache.commons.lang.StringUtils;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.GSWebsiteConfig;
import org.jets3t.service.model.WebsiteConfig;

import java.util.Arrays;
import java.util.List;

/**
 * @version $Id$
 */
public class GoogleStorageWebsiteDistributionConfiguration implements DistributionConfiguration {

    private GoogleStorageSession session;

    private PathContainerService containerService
            = new PathContainerService();

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
        return Arrays.asList(Distribution.WEBSITE);
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
        final Distribution distribution = new Distribution(
                containerService.getContainer(file).getName(), Distribution.DOWNLOAD);
        distribution.setUrl(String.format("%s://%s.%s", Distribution.DOWNLOAD.getScheme(), containerService.getContainer(file).getName(),
                session.getHost().getProtocol().getDefaultHostname()));
        return new DistributionUrlProvider(distribution).toUrl(file);
    }

    @Override
    public Distribution read(final Path container, final Distribution.Method method, final LoginController prompt) throws BackgroundException {
        try {
            final WebsiteConfig configuration = session.getClient().getWebsiteConfigImpl(container.getName());
            final Distribution distribution = new Distribution(
                    container.getName(), method, configuration.isWebsiteConfigActive());
            distribution.setUrl(String.format("%s://%s.%s", method.getScheme(), container.getName(), session.getHost().getProtocol().getDefaultHostname()));
            distribution.setStatus(LocaleFactory.localizedString("Deployed", "S3"));
            distribution.setIndexDocument(configuration.getIndexDocumentSuffix());
            return distribution;
        }
        catch(ServiceException e) {
            // Not found. Website configuration not enbabled.
            final Distribution distribution = new Distribution(
                    container.getName(),
                    method,
                    //Disabled
                    false);
            distribution.setStatus(e.getErrorMessage());
            distribution.setUrl(String.format("%s://%s.%s", method.getScheme(), container.getName(), session.getHost().getProtocol().getDefaultHostname()));
            return distribution;
        }
    }

    @Override
    public void write(final Path container, final Distribution distribution, final LoginController prompt) throws BackgroundException {
        try {
            if(distribution.isEnabled()) {
                String suffix = "index.html";
                if(StringUtils.isNotBlank(distribution.getIndexDocument())) {
                    suffix = Path.getName(distribution.getIndexDocument());
                }
                // Enable website endpoint
                session.getClient().setWebsiteConfigImpl(container.getName(), new GSWebsiteConfig(suffix));
            }
            else {
                // Disable website endpoint
                session.getClient().setWebsiteConfigImpl(container.getName(), new GSWebsiteConfig());
            }
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot write website configuration", e);
        }
    }

    @Override
    public <T> T getFeature(final Class<T> type, final Distribution.Method method) {
        if(type == Index.class) {
            return (T) this;
        }
        if(type == Logging.class) {
            if(method.equals(Distribution.DOWNLOAD)
                    || method.equals(Distribution.STREAMING)
                    || method.equals(Distribution.CUSTOM)) {
                return (T) this;
            }
        }
        if(type == AnalyticsProvider.class) {
            if(method.equals(Distribution.DOWNLOAD)
                    || method.equals(Distribution.STREAMING)
                    || method.equals(Distribution.CUSTOM)) {
                return (T) new QloudstatAnalyticsProvider();
            }
        }
        return null;
    }

    @Override
    public Protocol getProtocol() {
        return ProtocolFactory.GOOGLESTORAGE_SSL;
    }
}
