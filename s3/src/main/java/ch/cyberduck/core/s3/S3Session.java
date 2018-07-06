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
import ch.cyberduck.core.auth.AWSProfileCredentialsConfigurator;
import ch.cyberduck.core.auth.AWSSessionCredentialsRetriever;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cloudfront.CloudFrontDistributionConfigurationPreloader;
import ch.cyberduck.core.cloudfront.WebsiteCloudFrontDistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.ListCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.ResolveFailedException;
import ch.cyberduck.core.features.*;
import ch.cyberduck.core.http.HttpSession;
import ch.cyberduck.core.iam.AmazonIdentityConfiguration;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.kms.KMSEncryptionFeature;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.shared.DelegatingSchedulerFeature;
import ch.cyberduck.core.shared.DisabledBulkFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.ThreadLocalHostnameDelegatingTrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.log4j.Logger;
import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.XmlResponsesSaxParser;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.security.AWSSessionCredentials;
import org.jets3t.service.security.ProviderCredentials;

import java.util.Collections;
import java.util.Map;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetSessionTokenRequest;
import com.amazonaws.services.securitytoken.model.GetSessionTokenResult;

public class S3Session extends HttpSession<RequestEntityRestStorageService> {
    private static final Logger log = Logger.getLogger(S3Session.class);

    private final Preferences preferences
        = PreferencesFactory.get();

    private Versioning versioning
        = new S3VersioningFeature(this, new S3AccessControlListFeature(this));

    private Map<Path, Distribution> distributions = Collections.emptyMap();

    private S3Protocol.AuthenticationHeaderSignatureVersion authenticationHeaderSignatureVersion
        = S3Protocol.AuthenticationHeaderSignatureVersion.getDefault(host.getProtocol());

    public S3Session(final Host host) {
        super(host, host.getHostname().endsWith(PreferencesFactory.get().getProperty("s3.hostname.default")) ?
            new LaxHostnameDelegatingTrustManager(new DisabledX509TrustManager(), host.getHostname()) :
            new ThreadLocalHostnameDelegatingTrustManager(new DisabledX509TrustManager(), host.getHostname()), new DefaultX509KeyManager());
    }

    public S3Session(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, host.getHostname().endsWith(PreferencesFactory.get().getProperty("s3.hostname.default")) ?
            new LaxHostnameDelegatingTrustManager(trust, host.getHostname()) :
            new ThreadLocalHostnameDelegatingTrustManager(trust, host.getHostname()), key);
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

    protected Jets3tProperties configure() {
        final Jets3tProperties configuration = new Jets3tProperties();
        if(log.isDebugEnabled()) {
            log.debug(String.format("Configure for endpoint %s", host));
        }
        if(host.getHostname().endsWith(preferences.getProperty("s3.hostname.default"))) {
            // Only for AWS
            configuration.setProperty("s3service.s3-endpoint", preferences.getProperty("s3.hostname.default"));
            configuration.setProperty("s3service.disable-dns-buckets",
                String.valueOf(preferences.getBoolean("s3.bucket.virtualhost.disable")));
        }
        else {
            configuration.setProperty("s3service.s3-endpoint", host.getHostname());
            configuration.setProperty("s3service.disable-dns-buckets", String.valueOf(true));
        }
        configuration.setProperty("s3service.enable-storage-classes", String.valueOf(true));
        if(StringUtils.isNotBlank(host.getProtocol().getContext())) {
            if(!Scheme.isURL(host.getProtocol().getContext())) {
                configuration.setProperty("s3service.s3-endpoint-virtual-path",
                    PathNormalizer.normalize(host.getProtocol().getContext()));
            }
        }
        configuration.setProperty("s3service.https-only", String.valueOf(host.getProtocol().isSecure()));
        if(host.getProtocol().isSecure()) {
            configuration.setProperty("s3service.s3-endpoint-https-port", String.valueOf(host.getPort()));
        }
        else {
            configuration.setProperty("s3service.s3-endpoint-http-port", String.valueOf(host.getPort()));
        }
        // The maximum number of retries that will be attempted when an S3 connection fails
        // with an InternalServer error. To disable retries of InternalError failures, set this to 0.
        configuration.setProperty("s3service.internal-error-retry-max", String.valueOf(0));
        // The maximum number of concurrent communication threads that will be started by
        // the multi-threaded service for upload and download operations.
        configuration.setProperty("s3service.max-thread-count", String.valueOf(1));
        configuration.setProperty("httpclient.proxy-autodetect", String.valueOf(false));
        configuration.setProperty("httpclient.retry-max", String.valueOf(0));
        configuration.setProperty("storage-service.internal-error-retry-max", String.valueOf(0));
        configuration.setProperty("storage-service.request-signature-version", authenticationHeaderSignatureVersion.toString());
        configuration.setProperty("storage-service.disable-live-md5", String.valueOf(true));
        configuration.setProperty("storage-service.default-region", host.getRegion());
        return configuration;
    }

    @Override
    public RequestEntityRestStorageService connect(final Proxy proxy, final HostKeyCallback hostkey, final LoginCallback prompt) throws BackgroundException {
        return new RequestEntityRestStorageService(this, this.configure(), builder.build(proxy, this, prompt));
    }

    @Override
    public void login(final Proxy proxy, final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel) throws BackgroundException {
        if(host.getProtocol().isTokenConfigurable()) {
            if(Scheme.isURL(host.getProtocol().getContext())) {
                try {
                    final Credentials temporary = new AWSSessionCredentialsRetriever(trust, key, this, host.getProtocol().getContext()).get();
                    client.setProviderCredentials(new AWSSessionCredentials(temporary.getUsername(), temporary.getPassword(),
                        temporary.getToken()));
                }
                catch(ConnectionTimeoutException | ConnectionRefusedException | ResolveFailedException | NotfoundException | InteroperabilityException e) {
                    log.warn(String.format("Failure to retrieve session credentials from . %s", e.getMessage()));
                    throw new LoginFailureException(e.getDetail(false), e);
                }
            }
            else {
                if(StringUtils.isBlank(host.getCredentials().getToken())) {
                    final String profile = host.getCredentials().getUsername();
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Load credentials from configuration using profile %s", profile));
                    }
                    final Credentials credentials = new AWSProfileCredentialsConfigurator(profile).configure(host);
                    if(credentials.validate(host.getProtocol(), new LoginOptions(host.getProtocol()))) {
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Obtained temporary credentials %s from AWS profile configuration", credentials));
                        }
                        client.setProviderCredentials(new AWSSessionCredentials(credentials.getUsername(),
                            credentials.getPassword(), credentials.getToken()));
                    }
                    else {
                        // Not obtained from config in ~/.aws/config
                        final ClientConfiguration configuration = new ClientConfiguration();
                        final int timeout = PreferencesFactory.get().getInteger("connection.timeout.seconds") * 1000;
                        configuration.setConnectionTimeout(timeout);
                        configuration.setSocketTimeout(timeout);
                        final UseragentProvider ua = new PreferencesUseragentProvider();
                        configuration.setUserAgentPrefix(ua.get());
                        configuration.setMaxErrorRetry(0);
                        configuration.setMaxConnections(1);
                        configuration.setUseGzip(PreferencesFactory.get().getBoolean("http.compression.enable"));
                        switch(proxy.getType()) {
                            case HTTP:
                            case HTTPS:
                                configuration.setProxyHost(proxy.getHostname());
                                configuration.setProxyPort(proxy.getPort());
                        }
                        final AWSSecurityTokenService sts = AWSSecurityTokenServiceClientBuilder.standard()
                            .withCredentials(new AWSStaticCredentialsProvider(new com.amazonaws.auth.AWSCredentials() {
                                @Override
                                public String getAWSAccessKeyId() {
                                    return host.getCredentials().getUsername();
                                }

                                @Override
                                public String getAWSSecretKey() {
                                    return host.getCredentials().getPassword();
                                }
                            }))
                            .withClientConfiguration(configuration)
                            .withRegion(Regions.DEFAULT_REGION).build();
                        // Obtain token from MFA
                        //final Credentials token = versioning.getToken(prompt);
                        final GetSessionTokenResult result = sts.getSessionToken(new GetSessionTokenRequest()
                            // Specify this value if the IAM user has a policy that requires MFA authentication
                            .withSerialNumber(null)
                            // The value provided by the MFA device, if MFA is required
                            .withTokenCode(null)
                            .withDurationSeconds(preferences.getInteger("sts.token.duration.seconds")));
                        client.setProviderCredentials(new AWSSessionCredentials(result.getCredentials().getAccessKeyId(),
                            result.getCredentials().getSecretAccessKey(),
                            result.getCredentials().getSessionToken()));
                    }
                }
                else {
                    client.setProviderCredentials(new AWSSessionCredentials(host.getCredentials().getUsername(), host.getCredentials().getPassword(),
                        host.getCredentials().getToken()));
                }
            }
        }
        else {
            client.setProviderCredentials(host.getCredentials().isAnonymousLogin() ? null :
                new AWSCredentials(host.getCredentials().getUsername(), host.getCredentials().getPassword()));
        }
        if(host.getCredentials().isPassed()) {
            log.warn(String.format("Skip verifying credentials with previous successful authentication event for %s", this));
            return;
        }
        try {
            this.getFeature(ListService.class).list(new S3HomeFinderService(this).find(), new DisabledListProgressListener() {
                @Override
                public void chunk(final Path parent, final AttributedList<Path> list) throws ListCanceledException {
                    throw new ListCanceledException(list);
                }
            });
        }
        catch(ListCanceledException e) {
            // Success
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T _getFeature(final Class<T> type) {
        if(type == ListService.class) {
            return (T) new S3ListService(this);
        }
        if(type == Read.class) {
            return (T) new S3ReadFeature(this);
        }
        if(type == MultipartWrite.class) {
            if(host.getHostname().endsWith(preferences.getProperty("s3.hostname.default"))) {
                return (T) new S3MultipartWriteFeature(this);
            }
            return (T) new S3MultipartWriteFeature(this);
        }
        if(type == Write.class) {
            return (T) new S3WriteFeature(this);
        }
        if(type == Upload.class) {
            return (T) new S3ThresholdUploadService(this);
        }
        if(type == Directory.class) {
            return (T) new S3DirectoryFeature(this, new S3WriteFeature(this, new S3DisabledMultipartService()));
        }
        if(type == Move.class) {
            return (T) new S3MoveFeature(this);
        }
        if(type == Copy.class) {
            if(host.getHostname().endsWith(preferences.getProperty("s3.hostname.default"))) {
                return (T) new S3ThresholdCopyFeature(this);
            }
            return (T) new S3CopyFeature(this);
        }
        if(type == Delete.class) {
            if(host.getHostname().endsWith(preferences.getProperty("s3.hostname.default"))) {
                return (T) new S3MultipleDeleteFeature(this);
            }
            return (T) new S3DefaultDeleteFeature(this);
        }
        if(type == AclPermission.class) {
            return (T) new S3AccessControlListFeature(this);
        }
        if(type == Headers.class) {
            return (T) new S3MetadataFeature(this, new S3AccessControlListFeature(this));
        }
        if(type == Metadata.class) {
            return (T) new S3MetadataFeature(this, new S3AccessControlListFeature(this));
        }
        if(type == Touch.class) {
            return (T) new S3TouchFeature(this);
        }
        if(type == Location.class) {
            if(this.isConnected()) {
                return (T) new S3LocationFeature(this, client.getRegionEndpointCache());
            }
            return (T) new S3LocationFeature(this);
        }
        if(type == AnalyticsProvider.class) {
            // Only for AWS
            if(host.getHostname().endsWith(preferences.getProperty("s3.hostname.default"))) {
                return (T) new QloudstatAnalyticsProvider();
            }
            return null;
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
            // Only for AWS
            if(host.getHostname().endsWith(preferences.getProperty("s3.hostname.default"))) {
                return (T) new KMSEncryptionFeature(this);
            }
            return null;
        }
        if(type == Redundancy.class) {
            return (T) new S3StorageClassFeature(this);
        }
        if(type == IdentityConfiguration.class) {
            // Only for AWS
            if(host.getHostname().endsWith(preferences.getProperty("s3.hostname.default"))) {
                return (T) new AmazonIdentityConfiguration(host);
            }
            return null;
        }
        if(type == DistributionConfiguration.class) {
            return (T) new WebsiteCloudFrontDistributionConfiguration(this, distributions, trust, key);
        }
        if(type == UrlProvider.class) {
            return (T) new S3UrlProvider(this);
        }
        if(type == Find.class) {
            return (T) new S3FindFeature(this);
        }
        if(type == AttributesFinder.class) {
            return (T) new S3AttributesFinderFeature(this);
        }
        if(type == Home.class) {
            return (T) new S3HomeFinderService(this);
        }
        if(type == TransferAcceleration.class) {
            // Only for AWS
            if(host.getHostname().endsWith(preferences.getProperty("s3.hostname.default"))) {
                return (T) new S3TransferAccelerationService(this);
            }
            return null;
        }
        if(type == Bulk.class) {
            // Only for AWS
            if(host.getHostname().endsWith(preferences.getProperty("s3.hostname.default"))) {
                return (T) new S3BulkTransferAccelerationFeature(this, new S3TransferAccelerationService(this));
            }
            return (T) new DisabledBulkFeature();
        }
        if(type == Search.class) {
            return (T) new S3SearchFeature(this);
        }
        if(type == IdProvider.class) {
            return (T) new S3VersionIdProvider(this);
        }
        if(type == Scheduler.class) {
            return (T) new DelegatingSchedulerFeature(
                new CloudFrontDistributionConfigurationPreloader(this) {
                    @Override
                    public Map<Path, Distribution> operate(final PasswordCallback callback, final Path container) throws BackgroundException {
                        return distributions = super.operate(callback, container);
                    }
                }
            );
        }
        return super._getFeature(type);
    }
}
