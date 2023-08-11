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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.auth.AWSSessionCredentialsRetriever;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cloudfront.CloudFrontDistributionConfigurationPreloader;
import ch.cyberduck.core.cloudfront.WebsiteCloudFrontDistributionConfiguration;
import ch.cyberduck.core.date.RFC822DateFormatter;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.ResolveFailedException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.kms.KMSEncryptionFeature;
import ch.cyberduck.core.oauth.OAuth2AuthorizationService;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesReader;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.proxy.ProxyFactory;
import ch.cyberduck.core.restore.Glacier;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.shared.DefaultPathHomeFeature;
import ch.cyberduck.core.shared.DelegatingHomeFeature;
import ch.cyberduck.core.shared.DelegatingSchedulerFeature;
import ch.cyberduck.core.shared.DisabledBulkFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.sts.AWSProfileSTSCredentialsConfigurator;
import ch.cyberduck.core.sts.S3WebIdentityTokenExpiredResponseInterceptor;
import ch.cyberduck.core.sts.STSCredentialsRequestInterceptor;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.XmlResponsesSaxParser;
import org.jets3t.service.impl.rest.httpclient.RegionEndpointCache;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.security.AWSSessionCredentials;
import org.jets3t.service.security.ProviderCredentials;
import org.jets3t.service.utils.RestUtils;
import org.jets3t.service.utils.ServiceUtils;
import org.jets3t.service.utils.SignatureUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.amazonaws.auth.internal.SignerConstants;

import static com.amazonaws.services.s3.Headers.*;

public class S3Session extends HttpSession<RequestEntityRestStorageService> {
    private static final Logger log = LogManager.getLogger(S3Session.class);

    private final PreferencesReader preferences
            = new HostPreferences(host);

    private STSCredentialsRequestInterceptor authorizationService;

    private final S3AccessControlListFeature acl = new S3AccessControlListFeature(this);

    private final Versioning versioning = preferences.getBoolean("s3.versioning.enable")
            ? new S3VersioningFeature(this, acl) : null;

    private final RegionEndpointCache regions = new RegionEndpointCache();

    private final Glacier glacier = new Glacier(this, new S3LocationFeature(this, regions), trust, key);

    private final Encryption encryption = S3Session.isAwsHostname(host.getHostname())
            ? new KMSEncryptionFeature(this, new S3LocationFeature(this, regions), acl, trust, key) : null;

    private final WebsiteCloudFrontDistributionConfiguration cloudfront = new WebsiteCloudFrontDistributionConfiguration(this,
            new S3LocationFeature(this, regions), trust, key) {
        @Override
        public Distribution read(final Path container, final Distribution.Method method, final LoginCallback prompt) throws BackgroundException {
            final Distribution distribution = super.read(container, method, prompt);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Cache distribution %s", distribution));
            }
            // Replace previously cached value
            final Set<Distribution> cached = distributions.getOrDefault(container, new HashSet<>());
            cached.add(distribution);
            distributions.put(container, cached);
            return distribution;
        }
    };

    private final Map<Path, Set<Distribution>> distributions = new ConcurrentHashMap<>();

    private S3Protocol.AuthenticationHeaderSignatureVersion authenticationHeaderSignatureVersion
            = S3Protocol.AuthenticationHeaderSignatureVersion.getDefault(host.getProtocol());

    public S3Session(final Host host) {
        super(host, new S3BucketHostnameTrustManager(new DisabledX509TrustManager(), host.getHostname()), new DefaultX509KeyManager());
    }

    public S3Session(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new S3BucketHostnameTrustManager(trust, host.getHostname()), key);
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            client.shutdown();
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map(e);
        }
    }

    protected XmlResponsesSaxParser getXmlResponseSaxParser() throws ServiceException {
        return new XmlResponsesSaxParser(client.getConfiguration(), false);
    }

    /**
     * @return the identifier for the signature algorithm.
     */
    protected String getSignatureIdentifier() {
        return "AWS";
    }

    public S3Protocol.AuthenticationHeaderSignatureVersion getSignatureVersion() {
        return authenticationHeaderSignatureVersion;
    }

    public void setSignatureVersion(final S3Protocol.AuthenticationHeaderSignatureVersion authenticationHeaderSignatureVersion) {
        this.authenticationHeaderSignatureVersion = authenticationHeaderSignatureVersion;
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

    @Override
    protected RequestEntityRestStorageService connect(final Proxy proxy, final HostKeyCallback hostkey, final LoginCallback prompt, final CancelCallback cancel) {
        final HttpClientBuilder configuration = builder.build(proxy, this, prompt);

        if((host.getProtocol().isOAuthConfigurable())) {
            authorizationService = new STSCredentialsRequestInterceptor(builder.build(ProxyFactory.get()
                    .find(host.getProtocol().getSTSEndpoint()), this, prompt).build(), host, trust, key, prompt, this)
                    .withRedirectUri(host.getProtocol().getOAuthRedirectUrl())
                    .withFlowType(OAuth2AuthorizationService.FlowType.valueOf(host.getProtocol().getAuthorization()));

            configuration.addInterceptorLast(authorizationService);
            configuration.setServiceUnavailableRetryStrategy(new S3WebIdentityTokenExpiredResponseInterceptor(this, prompt, authorizationService));
        }

        // Only for AWS
        if(S3Session.isAwsHostname(host.getHostname())) {
            configuration.setServiceUnavailableRetryStrategy(new S3TokenExpiredResponseInterceptor(this,
                    new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key, prompt));
        }
        if(preferences.getBoolean("s3.upload.expect-continue")) {
            final String header = HTTP.EXPECT_DIRECTIVE;
            if(log.isDebugEnabled()) {
                log.debug(String.format("Add request handler for %s", header));
            }
            configuration.addInterceptorLast(new HttpRequestInterceptor() {
                @Override
                public void process(final HttpRequest request, final HttpContext context) {
                    if(HttpPut.METHOD_NAME.equals(request.getRequestLine().getMethod())) {
                        // #7621
                        request.addHeader(header, HTTP.EXPECT_CONTINUE);
                    }
                }
            });
        }
        if(preferences.getBoolean("s3.bucket.requesterpays")) {
            // Only for AWS
            if(S3Session.isAwsHostname(host.getHostname())) {
                final String header = REQUESTER_PAYS_HEADER;
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Add request handler for %s", header));
                }
                configuration.addInterceptorLast(new HttpRequestInterceptor() {
                    @Override
                    public void process(final HttpRequest request, final HttpContext context) {
                        // Downloading Objects in Requester Pays Buckets
                        if(HttpGet.METHOD_NAME.equals(request.getRequestLine().getMethod()) || HttpPost.METHOD_NAME.equals(request.getRequestLine().getMethod())) {
                            // For GET and POST requests, include x-amz-request-payer : requester in the header
                            request.addHeader(header, "requester");
                        }
                    }
                });
            }
        }
        configuration.addInterceptorLast(new HttpRequestInterceptor() {
            @Override
            public void process(final HttpRequest request, final HttpContext context) {
                // Ensure the required Host header is set prior to signing.
                final HttpHost host = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
                if(host != null) {
                    request.setHeader(HttpHeaders.HOST, host.toHostString());
                }
            }
        });
        configuration.addInterceptorLast(new HttpRequestInterceptor() {
            private final RFC822DateFormatter formatter = new RFC822DateFormatter();

            @Override
            public void process(final HttpRequest request, final HttpContext context) {
                request.setHeader(S3_ALTERNATE_DATE, SignatureUtils.formatAwsFlavouredISO8601Date(client.getCurrentTimeWithOffset()));
            }
        });
        configuration.addInterceptorLast(new HttpRequestInterceptor() {
            @Override
            public void process(final HttpRequest request, final HttpContext context) {
                final ProviderCredentials credentials = client.getProviderCredentials();
                if(credentials instanceof AWSSessionCredentials) {
                    request.setHeader(SECURITY_TOKEN, ((AWSSessionCredentials) credentials).getSessionToken());
                }
            }
        });
        configuration.addInterceptorLast(new HttpRequestInterceptor() {
            @Override
            public void process(final HttpRequest request, final HttpContext context) throws IOException {
                if(!client.isAuthenticatedConnection()) {
                    log.warn(String.format("Skip authentication request %s", request));
                    return;
                }
                final ProviderCredentials credentials = client.getProviderCredentials();
                final String bucketName = context.getAttribute("bucket").toString();
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Use bucket name %s from context for %s signature", bucketName, authenticationHeaderSignatureVersion));
                }
                final URI uri;
                try {
                    uri = new URI(request.getRequestLine().getUri());
                }
                catch(URISyntaxException e) {
                    throw new IOException(e);
                }
                switch(authenticationHeaderSignatureVersion) {
                    case AWS2: {
                        String path = uri.getRawPath();
                        // If bucket name is not already part of the full path, add it.
                        // This can be the case if the Host name has a bucket-name prefix,
                        // or if the Host name constitutes the bucket name for DNS-redirects.
                        if(!StringUtils.startsWith(path, bucketName)) {
                            path = String.format("/%s%s", bucketName, path);
                        }
                        final String queryString = uri.getRawQuery();
                        if(StringUtils.isNotBlank(queryString)) {
                            path += String.format("?%s", queryString);
                        }
                        // Generate a canonical string representing the operation.
                        final String canonicalString = RestUtils.makeServiceCanonicalString(
                                request.getRequestLine().getMethod(),
                                path,
                                this.getHeadersAsObject(request),
                                null,
                                getRestHeaderPrefix(),
                                client.getResourceParameterNames());
                        // Sign the canonical string.
                        final String signedCanonical = ServiceUtils.signWithHmacSha1(
                                credentials.getSecretKey(), canonicalString);
                        // Add encoded authorization to connection as HTTP Authorization header.
                        final String authorizationString = getSignatureIdentifier() + " "
                                + credentials.getAccessKey() + ":" + signedCanonical;
                        request.setHeader(HttpHeaders.AUTHORIZATION, authorizationString);
                        break;
                    }
                    case AWS4HMACSHA256: {
                        String region = regions.getRegionForBucketName(bucketName);
                        if(null == region) {
                            final HttpHost host = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
                            if(host != null) {
                                try {
                                    region = SignatureUtils.awsRegionForRequest(new URI(host.toURI()));
                                }
                                catch(URISyntaxException e) {
                                    throw new IOException(e);
                                }
                            }
                            if(region != null) {
                                if(log.isDebugEnabled()) {
                                    log.debug(String.format("Cache region %s for bucket %s", region, bucketName));
                                }
                                regions.putRegionForBucketName(bucketName, region);
                            }
                        }
                        if(null == region) {
                            region = host.getRegion();
                        }
                        if(null == region) {
                            region = new HostPreferences(host).getProperty("s3.location");
                        }
                        final HttpUriRequest message = (HttpUriRequest) request;
                        String requestPayloadHexSHA256Hash =
                                SignatureUtils.awsV4GetOrCalculatePayloadHash(message);
                        message.setHeader(SignerConstants.X_AMZ_CONTENT_SHA256, requestPayloadHexSHA256Hash);
                        // Generate AWS-flavoured ISO8601 timestamp string
                        final String timestampISO8601 = message.getFirstHeader(S3_ALTERNATE_DATE).getValue();
                        // Canonical request string
                        final String canonicalRequestString =
                                SignatureUtils.awsV4BuildCanonicalRequestString(uri,
                                        request.getRequestLine().getMethod(), this.getHeadersAsString(request), requestPayloadHexSHA256Hash);
                        // String to sign
                        final String stringToSign = SignatureUtils.awsV4BuildStringToSign(
                                authenticationHeaderSignatureVersion.toString(), canonicalRequestString,
                                timestampISO8601, region);
                        // Signing key
                        final byte[] signingKey = SignatureUtils.awsV4BuildSigningKey(
                                credentials.getSecretKey(), timestampISO8601, region);
                        // Request signature
                        final String signature = ServiceUtils.toHex(ServiceUtils.hmacSHA256(
                                signingKey, ServiceUtils.stringToBytes(stringToSign)));
                        // Authorization header value
                        final String authorizationHeaderValue =
                                SignatureUtils.awsV4BuildAuthorizationHeaderValue(
                                        credentials.getAccessKey(), signature,
                                        authenticationHeaderSignatureVersion.toString(), canonicalRequestString,
                                        timestampISO8601, region);
                        message.setHeader(HttpHeaders.AUTHORIZATION, authorizationHeaderValue);
                        break;
                    }
                }
            }

            final class HttpHeaderFilter implements Predicate<Header> {
                @Override
                public boolean test(final Header header) {
                    return !new HostPreferences(host).getList("s3.signature.headers.exclude").stream()
                            .filter(s -> StringUtils.equalsIgnoreCase(s, header.getName())).findAny().isPresent();
                }
            }

            private Map<String, String> getHeadersAsString(final HttpRequest request) {
                final Map<String, String> headers = new HashMap<>();
                for(Header header : Arrays.stream(request.getAllHeaders()).filter(new HttpHeaderFilter()).collect(Collectors.toList())) {
                    headers.put(StringUtils.lowerCase(StringUtils.trim(header.getName())), StringUtils.trim(header.getValue()));
                }
                return headers;
            }

            private Map<String, Object> getHeadersAsObject(final HttpRequest request) {
                final Map<String, Object> headers = new HashMap<>();
                for(Header header : Arrays.stream(request.getAllHeaders()).filter(new HttpHeaderFilter()).collect(Collectors.toList())) {
                    headers.put(StringUtils.lowerCase(StringUtils.trim(header.getName())), StringUtils.trim(header.getValue()));
                }
                return headers;
            }
        });
        final RequestEntityRestStorageService client = new RequestEntityRestStorageService(this, configuration);
        client.setRegionEndpointCache(regions);
        return client;
    }

    @Override
    public void login(final Proxy proxy, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        if(host.getProtocol().isOAuthConfigurable()) {
            authorizationService.authorize(host, prompt, cancel);
        }

        if(Scheme.isURL(host.getProtocol().getContext())) {
            try {
                final Credentials temporary = new AWSSessionCredentialsRetriever(trust, key, this, host.getProtocol().getContext()).get();
                client.setProviderCredentials(new AWSSessionCredentials(temporary.getUsername(), temporary.getPassword(),
                        temporary.getToken()));
            }
            catch(ConnectionTimeoutException | ConnectionRefusedException | ResolveFailedException | NotfoundException |
                  InteroperabilityException e) {
                log.warn(String.format("Failure to retrieve session credentials from . %s", e.getMessage()));
                throw new LoginFailureException(e.getDetail(false), e);
            }
        }
        else {
            final Credentials credentials;
            // get temporary STS credentials with oAuth token
            if(host.getProtocol().isOAuthConfigurable()) {
                credentials = authorizationService.assumeRoleWithWebIdentity();
            }
            // Only for AWS
            else if(isAwsHostname(host.getHostname())) {
                // Try auto-configure
                credentials = new AWSProfileSTSCredentialsConfigurator(
                        new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key, prompt).configure(host);
            }
            else {
                credentials = host.getCredentials();
            }
            if(StringUtils.isNotBlank(credentials.getToken())) {
                client.setProviderCredentials(credentials.isAnonymousLogin() ? null :
                        new AWSSessionCredentials(credentials.getUsername(), credentials.getPassword(),
                                credentials.getToken()));
            }
            else {
                client.setProviderCredentials(credentials.isAnonymousLogin() ? null :
                        new AWSCredentials(credentials.getUsername(), credentials.getPassword()));
            }
        }
        if(host.getCredentials().isPassed()) {
            log.warn(String.format("Skip verifying credentials with previous successful authentication event for %s", this));
            return;
        }
        try {
            final Path home = new DelegatingHomeFeature(new DefaultPathHomeFeature(host)).find();
            final Location.Name location = new S3PathStyleFallbackAdapter<>(client, new BackgroundExceptionCallable<Location.Name>() {
                @Override
                public Location.Name call() throws BackgroundException {
                    return new S3LocationFeature(S3Session.this, regions).getLocation(home);
                }
            }).call();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Retrieved region %s", location));
            }
            if(!Location.unknown.equals(location)) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Set default region to %s determined from %s", location, home));
                }
                //
                host.setProperty("s3.location", location.getIdentifier());
            }
        }
        catch(AccessDeniedException | InteroperabilityException e) {
            log.warn(String.format("Failure %s querying region", e));
            final Path home = new DefaultHomeFinderService(this).find();
            if(log.isDebugEnabled()) {
                log.debug(String.format("Retrieved %s", home));
            }
        }
    }

    public static boolean isAwsHostname(final String hostname) {
        return isAwsHostname(hostname, true);
    }

    public static boolean isAwsHostname(final String hostname, boolean cn) {
        if(cn) {
            // Matches s3.amazonaws.com
            // Matches s3.cn-north-1.amazonaws.com.cn
            // Matches s3.cn-northwest-1.amazonaws.com.cn
            // Matches s3-us-gov-west-1.amazonaws.com
            return hostname.matches("([a-z0-9\\-]+\\.)?s3(\\.dualstack)?(\\.[a-z0-9\\-]+)?(\\.vpce)?\\.amazonaws\\.com(\\.cn)?");
        }
        return hostname.matches("([a-z0-9\\-]+\\.)?s3(\\.dualstack)?(\\.[a-z0-9\\-]+)?(\\.vpce)?\\.amazonaws\\.com");
    }

    public STSCredentialsRequestInterceptor getAuthorizationService() {
        return authorizationService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == ListService.class) {
            final S3ListService proxy = new S3ListService(this, acl);
            return (T) new ListService() {
                @Override
                public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
                    return new S3PathStyleFallbackAdapter<>(client, new BackgroundExceptionCallable<AttributedList<Path>>() {
                        @Override
                        public AttributedList<Path> call() throws BackgroundException {
                            return proxy.list(directory, listener);
                        }
                    }).call();
                }
            };
        }
        if(type == Read.class) {
            return (T) new S3ReadFeature(this);
        }
        if(type == MultipartWrite.class) {
            if(S3Session.isAwsHostname(host.getHostname())) {
                return (T) new S3MultipartWriteFeature(this, acl);
            }
            return (T) new S3MultipartWriteFeature(this, acl);
        }
        if(type == Write.class) {
            return (T) new S3WriteFeature(this, acl);
        }
        if(type == Upload.class) {
            return (T) new S3ThresholdUploadService(this, acl);
        }
        if(type == Directory.class) {
            final S3DirectoryFeature proxy = new S3DirectoryFeature(this, new S3WriteFeature(this, acl), acl);
            return (T) new Directory<StorageObject>() {
                @Override
                public Path mkdir(final Path folder, final TransferStatus status) throws BackgroundException {
                    return new S3PathStyleFallbackAdapter<>(client, new BackgroundExceptionCallable<Path>() {
                        @Override
                        public Path call() throws BackgroundException {
                            return proxy.mkdir(folder, status);
                        }
                    }).call();
                }

                @Override
                public boolean isSupported(final Path workdir, final String name) {
                    return proxy.isSupported(workdir, name);
                }

                @Override
                public Directory<StorageObject> withWriter(final Write<StorageObject> writer) {
                    return proxy.withWriter(writer);
                }
            };
        }
        if(type == Move.class) {
            return (T) new S3MoveFeature(this, acl);
        }
        if(type == Copy.class) {
            if(S3Session.isAwsHostname(host.getHostname())) {
                return (T) new S3ThresholdCopyFeature(this);
            }
            return (T) new S3CopyFeature(this, acl);
        }
        if(type == Delete.class) {
            if(S3Session.isAwsHostname(host.getHostname())) {
                return (T) new S3ThresholdDeleteFeature(this, acl);
            }
            return (T) new S3DefaultDeleteFeature(this);
        }
        if(type == AclPermission.class) {
            return (T) acl;
        }
        if(type == Headers.class) {
            return (T) new S3MetadataFeature(this, acl);
        }
        if(type == Metadata.class) {
            return (T) new S3MetadataFeature(this, acl);
        }
        if(type == Touch.class) {
            return (T) new S3TouchFeature(this, acl);
        }
        if(type == Location.class) {
            return (T) new S3LocationFeature(this, regions);
        }
        if(type == Versioning.class) {
            return (T) versioning;
        }
        if(type == Logging.class) {
            return (T) new S3LoggingFeature(this);
        }
        if(type == Lifecycle.class) {
            return (T) new S3LifecycleConfiguration(this);
        }
        if(type == Encryption.class) {
            return (T) encryption;
        }
        if(type == Redundancy.class) {
            return (T) new S3StorageClassFeature(this, acl);
        }
        if(type == DistributionConfiguration.class) {
            return (T) cloudfront;
        }
        if(type == UrlProvider.class) {
            return (T) new S3UrlProvider(this, distributions);
        }
        if(type == PromptUrlProvider.class) {
            return (T) new S3PublicUrlProvider(this, acl);
        }
        if(type == Find.class) {
            return (T) new S3FindFeature(this, acl);
        }
        if(type == AttributesFinder.class) {
            final S3AttributesFinderFeature proxy = new S3AttributesFinderFeature(this, acl);
            return (T) new AttributesFinder() {
                @Override
                public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
                    return new S3PathStyleFallbackAdapter<>(client, new BackgroundExceptionCallable<PathAttributes>() {
                        @Override
                        public PathAttributes call() throws BackgroundException {
                            return proxy.find(file, listener);
                        }
                    }).call();
                }
            };
        }
        if(type == TransferAcceleration.class) {
            // Only for AWS. Disable transfer acceleration for AWS GovCloud
            if(host.getHostname().endsWith(preferences.getProperty("s3.hostname.default"))) {
                return (T) new S3TransferAccelerationService(this);
            }
            return null;
        }
        if(type == Bulk.class) {
            if(preferences.getBoolean("s3.accelerate.enable")) {
                // Only for AWS. Disable transfer acceleration for AWS GovCloud
                if(host.getHostname().endsWith(preferences.getProperty("s3.hostname.default"))) {
                    return (T) new S3BulkTransferAccelerationFeature(this, new S3TransferAccelerationService(this));
                }
            }
            return (T) new DisabledBulkFeature();
        }
        if(type == Search.class) {
            return (T) new S3SearchFeature(this, acl);
        }
        if(type == Scheduler.class) {
            return (T) new DelegatingSchedulerFeature(new CloudFrontDistributionConfigurationPreloader(this));
        }
        if(type == Restore.class) {
            return (T) glacier;
        }
        if(type == Timestamp.class) {
            if(preferences.getBoolean("s3.timestamp.enable")) {
                return (T) new S3TimestampFeature(this);
            }
            return null;
        }
        if(type == PathContainerService.class) {
            return (T) new S3PathContainerService(host);
        }
        return super._getFeature(type);
    }
}
