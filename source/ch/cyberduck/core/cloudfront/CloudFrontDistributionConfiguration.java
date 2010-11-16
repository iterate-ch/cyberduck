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
import ch.cyberduck.core.http.HTTP3Session;
import ch.cyberduck.core.http.StickyHostConfiguration;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpHost;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.log4j.Logger;
import org.jets3t.service.CloudFrontService;
import org.jets3t.service.CloudFrontServiceException;
import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.model.cloudfront.*;
import org.jets3t.service.security.AWSCredentials;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;

/**
 * Amazon CloudFront CDN configuration.
 *
 * @version $Id:$
 */
public class CloudFrontDistributionConfiguration extends HTTP3Session implements DistributionConfiguration {
    private static Logger log = Logger.getLogger(CloudFrontDistributionConfiguration.class);

    /**
     * Cached instance for session
     */
    private CloudFrontService client;
    private LoginController login;
    private ErrorListener listener;

    /**
     * Cache distribution status result.
     */
    private Map<ch.cyberduck.core.cdn.Distribution.Method, Map<String, ch.cyberduck.core.cdn.Distribution>> distributionStatus
            = new HashMap<ch.cyberduck.core.cdn.Distribution.Method, Map<String, ch.cyberduck.core.cdn.Distribution>>();

    public CloudFrontDistributionConfiguration(LoginController parent, Credentials credentials, ErrorListener listener) {
        // Configure with the same host as S3 to get the same credentials from the keychain.
        super(new Host(Protocol.S3_SSL, Protocol.S3_SSL.getDefaultHostname(), credentials));
        this.login = parent;
        this.listener = listener;
        this.clear();
    }

    /**
     * Amazon CloudFront Extension
     *
     * @return A cached cloud front service interface
     * @throws org.jets3t.service.CloudFrontServiceException
     *          CloudFront failure
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
            // Construct a CloudFrontService object to interact with the service.
            HostConfiguration hostconfig = null;
            try {
                hostconfig = new StickyHostConfiguration();
                final HttpHost endpoint = new HttpHost(new URI(CloudFrontService.ENDPOINT, false));
                hostconfig.setHost(endpoint.getHostName(), endpoint.getPort(),
                        new org.apache.commons.httpclient.protocol.Protocol(endpoint.getProtocol().getScheme(),
                                new SSLSocketFactory(this.getTrustManager(endpoint.getHostName())), endpoint.getPort())
                );
            }
            catch(URIException e) {
                log.error(e.getMessage(), e);
            }
            client = new CloudFrontService(
                    new AWSCredentials(credentials.getUsername(), credentials.getPassword()),
                    this.getUserAgent(), // Invoking application description
                    null, // Credentials Provider
                    new Jets3tProperties(),
                    hostconfig);
            // Provoke authentication error if any.
            client.listDistributions();
        }
        catch(CloudFrontServiceException e) {
            log.warn("Invalid CloudFront account:" + e.getMessage());
            this.message(Locale.localizedString("Login failed", "Credentials"));
            try {
                controller.fail(host.getProtocol(), credentials);
            }
            catch(LoginCanceledException canceled) {
                // User canceled Cloudfront login. Possibly not enabled in Amazon configuration.
                throw canceled;
            }
            this.login();
        }
    }

    @Override
    protected void prompt(LoginController login) throws LoginCanceledException {
        login.check(host, this.toString(), null, true, false, false);
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
    public void error(Path path, String message, Throwable e) {
        listener.error(new BackgroundException(this, path, message, e));
    }

    @Override
    public String toString() {
        return Locale.localizedString("Amazon CloudFront", "S3");
    }

    public boolean isConfigured() {
        for(ch.cyberduck.core.cdn.Distribution.Method method : this.getMethods()) {
            if(!distributionStatus.get(method).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public String getOrigin(ch.cyberduck.core.cdn.Distribution.Method method, String container) {
        return container + CloudFrontService.DEFAULT_BUCKET_SUFFIX;
    }

    public List<ch.cyberduck.core.cdn.Distribution.Method> getMethods() {
        return Arrays.asList(ch.cyberduck.core.cdn.Distribution.DOWNLOAD, ch.cyberduck.core.cdn.Distribution.STREAMING);
    }

    public ch.cyberduck.core.cdn.Distribution read(String origin, ch.cyberduck.core.cdn.Distribution.Method method) {
        if(!distributionStatus.get(method).containsKey(origin)) {
            try {
                this.check();
                this.message(MessageFormat.format(Locale.localizedString("Reading CDN configuration of {0}", "Status"),
                        origin));

                for(ch.cyberduck.core.cdn.Distribution d : this.listDistributions(origin, method)) {
                    if(d.isDeployed()) {
                        distributionStatus.get(method).put(origin, d);
                    }
                    // We currently only support one distribution per bucket
                    return d;
                }
            }
            catch(CloudFrontServiceException e) {
                this.error("Cannot read CDN configuration", e);
            }
            catch(IOException e) {
                this.error("Cannot read CDN configuration", e);
            }
        }
        if(distributionStatus.get(method).containsKey(origin)) {
            return distributionStatus.get(method).get(origin);
        }
        return new ch.cyberduck.core.cdn.Distribution(origin, method);
    }

    public void write(boolean enabled, String origin, ch.cyberduck.core.cdn.Distribution.Method method,
                      String[] cnames, boolean logging, String defaultRootObject) {
        try {
            this.check();
            this.message(MessageFormat.format(Locale.localizedString("Writing CDN configuration of {0}", "Status"),
                    origin));

            // Configure CDN
            LoggingStatus l = null;
            if(logging) {
                l = new LoggingStatus(origin, Preferences.instance().getProperty("cloudfront.logging.prefix"));
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
                this.createDistribution(enabled, method, origin, cnames, l, defaultRootObject);
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
                if(null == d.getDefaultRootObject() && null == defaultRootObject) {
                    // No change to default root object
                }
                else if(null != d.getDefaultRootObject() && null == defaultRootObject) {
                    modified = true;
                }
                else if(null == d.getDefaultRootObject() && null != defaultRootObject) {
                    modified = true;
                }
                else if(!d.getDefaultRootObject().equals(defaultRootObject)) {
                    modified = true;
                }

                if(modified) {
                    this.updateDistribution(enabled, method, origin, d.getId(), cnames, l, defaultRootObject);
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

    public boolean isDefaultRootSupported() {
        return true;
    }

    public boolean isInvalidationSupported() {
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
     * @param origin
     * @param method
     * @param files
     * @throws CloudFrontServiceException
     */
    public void invalidate(String origin, ch.cyberduck.core.cdn.Distribution.Method method, List<Path> files) {
        try {
            this.check();
            this.message(MessageFormat.format(Locale.localizedString("Writing CDN configuration of {0}", "Status"),
                    origin));

            final long reference = System.currentTimeMillis();
            ch.cyberduck.core.cdn.Distribution d = distributionStatus.get(method).get(origin);
            List<String> keys = this.getInvalidationKeys(files);
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
     * @param files
     * @return
     */
    private List<String> getInvalidationKeys(List<Path> files) {
        List<String> keys = new ArrayList<String>();
        for(Path file : files) {
            if(file.attributes().isDirectory()) {
                keys.addAll(this.getInvalidationKeys(file.<Path>children()));
            }
            else {
                keys.add(file.getKey());
            }
        }
        return keys;
    }

    /**
     * @param distribution
     * @return
     */
    private String readInvalidationStatus(ch.cyberduck.core.cdn.Distribution distribution) throws IOException {
        if(this.isInvalidationSupported()) {
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
            }
            catch(CloudFrontServiceException e) {
                this.error("Cannot read CDN configuration", e);
            }
        }
        return Locale.localizedString("None");
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
     * @param enabled Distribution status
     * @param origin  Name of the container
     * @param cnames  DNS CNAME aliases for distribution
     * @param logging Access log configuration
     * @return Distribution configuration
     * @throws CloudFrontServiceException CloudFront failure details
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
        if(method.equals(ch.cyberduck.core.cdn.Distribution.CUSTOM)) {
            return cf.createDistribution(
                    new CustomOrigin(origin, CustomOrigin.OriginProtocolPolicy.MATCH_VIEWER),
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
        return null;
    }

    /**
     * Amazon CloudFront Extension used to enable or disable a distribution configuration and its CNAMESs
     *
     * @param enabled Distribution status
     * @param id      Distribution reference
     * @param cnames  DNS CNAME aliases for distribution
     * @param logging Access log configuration
     * @throws CloudFrontServiceException CloudFront failure details
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
        else if(method.equals(ch.cyberduck.core.cdn.Distribution.CUSTOM)) {
            cf.updateDistributionConfig(
                    id,
                    new CustomOrigin(origin, CustomOrigin.OriginProtocolPolicy.MATCH_VIEWER),
                    cnames, // CNAME aliases for distribution
                    new Date(reference).toString(), // Comment
                    enabled, // Enabled?
                    logging, // Logging Status. Disabled if null
                    false,
                    null,
                    null,
                    defaultRootObject);
        }
    }

    /**
     * Amazon CloudFront Extension used to list all configured distributions
     *
     * @param origin Name of the container
     * @param method
     * @return All distributions for the given AWS Credentials
     * @throws CloudFrontServiceException CloudFront failure details
     */
    private List<ch.cyberduck.core.cdn.Distribution> listDistributions(String origin,
                                                                       ch.cyberduck.core.cdn.Distribution.Method method)
            throws IOException, CloudFrontServiceException {
        log.debug("listDistributions:" + origin);

        CloudFrontService cf = this.getClient();

        List<ch.cyberduck.core.cdn.Distribution> list = new ArrayList<ch.cyberduck.core.cdn.Distribution>();
        if(method.equals(ch.cyberduck.core.cdn.Distribution.STREAMING)) {
            for(Distribution d : cf.listStreamingDistributions(origin)) {
                list.add(this.convert(d, method));
            }
        }
        else if(method.equals(ch.cyberduck.core.cdn.Distribution.DOWNLOAD)) {
            for(Distribution d : cf.listDistributions(origin)) {
                if(d.getOrigin() instanceof S3Origin) {
                    list.add(this.convert(d, method));
                }
            }
        }
        else if(method.equals(ch.cyberduck.core.cdn.Distribution.CUSTOM)) {
            for(org.jets3t.service.model.cloudfront.Distribution d : cf.listDistributions()) {
                if(d.getOrigin() instanceof CustomOrigin) {
                    if(d.getOrigin().getDnsName().equals(origin)) {
                        list.add(this.convert(d, method));
                    }
                }
            }
        }
        return list;
    }

    private ch.cyberduck.core.cdn.Distribution convert(Distribution d,
                                                       ch.cyberduck.core.cdn.Distribution.Method method)
            throws IOException, CloudFrontServiceException {
        // Retrieve distribution's configuration to access current logging status settings.
        final DistributionConfig distributionConfig = this.getDistributionConfig(d);
        final ch.cyberduck.core.cdn.Distribution distribution = new ch.cyberduck.core.cdn.Distribution(
                d.getId(),
                d.getOrigin().getDnsName(),
                method,
                d.isEnabled(),
                d.isDeployed(),
                method.getProtocol() + d.getDomainName() + method.getContext(),
                Locale.localizedString(d.getStatus(), "S3"),
                d.getCNAMEs(),
                distributionConfig.isLoggingEnabled(),
                distributionConfig.getDefaultRootObject());
        distribution.setInvalidationStatus(this.readInvalidationStatus(distribution));
        return distribution;
    }

    /**
     * @param distribution Distribution configuration
     * @throws CloudFrontServiceException CloudFront failure details
     * @returann
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
        else if(distribution.getMethod().equals(ch.cyberduck.core.cdn.Distribution.CUSTOM)) {
            cf.deleteDistribution(distribution.getId());
        }
    }
}