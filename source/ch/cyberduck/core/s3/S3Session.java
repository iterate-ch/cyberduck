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
import ch.cyberduck.core.cloudfront.WebsiteCloudFrontDistributionConfiguration;
import ch.cyberduck.core.date.UserDateFormatterFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.ServiceExceptionMappingService;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Lifecycle;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Logging;
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.identity.AWSIdentityConfiguration;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.threading.NamedThreadFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.AbstractHttpEntity;
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
import org.jets3t.service.model.MultipartPart;
import org.jets3t.service.model.MultipartUpload;
import org.jets3t.service.model.StorageBucket;
import org.jets3t.service.model.StorageBucketLoggingStatus;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.model.WebsiteConfig;
import org.jets3t.service.model.container.ObjectKeyAndVersion;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.security.OAuth2Credentials;
import org.jets3t.service.security.OAuth2Tokens;
import org.jets3t.service.security.ProviderCredentials;
import org.jets3t.service.utils.RestUtils;
import org.jets3t.service.utils.ServiceUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * @version $Id$
 */
public class S3Session extends HttpSession<S3Session.RequestEntityRestStorageService> {
    private static final Logger log = Logger.getLogger(S3Session.class);

    private PathContainerService containerService = new PathContainerService();

    public S3Session(Host h) {
        super(h);
    }

    /**
     * Exposing protected methods
     */
    public class RequestEntityRestStorageService extends RestS3Service {
        public RequestEntityRestStorageService(final Jets3tProperties configuration) {
            super(host.getCredentials().isAnonymousLogin() ? null :
                    new AWSCredentials(host.getCredentials().getUsername(), host.getCredentials().getPassword()),
                    new PreferencesUseragentProvider().get(), null, configuration);
        }

        @Override
        protected HttpClient initHttpConnection() {
            final AbstractHttpClient client = S3Session.this.http();
            if(Preferences.instance().getBoolean("s3.expect-continue")) {
                // Activates 'Expect: 100-Continue' handshake for the entity enclosing methods
                HttpProtocolParams.setUseExpectContinue(client.getParams(), true);
            }
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

    private String getHostnameForContainer(final Path bucket) {
        if(this.configure(this.getHost().getHostname(true)).getBoolProperty("s3service.disable-dns-buckets", false)) {
            return this.getHost().getHostname(true);
        }
        if(!ServiceUtils.isBucketNameValidDNSName(containerService.getContainer(bucket).getName())) {
            return this.getHost().getHostname(true);
        }
        if(this.getHost().getHostname().equals(this.getHost().getProtocol().getDefaultHostname())) {
            return String.format("%s.%s", bucket.getName(), this.getHost().getHostname(true));
        }
        return this.getHost().getHostname(true);
    }

    @Override
    public RequestEntityRestStorageService connect(final HostKeyController key) throws BackgroundException {
        return new RequestEntityRestStorageService(this.configure(host.getHostname()));
    }

    @Override
    public void login(final PasswordStore keychain, final LoginController prompt) throws BackgroundException {
        client.setProviderCredentials(new AWSCredentials(
                host.getCredentials().getUsername(), host.getCredentials().getPassword()));
        for(Path bucket : new S3BucketListService().list(this)) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Found bucket %s", bucket));
            }
        }
    }

    /**
     * Prompt for MFA credentials
     *
     * @param controller Prompt controller
     * @return MFA one time authentication password.
     * @throws ch.cyberduck.core.exception.ConnectionCanceledException
     *          Prompt dismissed
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
                Locale.localizedString("Multi-Factor Authentication", "S3"), new LoginOptions());

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
    public boolean isRenameSupported(final Path file) {
        return !file.attributes().isVolume();
    }

    @Override
    public boolean isCreateFileSupported(final Path workdir) {
        // Creating files is only possible inside a bucket.
        return !workdir.isRoot();
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
    private String toURL(final Path file, final String scheme) {
        final StringBuilder url = new StringBuilder(scheme);
        url.append("://");
        if(file.isRoot()) {
            url.append(this.getHost().getHostname());
        }
        else {
            final String hostname = this.getHostnameForContainer(containerService.getContainer(file));
            if(hostname.startsWith(containerService.getContainer(file).getName())) {
                url.append(hostname);
                if(!containerService.isContainer(file)) {
                    url.append(URIEncoder.encode(containerService.getKey(file)));
                }
            }
            else {
                url.append(this.getHost().getHostname());
                url.append(URIEncoder.encode(file.getAbsolute()));
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
    private String createSignedUrl(final Path file, final int expiry) {
        if(this.getHost().getCredentials().isAnonymousLogin()) {
            log.info("Anonymous cannot create signed URL");
            return null;
        }
        // Determine expiry time for URL
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.SECOND, expiry);
        long secondsSinceEpoch = cal.getTimeInMillis() / 1000;

        // Generate URL
        return client.createSignedUrl("GET",
                containerService.getContainer(file).getName(), containerService.getKey(file), null,
                null, secondsSinceEpoch, false, this.getHost().getProtocol().isSecure(), false);
    }

    /**
     * Generates a URL string that will return a Torrent file for an object in S3,
     * which file can be downloaded and run in a BitTorrent client.
     *
     * @return Torrent URL
     */
    public DescriptiveUrl toTorrentUrl(final Path path) {
        if(path.attributes().isFile()) {
            return new DescriptiveUrl(client.createTorrentUrl(containerService.getContainer(path).getName(),
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

    @Override
    public InputStream read(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            if(file.attributes().isDuplicate()) {
                return this.getClient().getVersionedObject(file.attributes().getVersionId(),
                        containerService.getContainer(file).getName(), containerService.getKey(file),
                        null, // ifModifiedSince
                        null, // ifUnmodifiedSince
                        null, // ifMatch
                        null, // ifNoneMatch
                        status.isResume() ? status.getCurrent() : null, null).getDataInputStream();
            }
            return this.getClient().getObject(containerService.getContainer(file).getName(), containerService.getKey(file),
                    null, // ifModifiedSince
                    null, // ifUnmodifiedSince
                    null, // ifMatch
                    null, // ifNoneMatch
                    status.isResume() ? status.getCurrent() : null, null).getDataInputStream();
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Download failed", e, file);
        }
    }

    /**
     * Default size threshold for when to use multipart uploads.
     */
    private static final long DEFAULT_MULTIPART_UPLOAD_THRESHOLD =
            Preferences.instance().getLong("s3.upload.multipart.threshold");

    /**
     * Default minimum part size for upload parts.
     */
    private static final int DEFAULT_MINIMUM_UPLOAD_PART_SIZE =
            Preferences.instance().getInteger("s3.upload.multipart.size");

    /**
     * The maximum allowed parts in a multipart upload.
     */
    public static final int MAXIMUM_UPLOAD_PARTS = 10000;

    @Override
    public void upload(final Path file, final BandwidthThrottle throttle,
                       final StreamListener listener, final TransferStatus status) throws BackgroundException {
        try {
            if(file.attributes().isFile()) {
                final StorageObject object = this.createObjectDetails(file);

                this.message(MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                        file.getName()));

                // Only for AWS
                if(this.getHost().getHostname().equals(Protocol.S3_SSL.getDefaultHostname())
                        && Preferences.instance().getBoolean("s3.upload.multipart")
                        && status.getLength() > DEFAULT_MULTIPART_UPLOAD_THRESHOLD) {
                    this.uploadMultipart(file, throttle, listener, status, object);
                }
                else {
                    this.uploadSingle(file, throttle, listener, status, object);
                }
            }
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Upload failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload failed", e, file);
        }
    }

    private StorageObject createObjectDetails(final Path file) throws BackgroundException {
        final StorageObject object = new StorageObject(containerService.getKey(file));
        final String type = new MappingMimeTypeService().getMime(file.getName());
        object.setContentType(type);
        if(Preferences.instance().getBoolean("s3.upload.metadata.md5")) {
            this.message(MessageFormat.format(
                    Locale.localizedString("Compute MD5 hash of {0}", "Status"), file.getName()));
            object.setMd5Hash(ServiceUtils.fromHex(file.getLocal().attributes().getChecksum()));
        }
        Acl acl = file.attributes().getAcl();
        if(Acl.EMPTY.equals(acl)) {
            if(Preferences.instance().getProperty("s3.bucket.acl.default").equals("public-read")) {
                object.setAcl(this.getPublicCannedReadAcl());
            }
            else {
                // Owner gets FULL_CONTROL. No one else has access rights (default).
                object.setAcl(this.getPrivateCannedAcl());
            }
        }
        else {
            object.setAcl(new S3AccessControlListFeature(this).convert(acl));
        }
        // Storage class
        if(StringUtils.isNotBlank(Preferences.instance().getProperty("s3.storage.class"))) {
            object.setStorageClass(Preferences.instance().getProperty("s3.storage.class"));
        }
        if(StringUtils.isNotBlank(Preferences.instance().getProperty("s3.encryption.algorithm"))) {
            object.setServerSideEncryptionAlgorithm(Preferences.instance().getProperty("s3.encryption.algorithm"));
        }
        // Default metadata for new files
        for(String m : Preferences.instance().getList("s3.metadata.default")) {
            if(StringUtils.isBlank(m)) {
                continue;
            }
            if(!m.contains("=")) {
                log.warn(String.format("Invalid header %s", m));
                continue;
            }
            int split = m.indexOf('=');
            String name = m.substring(0, split);
            if(StringUtils.isBlank(name)) {
                log.warn(String.format("Missing key in header %s", m));
                continue;
            }
            String value = m.substring(split + 1);
            if(StringUtils.isEmpty(value)) {
                log.warn(String.format("Missing value in header %s", m));
                continue;
            }
            object.addMetadata(name, value);
        }
        return object;
    }

    /**
     * @param throttle Bandwidth throttle
     * @param listener Callback for bytes sent
     * @param status   Transfer status
     * @param object   File location
     */
    protected void uploadSingle(final Path file,
                                final BandwidthThrottle throttle, final StreamListener listener,
                                final TransferStatus status, final StorageObject object) throws BackgroundException {

        InputStream in = null;
        ResponseOutputStream<StorageObject> out = null;
        MessageDigest digest = null;
        if(!Preferences.instance().getBoolean("s3.upload.metadata.md5")) {
            // Content-MD5 not set. Need to verify ourselves instad of S3
            try {
                digest = MessageDigest.getInstance("MD5");
            }
            catch(NoSuchAlgorithmException e) {
                log.error(e.getMessage());
            }
        }
        try {
            if(null == digest) {
                log.warn("MD5 calculation disabled");
                in = file.getLocal().getInputStream();
            }
            else {
                in = new DigestInputStream(file.getLocal().getInputStream(), digest);
            }
            out = this.write(file, object, status.getLength() - status.getCurrent(),
                    Collections.<String, String>emptyMap());
            try {
                this.upload(file, out, in, throttle, listener, status);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map("Upload failed", e, file);
            }
        }
        catch(FileNotFoundException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
        if(null != digest) {
            final StorageObject part = out.getResponse();
            this.message(MessageFormat.format(
                    Locale.localizedString("Compute MD5 hash of {0}", "Status"), file.getName()));
            // Obtain locally-calculated MD5 hash.
            String hexMD5 = ServiceUtils.toHex(digest.digest());
            try {
                this.getClient().verifyExpectedAndActualETagValues(hexMD5, part);
            }
            catch(ServiceException e) {
                throw new ServiceExceptionMappingService().map("Upload failed", e, file);
            }
        }
    }

    /**
     * @param throttle Bandwidth throttle
     * @param listener Callback for bytes sent
     * @param status   Transfer status
     * @param object   File location
     * @throws IOException      I/O error
     * @throws ServiceException Service error
     */
    protected void uploadMultipart(final Path file, final BandwidthThrottle throttle, final StreamListener listener,
                                   final TransferStatus status, final StorageObject object)
            throws IOException, ServiceException, BackgroundException {

        final ThreadFactory threadFactory = new NamedThreadFactory("multipart");

        MultipartUpload multipart = null;
        if(status.isResume()) {
            // This operation lists in-progress multipart uploads. An in-progress multipart upload is a
            // multipart upload that has been initiated, using the Initiate Multipart Upload request, but has
            // not yet been completed or aborted.
            final List<MultipartUpload> uploads = this.getClient().multipartListUploads(
                    containerService.getContainer(file).getName());
            for(MultipartUpload upload : uploads) {
                if(!upload.getBucketName().equals(containerService.getContainer(file).getName())) {
                    continue;
                }
                if(!upload.getObjectKey().equals(containerService.getKey(file))) {
                    continue;
                }
                if(log.isInfoEnabled()) {
                    log.info(String.format("Resume multipart upload %s", upload.getUploadId()));
                }
                multipart = upload;
                break;
            }
        }
        if(null == multipart) {
            log.info("No pending multipart upload found");

            // Initiate multipart upload with metadata
            Map<String, Object> metadata = object.getModifiableMetadata();
            if(StringUtils.isNotBlank(Preferences.instance().getProperty("s3.storage.class"))) {
                metadata.put(this.getClient().getRestHeaderPrefix() + "storage-class",
                        Preferences.instance().getProperty("s3.storage.class"));
            }
            if(StringUtils.isNotBlank(Preferences.instance().getProperty("s3.encryption.algorithm"))) {
                metadata.put(this.getClient().getRestHeaderPrefix() + "server-side-encryption",
                        Preferences.instance().getProperty("s3.encryption.algorithm"));
            }

            multipart = this.getClient().multipartStartUpload(
                    containerService.getContainer(file).getName(), containerService.getKey(file), metadata);
        }

        final List<MultipartPart> completed;
        if(status.isResume()) {
            log.info(String.format("List completed parts of %s", multipart.getUploadId()));
            // This operation lists the parts that have been uploaded for a specific multipart upload.
            completed = this.getClient().multipartListParts(multipart);
        }
        else {
            completed = new ArrayList<MultipartPart>();
        }

        /**
         * At any point, at most
         * <tt>nThreads</tt> threads will be active processing tasks.
         */
        final ExecutorService pool = Executors.newFixedThreadPool(
                Preferences.instance().getInteger("s3.upload.multipart.concurency"), threadFactory);

        try {
            final List<Future<MultipartPart>> parts = new ArrayList<Future<MultipartPart>>();

            final long defaultPartSize = Math.max((status.getLength() / MAXIMUM_UPLOAD_PARTS),
                    DEFAULT_MINIMUM_UPLOAD_PART_SIZE);

            long remaining = status.getLength();
            long marker = 0;

            for(int partNumber = 1; remaining > 0; partNumber++) {
                boolean skip = false;
                if(status.isResume()) {
                    log.info(String.format("Determine if part %d can be skipped", partNumber));
                    for(MultipartPart c : completed) {
                        if(c.getPartNumber().equals(partNumber)) {
                            log.info("Skip completed part number " + partNumber);
                            listener.bytesSent(c.getSize());
                            skip = true;
                            break;
                        }
                    }
                }

                // Last part can be less than 5 MB. Adjust part size.
                final long length = Math.min(defaultPartSize, remaining);

                if(!skip) {
                    // Submit to queue
                    parts.add(this.submitPart(file, throttle, listener, status, multipart, pool, partNumber, marker, length));
                }

                remaining -= length;
                marker += length;
            }
            for(Future<MultipartPart> future : parts) {
                try {
                    completed.add(future.get());
                }
                catch(InterruptedException e) {
                    log.error("Part upload failed:" + e.getMessage());
                    throw new ConnectionCanceledException(e);
                }
                catch(ExecutionException e) {
                    log.warn("Part upload failed:" + e.getMessage());
                    if(e.getCause() instanceof ServiceException) {
                        throw (ServiceException) e.getCause();
                    }
                    if(e.getCause() instanceof IOException) {
                        throw (IOException) e.getCause();
                    }
                    throw new ConnectionCanceledException(e);
                }
            }
            if(status.isComplete()) {
                this.getClient().multipartCompleteUpload(multipart, completed);
            }
        }
        finally {
            if(!status.isComplete()) {
                // Cancel all previous parts
                log.info(String.format("Cancel multipart upload %s", multipart.getUploadId()));
                this.getClient().multipartAbortUpload(multipart);
            }
            // Cancel future tasks
            pool.shutdown();
        }
    }

    private Future<MultipartPart> submitPart(final Path file,
                                             final BandwidthThrottle throttle, final StreamListener listener,
                                             final TransferStatus status, final MultipartUpload multipart,
                                             final ExecutorService pool,
                                             final int partNumber,
                                             final long offset, final long length) throws BackgroundException {
        if(pool.isShutdown()) {
            throw new ConnectionCanceledException();
        }
        log.info(String.format("Submit part %d to queue", partNumber));
        return pool.submit(new Callable<MultipartPart>() {
            @Override
            public MultipartPart call() throws BackgroundException {
                final Map<String, String> requestParameters = new HashMap<String, String>();
                requestParameters.put("uploadId", multipart.getUploadId());
                requestParameters.put("partNumber", String.valueOf(partNumber));

                InputStream in = null;
                ResponseOutputStream<StorageObject> out = null;
                MessageDigest digest = null;
                try {
                    if(!Preferences.instance().getBoolean("s3.upload.metadata.md5")) {
                        // Content-MD5 not set. Need to verify ourselves instad of S3
                        try {
                            digest = MessageDigest.getInstance("MD5");
                        }
                        catch(NoSuchAlgorithmException e) {
                            log.error(e.getMessage());
                        }
                    }
                    if(null == digest) {
                        log.warn("MD5 calculation disabled");
                        in = file.getLocal().getInputStream();
                    }
                    else {
                        in = new DigestInputStream(file.getLocal().getInputStream(), digest);
                    }
                    out = write(file, new StorageObject(containerService.getKey(file)), length, requestParameters);
                    upload(file, out, in, throttle, listener, offset, length, status);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map(e);
                }
                finally {
                    IOUtils.closeQuietly(in);
                    IOUtils.closeQuietly(out);
                }
                final StorageObject part = out.getResponse();
                if(null != digest) {
                    // Obtain locally-calculated MD5 hash
                    String hexMD5 = ServiceUtils.toHex(digest.digest());
                    try {
                        getClient().verifyExpectedAndActualETagValues(hexMD5, part);
                    }
                    catch(ServiceException e) {
                        throw new ServiceExceptionMappingService().map("Upload failed", e, file);
                    }
                }
                // Populate part with response data that is accessible via the object's metadata
                return new MultipartPart(partNumber, part.getLastModifiedDate(),
                        part.getETag(), part.getContentLength());
            }
        });
    }

    @Override
    public OutputStream write(final Path file, final TransferStatus status) throws BackgroundException {
        return this.write(file, this.createObjectDetails(file), status.getLength() - status.getCurrent(),
                Collections.<String, String>emptyMap());
    }

    private ResponseOutputStream<StorageObject> write(final Path file,
                                                      final StorageObject part, final Long contentLength,
                                                      final Map<String, String> requestParams) throws BackgroundException {
        DelayedHttpEntityCallable<StorageObject> command = new DelayedHttpEntityCallable<StorageObject>() {
            @Override
            public StorageObject call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    getClient().putObjectWithRequestEntityImpl(
                            containerService.getContainer(file).getName(), part, entity, requestParams);
                }
                catch(ServiceException e) {
                    throw new ServiceExceptionMappingService().map("Upload failed", e, file);
                }
                return part;
            }

            @Override
            public long getContentLength() {
                return contentLength;
            }
        };
        return this.write(file, command);
    }

    @Override
    public AttributedList<Path> list(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            // List all buckets
            return new AttributedList<Path>(new S3BucketListService().list(this));
        }
        else {
            return new S3ObjectListService(this).list(file);
        }
    }

    @Override
    public void mkdir(final Path file) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                new S3BucketCreateService(this).create(file);
            }
            else {
                // Add placeholder object
                final StorageObject object = new StorageObject(containerService.getKey(file) + Path.DELIMITER);
                object.setBucketName(containerService.getContainer(file).getName());
                // Set object explicitly to private access by default.
                object.setAcl(this.getPrivateCannedAcl());
                object.setContentLength(0);
                object.setContentType("application/x-directory");
                this.getClient().putObject(containerService.getContainer(file).getName(), object);
            }
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot create folder {0}", e, file);
        }
    }

    @Override
    public void delete(final Path file, final LoginController prompt) throws BackgroundException {
        try {
            this.message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                    file.getName()));

            if(file.attributes().isFile()) {
                this.delete(prompt, containerService.getContainer(file), Collections.singletonList(
                        new ObjectKeyAndVersion(containerService.getKey(file), file.attributes().getVersionId())));
            }
            else if(file.attributes().isDirectory()) {
                final List<ObjectKeyAndVersion> files = new ArrayList<ObjectKeyAndVersion>();
                for(Path child : this.list(file)) {
                    if(!this.isConnected()) {
                        throw new ConnectionCanceledException();
                    }
                    if(child.attributes().isDirectory()) {
                        this.delete(child, prompt);
                    }
                    else {
                        files.add(new ObjectKeyAndVersion(containerService.getKey(child), child.attributes().getVersionId()));
                    }
                }
                if(!containerService.isContainer(file)) {
                    // Because we normalize paths and remove a trailing delimiter we add it here again as the
                    // default directory placeholder formats has the format `/placeholder/' as a key.
                    files.add(new ObjectKeyAndVersion(containerService.getKey(file) + Path.DELIMITER,
                            file.attributes().getVersionId()));
                    // Always returning 204 even if the key does not exist.
                    // Fallback to legacy directory placeholders with metadata instead of key with trailing delimiter
                    files.add(new ObjectKeyAndVersion(containerService.getKey(file),
                            file.attributes().getVersionId()));
                    // AWS does not return 404 for non-existing keys
                }
                if(!files.isEmpty()) {
                    this.delete(prompt, containerService.getContainer(file), files);
                }
                if(containerService.isContainer(file)) {
                    // Finally delete bucket itself
                    this.getClient().deleteBucket(containerService.getContainer(file).getName());
                }
            }
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot delete {0}", e, file);
        }
    }

    /**
     * @param container Bucket
     * @param keys      Key and version ID for versioned object or null
     * @throws ConnectionCanceledException Authentication canceled for MFA delete
     * @throws ServiceException            Service error
     */
    protected void delete(final LoginController prompt, final Path container, final List<ObjectKeyAndVersion> keys) throws ServiceException, BackgroundException {
        if(new S3VersioningFeature(this).getConfiguration(container).isMultifactor()) {
            final Credentials factor = this.mfa(prompt);
            this.getClient().deleteMultipleObjectsWithMFA(container.getName(),
                    keys.toArray(new ObjectKeyAndVersion[keys.size()]),
                    factor.getUsername(),
                    factor.getPassword(),
                    true);
        }
        else {
            if(this.getHost().getHostname().equals(Protocol.S3_SSL.getDefaultHostname())) {
                this.getClient().deleteMultipleObjects(container.getName(),
                        keys.toArray(new ObjectKeyAndVersion[keys.size()]),
                        true);
            }
            else {
                for(ObjectKeyAndVersion k : keys) {
                    this.getClient().deleteObject(container.getName(), k.getKey());
                }
            }
        }
    }

    @Override
    public void rename(final Path file, final Path renamed) throws BackgroundException {
        try {
            this.message(MessageFormat.format(Locale.localizedString("Renaming {0} to {1}", "Status"),
                    file.getName(), renamed));

            if(file.attributes().isFile() || file.attributes().isPlaceholder()) {
                final StorageObject destination = new StorageObject(containerService.getKey(renamed));
                // Keep same storage class
                destination.setStorageClass(file.attributes().getStorageClass());
                // Keep encryption setting
                destination.setServerSideEncryptionAlgorithm(file.attributes().getEncryption());
                // Apply non standard ACL
                final S3AccessControlListFeature acl = new S3AccessControlListFeature(this);
                destination.setAcl(acl.convert(acl.read(file)));
                // Moving the object retaining the metadata of the original.
                this.getClient().moveObject(containerService.getContainer(file).getName(), containerService.getKey(file),
                        containerService.getContainer(renamed).getName(),
                        destination, false);
            }
            else if(file.attributes().isDirectory()) {
                for(Path i : this.list(file)) {
                    if(!this.isConnected()) {
                        throw new ConnectionCanceledException();
                    }
                    this.rename(i, new Path(renamed, i.getName(), i.attributes().getType()));
                }
            }
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public <T> T getFeature(final Class<T> type, final LoginController prompt) {
        if(type == ch.cyberduck.core.features.AccessControlList.class) {
            return (T) new S3AccessControlListFeature(this);
        }
        if(type == Headers.class) {
            return (T) new S3MetadataFeature(this);
        }
        if(type == Location.class) {
            // Only for AWS
            if(this.getHost().getHostname().equals(Protocol.S3_SSL.getDefaultHostname())) {
                return (T) new S3LocationFeature(this);
            }
        }
        if(type == AnalyticsProvider.class) {
            // Only for AWS
            if(this.getHost().getHostname().equals(Protocol.S3_SSL.getDefaultHostname())) {
                return (T) new QloudstatAnalyticsProvider();
            }
            return null;
        }
        if(type == Versioning.class) {
            // Only for AWS
            if(this.getHost().getHostname().equals(Protocol.S3_SSL.getDefaultHostname())) {
                return (T) new S3VersioningFeature(this);
            }
            return null;
        }
        if(type == Logging.class) {
            // Only for AWS
            if(this.getHost().getHostname().equals(Protocol.S3_SSL.getDefaultHostname())) {
                return (T) new S3LoggingFeature(this);
            }
            return null;
        }
        if(type == Lifecycle.class) {
            // Only for AWS
            if(this.getHost().getHostname().equals(Protocol.S3_SSL.getDefaultHostname())) {
                return (T) new S3LifecycleConfiguration(this);
            }
        }
        if(type == Encryption.class) {
            // Only for AWS
            if(this.getHost().getHostname().equals(Protocol.S3_SSL.getDefaultHostname())) {
                return (T) new S3EncryptionFeature(this);
            }
            return null;
        }
        if(type == Redundancy.class) {
            // Only for AWS
            if(this.getHost().getHostname().equals(Protocol.S3_SSL.getDefaultHostname())) {
                return (T) new S3StorageClassFeature(this);
            }
            return null;
        }
        if(type == IdentityConfiguration.class) {
            // Only for AWS
            if(this.getHost().getHostname().equals(Protocol.S3_SSL.getDefaultHostname())) {
                return (T) new AWSIdentityConfiguration(host, prompt);
            }
        }
        if(type == DistributionConfiguration.class) {
            if(host.getHostname().endsWith(Protocol.S3_SSL.getDefaultHostname())) {
                return (T) new WebsiteCloudFrontDistributionConfiguration(this);
            }
            else {
                // Amazon CloudFront custom origin
                return super.getFeature(type, prompt);
            }
        }
        return null;
    }
}