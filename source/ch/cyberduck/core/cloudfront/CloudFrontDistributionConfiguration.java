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

import ch.cyberduck.core.ConnectionCanceledException;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.CloudFrontServiceExceptionMappingService;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.s3.S3BucketListService;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.model.cloudfront.CacheBehavior;
import org.jets3t.service.model.cloudfront.CustomOrigin;
import org.jets3t.service.model.cloudfront.DistributionConfig;
import org.jets3t.service.model.cloudfront.InvalidationSummary;
import org.jets3t.service.model.cloudfront.LoggingStatus;
import org.jets3t.service.model.cloudfront.Origin;
import org.jets3t.service.model.cloudfront.S3Origin;
import org.jets3t.service.model.cloudfront.StreamingDistributionConfig;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.utils.ServiceUtils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Amazon CloudFront CDN configuration.
 *
 * @version $Id$
 */
public class CloudFrontDistributionConfiguration implements DistributionConfiguration {
    private static Logger log = Logger.getLogger(CloudFrontDistributionConfiguration.class);

    private S3Session session;

    final CloudFrontService client;

    public CloudFrontDistributionConfiguration(final S3Session session) {
        this.session = session;
        this.client = new CloudFrontService(
                new AWSCredentials(session.getHost().getCredentials().getUsername(),
                        session.getHost().getCredentials().getPassword())) {

            @Override
            protected HttpClient initHttpConnection() {
                return session.http();
            }
        };
    }

    @Override
    public String getName() {
        return Locale.localizedString("Amazon CloudFront", "S3");
    }

    @Override
    public String getName(final Distribution.Method method) {
        return this.getName();
    }

    @Override
    public Protocol getProtocol() {
        return Protocol.S3_SSL;
    }

    /**
     * @param method Distribution method
     * @return Origin server hostname. This is not the same as the container for
     *         custom origin configurations and website endpoints. <bucketname>.s3.amazonaws.com
     */
    public String getOrigin(final Path container, final Distribution.Method method) {
        return String.format("%s.%s", container.getName(), container.getSession().getHost().getProtocol().getDefaultHostname());
    }

    @Override
    public List<Distribution.Method> getMethods(final Path container) {
        return Arrays.asList(Distribution.DOWNLOAD, Distribution.STREAMING);
    }

    @Override
    public Distribution read(final Path container, final Distribution.Method method) throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Reading CDN configuration of {0}", "Status"),
                    container.getName()));

            if(log.isDebugEnabled()) {
                log.debug(String.format("List %s distributions", method));
            }
            if(method.equals(Distribution.STREAMING)) {
                for(org.jets3t.service.model.cloudfront.Distribution d : client.listStreamingDistributions(this.getOrigin(container, method))) {
                    for(Origin o : d.getConfig().getOrigins()) {
                        if(o instanceof S3Origin) {
                            // We currently only support one distribution per bucket
                            return this.convert(client, d, method);
                        }
                    }
                }
            }
            else if(method.equals(Distribution.DOWNLOAD)) {
                // List distributions restricting to bucket name origin
                for(org.jets3t.service.model.cloudfront.Distribution d : client.listDistributions(this.getOrigin(container, method))) {
                    for(Origin o : d.getConfig().getOrigins()) {
                        if(o instanceof S3Origin) {
                            // We currently only support one distribution per bucket
                            return this.convert(client, d, method);
                        }
                    }
                }
            }
            else if(method.equals(Distribution.CUSTOM) || method.equals(Distribution.WEBSITE_CDN)) {
                for(org.jets3t.service.model.cloudfront.Distribution d : client.listDistributions()) {
                    for(Origin o : d.getConfig().getOrigins()) {
                        // Listing all distributions and look for custom origin
                        if(o instanceof CustomOrigin) {
                            if(o.getDomainName().equals(this.getOrigin(container, method))) {
                                // We currently only support one distribution per bucket
                                return this.convert(client, d, method);
                            }
                        }
                    }
                }
            }
            return new Distribution(this.getOrigin(container, method), method);
        }
        catch(CloudFrontServiceException e) {
            throw new CloudFrontServiceExceptionMappingService().map("Cannot read CDN configuration", e);
        }
    }

    @Override
    public void write(final Path container, final boolean enabled, final Distribution.Method method,
                      final String[] cnames, final boolean logging, final String loggingBucket, final String defaultRootObject) throws BackgroundException {
        try {
            // Configure CDN
            LoggingStatus loggingStatus = null;
            if(logging) {
                if(this.isLoggingSupported(method)) {
                    final String loggingDestination = StringUtils.isNotBlank(loggingBucket) ?
                            ServiceUtils.generateS3HostnameForBucket(loggingBucket, false, Protocol.S3_SSL.getDefaultHostname()) :
                            this.getOrigin(container, method);
                    loggingStatus = new LoggingStatus(loggingDestination,
                            Preferences.instance().getProperty("cloudfront.logging.prefix"));
                }
            }
            final StringBuilder name = new StringBuilder(Locale.localizedString("Amazon CloudFront", "S3")).append(" ").append(method.toString());
            if(enabled) {
                session.message(MessageFormat.format(Locale.localizedString("Enable {0} Distribution", "Status"), name));
            }
            else {
                session.message(MessageFormat.format(Locale.localizedString("Disable {0} Distribution", "Status"), name));
            }
            final Distribution d = this.read(container, method);
            if(null == d) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("No existing distribution found for method %s", method));
                }
                this.createDistribution(client, enabled, method, this.getOrigin(container, method), cnames, loggingStatus, defaultRootObject);
            }
            else {
                boolean modified = false;
                if(d.isEnabled() != enabled) {
                    modified = true;
                }
                if(!Arrays.equals(d.getCNAMEs(), cnames)) {
                    modified = true;
                }
                if(d.isLogging() != logging) {
                    modified = true;
                }
                // Compare default root object for possible change
                if(!StringUtils.equals(d.getDefaultRootObject(), defaultRootObject)) {
                    modified = true;
                }
                // Compare logging target for possible change
                if(!StringUtils.equals(d.getLoggingTarget(), loggingBucket)) {
                    modified = true;
                }
                if(modified) {
                    this.updateDistribution(client, enabled, method, this.getOrigin(container, method), d.getId(), d.getEtag(), d.getReference(),
                            cnames, loggingStatus, defaultRootObject);
                }
                else {
                    log.info("Skip updating distribution not modified.");
                }
            }
        }
        catch(CloudFrontServiceException e) {
            throw new CloudFrontServiceExceptionMappingService().map("Cannot write CDN configuration", e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot write CDN configuration", e);
        }
    }

    @Override
    public boolean isDefaultRootSupported(final Distribution.Method method) {
        return method.equals(Distribution.DOWNLOAD)
                || method.equals(Distribution.WEBSITE_CDN)
                || method.equals(Distribution.CUSTOM);
    }

    @Override
    public boolean isInvalidationSupported(final Distribution.Method method) {
        return method.equals(Distribution.DOWNLOAD)
                || method.equals(Distribution.WEBSITE_CDN)
                || method.equals(Distribution.CUSTOM);
    }

    @Override
    public boolean isLoggingSupported(final Distribution.Method method) {
        return method.equals(Distribution.DOWNLOAD)
                || method.equals(Distribution.STREAMING)
                || method.equals(Distribution.CUSTOM);
    }

    @Override
    public boolean isAnalyticsSupported(final Distribution.Method method) {
        return this.isLoggingSupported(method);
    }

    @Override
    public boolean isCnameSupported(final Distribution.Method method) {
        return true;
    }

    /**
     * You can make any number of invalidation requests, but you can have only three invalidation requests
     * in progress at one time. Each request can contain up to 1,000 objects to invalidate. If you
     * exceed these limits, you get an error message.
     * <p/>
     * It usually takes 10 to 15 minutes to complete your invalidation request, depending on
     * the size of your request.
     */
    @Override
    public void invalidate(final Path container, final Distribution.Method method, final List<Path> files, final boolean recursive) throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Writing CDN configuration of {0}", "Status"),
                    container.getName()));
            final long reference = System.currentTimeMillis();
            final Distribution d = this.read(container, method);
            if(null == d) {
                log.error(String.format("No cached distribution for origin %s", this.getOrigin(container, method)));
            }
            else {
                List<String> keys = this.getInvalidationKeys(files, recursive);
                if(keys.isEmpty()) {
                    log.warn("No keys selected for invalidation");
                    return;
                }
                client.invalidateObjects(d.getId(),
                        keys.toArray(new String[keys.size()]), // objects
                        new Date(reference).toString() // Comment
                );
            }
        }
        catch(CloudFrontServiceException e) {
            throw new CloudFrontServiceExceptionMappingService().map("Cannot write CDN configuration", e);
        }
    }

    /**
     * @param files     Files to purge
     * @param recursive Recursivly for folders
     * @return Key to files
     */
    protected List<String> getInvalidationKeys(final List<Path> files, final boolean recursive) throws BackgroundException {
        List<String> keys = new ArrayList<String>();
        for(Path file : files) {
            if(file.isContainer()) {
                keys.add(String.valueOf(Path.DELIMITER));
            }
            else {
                keys.add(file.getKey());
            }
            if(file.attributes().isDirectory()) {
                if(recursive) {
                    keys.addAll(this.getInvalidationKeys(file.list(), recursive));
                }
            }
        }
        return keys;
    }

    /**
     * @param distribution Configuration
     * @return Status message from service
     */
    private String readInvalidationStatus(final CloudFrontService client,
                                          final Distribution distribution) throws BackgroundException {
        boolean complete = false;
        int inprogress = 0;
        try {
            final List<InvalidationSummary> summaries = client.listInvalidations(distribution.getId());
            for(InvalidationSummary s : summaries) {
                if("Completed".equals(s.getStatus())) {
                    // No schema for status enumeration. Fail.
                    complete = true;
                }
                else {
                    // InProgress
                    inprogress++;
                }
            }
            if(inprogress > 0) {
                return MessageFormat.format(Locale.localizedString("{0} invalidations in progress", "S3"), inprogress);
            }
            if(complete) {
                return MessageFormat.format(Locale.localizedString("{0} invalidations completed", "S3"), summaries.size());
            }
            return Locale.localizedString("None");
        }
        catch(CloudFrontServiceException e) {
            throw new CloudFrontServiceExceptionMappingService().map("Cannot read CDN configuration", e);
        }
    }

    /**
     * Amazon CloudFront Extension to create a new distribution configuration
     * *
     *
     * @param enabled           Distribution status
     * @param method            Distribution method
     * @param origin            Name of the container
     * @param cnames            DNS CNAME aliases for distribution
     * @param logging           Access log configuration
     * @param defaultRootObject Index file for distribution. Only supported for download and custom origins.
     * @return Distribution configuration
     * @throws CloudFrontServiceException  CloudFront failure details
     * @throws ConnectionCanceledException Authentication canceled
     */
    private org.jets3t.service.model.cloudfront.Distribution createDistribution(final CloudFrontService client,
                                                                                final boolean enabled,
                                                                                final Distribution.Method method,
                                                                                final String origin,
                                                                                final String[] cnames,
                                                                                final LoggingStatus logging,
                                                                                final String defaultRootObject)
            throws ConnectionCanceledException, CloudFrontServiceException {

        final String reference = String.valueOf(System.currentTimeMillis());

        if(log.isDebugEnabled()) {
            log.debug(String.format("Create new %s distribution", method.toString()));
        }
        final String originId = UUID.randomUUID().toString();
        final CacheBehavior cacheBehavior = new CacheBehavior(
                originId, false, null, CacheBehavior.ViewerProtocolPolicy.ALLOW_ALL, 0L
        );

        if(method.equals(Distribution.STREAMING)) {
            final StreamingDistributionConfig config = new StreamingDistributionConfig(
                    new S3Origin[]{new S3Origin(originId, origin, null)},
                    reference, cnames, null, enabled, logging, null);
            return client.createDistribution(config
            );
        }
        if(method.equals(Distribution.DOWNLOAD)) {
            DistributionConfig config = new DistributionConfig(
                    new Origin[]{new S3Origin(originId, origin, null)},
                    reference, cnames, null, enabled, logging,
                    defaultRootObject, cacheBehavior, new CacheBehavior[]{});
            return client.createDistribution(config);
        }
        if(method.equals(Distribution.CUSTOM)
                || method.equals(Distribution.WEBSITE_CDN)) {
            DistributionConfig config = new DistributionConfig(
                    new Origin[]{new CustomOrigin(originId, origin, CustomOrigin.OriginProtocolPolicy.MATCH_VIEWER)},
                    reference, cnames, null, enabled, logging,
                    defaultRootObject, cacheBehavior, new CacheBehavior[]{});
            return client.createDistribution(config);
        }
        throw new RuntimeException("Invalid distribution method:" + method);
    }

    /**
     * Amazon CloudFront Extension used to enable or disable a distribution configuration and its CNAMESs
     *
     * @param enabled           Distribution status
     * @param method            Distribution method
     * @param origin            Name of the container
     * @param distributionId    Distribution reference
     * @param cnames            DNS CNAME aliases for distribution
     * @param logging           Access log configuration
     * @param defaultRootObject Index file for distribution. Only supported for download and custom origins.
     * @throws CloudFrontServiceException CloudFront failure details
     * @throws IOException                I/O error
     */
    private void updateDistribution(final CloudFrontService client,
                                    boolean enabled, Distribution.Method method, final String origin,
                                    final String distributionId, final String etag, final String reference,
                                    final String[] cnames, final LoggingStatus logging, final String defaultRootObject)
            throws CloudFrontServiceException, IOException {

        if(log.isDebugEnabled()) {
            log.debug(String.format("Update %s distribution with origin %s", method.toString(), origin));
        }

        final String originId = UUID.randomUUID().toString();
        final CacheBehavior cacheBehavior = new CacheBehavior(
                originId, false, null, CacheBehavior.ViewerProtocolPolicy.ALLOW_ALL, 0L
        );

        if(method.equals(Distribution.STREAMING)) {
            StreamingDistributionConfig config = new StreamingDistributionConfig(
                    new Origin[]{new S3Origin(originId, origin, null)}, reference, cnames, null, enabled, logging, null);
            config.setEtag(etag);
            client.updateDistributionConfig(distributionId, config);
        }
        else if(method.equals(Distribution.DOWNLOAD)) {
            DistributionConfig config = new DistributionConfig(
                    new Origin[]{new S3Origin(originId, origin, null)},
                    reference, cnames, null, enabled, logging,
                    defaultRootObject, cacheBehavior, new CacheBehavior[]{});
            config.setEtag(etag);
            client.updateDistributionConfig(distributionId, config);
        }
        else if(method.equals(Distribution.CUSTOM)
                || method.equals(Distribution.WEBSITE_CDN)) {
            DistributionConfig config = new DistributionConfig(
                    new Origin[]{new CustomOrigin(originId, origin, this.getPolicy(method))},
                    reference, cnames, null, enabled, logging,
                    defaultRootObject, cacheBehavior, new CacheBehavior[]{});
            config.setEtag(etag);
            client.updateDistributionConfig(distributionId, config);
        }
        else {
            throw new RuntimeException("Invalid distribution method:" + method);
        }
    }

    /**
     * @param method Distribution method
     * @return Match viewer policy
     */
    protected CustomOrigin.OriginProtocolPolicy getPolicy(final Distribution.Method method) {
        return CustomOrigin.OriginProtocolPolicy.MATCH_VIEWER;
    }

    private Distribution convert(final CloudFrontService client,
                                 final org.jets3t.service.model.cloudfront.Distribution d,
                                 Distribution.Method method) throws BackgroundException {
        // Retrieve distributions configuration to access current logging status settings.
        final DistributionConfig distributionConfig = this.getDistributionConfig(client, d);
        final String loggingTarget;
        if(null == distributionConfig.getLoggingStatus()) {
            // Default logging target to origin itself
            loggingTarget = ServiceUtils.findBucketNameInHostname(d.getConfig().getOrigin().getDomainName(),
                    Protocol.S3_SSL.getDefaultHostname());
        }
        else {
            loggingTarget = ServiceUtils.findBucketNameInHostname(distributionConfig.getLoggingStatus().getBucket(),
                    Protocol.S3_SSL.getDefaultHostname());
        }
        final Distribution distribution = new Distribution(
                d.getId(),
                distributionConfig.getEtag(),
                distributionConfig.getCallerReference(),
                d.getConfig().getOrigin().getDomainName(),
                method,
                d.getConfig().isEnabled(),
                d.isDeployed(),
                // CloudFront URL
                String.format("%s://%s%s", method.getScheme(), d.getDomainName(), method.getContext()),
                method.equals(Distribution.DOWNLOAD) || method.equals(Distribution.CUSTOM)
                        ? String.format("https://%s%s", d.getDomainName(), method.getContext()) : null, // No SSL
                null,
                Locale.localizedString(d.getStatus(), "S3"),
                distributionConfig.getCNAMEs(),
                distributionConfig.getLoggingStatus().isEnabled(),
                loggingTarget,
                distributionConfig.getDefaultRootObject());
        if(this.isInvalidationSupported(method)) {
            distribution.setInvalidationStatus(this.readInvalidationStatus(client, distribution));
        }
        if(this.isLoggingSupported(method)) {
            distribution.setContainers(new S3BucketListService().list(session));
        }
        return distribution;
    }

    /**
     * @param distribution Distribution configuration
     * @return Configuration
     */
    private DistributionConfig getDistributionConfig(final CloudFrontService client,
                                                     final org.jets3t.service.model.cloudfront.Distribution distribution)
            throws BackgroundException {

        try {
            if(distribution.isStreamingDistribution()) {
                return client.getStreamingDistributionConfig(distribution.getId());
            }
            return client.getDistributionConfig(distribution.getId());
        }
        catch(CloudFrontServiceException e) {
            throw new CloudFrontServiceExceptionMappingService().map("Cannot read CDN configuration", e);
        }
    }
}