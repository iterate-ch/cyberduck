package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesReader;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.Constants;
import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.impl.rest.XmlResponsesSaxParser;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.StorageBucket;
import org.jets3t.service.model.StorageBucketLoggingStatus;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.model.WebsiteConfig;
import org.jets3t.service.utils.RestUtils;
import org.jets3t.service.utils.ServiceUtils;

import java.util.Calendar;
import java.util.Map;

public class RequestEntityRestStorageService extends RestS3Service {
    private static final Logger log = LogManager.getLogger(RequestEntityRestStorageService.class);

    private final S3Session session;
    private final Jets3tProperties properties;

    protected static Jets3tProperties toProperties(final Host bookmark, final S3Protocol.AuthenticationHeaderSignatureVersion signatureVersion) {
        final Jets3tProperties properties = new Jets3tProperties();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Configure for endpoint %s", bookmark));
        }
        if(InetAddressUtils.isIPv4Address(bookmark.getHostname()) || InetAddressUtils.isIPv6Address(bookmark.getHostname())) {
            if(log.isWarnEnabled()) {
                log.warn(String.format("Disable virtual host style requests for hostname %s", bookmark.getHostname()));
            }
            properties.setProperty("s3service.disable-dns-buckets", String.valueOf(true));
        }
        else {
            properties.setProperty("s3service.disable-dns-buckets",
                    String.valueOf(new HostPreferences(bookmark).getBoolean("s3.bucket.virtualhost.disable")));
        }
        properties.setProperty("s3service.enable-storage-classes", String.valueOf(true));
        // The maximum number of retries that will be attempted when an S3 connection fails
        // with an InternalServer error. To disable retries of InternalError failures, set this to 0.
        properties.setProperty("s3service.internal-error-retry-max", String.valueOf(0));
        // The maximum number of concurrent communication threads that will be started by
        // the multi-threaded service for upload and download operations.
        properties.setProperty("s3service.max-thread-count", String.valueOf(1));
        properties.setProperty("httpclient.proxy-autodetect", String.valueOf(false));
        properties.setProperty("httpclient.retry-max", String.valueOf(0));
        properties.setProperty("storage-service.internal-error-retry-max", String.valueOf(0));
        properties.setProperty("storage-service.request-signature-version", signatureVersion.toString());
        properties.setProperty("storage-service.disable-live-md5", String.valueOf(true));
        properties.setProperty("xmlparser.sanitize-listings", String.valueOf(false));
        for(Map.Entry<String, String> property : bookmark.getProtocol().getProperties().entrySet()) {
            properties.setProperty(property.getKey(), property.getValue());
        }
        return properties;
    }

    public RequestEntityRestStorageService(final S3Session session, final HttpClientBuilder configuration) {
        super(null, new PreferencesUseragentProvider().get(), null, toProperties(session.getHost(), session.getSignatureVersion()));
        this.session = session;
        this.properties = this.getJetS3tProperties();
        // Client configuration
        configuration.setRedirectStrategy(new S3BucketRegionRedirectStrategy(this, session.getHost()));
        this.setHttpClient(configuration.build());
    }

    public Jets3tProperties getConfiguration() {
        return properties;
    }

    @Override
    public String getEndpoint() {
        return session.getHost().getHostname();
    }

    @Override
    protected void initializeDefaults() {
        //
    }

    @Override
    protected HttpClientBuilder initHttpClientBuilder() {
        return null;
    }


    @Override
    protected void initializeProxy(final HttpClientBuilder httpClientBuilder) {
        //
    }

    protected HttpUriRequest setupConnection(final String method, final String bucketName,
                                             final String objectKey, final Map<String, String> requestParameters) throws S3ServiceException {
        return this.setupConnection(HTTP_METHOD.valueOf(method), bucketName, objectKey, requestParameters);
    }

    @Override
    public HttpUriRequest setupConnection(final HTTP_METHOD method, final String bucketName,
                                          final String objectKey, final Map<String, String> requestParameters) throws S3ServiceException {
        final Host host = session.getHost();
        final PreferencesReader preferences = new HostPreferences(host);
        // Hostname taking into account transfer acceleration and bucket region
        String endpoint = host.getHostname();
        // Apply default configuration
        if(S3Session.isAwsHostname(host.getHostname(), false)) {
            if(StringUtils.isNotBlank(host.getRegion())) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Apply default region %s to endpoint", host.getRegion()));
                }
                // Apply default region
                endpoint = createRegionSpecificEndpoint(host, host.getRegion());
            }
            else {
                if(StringUtils.isNotBlank(bucketName)) {
                    // Only for AWS set endpoint to region specific
                    if(preferences.getBoolean(String.format("s3.transferacceleration.%s.enable", bucketName))) {
                        // Already set to accelerated endpoint
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Use accelerated endpoint %s", S3TransferAccelerationService.S3_ACCELERATE_DUALSTACK_HOSTNAME));
                        }
                        endpoint = S3TransferAccelerationService.S3_ACCELERATE_DUALSTACK_HOSTNAME;
                    }
                    else {
                        // Only attempt to determine region specific endpoint if virtual host style requests are enabled
                        if(!this.getDisableDnsBuckets()) {
                            // Check if not already request to query bucket location
                            if(requestParameters == null || !requestParameters.containsKey("location")) {
                                try {
                                    // Determine region for bucket using cache
                                    final Location.Name region = new S3LocationFeature(session, regionEndpointCache).getLocation(bucketName);
                                    if(Location.unknown == region) {
                                        // Missing permission or not supported
                                        log.warn(String.format("Failure determining bucket location for %s", bucketName));
                                        endpoint = host.getHostname();
                                    }
                                    else {
                                        if(log.isDebugEnabled()) {
                                            log.debug(String.format("Determined region %s for bucket %s", region, bucketName));
                                        }
                                        endpoint = createRegionSpecificEndpoint(host, region.getIdentifier());
                                    }
                                }
                                catch(BackgroundException e) {
                                    // Ignore failure reading location for bucket
                                    log.error(String.format("Failure %s determining bucket location for %s", e, bucketName));
                                    endpoint = createRegionSpecificEndpoint(host, preferences.getProperty("s3.location"));
                                }
                            }
                        }
                    }
                }
            }
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Set endpoint to %s", endpoint));
        }
        // Virtual host style endpoint including bucket name
        String hostname = endpoint;
        String resource = String.valueOf(Path.DELIMITER);
        if(!this.getDisableDnsBuckets()) {
            // Virtual host style requests enabled in connection profile
            if(StringUtils.isNotBlank(bucketName)) {
                if(ServiceUtils.isBucketNameValidDNSName(bucketName)) {
                    hostname = String.format("%s.%s", bucketName, endpoint);
                }
                else {
                    // Add bucket name to path
                    resource += bucketName + Path.DELIMITER;
                }
            }
        }
        else {
            if(StringUtils.isNotBlank(bucketName)) {
                // Add bucket name to path
                resource += bucketName + Path.DELIMITER;
            }
        }
        final HttpUriRequest request;
        // Prefix endpoint with bucket name for actual hostname
        if(log.isDebugEnabled()) {
            log.debug(String.format("Set hostname to %s", hostname));
        }
        final String virtualPath;
        // Allow for non-standard virtual directory paths on the server-side
        if(StringUtils.isNotBlank(host.getProtocol().getContext()) && !Scheme.isURL(host.getProtocol().getContext())) {
            virtualPath = PathNormalizer.normalize(host.getProtocol().getContext());
        }
        else {
            virtualPath = StringUtils.EMPTY;
        }
        if(objectKey != null) {
            resource += RestUtils.encodeUrlPath(objectKey, "/");
        }
        // Construct a URL representing a connection for the S3 resource.
        String url;
        // Add additional request parameters to the URL for special cases (eg ACL operations)
        try {
            url = this.addRequestParametersToUrlPath(
                    String.format("%s://%s:%d%s%s", host.getProtocol().getScheme(), hostname, host.getPort(), virtualPath, resource), requestParameters);
        }
        catch(ServiceException e) {
            throw new S3ServiceException(e);
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Set URL to %s", url));
        }
        switch(method) {
            case PUT:
                request = new HttpPut(url);
                break;
            case POST:
                request = new HttpPost(url);
                break;
            case HEAD:
                request = new HttpHead(url);
                break;
            case GET:
                request = new HttpGet(url);
                break;
            case DELETE:
                request = new HttpDelete(url);
                break;
            default:
                throw new IllegalArgumentException(String.format("Unrecognised HTTP method name %s", method));
        }
        return request;
    }

    @Override
    protected HttpResponse performRequest(final String bucketName, final HttpUriRequest httpMethod, final int[] expectedResponseCodes) throws ServiceException {
        final BasicHttpContext context = new BasicHttpContext();
        context.setAttribute("bucket", bucketName);
        return super.performRequest(bucketName, httpMethod, expectedResponseCodes, context);
    }

    protected static String createRegionSpecificEndpoint(final Host host, final String region) {
        final PreferencesReader preferences = new HostPreferences(host);
        final String endpoint = preferences.getBoolean("s3.endpoint.dualstack.enable")
                ? preferences.getProperty("s3.endpoint.format.ipv6") : preferences.getProperty("s3.endpoint.format.ipv4");
        if(log.isDebugEnabled()) {
            log.debug(String.format("Apply region %s to endpoint %s", region, endpoint));
        }
        return String.format(endpoint, region);
    }

    @Override
    protected boolean getDisableDnsBuckets() {
        return super.getDisableDnsBuckets();
    }

    public void disableDnsBuckets() {
        properties.setProperty("s3service.disable-dns-buckets", String.valueOf(true));
    }

    @Override
    protected boolean isTargettingGoogleStorageService() {
        return session.getHost().getHostname().equals(Constants.GS_DEFAULT_HOSTNAME);
    }

    @Override
    protected StorageBucket createBucketImpl(String bucketName, String location,
                                             AccessControlList acl, Map<String, Object> headers) throws ServiceException {
        return super.createBucketImpl(bucketName, location, acl, headers);
    }

    @Override
    public void putObjectWithRequestEntityImpl(String bucketName, StorageObject object,
                                               HttpEntity requestEntity, Map<String, String> requestParams) throws ServiceException {
        super.putObjectWithRequestEntityImpl(bucketName, object, requestEntity, requestParams);
    }

    @Override
    public StorageObject getObjectImpl(boolean headOnly, String bucketName, String objectKey,
                                       Calendar ifModifiedSince, Calendar ifUnmodifiedSince,
                                       String[] ifMatchTags, String[] ifNoneMatchTags,
                                       Long byteRangeStart, Long byteRangeEnd, String versionId,
                                       Map<String, Object> requestHeaders,
                                       Map<String, String> requestParameters) throws ServiceException {
        return super.getObjectImpl(headOnly, bucketName, objectKey, ifModifiedSince, ifUnmodifiedSince, ifMatchTags, ifNoneMatchTags, byteRangeStart, byteRangeEnd,
                versionId, requestHeaders, requestParameters);
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
        return session.getSignatureIdentifier();
    }

    /**
     * @return header prefix for general Google Storage headers: x-goog-.
     */
    @Override
    public String getRestHeaderPrefix() {
        return session.getRestHeaderPrefix();
    }

    /**
     * @return header prefix for Google Storage metadata headers: x-goog-meta-.
     */
    @Override
    public String getRestMetadataPrefix() {
        return session.getRestMetadataPrefix();
    }

    @Override
    protected int getHttpPort() {
        return session.getHost().getPort();
    }

    @Override
    protected int getHttpsPort() {
        return session.getHost().getPort();
    }

    @Override
    public boolean isHttpsOnly() {
        return session.getHost().getProtocol().isSecure();
    }

    @Override
    protected XmlResponsesSaxParser getXmlResponseSaxParser() throws ServiceException {
        return session.getXmlResponseSaxParser();
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
    public void authorizeHttpRequest(final String bucketName, final HttpUriRequest httpMethod, final HttpContext context,
                                     final String forceRequestSignatureVersion) throws ServiceException {
        if(forceRequestSignatureVersion != null) {
            final S3Protocol.AuthenticationHeaderSignatureVersion authenticationHeaderSignatureVersion
                    = S3Protocol.AuthenticationHeaderSignatureVersion.valueOf(StringUtils.remove(forceRequestSignatureVersion, "-"));
            log.warn(String.format("Switched authentication signature version to %s", forceRequestSignatureVersion));
            session.setSignatureVersion(authenticationHeaderSignatureVersion);
        }
    }

    @Override
    public HttpResponse performRestGet(final String bucketName, final String objectKey,
                                       final Map<String, String> requestParameters, final Map<String, Object> requestHeaders,
                                       final int[] expectedStatusCodes) throws ServiceException {
        return super.performRestGet(bucketName, objectKey, requestParameters, requestHeaders, expectedStatusCodes);
    }

    @Override
    protected boolean isXmlContentType(final String contentType) {
        if(null == contentType) {
            return false;
        }
        if(StringUtils.startsWithIgnoreCase(contentType, "application/xml")) {
            return true;
        }
        if(StringUtils.startsWithIgnoreCase(contentType, "text/xml")) {
            return true;
        }
        return false;
    }


    /**
     * @return Null if no container component in hostname prepended
     */
    public static String findBucketInHostname(final Host host) {
        if(StringUtils.isBlank(host.getProtocol().getDefaultHostname())) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("No default hostname set in %s", host.getProtocol()));
            }
            return null;
        }
        final String hostname = host.getHostname();
        if(hostname.equals(host.getProtocol().getDefaultHostname())) {
            return null;
        }
        if(hostname.endsWith(host.getProtocol().getDefaultHostname())) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Find bucket name in %s", hostname));
            }
            return ServiceUtils.findBucketNameInHostname(hostname, host.getProtocol().getDefaultHostname());
        }
        return null;
    }
}
