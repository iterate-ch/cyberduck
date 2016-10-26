package ch.cyberduck.core.cloudfront;

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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.features.Cname;
import ch.cyberduck.core.cdn.features.Index;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.s3.S3BucketListService;
import ch.cyberduck.core.s3.S3ExceptionMappingService;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3WebsiteConfig;
import org.jets3t.service.model.WebsiteConfig;
import org.jets3t.service.model.cloudfront.CustomOrigin;
import org.jets3t.service.utils.ServiceUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

public class WebsiteCloudFrontDistributionConfiguration extends CloudFrontDistributionConfiguration {

    private final Preferences preferences
            = PreferencesFactory.get();

    private final S3Session session;

    public WebsiteCloudFrontDistributionConfiguration(final S3Session session, final X509TrustManager trust, final X509KeyManager key) {
        super(session, trust, key);
        this.session = session;
    }

    /**
     * Distribution methods supported by this S3 provider.
     *
     * @return Download and Streaming for AWS.
     */
    @Override
    public List<Distribution.Method> getMethods(final Path container) {
        if(!ServiceUtils.isBucketNameValidDNSName(container.getName())) {
            // Disable website configuration if bucket name is not DNS compatible
            return super.getMethods(container);
        }
        final List<Distribution.Method> methods = new ArrayList<Distribution.Method>();
        if(session.getHost().getHostname().endsWith(preferences.getProperty("s3.hostname.default"))) {
            methods.addAll(super.getMethods(container));
            methods.addAll(Arrays.asList(Distribution.WEBSITE, Distribution.WEBSITE_CDN));
        }
        else {
            // Only allow website configuration for non AWS endpoints.
            methods.add(Distribution.WEBSITE);
        }
        return methods;
    }

    @Override
    public String getName(final Distribution.Method method) {
        if(method.equals(Distribution.WEBSITE)) {
            return method.toString();
        }
        return super.getName(method);
    }

    @Override
    protected URI getOrigin(final Path container, final Distribution.Method method) {
        if(method.equals(Distribution.WEBSITE_CDN)) {
            return URI.create(String.format("http://%s", this.getWebsiteHostname(container)));
        }
        return super.getOrigin(container, method);
    }

    @Override
    public Distribution read(final Path container, final Distribution.Method method, final LoginCallback prompt) throws BackgroundException {
        if(method.equals(Distribution.WEBSITE)) {
            try {
                final WebsiteConfig configuration = session.getClient().getWebsiteConfig(container.getName());
                final Distribution distribution = new Distribution(this.getOrigin(container, method),
                        method, configuration.isWebsiteConfigActive());
                distribution.setStatus(LocaleFactory.localizedString("Deployed", "S3"));
                // http://example-bucket.s3-website-us-east-1.amazonaws.com/
                distribution.setUrl(URI.create(String.format("%s://%s", method.getScheme(), this.getWebsiteHostname(container))));
                distribution.setIndexDocument(configuration.getIndexDocumentSuffix());
                distribution.setContainers(new S3BucketListService(session).list(
                        new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)), new DisabledListProgressListener()));
                return distribution;
            }
            catch(ServiceException e) {
                // Not found. Website configuration not enabled.
                final Distribution distribution = new Distribution(this.getOrigin(container, method), method, false);
                distribution.setStatus(e.getErrorMessage());
                distribution.setUrl(URI.create(String.format("%s://%s", method.getScheme(), this.getWebsiteHostname(container))));
                return distribution;
            }
        }
        else {
            return super.read(container, method, prompt);
        }
    }

    @Override
    public void write(final Path container, final Distribution distribution, final LoginCallback prompt) throws BackgroundException {
        if(distribution.getMethod().equals(Distribution.WEBSITE)) {
            try {
                if(distribution.isEnabled()) {
                    String suffix = "index.html";
                    if(StringUtils.isNotBlank(distribution.getIndexDocument())) {
                        suffix = PathNormalizer.name(distribution.getIndexDocument());
                    }
                    // Enable website endpoint
                    session.getClient().setWebsiteConfig(container.getName(), new S3WebsiteConfig(suffix));
                }
                else {
                    // Disable website endpoint
                    session.getClient().deleteWebsiteConfig(container.getName());
                }
            }
            catch(S3ServiceException e) {
                throw new S3ExceptionMappingService().map("Cannot write website configuration", e);
            }
        }
        else {
            super.write(container, distribution, prompt);
        }
    }

    @Override
    protected CustomOrigin.OriginProtocolPolicy getPolicy(final Distribution.Method method) {
        if(method.equals(Distribution.WEBSITE_CDN)) {
            return CustomOrigin.OriginProtocolPolicy.HTTP_ONLY;
        }
        return super.getPolicy(method);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type, final Distribution.Method method) {
        if(type == Index.class) {
            if(method.equals(Distribution.WEBSITE)) {
                return (T) this;
            }
        }
        if(type == Cname.class) {
            return (T) this;
        }
        return super.getFeature(type, method);
    }

    /**
     * The website endpoint given the location of the bucket. When you configure a bucket as
     * a website, the website is available via the region-specific website endpoint.
     * The website endpoint you use must be in the same region that your bucket resides.
     * These website endpoints are different than the REST API endpoints (see Request
     * Endpoints). Amazon S3 supports the following website endpoint.
     *
     * @param bucket Bucket name
     * @return Website distribution hostname
     */
    protected String getWebsiteHostname(final Path bucket) {
        // Geographical location
        final String location = bucket.attributes().getRegion();
        // US Standard
        final String endpoint;
        if(null == location) {
            endpoint = "s3-website-us-east-1.amazonaws.com";
        }
        else if("US".equals(location)) {
            endpoint = "s3-website-us-east-1.amazonaws.com";
        }
        else if("EU".equals(location)) {
            endpoint = "s3-website-eu-west-1.amazonaws.com";
        }
        else {
            endpoint = String.format("s3-website-%s.amazonaws.com", location);
        }
        return String.format("%s.%s", bucket.getName(), endpoint);
    }
}
