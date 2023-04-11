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
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.features.Cname;
import ch.cyberduck.core.cdn.features.Index;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.s3.S3BucketListService;
import ch.cyberduck.core.s3.S3ExceptionMappingService;
import ch.cyberduck.core.s3.S3LocationFeature;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3WebsiteConfig;
import org.jets3t.service.model.WebsiteConfig;
import org.jets3t.service.utils.ServiceUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import com.amazonaws.services.cloudfront.model.OriginProtocolPolicy;

public class WebsiteCloudFrontDistributionConfiguration extends CloudFrontDistributionConfiguration {

    private final S3Session session;
    private final PathContainerService containerService;

    public WebsiteCloudFrontDistributionConfiguration(final S3Session session, final X509TrustManager trust, final X509KeyManager key) {
        super(session, trust, key);
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    /**
     * Distribution methods supported by this S3 provider.
     *
     * @return Download and Streaming for AWS.
     */
    @Override
    public List<Distribution.Method> getMethods(final Path file) {
        if(!ServiceUtils.isBucketNameValidDNSName(containerService.getContainer(file).getName())) {
            // Disable website configuration if bucket name is not DNS compatible
            return super.getMethods(file);
        }
        final List<Distribution.Method> methods = new ArrayList<>();
        if(S3Session.isAwsHostname(session.getHost().getHostname())) {
            methods.addAll(super.getMethods(file));
            methods.addAll(Arrays.asList(Distribution.WEBSITE, Distribution.WEBSITE_CDN));
        }
        else {
            // Only allow website configuration for non AWS endpoints.
            methods.add(Distribution.WEBSITE);
        }
        return methods;
    }

    @Override
    protected URI getOrigin(final Path container, final Distribution.Method method) throws BackgroundException {
        if(Distribution.WEBSITE_CDN.equals(method)) {
            return URI.create(String.format("http://%s", this.getWebsiteHostname(container)));
        }
        return super.getOrigin(container, method);
    }

    @Override
    public Distribution read(final Path container, final Distribution.Method method, final LoginCallback prompt) throws BackgroundException {
        if(Distribution.WEBSITE.equals(method)) {
            try {
                final WebsiteConfig configuration = session.getClient().getWebsiteConfig(container.getName());
                final Distribution distribution = new Distribution(method, this.getOrigin(container, method),
                    configuration.isWebsiteConfigActive());
                distribution.setStatus(LocaleFactory.localizedString("Deployed", "S3"));
                // http://example-bucket.s3-website-us-east-1.amazonaws.com/
                distribution.setUrl(URI.create(String.format("%s://%s", method.getScheme(), this.getWebsiteHostname(container))));
                distribution.setIndexDocument(configuration.getIndexDocumentSuffix());
                distribution.setContainers(new S3BucketListService(session, new S3LocationFeature.S3Region(session.getHost().getRegion())).list(
                    new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)), new DisabledListProgressListener()).toList());
                return distribution;
            }
            catch(ServiceException e) {
                // Not found. Website configuration not enabled.
                final Distribution distribution = new Distribution(method, this.getOrigin(container, method), false);
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
        if(Distribution.WEBSITE.equals(distribution.getMethod())) {
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
    protected OriginProtocolPolicy getPolicy(final Distribution.Method method) {
        if(method.equals(Distribution.WEBSITE)) {
            // HTTP Only is the default setting when the origin is an Amazon S3 static website hosting endpoint,
            // because Amazon S3 doesn’t support HTTPS connections for static website hosting endpoints.
            return OriginProtocolPolicy.HttpOnly;
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
     * The website endpoint given the location of the bucket. When you configure a bucket as a website, the website is
     * available via the region-specific website endpoint. The website endpoint you use must be in the same region that
     * your bucket resides. These website endpoints are different than the REST API endpoints (see Request Endpoints).
     * Amazon S3 supports the following website endpoint.
     *
     * @param bucket Bucket name
     * @return Website distribution hostname
     */
    protected String getWebsiteHostname(final Path bucket) throws BackgroundException {
        // Geographical location
        final Location.Name location = new S3LocationFeature(session, session.getClient().getRegionEndpointCache()).getLocation(bucket);
        // US Standard
        final String endpoint;
        if(Location.unknown == location) {
            endpoint = "s3-website-us-east-1.amazonaws.com";
        }
        else if("US".equals(location.getIdentifier())) {
            endpoint = "s3-website-us-east-1.amazonaws.com";
        }
        else if("EU".equals(location.getIdentifier())) {
            endpoint = "s3-website-eu-west-1.amazonaws.com";
        }
        else {
            endpoint = String.format("s3-website-%s.amazonaws.com", location.getIdentifier());
        }
        return String.format("%s.%s", bucket.getName(), endpoint);
    }
}
