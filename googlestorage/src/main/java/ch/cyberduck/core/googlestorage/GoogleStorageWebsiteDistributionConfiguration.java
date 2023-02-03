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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cdn.features.DistributionLogging;
import ch.cyberduck.core.cdn.features.Index;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;

public class GoogleStorageWebsiteDistributionConfiguration implements DistributionConfiguration, Index {

    private final GoogleStorageSession session;
    private final PathContainerService containerService;

    public GoogleStorageWebsiteDistributionConfiguration(final GoogleStorageSession session) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
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
    public String getName() {
        return LocaleFactory.localizedString("Website Configuration", "S3");
    }

    @Override
    public Distribution read(final Path file, final Distribution.Method method, final LoginCallback prompt) throws BackgroundException {
        final Path container = containerService.getContainer(file);
        final URI origin = URI.create(String.format("%s://%s.%s", method.getScheme(), container.getName(),
            session.getHost().getProtocol().getDefaultHostname()));
        try {
            final Storage.Buckets.Get request = session.getClient().buckets().get(container.getName());
            if(containerService.getContainer(file).attributes().getCustom().containsKey(GoogleStorageAttributesFinderFeature.KEY_REQUESTER_PAYS)) {
                request.setUserProject(session.getHost().getCredentials().getUsername());
            }
            final Bucket configuration = request.execute();
            final Bucket.Website website = configuration.getWebsite();
            final Distribution distribution = new Distribution(method, this.getName(), origin, website != null);
            if(website != null) {
                distribution.setUrl(URI.create(String.format("%s://%s.%s", method.getScheme(), container.getName(),
                        session.getHost().getProtocol().getDefaultHostname())));
                distribution.setStatus(LocaleFactory.localizedString("Deployed", "S3"));
                distribution.setIndexDocument(website.getMainPageSuffix());
            }
            final Bucket.Logging logging = configuration.getLogging();
            if(logging != null) {
                distribution.setLogging(logging.getLogObjectPrefix() != null);
                distribution.setLoggingContainer(logging.getLogBucket());
                distribution.setContainers(new GoogleStorageBucketListService(session).list(
                    new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)), new DisabledListProgressListener()).toList());
            }
            return distribution;
        }
        catch(IOException e) {
            throw new GoogleStorageExceptionMappingService().map("Cannot read CDN configuration", e);
        }
    }

    @Override
    public void write(final Path file, final Distribution distribution, final LoginCallback prompt) throws BackgroundException {
        final Path container = containerService.getContainer(file);
        try {
            String suffix = "index.html";
            if(StringUtils.isNotBlank(distribution.getIndexDocument())) {
                suffix = PathNormalizer.name(distribution.getIndexDocument());
            }
            // Enable website endpoint
            final Storage.Buckets.Patch request = session.getClient().buckets().patch(container.getName(), new Bucket()
                    .setLogging(new Bucket.Logging()
                            .setLogObjectPrefix(distribution.isEnabled() ? new HostPreferences(session.getHost()).getProperty("google.logging.prefix") : null)
                            .setLogBucket(StringUtils.isNotBlank(distribution.getLoggingContainer()) ? distribution.getLoggingContainer() : container.getName()))
                    .setWebsite(
                            distribution.isEnabled() ? new Bucket.Website().setMainPageSuffix(suffix) : null
                    ));
            if(containerService.getContainer(file).attributes().getCustom().containsKey(GoogleStorageAttributesFinderFeature.KEY_REQUESTER_PAYS)) {
                request.setUserProject(session.getHost().getCredentials().getUsername());
            }
            request.execute();
        }
        catch(IOException e) {
            throw new GoogleStorageExceptionMappingService().map("Cannot write website configuration", e);
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
        return null;
    }
}
