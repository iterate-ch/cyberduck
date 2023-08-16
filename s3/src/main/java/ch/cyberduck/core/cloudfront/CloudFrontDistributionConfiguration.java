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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.auth.AWSCredentialsConfigurator;
import ch.cyberduck.core.aws.AmazonServiceExceptionMappingService;
import ch.cyberduck.core.aws.CustomClientConfiguration;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cdn.features.Cname;
import ch.cyberduck.core.cdn.features.DistributionLogging;
import ch.cyberduck.core.cdn.features.Index;
import ch.cyberduck.core.cdn.features.Purge;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.s3.S3BucketListService;
import ch.cyberduck.core.s3.S3LocationFeature;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.utils.ServiceUtils;

import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudfront.AmazonCloudFront;
import com.amazonaws.services.cloudfront.AmazonCloudFrontClientBuilder;
import com.amazonaws.services.cloudfront.model.*;

/**
 * Amazon CloudFront CDN configuration.
 */
public class CloudFrontDistributionConfiguration implements DistributionConfiguration, Purge, Index, DistributionLogging, Cname {
    private static final Logger log = LogManager.getLogger(CloudFrontDistributionConfiguration.class);

    private final Preferences preferences = PreferencesFactory.get();

    protected final S3Session session;

    private final Host bookmark;
    private final ClientConfiguration configuration;
    private final Location location;

    public CloudFrontDistributionConfiguration(final S3Session session, final Location location, final X509TrustManager trust, final X509KeyManager key) {
        this.session = session;
        this.bookmark = session.getHost();
        this.location = location;
        this.configuration = new CustomClientConfiguration(bookmark,
            new ThreadLocalHostnameDelegatingTrustManager(trust, bookmark.getHostname()), key);
    }

    @Override
    public String getName() {
        return LocaleFactory.localizedString("Amazon CloudFront", "S3");
    }

    /**
     * @param method Distribution method
     * @return Origin server hostname. This is not the same as the container for custom origin configurations and
     * website endpoints. <bucketname>.s3.amazonaws.com
     */
    protected URI getOrigin(final Path container, final Distribution.Method method) throws BackgroundException {
        return URI.create(String.format("http://%s.%s", container.getName(), bookmark.getProtocol().getDefaultHostname()));
    }

    @Override
    public List<Distribution.Method> getMethods(final Path container) {
        return Arrays.asList(Distribution.DOWNLOAD, Distribution.STREAMING);
    }

    @Override
    public Distribution read(final Path file, final Distribution.Method method, final LoginCallback prompt) throws BackgroundException {
        final Path container = session.getFeature(PathContainerService.class).getContainer(file);
        try {
            if(log.isDebugEnabled()) {
                log.debug(String.format("List %s distributions", method));
            }
            final AmazonCloudFront client = this.client(container);
            if(method.equals(Distribution.STREAMING)) {
                for(StreamingDistributionSummary d : client.listStreamingDistributions(
                    new ListStreamingDistributionsRequest()).getStreamingDistributionList().getItems()) {
                    final S3Origin config = d.getS3Origin();
                    if(config != null) {
                        final URI origin = this.getOrigin(container, method);
                        if(config.getDomainName().equals(origin.getHost())) {
                            // We currently only support one distribution per bucket
                            return this.readStreamingDistribution(client, d, container, method);
                        }
                    }
                }
            }
            else if(method.equals(Distribution.DOWNLOAD)) {
                // List distributions restricting to bucket name origin
                for(DistributionSummary d : client.listDistributions(
                    new ListDistributionsRequest()).getDistributionList().getItems()) {
                    for(Origin o : d.getOrigins().getItems()) {
                        final S3OriginConfig config = o.getS3OriginConfig();
                        if(config != null) {
                            if(o.getDomainName().equals(this.getOrigin(container, method).getHost())) {
                                // We currently only support one distribution per bucket
                                return this.readDownloadDistribution(client, d, container, method);
                            }
                        }
                    }
                }
            }
            else if(method.equals(Distribution.CUSTOM) || method.equals(Distribution.WEBSITE_CDN)) {
                for(DistributionSummary d : client.listDistributions(new ListDistributionsRequest()).getDistributionList().getItems()) {
                    final URI origin = this.getOrigin(container, method);
                    for(Origin o : d.getOrigins().getItems()) {
                        // Listing all distributions and look for custom origin
                        final CustomOriginConfig config = o.getCustomOriginConfig();
                        if(config != null) {
                            if(o.getDomainName().equals(origin.getHost())) {
                                // We currently only support one distribution per bucket
                                return this.readDownloadDistribution(client, d, container, method);
                            }
                        }
                    }
                }
            }
            final URI origin = this.getOrigin(container, method);
            // Return disabled configuration
            return new Distribution(method, this.getName(), origin, false);
        }
        catch(AmazonClientException e) {
            throw new AmazonServiceExceptionMappingService().map("Cannot read CDN configuration", e);
        }
    }

    @Override
    public void write(final Path file, final Distribution distribution, final LoginCallback prompt) throws BackgroundException {
        final Path container = session.getFeature(PathContainerService.class).getContainer(file);
        try {
            if(null == distribution.getId()) {
                // No existing configuration
                if(log.isDebugEnabled()) {
                    log.debug(String.format("No existing distribution found for method %s", distribution.getMethod()));
                }
                if(distribution.getMethod().equals(Distribution.STREAMING)) {
                    distribution.setId(this.createStreamingDistribution(container, distribution).getId());
                }
                else if(distribution.getMethod().equals(Distribution.DOWNLOAD)) {
                    distribution.setId(this.createDownloadDistribution(container, distribution).getId());
                }
                else if(distribution.getMethod().equals(Distribution.CUSTOM)
                    || distribution.getMethod().equals(Distribution.WEBSITE_CDN)) {
                    distribution.setId(this.createCustomDistribution(container, distribution).getId());
                }
            }
            else {
                if(distribution.getMethod().equals(Distribution.DOWNLOAD)) {
                    distribution.setEtag(this.updateDownloadDistribution(container, distribution).getETag());
                }
                else if(distribution.getMethod().equals(Distribution.STREAMING)) {
                    distribution.setEtag(this.updateStreamingDistribution(container, distribution).getETag());
                }
                else if(distribution.getMethod().equals(Distribution.CUSTOM)
                    || distribution.getMethod().equals(Distribution.WEBSITE_CDN)) {
                    distribution.setEtag(this.updateCustomDistribution(container, distribution).getETag());
                }
            }
        }
        catch(AmazonClientException e) {
            throw new AmazonServiceExceptionMappingService().map("Cannot write CDN configuration", e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type, final Distribution.Method method) {
        if(type == Purge.class || type == Index.class) {
            if(method.equals(Distribution.DOWNLOAD)
                || method.equals(Distribution.WEBSITE_CDN)
                || method.equals(Distribution.CUSTOM)) {
                return (T) this;
            }
        }
        if(type == DistributionLogging.class) {
            if(method.equals(Distribution.DOWNLOAD)
                || method.equals(Distribution.STREAMING)
                || method.equals(Distribution.CUSTOM)) {
                return (T) this;
            }
        }
        if(type == Cname.class) {
            return (T) this;
        }
        return null;
    }

    /**
     * You can make any number of invalidation requests, but you can have only three invalidation requests in progress
     * at one time. Each request can contain up to 1,000 objects to invalidate. If you exceed these limits, you get an
     * error message.
     * <p>
     * It usually takes 10 to 15 minutes to complete your invalidation request, depending on the size of your request.
     */
    @Override
    public void invalidate(final Path container, final Distribution.Method method, final List<Path> files, final LoginCallback prompt) throws BackgroundException {
        try {
            final Distribution d = this.read(container, method, prompt);
            if(d.isEnabled()) {
                final List<String> keys = new ArrayList<>();
                for(Path file : files) {
                    if(session.getFeature(PathContainerService.class).isContainer(file)) {
                        // To invalidate all of the objects in a distribution
                        keys.add(String.format("%s*", Path.DELIMITER));
                    }
                    else {
                        if(file.isDirectory()) {
                            // The *, which replaces 0 or more characters, must be the last character in the invalidation path
                            keys.add(String.format("/%s*", session.getFeature(PathContainerService.class).getKey(file)));
                        }
                        else {
                            keys.add(String.format("/%s", session.getFeature(PathContainerService.class).getKey(file)));
                        }
                    }
                }
                if(keys.isEmpty()) {
                    log.warn("No keys selected for invalidation");
                }
                else {
                    final AmazonCloudFront client = this.client(container);
                    client.createInvalidation(new CreateInvalidationRequest(d.getId(),
                        new InvalidationBatch(new Paths().withItems(keys).withQuantity(keys.size()), new AlphanumericRandomStringService().random())
                    ));
                }
            }
        }
        catch(AmazonClientException e) {
            throw new AmazonServiceExceptionMappingService().map("Cannot write CDN configuration", e);
        }
    }

    /**
     * @param distribution Configuration
     * @return Status message from service
     */
    private String readInvalidationStatus(final AmazonCloudFront client,
                                          final Distribution distribution) throws BackgroundException {
        try {
            int pending = 0;
            int completed = 0;
            String marker = null;
            do {
                final ListInvalidationsResult response = client.listInvalidations(new ListInvalidationsRequest(distribution.getId())
                    .withMaxItems(String.valueOf(1000))
                    .withMarker(marker));
                for(InvalidationSummary s : response.getInvalidationList().getItems()) {
                    // When the invalidation batch is finished, the status is Completed.
                    if("Completed".equals(s.getStatus())) {
                        // No schema for status enumeration. Fail.
                        completed++;
                    }
                    else {
                        // InProgress
                        pending++;
                    }
                }
                marker = response.getInvalidationList().getNextMarker();
            }
            while(marker != null);
            if(pending > 0) {
                return MessageFormat.format(LocaleFactory.localizedString("{0} invalidations in progress", "S3"), pending);
            }
            if(completed > 0) {
                return MessageFormat.format(LocaleFactory.localizedString("{0} invalidations completed", "S3"), completed);
            }
            return LocaleFactory.localizedString("None");
        }
        catch(AmazonClientException e) {
            throw new AmazonServiceExceptionMappingService().map("Cannot read CDN configuration", e);
        }
    }

    /**
     * Amazon CloudFront Extension to create a new distribution configuration
     *
     * @return Distribution configuration
     */
    protected StreamingDistribution createStreamingDistribution(final Path container, final Distribution distribution)
        throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Create new %s distribution", distribution));
        }
        final AmazonCloudFront client = this.client(container);
        final URI origin = this.getOrigin(container, distribution.getMethod());
        final String originId = String.format("%s-%s", preferences.getProperty("application.name"), new AlphanumericRandomStringService().random());
        final StreamingDistributionConfig config = new StreamingDistributionConfig(new AlphanumericRandomStringService().random(),
            new S3Origin(origin.getHost(), StringUtils.EMPTY), distribution.isEnabled())
            .withComment(originId)
            .withTrustedSigners(new TrustedSigners().withEnabled(false).withQuantity(0))
            .withAliases(new Aliases().withItems(distribution.getCNAMEs()).withQuantity(distribution.getCNAMEs().length));
        // Make bucket name fully qualified
        final String loggingTarget = ServiceUtils.generateS3HostnameForBucket(distribution.getLoggingContainer(),
            false, new S3Protocol().getDefaultHostname());
        if(log.isDebugEnabled()) {
            log.debug(String.format("Set logging target for %s to %s", distribution, loggingTarget));
        }
        config.setLogging(new StreamingLoggingConfig()
            .withEnabled(distribution.isLogging())
            .withBucket(loggingTarget)
            .withPrefix(new HostPreferences(session.getHost()).getProperty("cloudfront.logging.prefix"))
        );
        return client.createStreamingDistribution(new CreateStreamingDistributionRequest(config)).getStreamingDistribution();
    }

    protected com.amazonaws.services.cloudfront.model.Distribution createDownloadDistribution(final Path container, final Distribution distribution)
        throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Create new %s distribution", distribution));
        }
        final AmazonCloudFront client = this.client(container);
        final URI origin = this.getOrigin(container, distribution.getMethod());
        final String originId = String.format("%s-%s", preferences.getProperty("application.name"), new AlphanumericRandomStringService().random());
        final DistributionConfig config = new DistributionConfig(new AlphanumericRandomStringService().random(), distribution.isEnabled())
            .withComment(originId)
            .withOrigins(new Origins()
                .withQuantity(1)
                .withItems(new Origin()
                    .withId(originId)
                    .withCustomHeaders(new CustomHeaders().withQuantity(0))
                    .withOriginPath(StringUtils.EMPTY)
                    .withDomainName(origin.getHost())
                    .withS3OriginConfig(new S3OriginConfig().withOriginAccessIdentity(StringUtils.EMPTY))
                )
            )
            .withPriceClass(PriceClass.PriceClass_All)
            .withDefaultCacheBehavior(new DefaultCacheBehavior()
                .withTargetOriginId(originId)
                .withForwardedValues(new ForwardedValues().withQueryString(true).withCookies(new CookiePreference().withForward(ItemSelection.All)))
                .withViewerProtocolPolicy(ViewerProtocolPolicy.AllowAll)
                .withMinTTL(0L)
                .withTrustedSigners(new TrustedSigners().withEnabled(false).withQuantity(0)))
            .withDefaultRootObject(distribution.getIndexDocument())
            .withAliases(new Aliases().withItems(distribution.getCNAMEs()).withQuantity(distribution.getCNAMEs().length));
        // Make bucket name fully qualified
        final String loggingTarget = ServiceUtils.generateS3HostnameForBucket(distribution.getLoggingContainer(),
            false, new S3Protocol().getDefaultHostname());
        if(log.isDebugEnabled()) {
            log.debug(String.format("Set logging target for %s to %s", distribution, loggingTarget));
        }
        config.setLogging(new LoggingConfig()
            .withEnabled(distribution.isLogging())
            .withIncludeCookies(true)
            .withBucket(loggingTarget)
            .withPrefix(new HostPreferences(session.getHost()).getProperty("cloudfront.logging.prefix")
            ));
        return client.createDistribution(new CreateDistributionRequest(config)).getDistribution();
    }

    protected com.amazonaws.services.cloudfront.model.Distribution createCustomDistribution(final Path container, final Distribution distribution)
        throws BackgroundException {
        final AmazonCloudFront client = this.client(container);
        int httpPort = 80;
        int httpsPort = 443;
        final URI origin = this.getOrigin(container, distribution.getMethod());
        if(origin.getPort() != -1) {
            if(origin.getScheme().equals(Scheme.http.name())) {
                httpPort = origin.getPort();
            }
            if(origin.getScheme().equals(Scheme.https.name())) {
                httpsPort = origin.getPort();
            }
        }
        final String originId = String.format("%s-%s", preferences.getProperty("application.name"), new AlphanumericRandomStringService().random());
        final DistributionConfig config = new DistributionConfig(new AlphanumericRandomStringService().random(), distribution.isEnabled())
            .withComment(originId)
            .withOrigins(new Origins()
                .withQuantity(1)
                .withItems(new Origin()
                    .withId(originId)
                    .withDomainName(origin.getHost())
                    .withCustomOriginConfig(new CustomOriginConfig()
                        .withHTTPPort(httpPort)
                        .withHTTPSPort(httpsPort)
                        .withOriginSslProtocols(new OriginSslProtocols().withQuantity(2).withItems("TLSv1.1", "TLSv1.2"))
                        .withOriginProtocolPolicy(this.getPolicy(distribution.getMethod()))
                    )
                )
            )
            .withPriceClass(PriceClass.PriceClass_All)
            .withDefaultCacheBehavior(new DefaultCacheBehavior()
                .withTargetOriginId(originId)
                .withForwardedValues(new ForwardedValues().withQueryString(true).withCookies(new CookiePreference().withForward(ItemSelection.All)))
                .withViewerProtocolPolicy(ViewerProtocolPolicy.AllowAll)
                .withMinTTL(0L)
                .withTrustedSigners(new TrustedSigners().withEnabled(false).withQuantity(0)))
            .withDefaultRootObject(distribution.getIndexDocument())
            .withAliases(new Aliases().withItems(distribution.getCNAMEs()).withQuantity(distribution.getCNAMEs().length));
        if(distribution.isLogging()) {
            // Make bucket name fully qualified
            final String loggingTarget = ServiceUtils.generateS3HostnameForBucket(distribution.getLoggingContainer(),
                false, new S3Protocol().getDefaultHostname());
            if(log.isDebugEnabled()) {
                log.debug(String.format("Set logging target for %s to %s", distribution, loggingTarget));
            }
            config.setLogging(new LoggingConfig()
                .withEnabled(distribution.isLogging())
                .withIncludeCookies(true)
                .withBucket(loggingTarget)
                .withPrefix(new HostPreferences(session.getHost()).getProperty("cloudfront.logging.prefix"))
            );
        }
        return client.createDistribution(new CreateDistributionRequest(config)).getDistribution();
    }

    /**
     * Amazon CloudFront Extension used to enable or disable a distribution configuration and its CNAMESs
     */
    protected UpdateDistributionResult updateDownloadDistribution(final Path container, final Distribution distribution)
        throws BackgroundException {
        final URI origin = this.getOrigin(container, distribution.getMethod());
        if(log.isDebugEnabled()) {
            log.debug(String.format("Update %s distribution with origin %s", distribution, origin));
        }
        final AmazonCloudFront client = this.client(container);
        final GetDistributionConfigResult response = client.getDistributionConfig(new GetDistributionConfigRequest(distribution.getId()));
        final DistributionConfig config = response.getDistributionConfig()
            .withEnabled(distribution.isEnabled())
            .withDefaultRootObject(distribution.getIndexDocument())
            .withAliases(new Aliases().withItems(distribution.getCNAMEs()).withQuantity(distribution.getCNAMEs().length));
        if(distribution.isLogging()) {
            // Make bucket name fully qualified
            final String loggingTarget = ServiceUtils.generateS3HostnameForBucket(distribution.getLoggingContainer(),
                false, new S3Protocol().getDefaultHostname());
            if(log.isDebugEnabled()) {
                log.debug(String.format("Set logging target for %s to %s", distribution, loggingTarget));
            }
            config.setLogging(new LoggingConfig()
                .withEnabled(distribution.isLogging())
                .withIncludeCookies(true)
                .withBucket(loggingTarget)
                .withPrefix(new HostPreferences(session.getHost()).getProperty("cloudfront.logging.prefix"))
            );
        }
        return client.updateDistribution(new UpdateDistributionRequest(config, distribution.getId(), response.getETag()));
    }

    protected UpdateStreamingDistributionResult updateStreamingDistribution(final Path container, final Distribution distribution)
        throws BackgroundException {
        final URI origin = this.getOrigin(container, distribution.getMethod());
        if(log.isDebugEnabled()) {
            log.debug(String.format("Update %s distribution with origin %s", distribution, origin));
        }
        final AmazonCloudFront client = this.client(container);
        final GetStreamingDistributionConfigResult response = client.getStreamingDistributionConfig(new GetStreamingDistributionConfigRequest(distribution.getId()));
        final StreamingDistributionConfig config = response.getStreamingDistributionConfig()
            .withEnabled(distribution.isEnabled())
            .withS3Origin(new S3Origin(origin.getHost(), StringUtils.EMPTY))
            .withAliases(new Aliases().withItems(distribution.getCNAMEs()).withQuantity(distribution.getCNAMEs().length));
        if(distribution.isLogging()) {
            // Make bucket name fully qualified
            final String loggingTarget = ServiceUtils.generateS3HostnameForBucket(distribution.getLoggingContainer(),
                false, new S3Protocol().getDefaultHostname());
            if(log.isDebugEnabled()) {
                log.debug(String.format("Set logging target for %s to %s", distribution, loggingTarget));
            }
            config.setLogging(new StreamingLoggingConfig()
                .withEnabled(distribution.isLogging())
                .withBucket(loggingTarget)
                .withPrefix(new HostPreferences(session.getHost()).getProperty("cloudfront.logging.prefix"))
            );
        }
        return client.updateStreamingDistribution(new UpdateStreamingDistributionRequest(config, distribution.getId(), response.getETag()));
    }

    protected UpdateDistributionResult updateCustomDistribution(final Path container, final Distribution distribution)
        throws BackgroundException {
        final URI origin = this.getOrigin(container, distribution.getMethod());
        if(log.isDebugEnabled()) {
            log.debug(String.format("Update %s distribution with origin %s", distribution, origin));
        }
        final AmazonCloudFront client = this.client(container);
        final GetDistributionConfigResult response = client.getDistributionConfig(new GetDistributionConfigRequest(distribution.getId()));
        final DistributionConfig config = response.getDistributionConfig()
            .withEnabled(distribution.isEnabled())
            .withDefaultRootObject(distribution.getIndexDocument() != null ? distribution.getIndexDocument() : StringUtils.EMPTY)
            .withAliases(new Aliases().withItems(distribution.getCNAMEs()).withQuantity(distribution.getCNAMEs().length));
        // Make bucket name fully qualified
        final String loggingTarget = ServiceUtils.generateS3HostnameForBucket(distribution.getLoggingContainer(),
            false, new S3Protocol().getDefaultHostname());
        if(log.isDebugEnabled()) {
            log.debug(String.format("Set logging target for %s to %s", distribution, loggingTarget));
        }
        config.setLogging(new LoggingConfig()
            .withEnabled(distribution.isLogging())
            .withIncludeCookies(true)
            .withBucket(loggingTarget)
            .withPrefix(new HostPreferences(session.getHost()).getProperty("cloudfront.logging.prefix"))
        );
        return client.updateDistribution(new UpdateDistributionRequest(config, distribution.getId(), response.getETag()));
    }

    protected void deleteDownloadDistribution(final Path container, final Distribution distribution)
        throws BackgroundException {
        final URI origin = this.getOrigin(container, distribution.getMethod());
        if(log.isDebugEnabled()) {
            log.debug(String.format("Update %s distribution with origin %s", distribution, origin));
        }
        final AmazonCloudFront client = this.client(container);
        client.deleteDistribution(new DeleteDistributionRequest(distribution.getId(), distribution.getEtag()));
    }

    protected void deleteStreamingDistribution(final Path container, final Distribution distribution)
        throws BackgroundException {
        final URI origin = this.getOrigin(container, distribution.getMethod());
        if(log.isDebugEnabled()) {
            log.debug(String.format("Update %s distribution with origin %s", distribution, origin));
        }
        final AmazonCloudFront client = this.client(container);
        client.deleteStreamingDistribution(new DeleteStreamingDistributionRequest(distribution.getId(), distribution.getEtag()));
    }

    /**
     * @param method Distribution method
     * @return Match viewer policy
     */
    protected OriginProtocolPolicy getPolicy(final Distribution.Method method) {
        return OriginProtocolPolicy.MatchViewer;
    }

    private Distribution readStreamingDistribution(final AmazonCloudFront client,
                                                   final StreamingDistributionSummary summary,
                                                   final Path container,
                                                   final Distribution.Method method) throws BackgroundException {
        // Retrieve distributions configuration to access current logging status settings.
        try {
            final GetStreamingDistributionConfigResult response = client.getStreamingDistributionConfig(new GetStreamingDistributionConfigRequest(summary.getId()));
            final StreamingDistributionConfig configuration = response.getStreamingDistributionConfig();
            final Distribution distribution = new Distribution(method, this.getName(), this.getOrigin(container, method), summary.isEnabled());
            distribution.setId(summary.getId());
            distribution.setDeployed("Deployed".equals(summary.getStatus()));
            distribution.setUrl(URI.create(String.format("%s://%s%s", method.getScheme(), summary.getDomainName(), method.getContext())));
            distribution.setSslUrl(method.equals(Distribution.DOWNLOAD) || method.equals(Distribution.CUSTOM) ? URI.create(String.format("https://%s%s", summary.getDomainName(), method.getContext())) : null);
            distribution.setReference(configuration.getCallerReference());
            distribution.setEtag(response.getETag());
            distribution.setStatus(LocaleFactory.localizedString(summary.getStatus(), "S3"));
            distribution.setCNAMEs(configuration.getAliases().getItems().toArray(new String[configuration.getAliases().getItems().size()]));
            distribution.setLogging(configuration.getLogging().isEnabled());
            distribution.setLoggingContainer(StringUtils.isNotBlank(configuration.getLogging().getBucket()) ?
                ServiceUtils.findBucketNameInHostname(configuration.getLogging().getBucket(), new S3Protocol().getDefaultHostname()) : null);
            if(this.getFeature(Purge.class, method) != null) {
                distribution.setInvalidationStatus(this.readInvalidationStatus(client, distribution));
            }
            if(this.getFeature(DistributionLogging.class, method) != null) {
                try {
                    distribution.setContainers(new S3BucketListService(session).list(
                        new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)),
                        new DisabledListProgressListener()).toList());
                }
                catch(AccessDeniedException | InteroperabilityException e) {
                    log.warn(String.format("Failure listing buckets. %s", e.getMessage()));
                }
            }
            return distribution;
        }
        catch(AmazonClientException e) {
            throw new AmazonServiceExceptionMappingService().map("Cannot read CDN configuration", e);
        }
    }

    private Distribution readDownloadDistribution(final AmazonCloudFront client,
                                                  final DistributionSummary summary,
                                                  final Path container,
                                                  final Distribution.Method method) throws BackgroundException {
        // Retrieve distributions configuration to access current logging status settings.
        try {
            final GetDistributionConfigResult response = client.getDistributionConfig(new GetDistributionConfigRequest(summary.getId()));
            final DistributionConfig configuration = response.getDistributionConfig();
            final Distribution distribution = new Distribution(method, this.getName(), this.getOrigin(container, method), summary.isEnabled());
            distribution.setId(summary.getId());
            distribution.setDeployed("Deployed".equals(summary.getStatus()));
            distribution.setUrl(URI.create(String.format("%s://%s%s", method.getScheme(), summary.getDomainName(), method.getContext())));
            distribution.setSslUrl(method.equals(Distribution.DOWNLOAD) || method.equals(Distribution.CUSTOM) ? URI.create(String.format("https://%s%s", summary.getDomainName(), method.getContext())) : null);
            distribution.setReference(configuration.getCallerReference());
            distribution.setEtag(response.getETag());
            distribution.setStatus(LocaleFactory.localizedString(summary.getStatus(), "S3"));
            distribution.setCNAMEs(configuration.getAliases().getItems().toArray(new String[configuration.getAliases().getItems().size()]));
            distribution.setLogging(configuration.getLogging().isEnabled());
            distribution.setLoggingContainer(StringUtils.isNotBlank(configuration.getLogging().getBucket()) ?
                ServiceUtils.findBucketNameInHostname(configuration.getLogging().getBucket(), new S3Protocol().getDefaultHostname()) : null);
            if(StringUtils.isNotBlank(configuration.getDefaultRootObject())) {
                distribution.setIndexDocument(configuration.getDefaultRootObject());
            }
            if(this.getFeature(Purge.class, method) != null) {
                distribution.setInvalidationStatus(this.readInvalidationStatus(client, distribution));
            }
            if(this.getFeature(DistributionLogging.class, method) != null) {
                distribution.setContainers(new S3BucketListService(session).list(
                    new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)),
                    new DisabledListProgressListener()).toList());
            }
            return distribution;
        }
        catch(AmazonClientException e) {
            throw new AmazonServiceExceptionMappingService().map("Cannot read CDN configuration", e);
        }
    }

    private AmazonCloudFront client(final Path container) throws BackgroundException {
        final AmazonCloudFrontClientBuilder builder = AmazonCloudFrontClientBuilder.standard()
            .withCredentials(AWSCredentialsConfigurator.toAWSCredentialsProvider(session.getClient().getProviderCredentials()))
            .withClientConfiguration(configuration);
        final Location.Name region = this.getRegion(container);
        if(S3Session.isAwsHostname(session.getHost().getHostname(), false)) {
            if(Location.unknown.equals(region)) {
                builder.withRegion(Regions.DEFAULT_REGION);
            }
            else {
                builder.withRegion(region.getIdentifier());
            }
        }
        else {
            builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                new HostUrlProvider(false).get(session.getHost()), region.getIdentifier()));
        }
        return builder.build();
    }

    protected Location.Name getRegion(final Path container) throws BackgroundException {
        return location.getLocation(container);
    }
}
