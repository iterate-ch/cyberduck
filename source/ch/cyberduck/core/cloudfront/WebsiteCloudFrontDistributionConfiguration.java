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

import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.features.Cname;
import ch.cyberduck.core.cdn.features.Index;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.s3.ServiceExceptionMappingService;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.s3.S3BucketListService;
import ch.cyberduck.core.s3.S3Session;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3WebsiteConfig;
import org.jets3t.service.model.WebsiteConfig;
import org.jets3t.service.model.cloudfront.CustomOrigin;
import org.jets3t.service.utils.ServiceUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @version $Id$
 */
public class WebsiteCloudFrontDistributionConfiguration extends CloudFrontDistributionConfiguration {

    private S3Session session;

    public WebsiteCloudFrontDistributionConfiguration(final S3Session session,
                                                      final LoginController prompt) {
        super(session, prompt);
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
        final List<Distribution.Method> methods = new ArrayList<Distribution.Method>(super.getMethods(container));
        methods.addAll(Arrays.asList(Distribution.WEBSITE, Distribution.WEBSITE_CDN));
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
    protected String getOrigin(final Path container, final Distribution.Method method) {
        if(method.equals(Distribution.WEBSITE_CDN)) {
            return this.getWebsiteHostname(container);
        }
        return super.getOrigin(container, method);
    }

    @Override
    public Distribution read(final Path container, final Distribution.Method method) throws BackgroundException {
        if(method.equals(Distribution.WEBSITE)) {
            try {
                final WebsiteConfig configuration = session.getClient().getWebsiteConfig(container.getName());
                final Distribution distribution = new Distribution(this.getOrigin(container, method),
                        method,
                        configuration.isWebsiteConfigActive());
                distribution.setStatus(Locale.localizedString("Deployed", "S3"));
                // http://example-bucket.s3-website-us-east-1.amazonaws.com/
                distribution.setUrl(String.format("%s://%s", method.getScheme(), this.getWebsiteHostname(container)));
                distribution.setIndexDocument(configuration.getIndexDocumentSuffix());
                distribution.setContainers(new S3BucketListService().list(session));
                return distribution;
            }
            catch(ServiceException e) {
                // Not found. Website configuration not enabled.
                final Distribution distribution = new Distribution(this.getOrigin(container, method), method, false);
                distribution.setStatus(e.getErrorMessage());
                distribution.setUrl(String.format("%s://%s", method.getScheme(), this.getWebsiteHostname(container)));
                return distribution;
            }
        }
        else {
            return super.read(container, method);
        }
    }

    @Override
    public void write(final Path container, final Distribution distribution) throws BackgroundException {
        if(distribution.getMethod().equals(Distribution.WEBSITE)) {
            try {
                if(distribution.isEnabled()) {
                    String suffix = "index.html";
                    if(StringUtils.isNotBlank(distribution.getIndexDocument())) {
                        suffix = FilenameUtils.getName(distribution.getIndexDocument());
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
                throw new ServiceExceptionMappingService().map("Cannot write website configuration", e);
            }
        }
        else {
            super.write(container, distribution);
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
    public Protocol getProtocol() {
        return session.getHost().getProtocol();
    }

    @Override
    public <T> T getFeature(final Class<T> type, final Distribution.Method method, final LoginController prompt) {
        if(type == Index.class) {
            if(method.equals(Distribution.WEBSITE)) {
                return (T) this;
            }
        }
        if(type == Cname.class) {
            return (T) this;
        }
        return super.getFeature(type, method, prompt);
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
        if(null == location || "US".equals(location)) {
            endpoint = "s3-website-us-east-1.amazonaws.com";
        }
        else if(S3Bucket.LOCATION_EUROPE.equals(location)) {
            endpoint = "s3-website-eu-west-1.amazonaws.com";
        }
        else {
            endpoint = String.format("s3-website-%s.amazonaws.com", location);
        }
        return String.format("%s.%s", bucket.getName(), endpoint);
    }
}
