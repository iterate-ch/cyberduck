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
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cloud.CloudSession;
import ch.cyberduck.core.cloudfront.WebsiteCloudFrontDistributionConfiguration;
import ch.cyberduck.core.exception.ServiceExceptionMappingService;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.identity.AWSIdentityConfiguration;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.acl.GroupGrantee;
import org.jets3t.service.impl.rest.XmlResponsesSaxParser;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.LifecycleConfig;
import org.jets3t.service.model.S3BucketLoggingStatus;
import org.jets3t.service.model.S3BucketVersioningStatus;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageBucket;
import org.jets3t.service.model.StorageBucketLoggingStatus;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.model.StorageOwner;
import org.jets3t.service.model.WebsiteConfig;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.security.OAuth2Credentials;
import org.jets3t.service.security.OAuth2Tokens;
import org.jets3t.service.security.ProviderCredentials;
import org.jets3t.service.utils.RestUtils;
import org.jets3t.service.utils.ServiceUtils;

import java.io.IOException;
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
public class S3Session extends CloudSession<S3Session.RequestEntityRestStorageService> {
    private static final Logger log = Logger.getLogger(S3Session.class);

    protected RequestEntityRestStorageService client;

    public S3Session(Host h) {
        super(h);
    }

    /**
     * Exposing protected methods
     */
    public class RequestEntityRestStorageService extends RestS3Service {
        public RequestEntityRestStorageService(final Jets3tProperties configuration) {
            super(null, new PreferencesUseragentProvider().get(), null, configuration);
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
            if(authorize(httpMethod, getProviderCredentials())) {
                return;
            }
            super.authorizeHttpRequest(httpMethod, context);
        }

        @Override
        protected boolean isRecoverable403(HttpUriRequest httpRequest, Exception exception) {
            if(getProviderCredentials() instanceof OAuth2Credentials) {
                OAuth2Tokens tokens;
                try {
                    tokens = ((OAuth2Credentials) getProviderCredentials()).getOAuth2Tokens();
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
        return new XmlResponsesSaxParser(client.getJetS3tProperties(), false);
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

    protected Jets3tProperties configure(final String hostname) {
        final Jets3tProperties configuration = new Jets3tProperties();
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
        return configuration;
    }

    @Override
    public String getHostnameForContainer(final Path bucket) {
        if(this.configure(this.getHost().getHostname(true)).getBoolProperty("s3service.disable-dns-buckets", false)) {
            return this.getHost().getHostname(true);
        }
        if(!ServiceUtils.isBucketNameValidDNSName(bucket.getContainer().getName())) {
            return this.getHost().getHostname(true);
        }
        if(this.getHost().getHostname().equals(this.getHost().getProtocol().getDefaultHostname())) {
            return String.format("%s.%s", bucket.getName(), this.getHost().getHostname(true));
        }
        return this.getHost().getHostname(true);
    }

    /**
     * Caching the user's buckets
     */
    private Map<Path, StorageBucket> buckets = new HashMap<Path, StorageBucket>();

    /**
     * @param reload Disregard cache
     * @return List of buckets
     */
    public List<Path> getContainers(final boolean reload) throws BackgroundException {
        if(buckets.isEmpty() || reload) {
            buckets.clear();
            for(StorageBucket bucket : new S3BucketListService().list(this)) {
                final S3Path container = new S3Path(this, bucket.getName(), Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
                if(null != bucket.getOwner()) {
                    container.attributes().setOwner(bucket.getOwner().getDisplayName());
                }
                if(null != bucket.getCreationDate()) {
                    container.attributes().setCreationDate(bucket.getCreationDate().getTime());
                }
                buckets.put(container, bucket);
            }
            if(reload) {
                loggingStatus.clear();
                lifecycleStatus.clear();
                versioningStatus.clear();
            }
        }
        return new ArrayList<Path>(buckets.keySet());
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

    @Override
    public String getLocation(final Path container) throws BackgroundException {
        if(this.isLocationSupported()) {
            try {
                if(null == container.attributes().getRegion()) {
                    if(this.getHost().getCredentials().isAnonymousLogin()) {
                        log.info("Anonymous cannot access bucket location");
                        return null;
                    }
                    String location = client.getBucketLocation(container.getContainer().getName());
                    if(StringUtils.isBlank(location)) {
                        location = "US"; //Default location US is null
                    }
                    container.attributes().setRegion(location);
                }
                return container.attributes().getRegion();
            }
            catch(ServiceException e) {
                log.warn("Bucket location not supported:" + e.getMessage());
                this.setBucketLocationSupported(false);
            }
        }
        return null;
    }

    @Override
    public RequestEntityRestStorageService connect() throws BackgroundException {
        client = new RequestEntityRestStorageService(configure(host.getHostname())) {
            @Override
            public ProviderCredentials getProviderCredentials() {
                return S3Session.this.getProviderCredentials(host.getCredentials());
            }
        };
        return client;
    }

    @Override
    public void login(final LoginController prompt) throws BackgroundException {
        for(Path bucket : this.getContainers(true)) {
            if(log.isDebugEnabled()) {
                log.debug("Bucket:" + bucket);
            }
        }
    }

    protected ProviderCredentials getProviderCredentials(final Credentials credentials) {
        if(credentials.isAnonymousLogin()) {
            return null;
        }
        return new AWSCredentials(credentials.getUsername(), credentials.getPassword()) {
            @Override
            public String getAccessKey() {
                return host.getCredentials().getUsername();
            }

            @Override
            public String getSecretKey() {
                return host.getCredentials().getPassword();
            }
        };
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
    protected Map<Path, StorageBucketLoggingStatus> loggingStatus
            = new HashMap<Path, StorageBucketLoggingStatus>();

    private void readLogging(final Path container) throws BackgroundException {
        if(!loggingStatus.containsKey(container)) {
            try {
                if(this.getHost().getCredentials().isAnonymousLogin()) {
                    log.info("Anonymous cannot access logging status");
                    return;
                }
                final StorageBucketLoggingStatus status
                        = client.getBucketLoggingStatusImpl(container.getName());
                loggingStatus.put(container, status);
            }
            catch(ServiceException e) {
                log.warn("Bucket logging not supported:" + e.getMessage());
                this.setLoggingSupported(false);
            }
        }
    }

    /**
     * @param container The bucket name
     * @return True if the bucket logging status is enabled.
     */
    @Override
    public boolean isLogging(final Path container) throws BackgroundException {
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
    public String getLoggingTarget(final Path container) throws BackgroundException {
        if(this.isLoggingSupported()) {
            this.readLogging(container);
            if(loggingStatus.containsKey(container)) {
                return loggingStatus.get(container).getTargetBucketName();
            }
            return container.getName();
        }
        return null;
    }

    /**
     * @param container   The bucket name
     * @param enabled     True if logging should be toggled on
     * @param destination Logging bucket name or null to choose container itself as target
     */
    @Override
    public void setLogging(final Path container, final boolean enabled, final String destination) throws BackgroundException {
        if(this.isLoggingSupported()) {
            try {
                // Logging target bucket
                final S3BucketLoggingStatus status = new S3BucketLoggingStatus(
                        StringUtils.isNotBlank(destination) ? destination : container.getName(), null);
                if(enabled) {
                    status.setLogfilePrefix(Preferences.instance().getProperty("s3.logging.prefix"));
                }
                client.setBucketLoggingStatus(container.getName(), status, true);
            }
            catch(ServiceException e) {
                throw new ServiceExceptionMappingService().map("Cannot write file attributes", e);
            }
            finally {
                loggingStatus.remove(container);
            }
        }
    }

    public Integer getTransition(final Path container) throws BackgroundException {
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

    public Integer getExpiration(final Path container) throws BackgroundException {
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

    protected Map<Path, LifecycleConfig> lifecycleStatus
            = new HashMap<Path, LifecycleConfig>();

    public void readLifecycle(final Path container) throws BackgroundException {
        if(this.isLifecycleSupported()) {
            if(!lifecycleStatus.containsKey(container)) {
                try {
                    if(this.getHost().getCredentials().isAnonymousLogin()) {
                        log.info("Anonymous cannot access logging status");
                        return;
                    }
                    final LifecycleConfig status = client.getLifecycleConfig(container.getName());
                    if(null != status) {
                        lifecycleStatus.put(container, status);
                    }
                }
                catch(ServiceException e) {
                    log.warn("Bucket logging not supported:" + e.getMessage());
                    this.setLoggingSupported(false);
                }
            }
        }
    }

    /**
     * @param container  The bucket name
     * @param transition Days Null to disable
     * @param expiration Days Null to disable
     */
    public void setLifecycle(final Path container, final Integer transition, final Integer expiration) throws BackgroundException {
        if(this.isLifecycleSupported()) {
            try {
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
                        client.setLifecycleConfig(container.getName(), config);
                    }
                }
                else {
                    client.deleteLifecycleConfig(container.getName());
                }
            }
            catch(ServiceException e) {
                throw new ServiceExceptionMappingService().map("Cannot write file attributes", e);
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
    private Map<Path, S3BucketVersioningStatus> versioningStatus
            = new HashMap<Path, S3BucketVersioningStatus>();

    /**
     * @param container The bucket name
     * @return True if enabled
     */
    @Override
    public boolean isVersioning(final Path container) throws BackgroundException {
        if(this.isVersioningSupported()) {
            if(!versioningStatus.containsKey(container)) {
                try {
                    final S3BucketVersioningStatus status
                            = client.getBucketVersioningStatus(container.getName());
                    versioningStatus.put(container, status);
                }
                catch(ServiceException e) {
                    throw new ServiceExceptionMappingService().map("Cannot read container configuration", e);
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
    public boolean isMultiFactorAuthentication(final Path container) throws BackgroundException {
        if(this.isVersioning(container)) {
            return versioningStatus.get(container).isMultiFactorAuthDeleteRequired();
        }
        return false;
    }

    /**
     * @param container  The bucket name
     * @param prompt     Login prompt for multi factor authentication
     * @param mfa        Multi factor authentication
     * @param versioning True if enabled
     */
    @Override
    public void setVersioning(final Path container, final LoginController prompt, final boolean mfa, final boolean versioning) throws BackgroundException {
        if(this.isVersioningSupported()) {
            try {
                if(this.isMultiFactorAuthentication(container)) {
                    // The bucket is already MFA protected.
                    final Credentials factor = this.mfa(prompt);
                    if(versioning) {
                        if(this.isVersioning(container)) {
                            log.debug("Versioning already enabled for bucket " + container);
                        }
                        else {
                            // Enable versioning if not already active.
                            log.debug("Enable bucket versioning with MFA " + factor.getUsername() + " for " + container);
                            client.enableBucketVersioningWithMFA(container.getName(),
                                    factor.getUsername(), factor.getPassword());
                        }
                    }
                    else {
                        log.debug("Suspend bucket versioning with MFA " + factor.getUsername() + " for " + container);
                        client.suspendBucketVersioningWithMFA(container.getName(),
                                factor.getUsername(), factor.getPassword());
                    }
                    if(versioning && !mfa) {
                        log.debug(String.format("Disable MFA %s for %s", factor.getUsername(), container));
                        // User has choosen to disable MFA
                        final Credentials factor2 = this.mfa(prompt);
                        client.disableMFAForVersionedBucket(container.getName(),
                                factor2.getUsername(), factor2.getPassword());
                    }
                }
                else {
                    if(versioning) {
                        if(mfa) {
                            final Credentials factor = this.mfa(prompt);
                            log.debug(String.format("Enable bucket versioning with MFA %s for %s", factor.getUsername(), container));
                            client.enableBucketVersioningWithMFA(container.getName(),
                                    factor.getUsername(), factor.getPassword());
                        }
                        else {
                            if(this.isVersioning(container)) {
                                log.debug(String.format("Versioning already enabled for bucket %s", container));
                            }
                            else {
                                log.debug(String.format("Enable bucket versioning for %s", container));
                                client.enableBucketVersioning(container.getName());
                            }
                        }
                    }
                    else {
                        log.debug(String.format("Susped bucket versioning for %s", container));
                        client.suspendBucketVersioning(container.getName());
                    }
                }
            }
            catch(ServiceException e) {
                throw new ServiceExceptionMappingService().map("Cannot write file attributes", e);
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

    @Override
    public DistributionConfiguration cdn(final LoginController prompt) {
        if(host.getHostname().endsWith(Protocol.S3_SSL.getDefaultHostname())) {
            return new WebsiteCloudFrontDistributionConfiguration(this);
        }
        else {
            // Amazon CloudFront custom origin
            return super.cdn(prompt);
        }
    }

    @Override
    public IdentityConfiguration iam() {
        return new AWSIdentityConfiguration(this.getHost());
    }

    @Override
    public AnalyticsProvider analytics() {
        return new QloudstatAnalyticsProvider();
    }
}