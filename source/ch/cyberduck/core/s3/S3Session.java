package ch.cyberduck.core.s3;

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
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.analytics.QloudstatAnalyticsProvider;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cloud.CloudSession;
import ch.cyberduck.core.cloudfront.CloudFrontDistributionConfiguration;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.identity.AWSIdentityConfiguration;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.acl.GroupGrantee;
import org.jets3t.service.impl.rest.XmlResponsesSaxParser;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.*;
import org.jets3t.service.model.cloudfront.CustomOrigin;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.security.OAuth2Credentials;
import org.jets3t.service.security.OAuth2Tokens;
import org.jets3t.service.security.ProviderCredentials;
import org.jets3t.service.utils.RestUtils;
import org.jets3t.service.utils.ServiceUtils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @version $Id$
 */
public class S3Session extends CloudSession {
    private static final Logger log = Logger.getLogger(S3Session.class);

    private RequestEntityRestStorageService client;

    public S3Session(Host h) {
        super(h);
    }

    @Override
    public RequestEntityRestStorageService getClient() throws ConnectionCanceledException {
        if(null == client) {
            throw new ConnectionCanceledException();
        }
        return client;
    }

    /**
     * Exposing protected methods
     */
    protected class RequestEntityRestStorageService extends RestS3Service {
        public RequestEntityRestStorageService(ProviderCredentials credentials) throws ServiceException {
            super(credentials, S3Session.this.getUserAgent(), null, S3Session.this.getProperties());
        }

        @Override
        protected HttpClient initHttpConnection() {
            final AbstractHttpClient client = S3Session.this.http();
            client.setHttpRequestRetryHandler(new RestUtils.JetS3tRetryHandler(5, this));
            return client;
        }

        @Override
        protected boolean isTargettingGoogleStorageService() {
            return getHost().getHostname().equals(Protocol.GOOGLESTORAGE_SSL.getDefaultHostname());
        }

        @Override
        protected void initializeProxy() {
            // Client already configured
        }

        @Override
        protected void putObjectWithRequestEntityImpl(String bucketName, StorageObject object,
                                                      HttpEntity requestEntity, Map<String, String> requestParams) throws ServiceException {
            super.putObjectWithRequestEntityImpl(bucketName, object, requestEntity, requestParams);
        }

        @Override
        public void verifyExpectedAndActualETagValues(String expectedETag, StorageObject uploadedObject) throws ServiceException {
            if(StringUtils.isBlank(uploadedObject.getETag())) {
                log.warn("No ETag to verify");
                return;
            }
            super.verifyExpectedAndActualETagValues(expectedETag, uploadedObject);
        }

        /**
         * @return the identifier for the signature algorithm.
         */
        @Override
        protected String getSignatureIdentifier() {
            return S3Session.this.getSignatureIdentifier();
        }

        /**
         * @return header prefix for general Google Storage headers: x-goog-.
         */
        @Override
        public String getRestHeaderPrefix() {
            return S3Session.this.getRestHeaderPrefix();
        }

        /**
         * @return header prefix for Google Storage metadata headers: x-goog-meta-.
         */
        @Override
        public String getRestMetadataPrefix() {
            return S3Session.this.getRestMetadataPrefix();
        }

        @Override
        protected XmlResponsesSaxParser getXmlResponseSaxParser() throws ServiceException {
            return S3Session.this.getXmlResponseSaxParser();
        }

        @Override
        public void setBucketLoggingStatusImpl(String bucketName, StorageBucketLoggingStatus status) throws ServiceException {
            super.setBucketLoggingStatusImpl(bucketName, status);
        }

        @Override
        public StorageBucketLoggingStatus getBucketLoggingStatusImpl(String bucketName) throws ServiceException {
            return super.getBucketLoggingStatusImpl(bucketName);
        }

        @Override
        public WebsiteConfig getWebsiteConfigImpl(String bucketName) throws ServiceException {
            return super.getWebsiteConfigImpl(bucketName);
        }

        @Override
        public void setWebsiteConfigImpl(String bucketName, WebsiteConfig config) throws ServiceException {
            super.setWebsiteConfigImpl(bucketName, config);
        }

        @Override
        public void deleteWebsiteConfigImpl(String bucketName) throws ServiceException {
            super.deleteWebsiteConfigImpl(bucketName);
        }

        @Override
        public void authorizeHttpRequest(HttpUriRequest httpMethod, HttpContext context)
                throws ServiceException {
            if(authorize(httpMethod, credentials)) {
                return;
            }
            super.authorizeHttpRequest(httpMethod, context);
        }

        @Override
        protected boolean isRecoverable403(HttpUriRequest httpRequest, Exception exception) {
            if(this.credentials instanceof OAuth2Credentials) {
                OAuth2Tokens tokens;
                try {
                    tokens = ((OAuth2Credentials) this.credentials).getOAuth2Tokens();
                }
                catch(IOException e) {
                    return false;
                }
                if(tokens != null) {
                    tokens.expireAccessToken();
                    return true;
                }
            }
            return super.isRecoverable403(httpRequest, exception);
        }

        @Override
        protected StorageBucket createBucketImpl(String bucketName, String location,
                                                 AccessControlList acl) throws ServiceException {
            if(StringUtils.isNotBlank(getProjectId())) {
                return super.createBucketImpl(bucketName, location, acl,
                        Collections.<String, Object>singletonMap("x-goog-project-id", getProjectId()));
            }
            return super.createBucketImpl(bucketName, location, acl);
        }

        @Override
        protected StorageBucket[] listAllBucketsImpl() throws ServiceException {
            if(StringUtils.isNotBlank(getProjectId())) {
                return super.listAllBucketsImpl(
                        Collections.<String, Object>singletonMap("x-goog-project-id", getProjectId()));
            }
            return super.listAllBucketsImpl();
        }
    }

    protected boolean authorize(HttpUriRequest httpMethod, ProviderCredentials credentials)
            throws ServiceException {
        return false;
    }

    protected XmlResponsesSaxParser getXmlResponseSaxParser() throws ServiceException {
        return new XmlResponsesSaxParser(configuration, false);
    }

    /**
     * @return the identifier for the signature algorithm.
     */
    protected String getSignatureIdentifier() {
        return "AWS";
    }

    /**
     * @return header prefix for general Google Storage headers: x-goog-.
     */
    protected String getRestHeaderPrefix() {
        return "x-amz-";
    }

    /**
     * @return header prefix for Google Storage metadata headers: x-goog-meta-.
     */
    protected String getRestMetadataPrefix() {
        return "x-amz-meta-";
    }

    protected String getProjectId() {
        return null;
    }

    @Override
    protected void configure(AbstractHttpClient client) {
        super.configure(client);
        // Activates 'Expect: 100-Continue' handshake for the entity enclosing methods
        HttpProtocolParams.setUseExpectContinue(client.getParams(), true);
    }

    /**
     *
     */
    protected Jets3tProperties configuration = new Jets3tProperties();

    /**
     * @return Client configuration
     */
    protected Jets3tProperties getProperties() {
        return configuration;
    }

    protected void configure(final String hostname) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Configure for endpoint %s", hostname));
        }
        if(StringUtils.isNotBlank(host.getProtocol().getDefaultHostname())
                && hostname.endsWith(host.getProtocol().getDefaultHostname())) {
            // The user specified a DNS bucket endpoint. Connect to the default hostname instead.
            configuration.setProperty("s3service.s3-endpoint", host.getProtocol().getDefaultHostname());
            configuration.setProperty("s3service.enable-storage-classes", String.valueOf(true));
        }
        else {
            // Standard configuration
            configuration.setProperty("s3service.s3-endpoint", hostname);
            configuration.setProperty("s3service.disable-dns-buckets", String.valueOf(true));
            configuration.setProperty("s3service.enable-storage-classes", String.valueOf(false));
        }
        if(StringUtils.isNotBlank(host.getProtocol().getContext())) {
            configuration.setProperty("s3service.s3-endpoint-virtual-path", PathNormalizer.normalize(host.getProtocol().getContext()));
        }
        configuration.setProperty("s3service.s3-endpoint-http-port", String.valueOf(host.getPort()));
        configuration.setProperty("s3service.s3-endpoint-https-port", String.valueOf(host.getPort()));
        configuration.setProperty("s3service.https-only", String.valueOf(host.getProtocol().isSecure()));
        // The maximum number of retries that will be attempted when an S3 connection fails
        // with an InternalServer error. To disable retries of InternalError failures, set this to 0.
        configuration.setProperty("s3service.internal-error-retry-max", String.valueOf(0));
        // The maximum number of concurrent communication threads that will be started by
        // the multi-threaded service for upload and download operations.
        configuration.setProperty("s3service.max-thread-count", String.valueOf(1));
    }

    @Override
    public String getHostnameForContainer(final String bucket) {
        if(configuration.getBoolProperty("s3service.disable-dns-buckets", false)) {
            return this.getHost().getHostname(true);
        }
        if(!ServiceUtils.isBucketNameValidDNSName(bucket)) {
            return this.getHost().getHostname(true);
        }
        if(this.getHost().getHostname().equals(this.getHost().getProtocol().getDefaultHostname())) {
            return String.format("%s.%s", bucket, this.getHost().getHostname(true));
        }
        return this.getHost().getHostname(true);
    }

    /**
     * Caching the user's buckets
     */
    private Map<String, StorageBucket> buckets
            = new HashMap<String, StorageBucket>();

    /**
     * @param reload Disregard cache
     * @return List of buckets
     * @throws ServiceException Error response
     * @throws IOException      I/O failure
     */
    protected List<StorageBucket> getBuckets(final boolean reload) throws IOException, ServiceException {
        if(buckets.isEmpty() || reload) {
            buckets.clear();
            for(StorageBucket b : new S3BucketListService().list(this)) {
                buckets.put(b.getName(), b);
            }
            if(reload) {
                loggingStatus.clear();
                lifecycleStatus.clear();
                versioningStatus.clear();
                this.cdn().clear();
            }
        }
        return new ArrayList<StorageBucket>(buckets.values());
    }

    /**
     * @param bucketname Name of bucket
     * @return Cached bucket
     * @throws IOException I/O failure
     */
    protected StorageBucket getBucket(final String bucketname) throws IOException {
        try {
            for(StorageBucket bucket : this.getBuckets(false)) {
                if(bucket.getName().equals(bucketname)) {
                    return bucket;
                }
            }
        }
        catch(ServiceException e) {
            this.error("Cannot read container configuration", e);
        }
        throw new ConnectionCanceledException(String.format("Bucket not found with name %s", bucketname));
    }

    /**
     * Set to false if permission error response indicates this
     * feature is not implemented.
     */
    private boolean bucketLocationSupported = true;

    @Override
    public boolean isLocationSupported() {
        return bucketLocationSupported;
    }

    protected void setBucketLocationSupported(boolean bucketLocationSupported) {
        this.bucketLocationSupported = bucketLocationSupported;
    }

    /**
     * @return Bucket geographical location
     */
    @Override
    public String getLocation(final String container) {
        if(this.isLocationSupported()) {
            try {
                final S3Bucket bucket = (S3Bucket) this.getBucket(container);
                if(bucket.isLocationKnown()) {
                    return bucket.getLocation();
                }
                if(this.getHost().getCredentials().isAnonymousLogin()) {
                    log.info("Anonymous cannot access bucket location");
                    return null;
                }
                this.check();
                String location = this.getClient().getBucketLocation(container);
                if(StringUtils.isBlank(location)) {
                    location = "US"; //Default location US is null
                }
                // Cache location
                bucket.setLocation(location);
                return location;
            }
            catch(ServiceException e) {
                log.warn("Bucket location not supported:" + e.getMessage());
                this.setBucketLocationSupported(false);
            }
            catch(IOException e) {
                this.error("Cannot read container configuration", e);
            }
        }
        return null;
    }

    @Override
    protected void connect() throws IOException {
        if(this.isConnected()) {
            return;
        }
        this.fireConnectionWillOpenEvent();

        // Configure connection options
        this.configure(host.getHostname());

        // Prompt the login credentials first
        this.login();

        this.fireConnectionDidOpenEvent();
    }

    @Override
    protected void login(final LoginController controller, final Credentials credentials) throws IOException {
        try {
            this.client = new RequestEntityRestStorageService(this.getProviderCredentials(credentials));
            for(StorageBucket bucket : this.getBuckets(true)) {
                if(log.isDebugEnabled()) {
                    log.debug("Bucket:" + bucket);
                }
            }
        }
        catch(ServiceException e) {
            if(this.isLoginFailure(e)) {
                this.message(Locale.localizedString("Login failed", "Credentials"));
                controller.fail(host.getProtocol(), credentials);
                this.login();
            }
            else {
                IOException failure = new IOException(e.getMessage());
                failure.initCause(e);
                throw failure;
            }
        }
    }

    protected ProviderCredentials getProviderCredentials(final Credentials credentials) {
        return credentials.isAnonymousLogin() ? null : new AWSCredentials(credentials.getUsername(),
                credentials.getPassword());
    }

    /**
     * Prompt for MFA credentials
     *
     * @param controller Prompt controller
     * @return MFA one time authentication password.
     * @throws ConnectionCanceledException Prompt dismissed
     */
    protected Credentials mfa(final LoginController controller) throws ConnectionCanceledException {
        final Credentials credentials = new Credentials(
                Preferences.instance().getProperty("s3.mfa.serialnumber"), null, false) {
            @Override
            public String getUsernamePlaceholder() {
                return Locale.localizedString("MFA Serial Number", "S3");
            }

            @Override
            public String getPasswordPlaceholder() {
                return Locale.localizedString("MFA Authentication Code", "S3");
            }
        };
        // Prompt for MFA credentials.
        controller.prompt(host.getProtocol(), credentials,
                Locale.localizedString("Provide additional login credentials", "Credentials"),
                Locale.localizedString("Multi-Factor Authentication", "S3"), false, false, false);

        Preferences.instance().setProperty("s3.mfa.serialnumber", credentials.getUsername());
        return credentials;
    }

    /**
     * Check for Invalid Access ID or Invalid Secret Key
     *
     * @param e Error response
     * @return True if the error code of the S3 exception is a login failure
     */
    protected boolean isLoginFailure(final ServiceException e) {
        if(403 == e.getResponseCode()) {
            return true;
        }
        if(null == e.getErrorCode()) {
            return false;
        }
        return e.getErrorCode().equals("InvalidAccessKeyId") // Invalid Access ID
                || e.getErrorCode().equals("SignatureDoesNotMatch"); // Invalid Secret Key
    }

    @Override
    public void close() {
        try {
            if(this.isConnected()) {
                this.fireConnectionWillCloseEvent();
                super.close();
            }
        }
        finally {
            // No logout required
            client = null;
            this.fireConnectionDidCloseEvent();
        }
    }

    /**
     * @return True
     */
    @Override
    public boolean isDownloadResumable() {
        return true;
    }

    /**
     * @return No Content-Range support
     */
    @Override
    public boolean isUploadResumable() {
        return false;
    }

    @Override
    public boolean isAclSupported() {
        return true;
    }

    public boolean isMultipartUploadSupported() {
        if(host.getHostname().endsWith(Protocol.S3_SSL.getDefaultHostname())) {
            return Preferences.instance().getBoolean("s3.upload.multipart");
        }
        return false;
    }

    @Override
    public boolean isRenameSupported(final Path file) {
        return !file.attributes().isVolume();
    }

    @Override
    public List<String> getSupportedStorageClasses() {
        return Arrays.asList(S3Object.STORAGE_CLASS_STANDARD,
                S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY,
                "GLACIER");
    }

    @Override
    public List<String> getSupportedEncryptionAlgorithms() {
        return Arrays.asList("AES256");
    }

    /**
     * Set to false if permission error response indicates this
     * feature is not implemented.
     */
    private boolean loggingSupported = true;

    /**
     * @return True if the service supports bucket logging.
     */
    @Override
    public boolean isLoggingSupported() {
        return loggingSupported;
    }

    @Override
    public boolean isAnalyticsSupported() {
        // Only for AWS
        return this.getHost().getHostname().equals(Protocol.S3_SSL.getDefaultHostname());
    }

    protected void setLoggingSupported(boolean loggingSupported) {
        this.loggingSupported = loggingSupported;
    }

    @Override
    public boolean isChecksumSupported() {
        return true;
    }

    /**
     * Cache versioning status result.
     */
    protected Map<String, StorageBucketLoggingStatus> loggingStatus
            = new HashMap<String, StorageBucketLoggingStatus>();

    private void readLogging(String container) {
        if(!loggingStatus.containsKey(container)) {
            try {
                if(this.getHost().getCredentials().isAnonymousLogin()) {
                    log.info("Anonymous cannot access logging status");
                    return;
                }
                this.check();
                final StorageBucketLoggingStatus status
                        = this.getClient().getBucketLoggingStatusImpl(container);
                loggingStatus.put(container, status);
            }
            catch(ServiceException e) {
                log.warn("Bucket logging not supported:" + e.getMessage());
                this.setLoggingSupported(false);
            }
            catch(IOException e) {
                this.error("Cannot read container configuration", e);
            }
        }
    }

    /**
     * @param container The bucket name
     * @return True if the bucket logging status is enabled.
     */
    @Override
    public boolean isLogging(final String container) {
        if(this.isLoggingSupported()) {
            this.readLogging(container);
            if(loggingStatus.containsKey(container)) {
                return loggingStatus.get(container).isLoggingEnabled();
            }
        }
        return false;
    }

    /**
     * @param container The bucket name
     * @return Null if bucket logging is not supported
     */
    @Override
    public String getLoggingTarget(final String container) {
        if(this.isLoggingSupported()) {
            this.readLogging(container);
            if(loggingStatus.containsKey(container)) {
                return loggingStatus.get(container).getTargetBucketName();
            }
            return container;
        }
        return null;
    }

    /**
     * @param container   The bucket name
     * @param enabled     True if logging should be toggled on
     * @param destination Logging bucket name or null to choose container itself as target
     */
    @Override
    public void setLogging(final String container, final boolean enabled, final String destination) {
        if(this.isLoggingSupported()) {
            try {
                // Logging target bucket
                final S3BucketLoggingStatus status = new S3BucketLoggingStatus(
                        StringUtils.isNotBlank(destination) ? destination : container, null);
                if(enabled) {
                    status.setLogfilePrefix(Preferences.instance().getProperty("s3.logging.prefix"));
                }
                this.check();
                this.getClient().setBucketLoggingStatus(container, status, true);
            }
            catch(ServiceException e) {
                this.error("Cannot write file attributes", e);
            }
            catch(IOException e) {
                this.error("Cannot write file attributes", e);
            }
            finally {
                loggingStatus.remove(container);
            }
        }
    }

    public Integer getTransition(final String container) {
        if(this.isLifecycleSupported()) {
            this.readLifecycle(container);
            if(lifecycleStatus.containsKey(container)) {
                for(LifecycleConfig.Rule rule : lifecycleStatus.get(container).getRules()) {
                    if(rule.getTransition() != null) {
                        if(rule.getTransition().getDays() != null) {
                            return rule.getTransition().getDays();
                        }
                    }
                }
            }
        }
        return null;
    }

    public Integer getExpiration(final String container) {
        if(this.isLifecycleSupported()) {
            this.readLifecycle(container);
            if(lifecycleStatus.containsKey(container)) {
                for(LifecycleConfig.Rule rule : lifecycleStatus.get(container).getRules()) {
                    if(rule.getExpiration() != null) {
                        if(rule.getExpiration().getDays() != null) {
                            return rule.getExpiration().getDays();
                        }
                    }
                }
            }
        }
        return null;
    }

    protected Map<String, LifecycleConfig> lifecycleStatus
            = new HashMap<String, LifecycleConfig>();

    protected void readLifecycle(final String container) {
        if(this.isLifecycleSupported()) {
            if(!lifecycleStatus.containsKey(container)) {
                try {
                    if(this.getHost().getCredentials().isAnonymousLogin()) {
                        log.info("Anonymous cannot access logging status");
                        return;
                    }
                    this.check();
                    final LifecycleConfig status = this.getClient().getLifecycleConfig(container);
                    if(null != status) {
                        lifecycleStatus.put(container, status);
                    }
                }
                catch(ServiceException e) {
                    log.warn("Bucket logging not supported:" + e.getMessage());
                    this.setLoggingSupported(false);
                }
                catch(IOException e) {
                    this.error("Cannot read container configuration", e);
                }
            }
        }
    }

    /**
     * @param container  The bucket name
     * @param transition Days Null to disable
     * @param expiration Days Null to disable
     */
    public void setLifecycle(final String container, final Integer transition, final Integer expiration) {
        if(this.isLifecycleSupported()) {
            try {
                this.check();
                if(transition != null || expiration != null) {
                    final LifecycleConfig config = new LifecycleConfig();
                    // Unique identifier for the rule. The value cannot be longer than 255 characters. When you specify an empty prefix, the rule applies to all objects in the bucket
                    final LifecycleConfig.Rule rule = config.newRule(UUID.randomUUID().toString(), StringUtils.EMPTY, true);
                    if(transition != null) {
                        rule.newTransition().setDays(transition);
                    }
                    if(expiration != null) {
                        rule.newExpiration().setDays(expiration);
                    }
                    if(!config.equals(lifecycleStatus.get(container))) {
                        this.getClient().setLifecycleConfig(container, config);
                    }
                }
                else {
                    this.getClient().deleteLifecycleConfig(container);
                }
            }
            catch(ServiceException e) {
                this.error("Cannot write file attributes", e);
            }
            catch(IOException e) {
                this.error("Cannot write file attributes", e);
            }
            finally {
                lifecycleStatus.remove(container);
            }
        }
    }

    /**
     * @return True if the service supports object versioning.
     */
    @Override
    public boolean isVersioningSupported() {
        return true;
    }

    @Override
    public boolean isLifecycleSupported() {
        return true;
    }

    @Override
    public boolean isRevertSupported() {
        return this.isVersioningSupported();
    }

    /**
     * Cache versioning status result.
     */
    private Map<String, S3BucketVersioningStatus> versioningStatus
            = new HashMap<String, S3BucketVersioningStatus>();

    /**
     * @param container The bucket name
     * @return True if enabled
     */
    @Override
    public boolean isVersioning(final String container) {
        if(this.isVersioningSupported()) {
            if(!versioningStatus.containsKey(container)) {
                try {
                    this.check();
                    final S3BucketVersioningStatus status
                            = this.getClient().getBucketVersioningStatus(container);
                    versioningStatus.put(container, status);
                }
                catch(ServiceException e) {
                    this.error("Cannot read container configuration", e);
                }
                catch(IOException e) {
                    this.error("Cannot read container configuration", e);
                }
            }
            if(versioningStatus.containsKey(container)) {
                return versioningStatus.get(container).isVersioningEnabled();
            }
        }
        return false;
    }

    /**
     * @param container The bucket name
     * @return True if MFA is required to delete objects in this bucket.
     */
    @Override
    public boolean isMultiFactorAuthentication(final String container) {
        if(this.isVersioning(container)) {
            return versioningStatus.get(container).isMultiFactorAuthDeleteRequired();
        }
        return false;
    }

    /**
     * @param container  The bucket name
     * @param mfa        Multi factor authentication
     * @param versioning True if enabled
     */
    @Override
    public void setVersioning(final String container, final boolean mfa, final boolean versioning) {
        if(this.isVersioningSupported()) {
            try {
                this.check();
                if(this.isMultiFactorAuthentication(container)) {
                    // The bucket is already MFA protected.
                    LoginController c = LoginControllerFactory.get(this);
                    final Credentials auth = this.mfa(c);
                    if(versioning) {
                        if(this.isVersioning(container)) {
                            log.debug("Versioning already enabled for bucket " + container);
                        }
                        else {
                            // Enable versioning if not already active.
                            log.debug("Enable bucket versioning with MFA " + auth.getUsername() + " for " + container);
                            this.getClient().enableBucketVersioningWithMFA(container,
                                    auth.getUsername(), auth.getPassword());
                        }
                    }
                    else {
                        log.debug("Suspend bucket versioning with MFA " + auth.getUsername() + " for " + container);
                        this.getClient().suspendBucketVersioningWithMFA(container,
                                auth.getUsername(), auth.getPassword());
                    }
                    if(versioning && !mfa) {
                        log.debug("Disable MFA " + auth.getUsername() + " for " + container);
                        // User has choosen to disable MFA
                        final Credentials auth2 = this.mfa(c);
                        this.getClient().disableMFAForVersionedBucket(container,
                                auth2.getUsername(), auth2.getPassword());
                    }
                }
                else {
                    if(versioning) {
                        if(mfa) {
                            LoginController c = LoginControllerFactory.get(this);
                            final Credentials auth = this.mfa(c);
                            log.debug("Enable bucket versioning with MFA " + auth.getUsername() + " for " + container);
                            this.getClient().enableBucketVersioningWithMFA(container,
                                    auth.getUsername(), auth.getPassword());
                        }
                        else {
                            if(this.isVersioning(container)) {
                                log.debug("Versioning already enabled for bucket " + container);
                            }
                            else {
                                log.debug("Enable bucket versioning for " + container);
                                this.getClient().enableBucketVersioning(container);
                            }
                        }
                    }
                    else {
                        log.debug("Susped bucket versioning for " + container);
                        this.getClient().suspendBucketVersioning(container);
                    }
                }
            }
            catch(ServiceException e) {
                this.error("Cannot write file attributes", e);
            }
            catch(IOException e) {
                this.error("Cannot write file attributes", e);
            }
            finally {
                versioningStatus.remove(container);
            }
        }
    }

    protected AccessControlList getPrivateCannedAcl() {
        return AccessControlList.REST_CANNED_PRIVATE;
    }

    protected AccessControlList getPublicCannedReadAcl() {
        return AccessControlList.REST_CANNED_PUBLIC_READ;
    }

    @Override
    public List<Acl.Role> getAvailableAclRoles(final List<Path> files) {
        return Arrays.asList(new Acl.Role(org.jets3t.service.acl.Permission.PERMISSION_FULL_CONTROL.toString()),
                new Acl.Role(org.jets3t.service.acl.Permission.PERMISSION_READ.toString()),
                new Acl.Role(org.jets3t.service.acl.Permission.PERMISSION_WRITE.toString()),
                new Acl.Role(org.jets3t.service.acl.Permission.PERMISSION_READ_ACP.toString()),
                new Acl.Role(org.jets3t.service.acl.Permission.PERMISSION_WRITE_ACP.toString()));
    }

    @Override
    public List<Acl.User> getAvailableAclUsers() {
        final List<Acl.User> users = new ArrayList<Acl.User>(Arrays.asList(
                new Acl.CanonicalUser(),
                new Acl.GroupUser(GroupGrantee.ALL_USERS.getIdentifier(), false),
                new Acl.EmailUser() {
                    @Override
                    public String getPlaceholder() {
                        return Locale.localizedString("Amazon Customer Email Address", "S3");
                    }
                })
        );
        for(final StorageBucket container : buckets.values()) {
            final StorageOwner owner = container.getOwner();
            if(null == owner) {
                log.warn(String.format("Owner not known for container %s", container));
                continue;
            }
            final Acl.CanonicalUser user = new Acl.CanonicalUser(owner.getId(), owner.getDisplayName(), false) {
                @Override
                public String getPlaceholder() {
                    return this.getDisplayName() + " (" + Locale.localizedString("Owner") + ")";
                }
            };
            if(users.contains(user)) {
                continue;
            }
            users.add(0, user);
        }
        return users;
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
    protected String getWebsiteEndpoint(final String bucket) {
        // Geographical location
        final String location = this.getLocation(bucket);
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
        return String.format("%s.%s", bucket, endpoint);
    }

    /**
     * Delegating CloudFront requests.
     */
    private DistributionConfiguration cf;

    @Override
    public DistributionConfiguration cdn() {
        if(host.getHostname().endsWith(Protocol.S3_SSL.getDefaultHostname())) {
            if(null == cf) {
                cf = new WebsiteCloudFrontDistributionConfiguration();
            }
        }
        else {
            // Amazon CloudFront custom origin
            return super.cdn();
        }
        return cf;
    }

    /**
     *
     */
    private class WebsiteCloudFrontDistributionConfiguration extends CloudFrontDistributionConfiguration {

        public WebsiteCloudFrontDistributionConfiguration() {
            super(LoginControllerFactory.get(S3Session.this), S3Session.this.host.getCredentials(),
                    new ErrorListener() {
                        @Override
                        public void error(BackgroundException exception) {
                            S3Session.this.error(exception);
                        }
                    },
                    new ProgressListener() {
                        @Override
                        public void message(String message) {
                            S3Session.this.message(message);
                        }
                    },
                    new TranscriptListener() {
                        @Override
                        public void log(boolean request, String message) {
                            S3Session.this.log(request, message);
                        }
                    }
            );
        }

        /**
         * Distribution methods supported by this S3 provider.
         *
         * @param container Origin bucket
         * @return Download and Streaming for AWS.
         */
        @Override
        public List<Distribution.Method> getMethods(final String container) {
            if(!ServiceUtils.isBucketNameValidDNSName(container)) {
                // Disable website configuration if bucket name is not DNS compatible
                return super.getMethods(container);
            }
            return Arrays.asList(Distribution.DOWNLOAD, Distribution.STREAMING, Distribution.WEBSITE, Distribution.WEBSITE_CDN);
        }

        @Override
        public String getName(final Distribution.Method method) {
            if(method.equals(Distribution.WEBSITE)) {
                return method.toString();
            }
            return super.getName(method);
        }

        @Override
        public String getOrigin(final Distribution.Method method, final String container) {
            if(method.equals(Distribution.WEBSITE)) {
                return S3Session.this.getHostnameForContainer(container);
            }
            if(method.equals(Distribution.WEBSITE_CDN)) {
                return S3Session.this.getWebsiteEndpoint(container);
            }
            return super.getOrigin(method, container);
        }

        @Override
        protected List<String> getContainers() {
            return new ArrayList<String>(buckets.keySet());
        }

        @Override
        public Distribution read(final String origin, final Distribution.Method method) {
            if(method.equals(Distribution.WEBSITE)) {
                final String bucket = S3Session.this.getContainerForHostname(origin);
                // Website Endpoint URL
                final String url = String.format("%s://%s", method.getScheme(), S3Session.this.getWebsiteEndpoint(bucket));
                if(!distributionStatus.get(method).containsKey(origin)) {
                    try {
                        S3Session.this.check();

                        try {
                            final WebsiteConfig configuration = S3Session.this.getClient().getWebsiteConfig(bucket);
                            final Distribution distribution = new Distribution(
                                    null,
                                    origin,
                                    method,
                                    configuration.isWebsiteConfigActive(),
                                    configuration.isWebsiteConfigActive(),
                                    // http://example-bucket.s3-website-us-east-1.amazonaws.com/
                                    url,
                                    Locale.localizedString("Deployed", "S3"),
                                    new String[]{},
                                    false,
                                    configuration.getIndexDocumentSuffix());
                            // Cache website configuration
                            distributionStatus.get(method).put(origin, distribution);
                        }
                        catch(ServiceException e) {
                            // Not found. Website configuration not enbabled.
                            String status = Locale.localizedString(e.getErrorCode());
                            if(status.equals(e.getErrorCode())) {
                                // No localization found. Use english text
                                status = e.getErrorMessage();
                            }
                            final Distribution distribution = new Distribution(null, origin, method, false, url, status);
                            distributionStatus.get(method).put(origin, distribution);
                        }
                    }
                    catch(IOException e) {
                        this.error("Cannot read website configuration", e);
                    }
                }
            }
            return super.read(origin, method);
        }

        @Override
        public void write(final boolean enabled, final String origin, final Distribution.Method method,
                          final String[] cnames, final boolean logging, final String loggingBucket, final String defaultRootObject) {
            if(method.equals(Distribution.WEBSITE)) {
                try {
                    S3Session.this.check();
                    // Configure Website Index Document
                    StringBuilder name = new StringBuilder(Locale.localizedString("Website", "S3")).append(" ").append(method.toString());
                    if(enabled) {
                        this.message(MessageFormat.format(Locale.localizedString("Enable {0} Distribution", "Status"), name));
                    }
                    else {
                        this.message(MessageFormat.format(Locale.localizedString("Disable {0} Distribution", "Status"), name));
                    }
                    final String bucket = S3Session.this.getContainerForHostname(origin);
                    if(enabled) {
                        String suffix = "index.html";
                        if(StringUtils.isNotBlank(defaultRootObject)) {
                            suffix = FilenameUtils.getName(defaultRootObject);
                        }
                        // Enable website endpoint
                        S3Session.this.getClient().setWebsiteConfig(bucket, new S3WebsiteConfig(suffix));
                    }
                    else {
                        // Disable website endpoint
                        S3Session.this.getClient().deleteWebsiteConfig(bucket);
                    }
                }
                catch(IOException e) {
                    this.error("Cannot write website configuration", e);
                }
                catch(S3ServiceException e) {
                    this.error("Cannot write website configuration", e);
                }
                finally {
                    distributionStatus.get(method).clear();
                }
            }
            else {
                super.write(enabled, origin, method, cnames, logging, loggingBucket, defaultRootObject);
            }
        }

        @Override
        protected CustomOrigin getCustomOriginConfiguration(final String id,
                                                            final Distribution.Method method,
                                                            final String origin) {
            if(method.equals(Distribution.WEBSITE_CDN)) {
                return new CustomOrigin(id, origin, CustomOrigin.OriginProtocolPolicy.HTTP_ONLY);
            }
            return super.getCustomOriginConfiguration(id, method, origin);
        }

        @Override
        public boolean isDefaultRootSupported(final Distribution.Method method) {
            if(method.equals(Distribution.WEBSITE)) {
                return true;
            }
            return super.isDefaultRootSupported(method);
        }

        @Override
        public boolean isLoggingSupported(final Distribution.Method method) {
            if(method.equals(Distribution.WEBSITE)) {
                return false;
            }
            return super.isLoggingSupported(method);
        }
    }

    @Override
    public IdentityConfiguration iam() {
        return new AWSIdentityConfiguration(this.getHost(), new ErrorListener() {
            @Override
            public void error(BackgroundException exception) {
                S3Session.this.error(exception);
            }
        });
    }

    @Override
    public AnalyticsProvider analytics() {
        return new QloudstatAnalyticsProvider();
    }
}