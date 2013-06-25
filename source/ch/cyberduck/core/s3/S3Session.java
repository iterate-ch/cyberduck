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
import ch.cyberduck.core.date.UserDateFormatterFactory;
import ch.cyberduck.core.exception.ServiceExceptionMappingService;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.identity.AWSIdentityConfiguration;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.lifecycle.LifecycleConfiguration;
import ch.cyberduck.core.logging.LoggingConfiguration;
import ch.cyberduck.core.threading.BackgroundException;
import ch.cyberduck.core.versioning.VersioningConfiguration;

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
import org.jets3t.service.model.WebsiteConfig;
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
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        configuration.setProperty("httpclient.proxy-autodetect", String.valueOf(false));
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

    @Override
    public String getLocation(final Path container) throws BackgroundException {
        if(this.getHost().getCredentials().isAnonymousLogin()) {
            log.info("Anonymous cannot access bucket location");
            return null;
        }
        try {
            if(null == container.attributes().getRegion()) {
                String location = client.getBucketLocation(container.getContainer().getName());
                if(StringUtils.isBlank(location)) {
                    location = "US"; //Default location US is null
                }
                container.attributes().setRegion(location);
            }
            return container.attributes().getRegion();
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot read container configuration", e);
        }
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
        for(Path bucket : new S3BucketListService().list(this)) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Found bucket %s", bucket));
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

    @Override
    public boolean isLocationSupported() {
        return true;
    }

    public boolean isMultipartUploadSupported() {
        // Only for AWS
        if(this.getHost().getHostname().equals(Protocol.S3_SSL.getDefaultHostname())) {
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
     * @return True if the service supports bucket logging.
     */
    @Override
    public boolean isLoggingSupported() {
        // Only for AWS
        return this.getHost().getHostname().equals(Protocol.S3_SSL.getDefaultHostname());
    }

    @Override
    public boolean isAnalyticsSupported() {
        // Only for AWS
        return this.getHost().getHostname().equals(Protocol.S3_SSL.getDefaultHostname());
    }

    @Override
    public boolean isChecksumSupported() {
        return true;
    }

    /**
     * @return True if the service supports object versioning.
     */
    @Override
    public boolean isVersioningSupported() {
        // Only for AWS
        return this.getHost().getHostname().equals(Protocol.S3_SSL.getDefaultHostname());
    }

    @Override
    public boolean isLifecycleSupported() {
        // Only for AWS
        return this.getHost().getHostname().equals(Protocol.S3_SSL.getDefaultHostname());
    }

    @Override
    public boolean isRevertSupported() {
        // Only for AWS
        return this.getHost().getHostname().equals(Protocol.S3_SSL.getDefaultHostname());
    }

    /**
     * @param container The bucket name
     * @return True if the bucket logging status is enabled.
     */
    @Override
    public LoggingConfiguration getLogging(final Path container) throws BackgroundException {
        if(this.getHost().getCredentials().isAnonymousLogin()) {
            log.info("Anonymous cannot access logging status");
            return new LoggingConfiguration(false);
        }
        try {
            final StorageBucketLoggingStatus status
                    = client.getBucketLoggingStatusImpl(container.getName());
            return new LoggingConfiguration(status.isLoggingEnabled(),
                    status.getTargetBucketName());
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot read container configuration", e);
        }
    }

    /**
     * @param container The bucket name
     */
    @Override
    public void setLogging(final Path container, final LoggingConfiguration configuration) throws BackgroundException {
        try {
            // Logging target bucket
            final S3BucketLoggingStatus status = new S3BucketLoggingStatus(
                    StringUtils.isNotBlank(configuration.getLoggingTarget()) ? configuration.getLoggingTarget() : container.getName(), null);
            if(configuration.isEnabled()) {
                status.setLogfilePrefix(Preferences.instance().getProperty("s3.logging.prefix"));
            }
            client.setBucketLoggingStatus(container.getName(), status, true);
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot write file attributes", e);
        }
    }

    public LifecycleConfiguration getLifecycle(final Path container) throws BackgroundException {
        if(this.getHost().getCredentials().isAnonymousLogin()) {
            log.info("Anonymous cannot access logging status");
            return new LifecycleConfiguration();
        }
        try {
            final LifecycleConfig status = client.getLifecycleConfig(container.getName());
            if(null != status) {
                Integer transition = null;
                Integer expiration = null;
                for(LifecycleConfig.Rule rule : status.getRules()) {
                    if(rule.getTransition() != null) {
                        transition = rule.getTransition().getDays();
                    }
                    if(rule.getExpiration() != null) {
                        expiration = rule.getExpiration().getDays();
                    }
                }
                return new LifecycleConfiguration(transition, expiration);
            }
            return new LifecycleConfiguration();
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map(e);
        }
    }

    /**
     * @param container The bucket name
     */
    @Override
    public void setLifecycle(final Path container, final LifecycleConfiguration configuration) throws BackgroundException {
        try {
            if(configuration.getTransition() != null || configuration.getExpiration() != null) {
                final LifecycleConfig config = new LifecycleConfig();
                // Unique identifier for the rule. The value cannot be longer than 255 characters. When you specify an empty prefix, the rule applies to all objects in the bucket
                final LifecycleConfig.Rule rule = config.newRule(UUID.randomUUID().toString(), StringUtils.EMPTY, true);
                if(configuration.getTransition() != null) {
                    rule.newTransition().setDays(configuration.getTransition());
                }
                if(configuration.getExpiration() != null) {
                    rule.newExpiration().setDays(configuration.getExpiration());
                }
                client.setLifecycleConfig(container.getName(), config);
            }
            else {
                client.deleteLifecycleConfig(container.getName());
            }
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot read container configuration", e);
        }
    }

    /**
     * @param container The bucket name
     * @return True if enabled
     */
    @Override
    public VersioningConfiguration getVersioning(final Path container) throws BackgroundException {
        try {
            final S3BucketVersioningStatus status
                    = client.getBucketVersioningStatus(container.getName());

            return new VersioningConfiguration(status.isVersioningEnabled(),
                    status.isMultiFactorAuthDeleteRequired());
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot read container configuration", e);
        }
    }

    /**
     * @param container The bucket name
     * @param prompt    Login prompt for multi factor authentication
     */
    @Override
    public void setVersioning(final Path container, final LoginController prompt,
                              final VersioningConfiguration configuration) throws BackgroundException {
        try {
            final VersioningConfiguration current = this.getVersioning(container);
            if(current.isMultifactor()) {
                // The bucket is already MFA protected.
                final Credentials factor = this.mfa(prompt);
                if(configuration.isEnabled()) {
                    if(current.isEnabled()) {
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
                if(configuration.isEnabled() && !configuration.isMultifactor()) {
                    log.debug(String.format("Disable MFA %s for %s", factor.getUsername(), container));
                    // User has choosen to disable MFA
                    final Credentials factor2 = this.mfa(prompt);
                    client.disableMFAForVersionedBucket(container.getName(),
                            factor2.getUsername(), factor2.getPassword());
                }
            }
            else {
                if(configuration.isEnabled()) {
                    if(configuration.isMultifactor()) {
                        final Credentials factor = this.mfa(prompt);
                        log.debug(String.format("Enable bucket versioning with MFA %s for %s", factor.getUsername(), container));
                        client.enableBucketVersioningWithMFA(container.getName(),
                                factor.getUsername(), factor.getPassword());
                    }
                    else {
                        if(current.isEnabled()) {
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
        return new ArrayList<Acl.User>(Arrays.asList(
                new Acl.CanonicalUser(),
                new Acl.GroupUser(GroupGrantee.ALL_USERS.getIdentifier(), false),
                new Acl.EmailUser() {
                    @Override
                    public String getPlaceholder() {
                        return Locale.localizedString("Amazon Customer Email Address", "S3");
                    }
                })
        );
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
    public IdentityConfiguration iam(final LoginController prompt) {
        return new AWSIdentityConfiguration(host, prompt);
    }

    @Override
    public AnalyticsProvider analytics() {
        return new QloudstatAnalyticsProvider();
    }

    /**
     * Overwritten to provide publicly accessible URL of given object
     *
     * @return Using scheme from protocol
     */
    @Override
    public String toURL(final Path path) {
        return this.toURL(path, this.getHost().getProtocol().getScheme().toString());
    }

    /**
     * Overwritten to provide publicy accessible URL of given object
     *
     * @return Plain HTTP link
     */
    @Override
    public String toHttpURL(final Path path) {
        return this.toURL(path, Scheme.http.name());
    }

    /**
     * Properly URI encode and prepend the bucket name.
     *
     * @param scheme Protocol
     * @return URL to be displayed in browser
     */
    private String toURL(final Path path, final String scheme) {
        final StringBuilder url = new StringBuilder(scheme);
        url.append("://");
        if(path.isRoot()) {
            url.append(this.getHost().getHostname());
        }
        else {
            final String hostname = this.getHostnameForContainer(path.getContainer());
            if(hostname.startsWith(path.getContainer().getName())) {
                url.append(hostname);
                if(!path.isContainer()) {
                    url.append(URIEncoder.encode(path.getKey()));
                }
            }
            else {
                url.append(this.getHost().getHostname());
                url.append(URIEncoder.encode(path.getAbsolute()));
            }
        }
        return url.toString();
    }

    /**
     * Query string authentication. Query string authentication is useful for giving HTTP or browser access to
     * resources that would normally require authentication. The signature in the query string secures the request
     *
     * @return A signed URL with a limited validity over time.
     */
    public DescriptiveUrl toSignedUrl(final Path path) {
        return toSignedUrl(path, Preferences.instance().getInteger("s3.url.expire.seconds"));
    }

    /**
     * @param seconds Expire after seconds elapsed
     * @return Temporary URL to be displayed in browser
     */
    protected DescriptiveUrl toSignedUrl(final Path path, final int seconds) {
        Calendar expiry = Calendar.getInstance();
        expiry.add(Calendar.SECOND, seconds);
        return new DescriptiveUrl(this.createSignedUrl(path, seconds),
                MessageFormat.format(Locale.localizedString("{0} URL"), Locale.localizedString("Signed", "S3"))
                        + " (" + MessageFormat.format(Locale.localizedString("Expires on {0}", "S3") + ")",
                        UserDateFormatterFactory.get().getShortFormat(expiry.getTimeInMillis()))
        );
    }

    /**
     * Query String Authentication generates a signed URL string that will grant
     * access to an S3 resource (bucket or object)
     * to whoever uses the URL up until the time specified.
     *
     * @param expiry Validity of URL
     * @return Temporary URL to be displayed in browser
     */
    private String createSignedUrl(final Path path, final int expiry) {
        if(path.attributes().isFile()) {
            if(this.getHost().getCredentials().isAnonymousLogin()) {
                log.info("Anonymous cannot create signed URL");
                return null;
            }
            // Determine expiry time for URL
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, expiry);
            long secondsSinceEpoch = cal.getTimeInMillis() / 1000;

            // Generate URL
            return this.getClient().createSignedUrl("GET",
                    path.getContainer().getName(), path.getKey(), null,
                    null, secondsSinceEpoch, false, this.getHost().getProtocol().isSecure(), false);
        }
        return null;
    }

    /**
     * Generates a URL string that will return a Torrent file for an object in S3,
     * which file can be downloaded and run in a BitTorrent client.
     *
     * @return Torrent URL
     */
    public DescriptiveUrl toTorrentUrl(final Path path) {
        if(path.attributes().isFile()) {
            return new DescriptiveUrl(this.getClient().createTorrentUrl(path.getContainer().getName(),
                    path.getKey()));
        }
        return new DescriptiveUrl(null, null);
    }

    @Override
    public Set<DescriptiveUrl> getHttpURLs(final Path path) {
        final Set<DescriptiveUrl> urls = super.getHttpURLs(path);
        // Always include HTTP URL
        urls.add(new DescriptiveUrl(this.toURL(path, Scheme.http.name()),
                MessageFormat.format(Locale.localizedString("{0} URL"), Scheme.http.name().toUpperCase(java.util.Locale.ENGLISH))));
        DescriptiveUrl hour = this.toSignedUrl(path, 60 * 60);
        if(StringUtils.isNotBlank(hour.getUrl())) {
            urls.add(hour);
        }
        // Default signed URL expiring in 24 hours.
        DescriptiveUrl day = this.toSignedUrl(path, Preferences.instance().getInteger("s3.url.expire.seconds"));
        if(StringUtils.isNotBlank(day.getUrl())) {
            urls.add(day);
        }
        DescriptiveUrl week = this.toSignedUrl(path, 7 * 24 * 60 * 60);
        if(StringUtils.isNotBlank(week.getUrl())) {
            urls.add(week);
        }
        DescriptiveUrl torrent = this.toTorrentUrl(path);
        if(StringUtils.isNotBlank(torrent.getUrl())) {
            urls.add(new DescriptiveUrl(torrent.getUrl(),
                    MessageFormat.format(Locale.localizedString("{0} URL"), Locale.localizedString("Torrent"))));
        }
        return urls;
    }
}