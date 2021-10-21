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
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.jets3t.service.Constants;
import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.XmlResponsesSaxParser;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.StorageBucketLoggingStatus;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.model.WebsiteConfig;
import org.jets3t.service.utils.ServiceUtils;

import java.util.Calendar;
import java.util.Map;

public class RequestEntityRestStorageService extends RestS3Service {
    private static final Logger log = Logger.getLogger(RequestEntityRestStorageService.class);

    private final S3Session session;
    private final Jets3tProperties properties;

    private static Jets3tProperties toProperties(final Host bookmark, final S3Protocol.AuthenticationHeaderSignatureVersion signatureVersion) {
        final Jets3tProperties properties = new Jets3tProperties();
        final PreferencesReader preferences = new HostPreferences(bookmark);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Configure for endpoint %s", bookmark));
        }
        // Use default endpoint for region lookup
        properties.setProperty("s3service.s3-endpoint", bookmark.getHostname());
        if(InetAddressUtils.isIPv4Address(bookmark.getHostname()) || InetAddressUtils.isIPv6Address(bookmark.getHostname())) {
            properties.setProperty("s3service.disable-dns-buckets", String.valueOf(true));
        }
        else {
            properties.setProperty("s3service.disable-dns-buckets",
                    String.valueOf(preferences.getBoolean("s3.bucket.virtualhost.disable")));
        }
        properties.setProperty("s3service.enable-storage-classes", String.valueOf(true));
        if(StringUtils.isNotBlank(bookmark.getProtocol().getContext())) {
            if(!Scheme.isURL(bookmark.getProtocol().getContext())) {
                properties.setProperty("s3service.s3-endpoint-virtual-path",
                        PathNormalizer.normalize(bookmark.getProtocol().getContext()));
            }
        }
        properties.setProperty("s3service.https-only", String.valueOf(bookmark.getProtocol().isSecure()));
        if(bookmark.getProtocol().isSecure()) {
            properties.setProperty("s3service.s3-endpoint-https-port", String.valueOf(bookmark.getPort()));
        }
        else {
            properties.setProperty("s3service.s3-endpoint-http-port", String.valueOf(bookmark.getPort()));
        }
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
        properties.setProperty("storage-service.default-region", bookmark.getRegion());
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
        final RequestEntityRestStorageService authorizer = this;
        configuration.setRetryHandler(new S3HttpRequestRetryHandler(authorizer, new HostPreferences(session.getHost()).getInteger("http.connections.retry")));
        configuration.setRedirectStrategy(new S3BucketRegionRedirectStrategy(this, session, authorizer));
        this.setHttpClient(configuration.build());
    }

    public Jets3tProperties getConfiguration() {
        return properties;
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

    @Override
    protected HttpUriRequest setupConnection(final HTTP_METHOD method, final String bucketName,
                                             final String objectKey, final Map<String, String> requestParameters) throws S3ServiceException {
        final Host host = session.getHost();
        // Apply default configuration
        final PreferencesReader preferences = new HostPreferences(session.getHost());
        if(S3Session.isAwsHostname(host.getHostname(), false)) {
            // Check if not already set to accelerated endpoint
            if(properties.getStringProperty("s3service.s3-endpoint", preferences.getProperty("s3.hostname.default")).matches("s3-accelerate(\\.dualstack)?\\.amazonaws\\.com")) {
                log.debug("Skip adjusting endpoint with transfer acceleration");
            }
            else {
                // Only for AWS set endpoint to region specific
                if(StringUtils.isNotBlank(bucketName) && (requestParameters == null || !requestParameters.containsKey("location"))) {
                    try {
                        // Determine region for bucket using cache
                        final Location.Name region = new S3LocationFeature(session, regionEndpointCache).getLocation(bucketName);
                        if(Location.unknown == region) {
                            log.warn(String.format("Failure determining bucket location for %s", bucketName));
                        }
                        else {
                            final String endpoint;
                            if(preferences.getBoolean("s3.endpoint.dualstack.enable")) {
                                endpoint = String.format(preferences.getProperty("s3.endpoint.format.ipv6"), region.getIdentifier());
                            }
                            else {
                                endpoint = String.format(preferences.getProperty("s3.endpoint.format.ipv4"), region.getIdentifier());
                            }
                            if(log.isDebugEnabled()) {
                                log.debug(String.format("Set endpoint to %s", endpoint));
                            }
                            properties.setProperty("s3service.s3-endpoint", endpoint);
                        }
                    }
                    catch(BackgroundException e) {
                        // Ignore failure reading location for bucket
                        log.error(String.format("Failure %s determining bucket location for %s", e, bucketName));
                    }
                }
                else {
                    if(StringUtils.isNotBlank(host.getRegion())) {
                        // Use default region
                        final String endpoint;
                        if(preferences.getBoolean("s3.endpoint.dualstack.enable")) {
                            endpoint = String.format(preferences.getProperty("s3.endpoint.format.ipv6"), host.getRegion());
                        }
                        else {
                            endpoint = String.format(preferences.getProperty("s3.endpoint.format.ipv4"), host.getRegion());
                        }
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Set endpoint to %s", endpoint));
                        }
                        properties.setProperty("s3service.s3-endpoint", endpoint);
                    }
                    if(StringUtils.isBlank(bucketName)) {
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Determine bucket from hostname %s", host.getHostname()));
                        }
                        final String bucketNameInHostname = RequestEntityRestStorageService.findBucketInHostname(host);
                        if(bucketNameInHostname != null) {
                            if(log.isDebugEnabled()) {
                                log.debug(String.format("Determined bucket %s from hostname %s", bucketNameInHostname, host.getHostname()));
                            }
                            if(!StringUtils.startsWith(properties.getStringProperty("s3service.s3-endpoint", host.getProtocol().getDefaultHostname()),
                                    bucketNameInHostname)) {
                                final String endpoint = String.format("%s.%s", bucketNameInHostname, properties.getStringProperty("s3service.s3-endpoint",
                                        host.getProtocol().getDefaultHostname()));
                                if(log.isDebugEnabled()) {
                                    log.debug(String.format("Set endpoint to %s", endpoint));
                                }
                                properties.setProperty("s3service.s3-endpoint", endpoint);
                            }
                        }
                    }
                }
            }
        }
        final HttpUriRequest request = super.setupConnection(method, bucketName, objectKey, requestParameters);
        if(preferences.getBoolean("s3.upload.expect-continue")) {
            if("PUT".equals(request.getMethod())) {
                // #7621
                if(!properties.getBoolProperty("s3service.disable-expect-continue", false)) {
                    request.addHeader(HTTP.EXPECT_DIRECTIVE, HTTP.EXPECT_CONTINUE);
                }
            }
        }
        if(preferences.getBoolean("s3.bucket.requesterpays")) {
            // Only for AWS
            if(S3Session.isAwsHostname(host.getHostname())) {
                // Downloading Objects in Requester Pays Buckets
                if("GET".equals(request.getMethod()) || "POST".equals(request.getMethod())) {
                    if(!properties.getBoolProperty("s3service.disable-request-payer", false)) {
                        // For GET and POST requests, include x-amz-request-payer : requester in the header
                        request.addHeader("x-amz-request-payer", "requester");
                    }
                }
            }
        }
        return request;
    }

    @Override
    protected boolean isTargettingGoogleStorageService() {
        return session.getHost().getHostname().equals(Constants.GS_DEFAULT_HOSTNAME);
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
    public void authorizeHttpRequest(final HttpUriRequest httpMethod, final HttpContext context,
                                     final String forceRequestSignatureVersion) throws ServiceException {
        if(forceRequestSignatureVersion != null) {
            final S3Protocol.AuthenticationHeaderSignatureVersion authenticationHeaderSignatureVersion
                    = S3Protocol.AuthenticationHeaderSignatureVersion.valueOf(StringUtils.remove(forceRequestSignatureVersion, "-"));
            log.warn(String.format("Switched authentication signature version to %s", forceRequestSignatureVersion));
            session.setSignatureVersion(authenticationHeaderSignatureVersion);
        }
        super.authorizeHttpRequest(httpMethod, context, forceRequestSignatureVersion);
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
    protected static String findBucketInHostname(final Host host) {
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
