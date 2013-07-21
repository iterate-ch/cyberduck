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

import ch.cyberduck.core.*;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.analytics.QloudstatAnalyticsProvider;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cdn.features.Cname;
import ch.cyberduck.core.cdn.features.Index;
import ch.cyberduck.core.cdn.features.Logging;
import ch.cyberduck.core.cdn.features.Purge;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.identity.AWSIdentityConfiguration;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.s3.S3BucketListService;
import ch.cyberduck.core.s3.S3Session;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
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
import org.jets3t.service.security.ProviderCredentials;
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
public class CloudFrontDistributionConfiguration implements DistributionConfiguration, Purge {
    private static Logger log = Logger.getLogger(CloudFrontDistributionConfiguration.class);

    private S3Session session;

    private CloudFrontService client;

    public CloudFrontDistributionConfiguration(final S3Session session, final LoginController prompt) {
        this.session = session;
        this.client = new CloudFrontService(
                new AWSCredentials(session.getHost().getCredentials().getUsername(),
                        session.getHost().getCredentials().getPassword())
        ) {
            @Override
            public ProviderCredentials getAWSCredentials() {
                return new AWSCredentials(session.getHost().getCredentials().getUsername(),
                        session.getHost().getCredentials().getPassword());
            }

            @Override
            protected HttpClient initHttpConnection() {
                return session.connect();
            }

            @Override
            protected HttpResponse performRestRequest(final HttpRequestBase httpMethod, final int expectedResponseCode)
                    throws CloudFrontServiceException {
                final LoginOptions options = new LoginOptions();
                options.anonymous = false;
                options.publickey = false;
                try {
                    final KeychainLoginService login = new KeychainLoginService(prompt, PasswordStoreFactory.get());
                    try {
                        login.validate(session.getHost(), getName(), options);
                    }
                    catch(LoginCanceledException e) {
                        throw new CloudFrontServiceException(e);
                    }
                    return super.performRestRequest(httpMethod, expectedResponseCode);
                }
                catch(CloudFrontServiceException failure) {
                    if(new CloudFrontServiceExceptionMappingService().map(failure) instanceof LoginFailureException) {
                        try {
                            prompt.prompt(session.getHost().getProtocol(), session.getHost().getCredentials(),
                                    Locale.localizedString("Login failed", "Credentials"), failure.getMessage(), options);
                        }
                        catch(LoginCanceledException e) {
                            throw new CloudFrontServiceException(e);
                        }
                        return this.performRestRequest(httpMethod, expectedResponseCode);
                    }
                    else {
                        throw failure;
                    }
                }
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
        return new AbstractProtocol() {
            @Override
            public String getDefaultHostname() {
                return "cloudfront.amazonaws.com";
            }

            @Override
            public String getIdentifier() {
                return "cloudfront";
            }

            @Override
            public Type getType() {
                return Type.s3;
            }

            @Override
            public String getDescription() {
                return Locale.localizedString("Amazon CloudFront", "S3");
            }

            @Override
            public Scheme getScheme() {
                return Scheme.https;
            }

            @Override
            public Session createSession(final Host host) {
                throw new FactoryException(host.getHostname());
            }
        };
    }

    /**
     * @param method Distribution method
     * @return Origin server hostname. This is not the same as the container for
     *         custom origin configurations and website endpoints. <bucketname>.s3.amazonaws.com
     */
    protected String getOrigin(final Path container, final Distribution.Method method) {
        return String.format("%s.%s", container.getName(), session.getHost().getProtocol().getDefaultHostname());
    }

    @Override
    public List<Distribution.Method> getMethods(final Path container) {
        return Arrays.asList(Distribution.DOWNLOAD, Distribution.STREAMING);
    }

    @Override
    public Distribution read(final Path container, final Distribution.Method method) throws BackgroundException {
        try {
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
            // Return disabled configuration
            return new Distribution(this.getOrigin(container, method), method);
        }
        catch(CloudFrontServiceException e) {
            throw new CloudFrontServiceExceptionMappingService().map("Cannot read CDN configuration", e);
        }
    }

    @Override
    public void write(final Path container, final Distribution distribution) throws BackgroundException {
        try {
            // Configure CDN
            LoggingStatus loggingStatus = null;
            if(distribution.isLogging()) {
                if(this.getFeature(Logging.class, distribution.getMethod(), new DisabledLoginController()) != null) {
                    final String loggingTarget;
                    if(StringUtils.isNotBlank(distribution.getLoggingTarget())) {
                        loggingTarget = ServiceUtils.generateS3HostnameForBucket(distribution.getLoggingTarget(),
                                false, Protocol.S3_SSL.getDefaultHostname());
                    }
                    else {
                        loggingTarget = this.getOrigin(container, distribution.getMethod());
                    }
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Set logging target for %s to %s", distribution, loggingTarget));
                    }
                    loggingStatus = new LoggingStatus(loggingTarget, Preferences.instance().getProperty("cloudfront.logging.prefix"));
                }
            }
            final Distribution current = this.read(container, distribution.getMethod());
            if(null == current.getId()) {
                // No existing configuration
                if(log.isDebugEnabled()) {
                    log.debug(String.format("No existing distribution found for method %s", distribution.getMethod()));
                }
                this.createDistribution(client, container, distribution, loggingStatus);
            }
            else {
                if(current.equals(distribution)) {
                    if(log.isInfoEnabled()) {
                        log.info("Skip updating distribution not modified.");
                    }
                }
                else {
                    this.updateDistribution(current, client, container, distribution, loggingStatus);
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
    public <T> T getFeature(final Class<T> type, final Distribution.Method method, final LoginController prompt) {
        if(type == Purge.class || type == Index.class) {
            if(method.equals(Distribution.DOWNLOAD)
                    || method.equals(Distribution.WEBSITE_CDN)
                    || method.equals(Distribution.CUSTOM)) {
                return (T) this;
            }
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
        if(type == Cname.class) {
            return (T) this;
        }
        if(type == IdentityConfiguration.class) {
            return (T) new AWSIdentityConfiguration(session.getHost(), prompt);
        }
        return null;
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
            if(new PathContainerService().isContainer(file)) {
                keys.add(String.valueOf(Path.DELIMITER));
            }
            else {
                keys.add(new PathContainerService().getKey(file));
            }
            if(file.attributes().isDirectory()) {
                if(recursive) {
                    keys.addAll(this.getInvalidationKeys(session.list(file, new DisabledListProgressListener()), recursive));
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
     * @param logging Access log configuration
     * @return Distribution configuration
     * @throws CloudFrontServiceException  CloudFront failure details
     * @throws ConnectionCanceledException Authentication canceled
     */
    protected org.jets3t.service.model.cloudfront.Distribution createDistribution(final CloudFrontService client,
                                                                                  final Path container,
                                                                                  final Distribution distribution,
                                                                                  final LoggingStatus logging)
            throws ConnectionCanceledException, CloudFrontServiceException {

        final String reference = String.valueOf(System.currentTimeMillis());

        if(log.isDebugEnabled()) {
            log.debug(String.format("Create new %s distribution", distribution.getMethod().toString()));
        }
        final String originId = UUID.randomUUID().toString();
        final CacheBehavior cacheBehavior = new CacheBehavior(
                originId, false, null, CacheBehavior.ViewerProtocolPolicy.ALLOW_ALL, 0L
        );
        final String origin = this.getOrigin(container, distribution.getMethod());
        if(distribution.getMethod().equals(Distribution.STREAMING)) {
            final StreamingDistributionConfig config = new StreamingDistributionConfig(
                    new S3Origin[]{new S3Origin(originId, origin, null)},
                    reference, distribution.getCNAMEs(), null, distribution.isEnabled(), logging, null);
            return client.createDistribution(config
            );
        }
        if(distribution.getMethod().equals(Distribution.DOWNLOAD)) {
            DistributionConfig config = new DistributionConfig(
                    new Origin[]{new S3Origin(originId, origin, null)},
                    reference, distribution.getCNAMEs(), null, distribution.isEnabled(), logging,
                    distribution.getIndexDocument(), cacheBehavior, new CacheBehavior[]{});
            return client.createDistribution(config);
        }
        if(distribution.getMethod().equals(Distribution.CUSTOM)
                || distribution.getMethod().equals(Distribution.WEBSITE_CDN)) {
            DistributionConfig config = new DistributionConfig(
                    new Origin[]{new CustomOrigin(originId, origin, CustomOrigin.OriginProtocolPolicy.MATCH_VIEWER)},
                    reference, distribution.getCNAMEs(), null, distribution.isEnabled(), logging,
                    distribution.getIndexDocument(), cacheBehavior, new CacheBehavior[]{});
            return client.createDistribution(config);
        }
        throw new ConnectionCanceledException(String.format("Invalid distribution method %s", distribution.getMethod()));
    }

    /**
     * Amazon CloudFront Extension used to enable or disable a distribution configuration and its CNAMESs
     *
     * @param logging Access log configuration
     * @throws CloudFrontServiceException CloudFront failure details
     * @throws IOException                I/O error
     */
    protected void updateDistribution(final Distribution current,
                                      final CloudFrontService client,
                                      final Path container,
                                      final Distribution distribution,
                                      final LoggingStatus logging)
            throws CloudFrontServiceException, IOException, ConnectionCanceledException {
        final String origin = this.getOrigin(container, distribution.getMethod());
        if(log.isDebugEnabled()) {
            log.debug(String.format("Update %s distribution with origin %s", distribution.getMethod().toString(), origin));
        }
        final String originId = UUID.randomUUID().toString();
        final CacheBehavior cacheBehavior = new CacheBehavior(
                originId, false, null, CacheBehavior.ViewerProtocolPolicy.ALLOW_ALL, 0L
        );
        if(distribution.getMethod().equals(Distribution.STREAMING)) {
            StreamingDistributionConfig config = new StreamingDistributionConfig(
                    new Origin[]{new S3Origin(originId, origin, null)}, current.getReference(),
                    distribution.getCNAMEs(), null, distribution.isEnabled(), logging, null);
            config.setEtag(current.getEtag());
            client.updateDistributionConfig(current.getId(), config);
        }
        else if(distribution.getMethod().equals(Distribution.DOWNLOAD)) {
            DistributionConfig config = new DistributionConfig(
                    new Origin[]{new S3Origin(originId, origin, null)},
                    current.getReference(), distribution.getCNAMEs(), null, distribution.isEnabled(), logging,
                    distribution.getIndexDocument(), cacheBehavior, new CacheBehavior[]{});
            config.setEtag(current.getEtag());
            client.updateDistributionConfig(current.getId(), config);
        }
        else if(distribution.getMethod().equals(Distribution.CUSTOM)
                || distribution.getMethod().equals(Distribution.WEBSITE_CDN)) {
            DistributionConfig config = new DistributionConfig(
                    new Origin[]{new CustomOrigin(originId, origin, this.getPolicy(distribution.getMethod()))},
                    current.getReference(), distribution.getCNAMEs(), null, distribution.isEnabled(), logging,
                    distribution.getIndexDocument(), cacheBehavior, new CacheBehavior[]{});
            config.setEtag(current.getEtag());
            client.updateDistributionConfig(current.getId(), config);
        }
        else {
            throw new ConnectionCanceledException(String.format("Invalid distribution method %s", distribution.getMethod()));
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
        final Distribution distribution = new Distribution(d.getConfig().getOrigin().getDomainName(),
                method,
                d.getConfig().isEnabled()
        );
        distribution.setId(d.getId());
        distribution.setDeployed(d.isDeployed());
        distribution.setUrl(String.format("%s://%s%s", method.getScheme(), d.getDomainName(), method.getContext()));
        distribution.setSslUrl(method.equals(Distribution.DOWNLOAD) || method.equals(Distribution.CUSTOM)
                ? String.format("https://%s%s", d.getDomainName(), method.getContext()) : null);
        distribution.setReference(distributionConfig.getCallerReference());
        distribution.setEtag(distributionConfig.getEtag());
        distribution.setStatus(Locale.localizedString(d.getStatus(), "S3"));
        distribution.setCNAMEs(distributionConfig.getCNAMEs());
        distribution.setLogging(distributionConfig.getLoggingStatus().isEnabled());
        distribution.setLoggingContainer(loggingTarget);
        distribution.setIndexDocument(distributionConfig.getDefaultRootObject());
        if(this.getFeature(Purge.class, method, new DisabledLoginController()) != null) {
            distribution.setInvalidationStatus(this.readInvalidationStatus(client, distribution));
        }
        if(this.getFeature(Logging.class, method, new DisabledLoginController()) != null) {
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