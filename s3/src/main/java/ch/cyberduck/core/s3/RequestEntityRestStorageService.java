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

import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.TranscriptListener;
import ch.cyberduck.core.http.HttpConnectionPoolBuilder;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpUriRequest;
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
import org.jets3t.service.security.AWSCredentials;

import java.util.Calendar;
import java.util.Map;

public class RequestEntityRestStorageService extends RestS3Service {
    private static final Logger log = Logger.getLogger(RequestEntityRestStorageService.class);

    private final S3Session session;

    private final Preferences preferences
            = PreferencesFactory.get();

    public RequestEntityRestStorageService(final S3Session session,
                                           final Jets3tProperties configuration,
                                           final HttpConnectionPoolBuilder pool,
                                           final TranscriptListener listener) {
        super(session.getHost().getCredentials().isAnonymousLogin() ? null :
                        new AWSCredentials(null, null) {
                            @Override
                            public String getAccessKey() {
                                return session.getHost().getCredentials().getUsername();
                            }

                            @Override
                            public String getSecretKey() {
                                return session.getHost().getCredentials().getPassword();
                            }
                        },
                new PreferencesUseragentProvider().get(), null, configuration);
        this.session = session;
        // Always inject new pool to builder on connect because the pool is shutdown on disconnect
        final HttpClientBuilder builder = pool.build(listener);
        builder.disableContentCompression();
        builder.setRetryHandler(new S3HttpRequestRetryHandler(this, preferences.getInteger("http.connections.retry")));
        this.setHttpClient(builder.build());
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
                                             final String objectKey, final Map<String, String> requestParameters)
            throws S3ServiceException {
        final HttpUriRequest request = super.setupConnection(method, bucketName, objectKey, requestParameters);
        if(preferences.getBoolean("s3.upload.expect-continue")) {
            if("PUT".equals(request.getMethod())) {
                // #7621
                request.addHeader(HTTP.EXPECT_DIRECTIVE, HTTP.EXPECT_CONTINUE);
            }
        }
        if(preferences.getBoolean("s3.bucket.requesterpays")) {
            // Downloading Objects in Requester Pays Buckets
            if("GET".equals(request.getMethod()) || "POST".equals(request.getMethod())) {
                // For GET and POST requests, include x-amz-request-payer : requester in the header
                request.addHeader("x-amz-request-payer", "requester");
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
        if(forceRequestSignatureVersion != null
                && !StringUtils.equals(session.getSignatureVersion().toString(), forceRequestSignatureVersion)) {
            log.warn(String.format("Switched authentication signature version to %s", forceRequestSignatureVersion));
            session.setSignatureVersion(S3Protocol.AuthenticationHeaderSignatureVersion.valueOf(
                    StringUtils.remove(forceRequestSignatureVersion, "-"))
            );
        }
        if(session.authorize(httpMethod, this.getProviderCredentials())) {
            return;
        }
        super.authorizeHttpRequest(httpMethod, context, forceRequestSignatureVersion);
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
}
