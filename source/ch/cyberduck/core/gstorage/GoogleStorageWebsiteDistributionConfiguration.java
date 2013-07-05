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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ServiceExceptionMappingService;
import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.io.FilenameUtils;
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

    private GSSession session;

    public GoogleStorageWebsiteDistributionConfiguration(final GSSession session) {
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
    public Distribution read(final Path container, final Distribution.Method method) throws BackgroundException {
        try {
            final WebsiteConfig configuration = session.getClient().getWebsiteConfigImpl(container.getName());
            return new Distribution(
                    null,
                    container.getName(),
                    method,
                    configuration.isWebsiteConfigActive(),
                    configuration.isWebsiteConfigActive(),
                    // http://example-bucket.s3-website-us-east-1.amazonaws.com/
                    String.format("%s://%s.%s", method.getScheme(), container.getName(), session.getHost().getProtocol().getDefaultHostname()),
                    Locale.localizedString("Deployed", "S3"),
                    new String[]{},
                    false,
                    configuration.getIndexDocumentSuffix());
        }
        catch(ServiceException e) {
            // Not found. Website configuration not enbabled.
            return new Distribution(
                    null,
                    container.getName(),
                    method,
                    false, //Disabled
                    String.format("%s://%s.%s", method.getScheme(), container.getName(), session.getHost().getProtocol().getDefaultHostname()),
                    e.getErrorMessage());
        }
    }

    @Override
    public void invalidate(final Path container, final Distribution.Method method, final List<Path> files, final boolean recursive) throws BackgroundException {
        //
    }

    @Override
    public boolean isInvalidationSupported(final Distribution.Method method) {
        return false;
    }

    @Override
    public String getName() {
        return Locale.localizedString("Website Configuration", "S3");
    }

    @Override
    public void write(final Path container, final boolean enabled, final Distribution.Method method,
                      final String[] cnames, final boolean logging, final String loggingBucket, final String defaultRootObject) throws BackgroundException {
        try {
            if(enabled) {
                String suffix = "index.html";
                if(StringUtils.isNotBlank(defaultRootObject)) {
                    suffix = FilenameUtils.getName(defaultRootObject);
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
    public boolean isDefaultRootSupported(final Distribution.Method method) {
        return true;
    }

    @Override
    public boolean isLoggingSupported(final Distribution.Method method) {
        return false;
    }

    @Override
    public boolean isCnameSupported(final Distribution.Method method) {
        return false;
    }

    @Override
    public boolean isAnalyticsSupported(final Distribution.Method method) {
        return false;
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.GOOGLESTORAGE_SSL;
    }
}
