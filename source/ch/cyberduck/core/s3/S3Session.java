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
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cloud.CloudSession;
import ch.cyberduck.core.cloudfront.CloudFrontDistributionConfiguration;
import ch.cyberduck.core.i18n.Locale;
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
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3BucketLoggingStatus;
import org.jets3t.service.model.S3BucketVersioningStatus;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageBucket;
import org.jets3t.service.model.StorageBucketLoggingStatus;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.model.StorageOwner;
import org.jets3t.service.model.WebsiteConfig;
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

/**
 * Connecting to S3 service with plain HTTP.
 *
 * @version $Id$
 */
public class S3Session extends CloudSession {
    private static Logger log = Logger.getLogger(S3Session.class);

    private static class Factory extends SessionFactory {
        @Override
        protected Session create(Host h) {
            return new S3Session(h);
        }
    }

    public static SessionFactory factory() {
        return new Factory();
    }

    private RequestEntityRestStorageService client;

    protected S3Session(Host h) {
        super(h);
    }

    @Override
    protected RequestEntityRestStorageService getClient() throws ConnectionCanceledException {
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
        protected void initializeProxy() {
            ; // Client already configured
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
        public void authorizeHttpRequest(HttpUriRequest httpMethod, HttpContext context)
                throws Exception {
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
            throws IOException, ServiceException {
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

    protected void configure(String hostname) {
        log.debug("configure:" + hostname);
        if(StringUtils.isNotBlank(host.getProtocol().getDefaultHostname())
                && hostname.endsWith(host.getProtocol().getDefaultHostname())) {
            // The user specified a DNS bucket endpoint. Connect to the default hostname instead.
            configuration.setProperty("s3service.s3-endpoint", host.getProtocol().getDefaultHostname());
        }
        else {
            // Standard configuration
            configuration.setProperty("s3service.s3-endpoint", hostname);
            configuration.setProperty("s3service.disable-dns-buckets", String.valueOf(true));
        }
        configuration.setProperty("s3service.s3-endpoint-http-port", String.valueOf(host.getPort()));
        configuration.setProperty("s3service.s3-endpoint-https-port", String.valueOf(host.getPort()));
        configuration.setProperty("s3service.enable-storage-classes", String.valueOf(true));
        configuration.setProperty("s3service.https-only", String.valueOf(host.getProtocol().isSecure()));
        // The maximum number of retries that will be attempted when an S3 connection fails
        // with an InternalServer error. To disable retries of InternalError failures, set this to 0.
        configuration.setProperty("s3service.internal-error-retry-max", String.valueOf(0));
        // The maximum number of concurrent communication threads that will be started by
        // the multi-threaded service for upload and download operations.
        configuration.setProperty("s3service.max-thread-count", String.valueOf(1));
    }

    @Override
    public String getHostnameForContainer(String bucket) {
        if(configuration.getBoolProperty("s3service.disable-dns-buckets", false)) {
            return this.getHost().getHostname(true);
        }
        if(!ServiceUtils.isBucketNameValidDNSName(bucket)) {
            return this.getHost().getHostname(true);
        }
        if(this.getHost().getHostname().equals(this.getHost().getProtocol().getDefaultHostname())) {
            return bucket + "." + this.getHost().getHostname(true);
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
    protected List<StorageBucket> getBuckets(boolean reload) throws IOException, ServiceException {
        if(buckets.isEmpty() || reload) {
            buckets.clear();
            if(host.getCredentials().isAnonymousLogin()) {
                log.info("Anonymous cannot list buckets");
                // Listing buckets not supported for thirdparty buckets
                String bucketname = this.getContainerForHostname(host.getHostname(true));
                if(StringUtils.isEmpty(bucketname)) {
                    if(StringUtils.isNotBlank(host.getDefaultPath())) {
                        Path d = PathFactory.createPath(this, host.getDefaultPath(), AbstractPath.DIRECTORY_TYPE);
                        while(!d.getParent().isRoot()) {
                            d = d.getParent();
                        }
                        bucketname = d.getName();
                    }
                    log.info(String.format("Using default path to determine bucket name %s", bucketname));
                }
                if(StringUtils.isEmpty(bucketname)) {
                    log.warn(String.format("No bucket name given in hostname %s", host.getHostname()));
                    // Rewrite endpoint to default S3 endpoint
                    this.configure(host.getProtocol().getDefaultHostname());
                    bucketname = host.getHostname(true);
                }
                if(!this.getClient().isBucketAccessible(bucketname)) {
                    this.error("Cannot read container configuration",
                            new ServiceException(String.format("Bucket %s not accessible", bucketname)));
                }
                S3Bucket bucket = new S3Bucket(bucketname);
                try {
                    StorageOwner owner = this.getClient().getBucketAcl(bucketname).getOwner();
                    bucket.setOwner(owner);
                }
                catch(ServiceException e) {
                    // ACL not readable by anonymous user.
                    log.warn(e.getMessage());
                }
                buckets.put(bucketname, bucket);
            }
            else {
                // If bucketname is specified in hostname, try to connect to this particular bucket only.
                String bucketname = this.getContainerForHostname(host.getHostname(true));
                if(StringUtils.isNotEmpty(bucketname)) {
                    if(!this.getClient().isBucketAccessible(bucketname)) {
                        this.error("Cannot read container configuration",
                                new ServiceException(String.format("Bucket %s not accessible", bucketname)));
                    }
                    S3Bucket bucket = new S3Bucket(bucketname);
                    try {
                        StorageOwner owner = this.getClient().getBucketAcl(bucketname).getOwner();
                        bucket.setOwner(owner);
                    }
                    catch(ServiceException e) {
                        // ACL not readable by anonymous or IAM user.
                        log.warn(e.getMessage());
                    }
                    buckets.put(bucketname, bucket);
                }
                else {
                    // List all buckets owned
                    for(StorageBucket bucket : this.getClient().listAllBuckets()) {
                        buckets.put(bucket.getName(), bucket);
                    }
                }
            }
            if(reload) {
                loggingStatus.clear();
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
    protected void login(LoginController controller, Credentials credentials) throws IOException {
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
    protected Credentials mfa(LoginController controller) throws ConnectionCanceledException {
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
    protected boolean isLoginFailure(ServiceException e) {
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

    /**
     * Creating files is only possible inside a bucket.
     *
     * @param workdir The workdir to create query
     * @return False if directory is root.
     */
    @Override
    public boolean isCreateFileSupported(Path workdir) {
        return !workdir.isRoot();
    }

    @Override
    public boolean isRenameSupported(Path file) {
        return !file.attributes().isVolume();
    }

    /**
     * AWS storage classes
     */
    private List<String> storageClasses
            = Arrays.asList(S3Object.STORAGE_CLASS_STANDARD, S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY);

    @Override
    public List<String> getSupportedStorageClasses() {
        return storageClasses;
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
    public void setLogging(final String container, final boolean enabled, String destination) {
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

    /**
     * Set to false if permission error response indicates this
     * feature is not implemented.
     */
    private boolean versioningSupported = true;

    /**
     * @return True if the service supports object versioning.
     */
    @Override
    public boolean isVersioningSupported() {
        return versioningSupported;
    }

    @Override
    public boolean isRevertSupported() {
        return this.isVersioningSupported();
    }

    protected void setVersioningSupported(boolean versioningSupported) {
        this.versioningSupported = versioningSupported;
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
                    log.warn("Bucket versioning not supported:" + e.getMessage());
                    this.setVersioningSupported(false);
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
    public void setVersioning(final String container, boolean mfa, boolean versioning) {
        if(this.isVersioningSupported()) {
            try {
                this.check();
                if(this.isMultiFactorAuthentication(container)) {
                    // The bucket is already MFA protected.
                    LoginController c = LoginControllerFactory.instance(this);
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
                            LoginController c = LoginControllerFactory.instance(this);
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
    public List<Acl.Role> getAvailableAclRoles(List<Path> files) {
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
     * @param container Bucket name
     * @return ACL with full control permission for owner of the bucket
     */
    protected Acl getPrivateAcl(String container) {
        for(final StorageBucket bucket : buckets.values()) {
            if(bucket.getName().equals(container)) {
                StorageOwner owner = bucket.getOwner();
                if(null == owner) {
                    log.warn(String.format("Owner not known for container %s", container));
                    continue;
                }
                return new Acl(new Acl.CanonicalUser(owner.getId()),
                        new Acl.Role(org.jets3t.service.acl.Permission.PERMISSION_FULL_CONTROL.toString()));
            }
        }
        log.warn(String.format("No such container:%s", container));
        return new Acl();
    }

    /**
     * @param container Bucket name
     * @param readable  Enable read permission for anonymous users
     * @return ACL with full control permission for owner of the bucket plus the read and write permissions
     *         for anonymous users if enabled.
     */
    protected Acl getUploadAcl(String container, boolean readable) {
        final Acl acl = this.getPrivateAcl(container);
        if(readable) {
            acl.addAll(new Acl.GroupUser(GroupGrantee.ALL_USERS.getIdentifier()),
                    new Acl.Role(org.jets3t.service.acl.Permission.PERMISSION_READ.toString()));
        }
        return acl;
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
    private String getWebsiteEndpoint(String bucket) {
        // Geographical location
        final String location = this.getLocation(bucket);
        // US Standard
        String endpoint = "s3-website-us-east-1.amazonaws.com";
        if(S3Bucket.LOCATION_EUROPE.equals(location)) {
            endpoint = "s3-website-eu-west-1.amazonaws.com";
        }
        else if(S3Bucket.LOCATION_US_WEST_NORTHERN_CALIFORNIA.equals(location)) {
            endpoint = "s3-website-us-west-1.amazonaws.com";
        }
        else if(S3Bucket.LOCATION_US_WEST_OREGON.equals(location)) {
            endpoint = "s3-website-us-west-2.amazonaws.com";
        }
        else if(S3Bucket.LOCATION_ASIA_PACIFIC_SINGAPORE.equals(location)) {
            endpoint = "s3-website-ap-southeast-1.amazonaws.com";
        }
        else if(S3Bucket.LOCATION_ASIA_PACIFIC_TOKYO.equals(location)) {
            endpoint = "s3-website-ap-northeast-1.amazonaws.com";
        }
        else if(S3Bucket.LOCATION_SOUTH_AMERICA_EAST.equals(location)) {
            endpoint = "s3-website-sa-east-1.amazonaws.com";
        }
        return bucket + "." + endpoint;
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
            super(LoginControllerFactory.instance(S3Session.this), S3Session.this.host.getCredentials(),
                    new ErrorListener() {
                        public void error(BackgroundException exception) {
                            S3Session.this.error(exception);
                        }
                    },
                    new ProgressListener() {
                        public void message(String message) {
                            S3Session.this.message(message);
                        }
                    },
                    new TranscriptListener() {
                        public void log(boolean request, String message) {
                            S3Session.this.log(request, message);
                        }
                    }
            );
        }

        /**
         * Distribution methods supported by this S3 provider.
         *
         * @return Download and Streaming for AWS.
         */
        @Override
        public List<Distribution.Method> getMethods() {
            return Arrays.asList(Distribution.WEBSITE, Distribution.WEBSITE_CDN, Distribution.DOWNLOAD, Distribution.STREAMING);
        }

        @Override
        public String toString(Distribution.Method method) {
            if(method.equals(Distribution.WEBSITE)) {
                return method.toString();
            }
            return super.toString(method);
        }

        @Override
        public String getOrigin(Distribution.Method method, String container) {
            if(method.equals(Distribution.WEBSITE)) {
                return S3Session.this.getHostnameForContainer(container);
            }
            if(method.equals(Distribution.WEBSITE_CDN)) {
                return S3Session.this.getWebsiteEndpoint(container);
            }
            return super.getOrigin(method, container);
        }

        @Override
        protected List<String> getContainers(Distribution.Method method) {
            return new ArrayList<String>(buckets.keySet());
        }

        @Override
        public Distribution read(String origin, Distribution.Method method) {
            if(method.equals(Distribution.WEBSITE)) {
                final String bucket = S3Session.this.getContainerForHostname(origin);
                // Website Endpoint URL
                final String url = method.getProtocol() + S3Session.this.getWebsiteEndpoint(bucket);
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
                        this.error("Cannot read CDN configuration", e);
                    }
                }
            }
            return super.read(origin, method);
        }

        @Override
        public void write(boolean enabled, String origin, Distribution.Method method, String[] cnames, boolean logging, String loggingBucket, String defaultRootObject) {
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
                    Distribution d = distributionStatus.get(method).get(origin);
                    final String bucket = S3Session.this.getContainerForHostname(origin);

                    if(enabled) {
                        String suffix = "index.html";
                        if(StringUtils.isNotBlank(defaultRootObject)) {
                            suffix = FilenameUtils.getName(defaultRootObject);
                        }
                        // Enable website endpoint
                        S3Session.this.getClient().setWebsiteConfig(bucket, new WebsiteConfig(suffix));
                    }
                    else {
                        // Disable website endpoint
                        S3Session.this.getClient().deleteWebsiteConfig(bucket);
                    }
                }
                catch(IOException e) {
                    this.error("Cannot write CDN configuration", e);
                }
                catch(S3ServiceException e) {
                    this.error("Cannot write CDN configuration", e);
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
        protected CustomOrigin getCustomOriginConfiguration(Distribution.Method method, String origin) {
            if(method.equals(Distribution.WEBSITE_CDN)) {
                return new CustomOrigin(origin, CustomOrigin.OriginProtocolPolicy.HTTP_ONLY);
            }
            return super.getCustomOriginConfiguration(method, origin);
        }

        @Override
        public boolean isDefaultRootSupported(Distribution.Method method) {
            if(method.equals(Distribution.WEBSITE)) {
                return true;
            }
            return super.isDefaultRootSupported(method);
        }

        @Override
        public boolean isLoggingSupported(Distribution.Method method) {
            if(method.equals(Distribution.WEBSITE)) {
                return false;
            }
            return super.isLoggingSupported(method);
        }
    }
}
