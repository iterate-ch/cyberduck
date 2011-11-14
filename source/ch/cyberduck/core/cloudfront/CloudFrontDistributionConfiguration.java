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
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.ssl.AbstractX509TrustManager;

import org.apache.commons.lang.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.log4j.Logger;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.model.cloudfront.*;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.utils.ServiceUtils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/**
 * Amazon CloudFront CDN configuration.
 *
 * @version $Id:$
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
    protected Map<ch.cyberduck.core.cdn.Distribution.Method, Map<String, ch.cyberduck.core.cdn.Distribution>> distributionStatus
            = new HashMap<ch.cyberduck.core.cdn.Distribution.Method, Map<String, ch.cyberduck.core.cdn.Distribution>>();

    public CloudFrontDistributionConfiguration(LoginController parent, Credentials credentials,
                                               ErrorListener error, ProgressListener progress,
                                               TranscriptListener transcript) {
        // Configure with the same host as S3 to get the same credentials from the keychain.
        super(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(), credentials));
        this.login = parent;
        this.addErrorListener(error);
        this.addProgressListener(progress);
        this.addTranscriptListener(transcript);
        this.clear();
    }

    @Override
    public AbstractX509TrustManager getTrustManager() {
        return this.getTrustManager("cloudfront.amazonaws.com");
    }

    /**
     * Amazon CloudFront Extension
     *
     * @return A cached cloud front service interface
     */
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
            // Provoke authentication error if any.
            for(ch.cyberduck.core.cdn.Distribution.Method method : getMethods()) {
                for(String container : this.getContainers(method)) {
                    // Cache first container
                    this.cache(this.getOrigin(method, container), method);
                    break;
                }
                break;
            }
        }
        catch(CloudFrontServiceException e) {
            log.warn(String.format("Invalid account:%s", e.getMessage()));
            this.message(Locale.localizedString("Login failed", "Credentials"));
            controller.fail(host.getProtocol(), credentials);
            this.login();
        }
    }

    @Override
    protected void prompt(LoginController controller) throws LoginCanceledException {
        controller.check(host, this.toString(), null, true, false, false);
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

    public String toString(ch.cyberduck.core.cdn.Distribution.Method method) {
        return this.toString();
    }

    public boolean isCached(ch.cyberduck.core.cdn.Distribution.Method method) {
        return !distributionStatus.get(method).isEmpty();
    }

    public String getOrigin(ch.cyberduck.core.cdn.Distribution.Method method, String container) {
        return container + CloudFrontService.DEFAULT_BUCKET_SUFFIX;
    }

    public List<ch.cyberduck.core.cdn.Distribution.Method> getMethods() {
        return Arrays.asList(ch.cyberduck.core.cdn.Distribution.DOWNLOAD, ch.cyberduck.core.cdn.Distribution.STREAMING);
    }

    public ch.cyberduck.core.cdn.Distribution read(String origin, ch.cyberduck.core.cdn.Distribution.Method method) {
        if(method.equals(ch.cyberduck.core.cdn.Distribution.DOWNLOAD)
                || method.equals(ch.cyberduck.core.cdn.Distribution.STREAMING)
                || method.equals(ch.cyberduck.core.cdn.Distribution.CUSTOM)
                || method.equals(ch.cyberduck.core.cdn.Distribution.WEBSITE_CDN)) {
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
                    distributionStatus.get(method).put(origin, new ch.cyberduck.core.cdn.Distribution(null,
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
        return new ch.cyberduck.core.cdn.Distribution(origin, method);
    }

    public void write(boolean enabled, String origin, ch.cyberduck.core.cdn.Distribution.Method method,
                      String[] cnames, boolean logging, String loggingBucket, String defaultRootObject) {
        try {
            this.check();

            // Configure CDN
            LoggingStatus loggingStatus = null;
            if(logging) {
                if(this.isLoggingSupported(method)) {
                    final String loggingDestination = StringUtils.isNotBlank(loggingBucket) ?
                            ServiceUtils.generateS3HostnameForBucket(loggingBucket, false, this.getHost().getHostname(true)) : origin;
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
            ch.cyberduck.core.cdn.Distribution d = distributionStatus.get(method).get(origin);
            if(null == d) {
                log.debug("No existing distribution found for method:" + method);
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
                    this.updateDistribution(enabled, method, origin, d.getId(), cnames, loggingStatus, defaultRootObject);
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

    public boolean isDefaultRootSupported(ch.cyberduck.core.cdn.Distribution.Method method) {
        return method.equals(ch.cyberduck.core.cdn.Distribution.DOWNLOAD)
                || method.equals(ch.cyberduck.core.cdn.Distribution.WEBSITE_CDN)
                || method.equals(ch.cyberduck.core.cdn.Distribution.CUSTOM);
    }

    public boolean isInvalidationSupported(ch.cyberduck.core.cdn.Distribution.Method method) {
        return method.equals(ch.cyberduck.core.cdn.Distribution.DOWNLOAD)
                || method.equals(ch.cyberduck.core.cdn.Distribution.WEBSITE_CDN)
                || method.equals(ch.cyberduck.core.cdn.Distribution.CUSTOM);
    }

    public boolean isLoggingSupported(ch.cyberduck.core.cdn.Distribution.Method method) {
        return method.equals(ch.cyberduck.core.cdn.Distribution.DOWNLOAD)
                || method.equals(ch.cyberduck.core.cdn.Distribution.STREAMING)
                || method.equals(ch.cyberduck.core.cdn.Distribution.CUSTOM);
    }

    public boolean isCnameSupported(ch.cyberduck.core.cdn.Distribution.Method method) {
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
    public void invalidate(String origin, ch.cyberduck.core.cdn.Distribution.Method method, List<Path> files, boolean recursive) {
        try {
            this.check();
            this.message(MessageFormat.format(Locale.localizedString("Writing CDN configuration of {0}", "Status"),
                    origin));

            final long reference = System.currentTimeMillis();
            ch.cyberduck.core.cdn.Distribution d = distributionStatus.get(method).get(origin);
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
    private String readInvalidationStatus(ch.cyberduck.core.cdn.Distribution distribution) throws IOException {
        try {
            CloudFrontService cf = this.getClient();

            final long reference = System.currentTimeMillis();
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

    protected List<String> getContainers(ch.cyberduck.core.cdn.Distribution.Method method) {
        // List S3 containers
        final Session session = SessionFactory.createSession(host);
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

    public void clear() {
        for(ch.cyberduck.core.cdn.Distribution.Method method : this.getMethods()) {
            distributionStatus.put(method, new HashMap<String, ch.cyberduck.core.cdn.Distribution>(0));
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
    private org.jets3t.service.model.cloudfront.Distribution createDistribution(boolean enabled,
                                                                                ch.cyberduck.core.cdn.Distribution.Method method,
                                                                                final String origin,
                                                                                String[] cnames,
                                                                                LoggingStatus logging,
                                                                                String defaultRootObject)
            throws ConnectionCanceledException, CloudFrontServiceException {
        final long reference = System.currentTimeMillis();

        log.debug("createDistribution:" + method);
        CloudFrontService cf = this.getClient();
        if(method.equals(ch.cyberduck.core.cdn.Distribution.STREAMING)) {
            return cf.createStreamingDistribution(
                    new S3Origin(origin),
                    String.valueOf(reference), // Caller reference - a unique string value
                    cnames, // CNAME aliases for distribution
                    new Date(reference).toString(), // Comment
                    enabled,  // Enabled?
                    logging
            );
        }
        if(method.equals(ch.cyberduck.core.cdn.Distribution.DOWNLOAD)) {
            return cf.createDistribution(
                    new S3Origin(origin),
                    String.valueOf(reference), // Caller reference - a unique string value
                    cnames, // CNAME aliases for distribution
                    new Date(reference).toString(), // Comment
                    enabled,  // Enabled?
                    logging, // Logging Status. Disabled if null
                    false,
                    null,
                    null,
                    defaultRootObject
            );
        }
        if(method.equals(ch.cyberduck.core.cdn.Distribution.CUSTOM)
                || method.equals(ch.cyberduck.core.cdn.Distribution.WEBSITE_CDN)) {
            return cf.createDistribution(
                    this.getCustomOriginConfiguration(method, origin),
                    String.valueOf(reference), // Caller reference - a unique string value
                    cnames, // CNAME aliases for distribution
                    new Date(reference).toString(), // Comment
                    enabled,  // Enabled?
                    logging, // Logging Status. Disabled if null
                    false,
                    null,
                    null,
                    defaultRootObject
            );
        }
        throw new RuntimeException("Invalid distribution method:" + method);
    }

    /**
     * Amazon CloudFront Extension used to enable or disable a distribution configuration and its CNAMESs
     *
     * @param enabled           Distribution status
     * @param method            Distribution method
     * @param origin            Name of the container
     * @param id                Distribution reference
     * @param cnames            DNS CNAME aliases for distribution
     * @param logging           Access log configuration
     * @param defaultRootObject Index file for distribution. Only supported for download and custom origins.
     * @throws CloudFrontServiceException CloudFront failure details
     * @throws IOException                I/O error
     */
    private void updateDistribution(boolean enabled, ch.cyberduck.core.cdn.Distribution.Method method, final String origin,
                                    String id, String[] cnames, LoggingStatus logging, String defaultRootObject)
            throws CloudFrontServiceException, IOException {

        log.debug("updateDistribution:" + origin);

        final long reference = System.currentTimeMillis();
        CloudFrontService cf = this.getClient();
        if(method.equals(ch.cyberduck.core.cdn.Distribution.STREAMING)) {
            cf.updateStreamingDistributionConfig(
                    id,
                    new S3Origin(origin),
                    cnames, // CNAME aliases for distribution
                    new Date(reference).toString(), // Comment
                    enabled, // Enabled?
                    logging);
        }
        else if(method.equals(ch.cyberduck.core.cdn.Distribution.DOWNLOAD)) {
            cf.updateDistributionConfig(
                    id,
                    new S3Origin(origin),
                    cnames, // CNAME aliases for distribution
                    new Date(reference).toString(), // Comment
                    enabled, // Enabled?
                    logging, // Logging Status. Disabled if null
                    false,
                    null,
                    null,
                    defaultRootObject);
        }
        else if(method.equals(ch.cyberduck.core.cdn.Distribution.CUSTOM)
                || method.equals(ch.cyberduck.core.cdn.Distribution.WEBSITE_CDN)) {
            cf.updateDistributionConfig(
                    id,
                    this.getCustomOriginConfiguration(method, origin),
                    cnames, // CNAME aliases for distribution
                    new Date(reference).toString(), // Comment
                    enabled, // Enabled?
                    logging, // Logging Status. Disabled if null
                    false,
                    null,
                    null,
                    defaultRootObject);
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
    protected CustomOrigin getCustomOriginConfiguration(ch.cyberduck.core.cdn.Distribution.Method method, String origin) {
//        int httpPort = 80;
//        if(method.getProtocol().equals("http")) {
//            httpPort = method.getDefaultPort();
//        }
//        int httpsPort = 443;
//        if(method.getProtocol().equals("https")) {
//            httpsPort = method.getDefaultPort();
//        }
//        return new CustomOrigin(origin, CustomOrigin.OriginProtocolPolicy.MATCH_VIEWER,
//                httpPort, httpsPort);
        return new CustomOrigin(origin, CustomOrigin.OriginProtocolPolicy.MATCH_VIEWER);
    }

    /**
     * Amazon CloudFront Extension used to list all configured distributions
     *
     * @param origin Name of the container
     * @param method Distribution method
     * @throws CloudFrontServiceException CloudFront failure details
     * @throws IOException                Service error
     */
    private void cache(String origin, ch.cyberduck.core.cdn.Distribution.Method method)
            throws IOException, CloudFrontServiceException {
        log.debug("listDistributions:" + origin);

        CloudFrontService cf = this.getClient();

        if(method.equals(ch.cyberduck.core.cdn.Distribution.STREAMING)) {
            for(Distribution d : cf.listStreamingDistributions(origin)) {
                // Write to cache
                distributionStatus.get(method).put(origin, this.convert(d, method));
                // We currently only support one distribution per bucket
                break;
            }
        }
        else if(method.equals(ch.cyberduck.core.cdn.Distribution.DOWNLOAD)) {
            // List distributions restricting to bucket name origin
            for(Distribution d : cf.listDistributions(origin)) {
                if(d.getOrigin() instanceof S3Origin) {
                    // Write to cache
                    distributionStatus.get(method).put(origin, this.convert(d, method));
                    // We currently only support one distribution per bucket
                    break;
                }
            }
        }
        else if(method.equals(ch.cyberduck.core.cdn.Distribution.CUSTOM)
                || method.equals(ch.cyberduck.core.cdn.Distribution.WEBSITE_CDN)) {
            for(org.jets3t.service.model.cloudfront.Distribution d : cf.listDistributions()) {
                // Listing all distributions and look for custom origin
                if(d.getOrigin() instanceof CustomOrigin) {
                    if(d.getOrigin().getDnsName().equals(origin)) {
                        distributionStatus.get(method).put(origin, this.convert(d, method));
                    }
                }
            }
        }
    }

    private ch.cyberduck.core.cdn.Distribution convert(Distribution d,
                                                       ch.cyberduck.core.cdn.Distribution.Method method)
            throws IOException, CloudFrontServiceException {
        // Retrieve distributions configuration to access current logging status settings.
        final DistributionConfig distributionConfig = this.getDistributionConfig(d);
        final String loggingTarget;
        if(null == distributionConfig.getLoggingStatus()) {
            loggingTarget = ServiceUtils.findBucketNameInHostname(d.getOrigin().getDnsName(),
                    this.getHost().getHostname(true));
        }
        else {
            loggingTarget = ServiceUtils.findBucketNameInHostname(distributionConfig.getLoggingStatus().getBucket(),
                    this.getHost().getHostname(true));
        }
        final ch.cyberduck.core.cdn.Distribution distribution = new ch.cyberduck.core.cdn.Distribution(
                d.getId(),
                d.getOrigin().getDnsName(),
                method,
                d.isEnabled(),
                d.isDeployed(),
                // CloudFront URL
                method.getProtocol() + d.getDomainName() + method.getContext(),
                null, // No SSL
                null,
                Locale.localizedString(d.getStatus(), "S3"),
                d.getCNAMEs(),
                distributionConfig.isLoggingEnabled(),
                // Default logging target to origin itself
                loggingTarget,
                distributionConfig.getDefaultRootObject());
        if(this.isInvalidationSupported(method)) {
            distribution.setInvalidationStatus(this.readInvalidationStatus(distribution));
        }
        if(this.isLoggingSupported(method)) {
            distribution.setContainers(this.getContainers(method));
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

    /**
     * @param distribution A distribution (the distribution must be disabled and deployed first)
     * @throws CloudFrontServiceException CloudFront failure details
     * @throws IOException                Service error
     */
    private void deleteDistribution(ch.cyberduck.core.cdn.Distribution distribution)
            throws IOException, CloudFrontServiceException {

        CloudFrontService cf = this.getClient();
        if(distribution.getMethod().equals(ch.cyberduck.core.cdn.Distribution.STREAMING)) {
            cf.deleteStreamingDistribution(distribution.getId());
        }
        else if(distribution.getMethod().equals(ch.cyberduck.core.cdn.Distribution.DOWNLOAD)) {
            cf.deleteDistribution(distribution.getId());
        }
        else if(distribution.getMethod().equals(ch.cyberduck.core.cdn.Distribution.CUSTOM)
                || distribution.getMethod().equals(ch.cyberduck.core.cdn.Distribution.WEBSITE_CDN)) {
            cf.deleteDistribution(distribution.getId());
        }
        else {
            throw new RuntimeException("Invalid distribution method:" + distribution.getMethod());
        }
    }
}
