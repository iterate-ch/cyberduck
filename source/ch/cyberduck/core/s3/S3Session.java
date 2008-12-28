package ch.cyberduck.core.s3;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import com.apple.cocoa.foundation.NSBundle;

import ch.cyberduck.core.*;
import ch.cyberduck.core.http.HTTPSession;
import ch.cyberduck.core.http.StickyHostConfiguration;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;
import ch.cyberduck.core.ssl.IgnoreX509TrustManager;
import ch.cyberduck.core.ssl.KeychainX509TrustManager;
import ch.cyberduck.core.ssl.SSLSession;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.apache.log4j.Logger;
import org.jets3t.service.*;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.cloudfront.Distribution;
import org.jets3t.service.security.AWSCredentials;

import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;

/**
 * @version $Id$
 */
public class S3Session extends HTTPSession implements SSLSession {
    private static Logger log = Logger.getLogger(S3Session.class);

    static {
        SessionFactory.addFactory(Protocol.S3, new Factory());
    }

    private static class Factory extends SessionFactory {
        protected Session create(Host h) {
            return new S3Session(h);
        }
    }

    protected S3Service S3;

    /**
     * A trust manager accepting any certificate by default
     */
    private X509TrustManager trustManager;

    /**
     * @return
     */
    public X509TrustManager getTrustManager() {
        return trustManager;
    }

    /**
     * Override the default ignoring trust manager
     *
     * @param trustManager
     */
    public void setTrustManager(X509TrustManager trustManager) {
        this.trustManager = trustManager;
    }

    protected S3Session(Host h) {
        super(h);
        if(Preferences.instance().getBoolean("s3.tls.acceptAnyCertificate")) {
            this.setTrustManager(new IgnoreX509TrustManager());
        }
        else {
            this.setTrustManager(new KeychainX509TrustManager(h.getHostname()));
        }
    }

    private Jets3tProperties configuration;

    protected void configure(Jets3tProperties configuration) {
        configuration.setProperty("s3service.s3-endpoint", host.getHostname());
        configuration.setProperty("s3service.https-only",
                String.valueOf(host.getProtocol().getScheme().equals("https"))
        );

        configuration.setProperty("httpclient.proxy-autodetect", "false");
        if(host.getProtocol().getScheme().equals("https")) {
            if(Proxy.isHTTPSProxyEnabled()) {
                configuration.setProperty("httpclient.proxy-host", Proxy.getHTTPSProxyHost());
                configuration.setProperty("httpclient.proxy-port", String.valueOf(Proxy.getHTTPSProxyPort()));
            }
        }
        else {
            if(Proxy.isHTTPProxyEnabled()) {
                configuration.setProperty("httpclient.proxy-host", Proxy.getHTTPProxyHost());
                configuration.setProperty("httpclient.proxy-port", String.valueOf(Proxy.getHTTPProxyPort()));
            }
        }
        configuration.setProperty("httpclient.connection-timeout-ms", String.valueOf(this.timeout()));
        configuration.setProperty("httpclient.socket-timeout-ms", String.valueOf(this.timeout()));
        configuration.setProperty("httpclient.useragent", this.getUserAgent());

//        final String cipher = Preferences.instance().getProperty("s3.crypto.algorithm");
//        if(EncryptionUtil.isCipherAvailableForUse(cipher)) {
//            configuration.setProperty("crypto.algorithm", cipher);
//        }
//        else {
//            log.warn("Cipher " + cipher + " not available for use.");
//        }

        configuration.setProperty("downloads.restoreLastModifiedDate",
                Preferences.instance().getProperty("queue.download.preserveDate"));
    }

    /**
     * Caching the uses's buckets
     */
    private S3Bucket[] buckets = new S3Bucket[]{};

    protected S3Bucket[] getBuckets(boolean reload) throws S3ServiceException {
        if(reload) {
            this.buckets = this.S3.listAllBuckets();
        }
        return this.buckets;
    }

    protected void connect() throws IOException, ConnectionCanceledException, LoginCanceledException {
        if(this.isConnected()) {
            return;
        }
        this.fireConnectionWillOpenEvent();

        this.message(MessageFormat.format(NSBundle.localizedString("Opening {0} connection to {1}", "Status", ""),
                host.getProtocol().getName(), host.getHostname()));

        // Configure connection options
        this.configure(configuration = new Jets3tProperties());

        // Prompt the login credentials first
        this.login();
        this.message(MessageFormat.format(NSBundle.localizedString("{0} connection opened", "Status", ""),
                host.getProtocol().getName()));
        this.fireConnectionDidOpenEvent();
    }

    protected void login(final Credentials credentials) throws IOException {
        try {
            final HostConfiguration hostConfiguration = new StickyHostConfiguration();
            hostConfiguration.setHost(host.getHostname(), host.getPort(),
                    new org.apache.commons.httpclient.protocol.Protocol(host.getProtocol().getScheme(),
                            new CustomTrustSSLProtocolSocketFactory(this.getTrustManager()), host.getPort())
            );

            this.S3 = new RestS3Service(credentials.isAnonymousLogin() ? null : new AWSCredentials(credentials.getUsername(),
                    credentials.getPassword()), this.getUserAgent(), new CredentialsProvider() {
                /**
                 * Implementation method for the CredentialsProvider interface
                 * @throws CredentialsNotAvailableException
                 */
                public org.apache.commons.httpclient.Credentials getCredentials(AuthScheme authscheme, String hostname, int port, boolean proxy)
                        throws CredentialsNotAvailableException {
                    log.error("Additional HTTP authentication not supported:" + authscheme.getSchemeName());
                    throw new CredentialsNotAvailableException("Unsupported authentication scheme: " +
                            authscheme.getSchemeName());
                }
            }, configuration, hostConfiguration);

            this.getBuckets(true);
        }
        catch(S3ServiceException e) {
            if(this.isLoginFailure(e)) {
                this.message(NSBundle.localizedString("Login failed", "Credentials", ""));
                this.login.fail(host,
                        NSBundle.localizedString("Login with username and password", "Credentials", ""));
                this.login();
            }
            else {
                throw new S3Exception(e);
            }
        }
    }

    private boolean isLoginFailure(S3ServiceException e) {
        if(null == e.getS3ErrorCode()) {
            return false;
        }
        return e.getS3ErrorCode().equals("InvalidAccessKeyId") // Invalid Access ID
                || e.getS3ErrorCode().equals("SignatureDoesNotMatch"); // Invalid Secret Key
    }

    public void close() {
        try {
            if(this.isConnected()) {
                this.fireConnectionWillCloseEvent();
            }
        }
        finally {
            S3 = null;
            this.fireConnectionDidCloseEvent();
        }
    }

    public void interrupt() {
        try {
            super.interrupt();
            if(this.isConnected()) {
                this.fireConnectionWillCloseEvent();
            }
        }
        finally {
            S3 = null;
            this.fireConnectionDidCloseEvent();
        }
    }

    public Path workdir() throws IOException {
        if(!this.isConnected()) {
            throw new ConnectionCanceledException();
        }
        if(null == workdir) {
            workdir = PathFactory.createPath(this, Path.DELIMITER, Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        }
        return workdir;
    }

    protected void noop() throws IOException {
        ;
    }

    public void sendCommand(String command) throws IOException {
        throw new UnsupportedOperationException();
    }

    public boolean isConnected() {
        return S3 != null;
    }

    /**
     * Amazon CloudFront Extension
     *
     * @param path
     * @return
     */
    public Distribution createDistribution(boolean enabled, final String bucket, String[] cnames) throws IOException {
        try {
            final long reference = System.currentTimeMillis();
            return this.createCloudFrontService().createDistribution(
                    bucket,
                    String.valueOf(reference), // Caller reference - a unique string value
                    cnames, // CNAME aliases for distribution
                    new Date(reference).toString(), // Comment
                    enabled  // Enabled?
            );
        }
        catch(CloudFrontServiceException e) {
            throw new S3Exception(e.getMessage(), e);
        }
    }

    /**
     * Amazon CloudFront Extension used to enable or disable a distribution configuration and its CNAMESs
     *
     * @param distribution
     * @param cnames       DNS CNAME aliases for distribution
     */
    public void updateDistribution(boolean enabled, final Distribution distribution, String[] cnames) throws IOException {
        try {
            final long reference = System.currentTimeMillis();
            this.createCloudFrontService().updateDistributionConfig(
                    distribution.getId(),
                    cnames, // CNAME aliases for distribution
                    new Date(reference).toString(), // Comment
                    enabled // Enabled?
            );
        }
        catch(CloudFrontServiceException e) {
            throw new S3Exception(e.getMessage(), e);
        }
    }

    /**
     * Amazon CloudFront Extension used to list all configured distributions
     *
     * @return All distributions for the given AWS Credentials
     */
    public Distribution[] listDistributions(String bucket) throws IOException {
        try {
            return this.createCloudFrontService().listDistributions(bucket);
        }
        catch(CloudFrontServiceException e) {
            throw new S3Exception(e.getMessage(), e);
        }
    }

    /**
     * 
     * @param distribution A distribution (the distribution must be disabled and deployed first)
     */
    public void deleteDistribution(final Distribution distribution) throws IOException {
        try {
            this.createCloudFrontService().deleteDistribution(distribution.getId());
        }
        catch(CloudFrontServiceException e) {
            throw new S3Exception(e.getMessage(), e);
        }
    }

    /**
     * Cached instance for session
     */
    private CloudFrontService cloudfront;

    /**
     * Amazon CloudFront Extension
     *
     * @return A cached cloud front service interface
     * @throws CloudFrontServiceException
     */
    private CloudFrontService createCloudFrontService() throws CloudFrontServiceException {
        if(null == cloudfront) {
            final Credentials credentials = host.getCredentials();
            // Construct a CloudFrontService object to interact with the service.
            cloudfront = new CloudFrontService(
                    credentials.isAnonymousLogin() ? null : new AWSCredentials(credentials.getUsername(),
                            credentials.getPassword()),
                    this.getUserAgent(), // Invoking application description
                    null, // Credentials Provider
                    new Jets3tProperties(),
                    new HostConfiguration());
        }
        return cloudfront;
    }

}
