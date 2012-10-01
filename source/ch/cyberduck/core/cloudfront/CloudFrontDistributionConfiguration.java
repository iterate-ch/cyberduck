package ch.cyberduck.core.cloudfront;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.i18n.Locale;

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
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Amazon CloudFront CDN configuration.
 *
 * @version $Id$
 */
public class CloudFrontDistributionConfiguration extends HttpSession implements DistributionConfiguration {
    private static Logger log = Logger.getLogger(CloudFrontDistributionConfiguration.class);

    /**
     * Cached instance for session
     */
    private CloudFrontService client;
    private LoginController login;

    /**
     * Cache distribution status result.
     */
    protected Map<Distribution.Method, Map<String, Distribution>> distributionStatus
            = new HashMap<Distribution.Method, Map<String, Distribution>>() {
        private static final long serialVersionUID = -2745277784871254371L;

        @Override
        public Map<String, Distribution> get(final Object key) {
            if(!this.containsKey(key)) {
                final HashMap<String, Distribution> create = new HashMap<String, Distribution>();
                this.put((Distribution.Method) key, create);
                return create;
            }
            return super.get(key);
        }
    };

    public CloudFrontDistributionConfiguration(LoginController parent, Credentials credentials,
                                               ErrorListener error, ProgressListener progress,
                                               TranscriptListener transcript) {
        super(new Host(Protocol.CLOUDFRONT, URI.create(CloudFrontService.ENDPOINT).getHost(), credentials));
        this.login = parent;
        this.addErrorListener(error);
        this.addProgressListener(progress);
        this.addTranscriptListener(transcript);
    }

    /**
     * Amazon CloudFront Extension
     *
     * @return A cached cloud front service interface
     */
    @Override
    protected CloudFrontService getClient() throws ConnectionCanceledException {
        if(null == client) {
            throw new ConnectionCanceledException();
        }
        return client;
    }

    @Override
    protected void connect() throws IOException {
        if(this.isConnected()) {
            return;
        }
        this.fireConnectionWillOpenEvent();

        // Prompt the login credentials first
        this.login();

        this.fireConnectionDidOpenEvent();
    }


    @Override
    protected void login() throws IOException {
        this.login(login);
    }

    @Override
    protected void login(LoginController controller, Credentials credentials) throws IOException {
        try {
            client = new CloudFrontService(
                    new AWSCredentials(credentials.getUsername(), credentials.getPassword())) {

                @Override
                protected HttpClient initHttpConnection() {
                    return CloudFrontDistributionConfiguration.this.http();
                }
            };
            // Provoke authentication error
            for(String container : this.getContainers()) {
                for(Distribution.Method method : getMethods(container)) {
                    // Cache first container
                    this.cache(this.getOrigin(method, container), method);
                    break;
                }
                break;
            }
        }
        catch(CloudFrontServiceException e) {
            log.warn(String.format("Invalid account: %s", e.getMessage()));
            this.message(Locale.localizedString("Login failed", "Credentials"));
            controller.fail(host.getProtocol(), credentials);
            this.login();
        }
    }

    @Override
    protected void prompt(LoginController controller) throws LoginCanceledException {
        // Configure with the same host as S3 to get the same credentials from the keychain.
        controller.check(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(),
                host.getCredentials()), this.toString(), null, true, false, false);
    }

    @Override
    protected void login(LoginController controller) throws IOException {
        super.login(controller);
        controller.success(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(),
                host.getCredentials()));
    }

    @Override
    public void close() {
        try {
            if(this.isConnected()) {
                this.fireConnectionWillCloseEvent();
            }
        }
        finally {
            this.clear();
            // No logout required
            client = null;
            this.fireConnectionDidCloseEvent();
        }
    }

    @Override
    public String toString() {
        return Locale.localizedString("Amazon CloudFront", "S3");
    }

    @Override
    public String toString(Distribution.Method method) {
        return this.toString();
    }

    @Override
    public boolean isCached(Distribution.Method method) {
        return !distributionStatus.get(method).isEmpty();
    }

    @Override
    public Protocol getProtocol() {
        return this.getHost().getProtocol();
    }

    @Override
    public String getOrigin(Distribution.Method method, String container) {
        return container + CloudFrontService.DEFAULT_BUCKET_SUFFIX;
    }

    @Override
    public List<Distribution.Method> getMethods(final String container) {
        return Arrays.asList(Distribution.DOWNLOAD, Distribution.STREAMING);
    }

    @Override
    public Distribution read(String origin, Distribution.Method method) {
        if(method.equals(Distribution.DOWNLOAD)
                || method.equals(Distribution.STREAMING)
                || method.equals(Distribution.CUSTOM)
                || method.equals(Distribution.WEBSITE_CDN)) {
            if(!distributionStatus.get(method).containsKey(origin)) {
                try {
                    this.check();
                    this.message(MessageFormat.format(Locale.localizedString("Reading CDN configuration of {0}", "Status"),
                            origin));

                    this.cache(origin, method);
                }
                catch(CloudFrontServiceException e) {
                    this.error("Cannot read CDN configuration", e);
                }
                catch(LoginCanceledException canceled) {
                    // User canceled Cloudfront login. Possibly not enabled in Amazon configuration.
                    distributionStatus.get(method).put(origin, new Distribution(null,
                            origin, method, false, null, canceled.getMessage()));
                }
                catch(IOException e) {
                    this.error("Cannot read CDN configuration", e);
                }
            }
        }
        if(distributionStatus.get(method).containsKey(origin)) {
            return distributionStatus.get(method).get(origin);
        }
        return new Distribution(origin, method);
    }

    @Override
    public void write(boolean enabled, String origin, Distribution.Method method,
                      String[] cnames, boolean logging, String loggingBucket, String defaultRootObject) {
        try {
            this.check();

            // Configure CDN
            LoggingStatus loggingStatus = null;
            if(logging) {
                if(this.isLoggingSupported(method)) {
                    final String loggingDestination = StringUtils.isNotBlank(loggingBucket) ?
                            ServiceUtils.generateS3HostnameForBucket(loggingBucket, false, Protocol.S3_SSL.getDefaultHostname()) : origin;
                    loggingStatus = new LoggingStatus(loggingDestination,
                            Preferences.instance().getProperty("cloudfront.logging.prefix"));
                }
            }
            StringBuilder name = new StringBuilder(Locale.localizedString("Amazon CloudFront", "S3")).append(" ").append(method.toString());
            if(enabled) {
                this.message(MessageFormat.format(Locale.localizedString("Enable {0} Distribution", "Status"), name));
            }
            else {
                this.message(MessageFormat.format(Locale.localizedString("Disable {0} Distribution", "Status"), name));
            }
            Distribution d = distributionStatus.get(method).get(origin);
            if(null == d) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("No existing distribution found for method %s", method));
                }
                this.createDistribution(enabled, method, origin, cnames, loggingStatus, defaultRootObject);
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
                    this.updateDistribution(enabled, method, origin, d.getId(), d.getEtag(), d.getReference(),
                            cnames, loggingStatus, defaultRootObject);
                }
                else {
                    log.info("Skip updating distribution not modified.");
                }
            }
        }
        catch(CloudFrontServiceException e) {
            this.error("Cannot write CDN configuration", e);
        }
        catch(IOException e) {
            this.error("Cannot write CDN configuration", e);
        }
        finally {
            distributionStatus.get(method).clear();
        }
    }

    @Override
    public boolean isDefaultRootSupported(Distribution.Method method) {
        return method.equals(Distribution.DOWNLOAD)
                || method.equals(Distribution.WEBSITE_CDN)
                || method.equals(Distribution.CUSTOM);
    }

    @Override
    public boolean isInvalidationSupported(Distribution.Method method) {
        return method.equals(Distribution.DOWNLOAD)
                || method.equals(Distribution.WEBSITE_CDN)
                || method.equals(Distribution.CUSTOM);
    }

    @Override
    public boolean isLoggingSupported(Distribution.Method method) {
        return method.equals(Distribution.DOWNLOAD)
                || method.equals(Distribution.STREAMING)
                || method.equals(Distribution.CUSTOM);
    }

    @Override
    public boolean isAnalyticsSupported(Distribution.Method method) {
        return this.isLoggingSupported(method);
    }

    @Override
    public boolean isCnameSupported(Distribution.Method method) {
        return true;
    }

    /**
     * You can make any number of invalidation requests, but you can have only three invalidation requests
     * in progress at one time. Each request can contain up to 1,000 objects to invalidate. If you
     * exceed these limits, you get an error message.
     * <p/>
     * It usually takes 10 to 15 minutes to complete your invalidation request, depending on
     * the size of your request.
     *
     * @param origin    Origin server
     * @param method    Distribution method
     * @param files     Files to purge
     * @param recursive Recursivly for folders
     */
    @Override
    public void invalidate(String origin, Distribution.Method method, List<Path> files, boolean recursive) {
        try {
            this.check();
            this.message(MessageFormat.format(Locale.localizedString("Writing CDN configuration of {0}", "Status"),
                    origin));

            final long reference = System.currentTimeMillis();
            Distribution d = distributionStatus.get(method).get(origin);
            if(null == d) {
                log.error("No cached distribution for origin:" + origin);
                return;
            }
            List<String> keys = this.getInvalidationKeys(files, recursive);
            if(keys.isEmpty()) {
                log.warn("No keys selected for invalidation");
                return;
            }
            CloudFrontService cf = this.getClient();
            cf.invalidateObjects(d.getId(),
                    keys.toArray(new String[keys.size()]), // objects
                    new Date(reference).toString() // Comment
            );
        }
        catch(CloudFrontServiceException e) {
            this.error("Cannot write CDN configuration", e);
        }
        catch(IOException e) {
            this.error("Cannot write CDN configuration", e);
        }
        finally {
            distributionStatus.get(method).clear();
        }
    }

    /**
     * @param files     Files to purge
     * @param recursive Recursivly for folders
     * @return Key to files
     */
    protected List<String> getInvalidationKeys(List<Path> files, boolean recursive) {
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
                    keys.addAll(this.getInvalidationKeys(file.<Path>children(), recursive));
                }
            }
        }
        return keys;
    }

    /**
     * @param distribution Configuration
     * @return Status message from service
     * @throws IOException Service error
     */
    private String readInvalidationStatus(Distribution distribution) throws IOException {
        try {
            final CloudFrontService cf = this.getClient();
            boolean complete = false;
            int inprogress = 0;
            List<InvalidationSummary> summaries = cf.listInvalidations(distribution.getId());
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
            this.error("Cannot read CDN configuration", e);
        }
        return Locale.localizedString("Unknown");
    }

    protected List<String> getContainers() {
        // List S3 containers
        final Session session = SessionFactory.createSession(
                new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(), host.getCredentials()));
        if(session.getHost().getCredentials().validate(session.getHost().getProtocol())) {
            List<String> buckets = new ArrayList<String>();
            for(Path bucket : session.mount().list()) {
                buckets.add(bucket.getName());
            }
            Collections.sort(buckets);
            return buckets;
        }
        return Collections.emptyList();
    }

    @Override
    public void clear() {
        distributionStatus.clear();
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
    private org.jets3t.service.model.cloudfront.Distribution createDistribution(boolean enabled,
                                                                                Distribution.Method method,
                                                                                final String origin,
                                                                                String[] cnames,
                                                                                LoggingStatus logging,
                                                                                String defaultRootObject)
            throws ConnectionCanceledException, CloudFrontServiceException {

        final String reference = String.valueOf(System.currentTimeMillis());

        if(log.isDebugEnabled()) {
            log.debug(String.format("Create new %s distribution", method.toString()));
        }
        CloudFrontService cf = this.getClient();

        final String originId = UUID.randomUUID().toString();
        final CacheBehavior cacheBehavior = new CacheBehavior(
                originId, false, null, CacheBehavior.ViewerProtocolPolicy.ALLOW_ALL, 0L
        );

        if(method.equals(Distribution.STREAMING)) {
            final StreamingDistributionConfig config = new StreamingDistributionConfig(
                    new S3Origin[]{new S3Origin(originId, origin, null)},
                    reference, cnames, null, enabled, logging, null);
            return cf.createDistribution(config
            );
        }
        if(method.equals(Distribution.DOWNLOAD)) {
            DistributionConfig config = new DistributionConfig(
                    new Origin[]{new S3Origin(originId, origin, null)},
                    reference, cnames, null, enabled, logging,
                    defaultRootObject, cacheBehavior, new CacheBehavior[]{});
            return cf.createDistribution(config);
        }
        if(method.equals(Distribution.CUSTOM)
                || method.equals(Distribution.WEBSITE_CDN)) {
            DistributionConfig config = new DistributionConfig(
                    new Origin[]{new CustomOrigin(originId, origin, CustomOrigin.OriginProtocolPolicy.MATCH_VIEWER)},
                    reference, cnames, null, enabled, logging,
                    defaultRootObject, cacheBehavior, new CacheBehavior[]{});
            return cf.createDistribution(config);
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
    private void updateDistribution(boolean enabled, Distribution.Method method, final String origin,
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

        final CloudFrontService cf = this.getClient();
        if(method.equals(Distribution.STREAMING)) {
            StreamingDistributionConfig config = new StreamingDistributionConfig(
                    new Origin[]{new S3Origin(originId, origin, null)}, reference, cnames, null, enabled, logging, null);
            config.setEtag(etag);
            cf.updateDistributionConfig(distributionId, config);
        }
        else if(method.equals(Distribution.DOWNLOAD)) {
            DistributionConfig config = new DistributionConfig(
                    new Origin[]{new S3Origin(originId, origin, null)},
                    reference, cnames, null, enabled, logging,
                    defaultRootObject, cacheBehavior, new CacheBehavior[]{});
            config.setEtag(etag);
            cf.updateDistributionConfig(distributionId, config);
        }
        else if(method.equals(Distribution.CUSTOM)
                || method.equals(Distribution.WEBSITE_CDN)) {
            DistributionConfig config = new DistributionConfig(
                    new Origin[]{this.getCustomOriginConfiguration(originId, method, origin)},
                    reference, cnames, null, enabled, logging,
                    defaultRootObject, cacheBehavior, new CacheBehavior[]{});
            config.setEtag(etag);
            cf.updateDistributionConfig(distributionId, config);
        }
        else {
            throw new RuntimeException("Invalid distribution method:" + method);
        }
    }

    /**
     * @param method Distribution method
     * @param origin Origin container
     * @return Match viewer policy
     */
    protected CustomOrigin getCustomOriginConfiguration(final String id,
                                                        final Distribution.Method method,
                                                        final String origin) {
        return new CustomOrigin(id, origin, CustomOrigin.OriginProtocolPolicy.MATCH_VIEWER);
    }

    /**
     * Amazon CloudFront Extension used to list all configured distributions
     *
     * @param origin Name of the container
     * @param method Distribution method
     * @throws CloudFrontServiceException CloudFront failure details
     * @throws IOException                Service error
     */
    private void cache(String origin, Distribution.Method method)
            throws IOException, CloudFrontServiceException {

        if(log.isDebugEnabled()) {
            log.debug(String.format("List distributions for origin %s", origin));
        }

        CloudFrontService cf = this.getClient();

        if(method.equals(Distribution.STREAMING)) {
            for(org.jets3t.service.model.cloudfront.Distribution d : cf.listStreamingDistributions(origin)) {
                for(Origin o : d.getConfig().getOrigins()) {
                    if(o instanceof S3Origin) {
                        // Write to cache
                        distributionStatus.get(method).put(origin, this.convert(d, method));
                        // We currently only support one distribution per bucket
                        break;
                    }
                }
            }
        }
        else if(method.equals(Distribution.DOWNLOAD)) {
            // List distributions restricting to bucket name origin
            for(org.jets3t.service.model.cloudfront.Distribution d : cf.listDistributions(origin)) {
                for(Origin o : d.getConfig().getOrigins()) {
                    if(o instanceof S3Origin) {
                        // Write to cache
                        distributionStatus.get(method).put(origin, this.convert(d, method));
                        // We currently only support one distribution per bucket
                        break;
                    }
                }
            }
        }
        else if(method.equals(Distribution.CUSTOM)
                || method.equals(Distribution.WEBSITE_CDN)) {
            for(org.jets3t.service.model.cloudfront.Distribution d : cf.listDistributions()) {
                for(Origin o : d.getConfig().getOrigins()) {
                    // Listing all distributions and look for custom origin
                    if(o instanceof CustomOrigin) {
                        if(o.getDomainName().equals(origin)) {
                            distributionStatus.get(method).put(origin, this.convert(d, method));
                        }
                    }
                }
            }
        }
    }

    private Distribution convert(final org.jets3t.service.model.cloudfront.Distribution d,
                                 Distribution.Method method)
            throws IOException, CloudFrontServiceException {
        // Retrieve distributions configuration to access current logging status settings.
        final DistributionConfig distributionConfig = this.getDistributionConfig(d);
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
                String.format("%s%s%s", method.getProtocol(), d.getDomainName(), method.getContext()),
                method.equals(Distribution.DOWNLOAD) || method.equals(Distribution.CUSTOM)
                        ? String.format("https://%s%s", d.getDomainName(), method.getContext()) : null, // No SSL
                null,
                Locale.localizedString(d.getStatus(), "S3"),
                distributionConfig.getCNAMEs(),
                distributionConfig.getLoggingStatus().isEnabled(),
                loggingTarget,
                distributionConfig.getDefaultRootObject());
        if(this.isInvalidationSupported(method)) {
            distribution.setInvalidationStatus(this.readInvalidationStatus(distribution));
        }
        if(this.isLoggingSupported(method)) {
            distribution.setContainers(this.getContainers());
        }
        return distribution;
    }

    /**
     * @param distribution Distribution configuration
     * @return Configuration
     * @throws CloudFrontServiceException CloudFront failure details
     * @throws IOException                Service error
     */
    private DistributionConfig getDistributionConfig(final org.jets3t.service.model.cloudfront.Distribution distribution)
            throws IOException, CloudFrontServiceException {

        CloudFrontService cf = this.getClient();
        if(distribution.isStreamingDistribution()) {
            return cf.getStreamingDistributionConfig(distribution.getId());
        }
        return cf.getDistributionConfig(distribution.getId());
    }
}