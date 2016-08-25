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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.analytics.AnalyticsProvider;
import ch.cyberduck.core.analytics.QloudstatAnalyticsProvider;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.cloudfront.WebsiteCloudFrontDistributionConfiguration;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
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
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.core.udt.qloudsonic.QloudsonicTransferOption;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.log4j.Logger;
import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.XmlResponsesSaxParser;
import org.jets3t.service.model.MultipartUpload;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.security.ProviderCredentials;

import java.util.EnumSet;

public class S3Session extends HttpSession<RequestEntityRestStorageService> {
    private static final Logger log = Logger.getLogger(S3Session.class);

    private DistributionConfiguration cdn;

    private Versioning versioning;

    private Preferences preferences
            = PreferencesFactory.get();

    private S3Protocol.AuthenticationHeaderSignatureVersion authenticationHeaderSignatureVersion
            = S3Protocol.AuthenticationHeaderSignatureVersion.getDefault(host.getProtocol());

    public S3Session(final Host host) {
        super(host, new LaxHostnameDelegatingTrustManager(new DisabledX509TrustManager(), host.getHostname()), new DefaultX509KeyManager());
    }

    public S3Session(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, new LaxHostnameDelegatingTrustManager(trust, host.getHostname()), key);
    }

    public S3Session(final Host host, final X509TrustManager trust, final X509KeyManager key, final ProxyFinder proxy) {
        super(host, new LaxHostnameDelegatingTrustManager(trust, host.getHostname()), key, proxy);
    }

    @Override
    protected void logout() throws BackgroundException {
        try {
            client.shutdown();
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map(e);
        }
        finally {
            super.logout();
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
            configuration.setProperty("s3service.s3-endpoint", host.getProtocol().getDefaultHostname());
            configuration.setProperty("s3service.disable-dns-buckets",
                    String.valueOf(preferences.getBoolean("s3.bucket.virtualhost.disable")));
        }
        else {
            configuration.setProperty("s3service.s3-endpoint", host.getHostname());
            configuration.setProperty("s3service.disable-dns-buckets", String.valueOf(true));
        }
        configuration.setProperty("s3service.enable-storage-classes", String.valueOf(true));
        if(StringUtils.isNotBlank(host.getProtocol().getContext())) {
            if(StringUtils.startsWith(host.getProtocol().getContext(), String.valueOf(Path.DELIMITER))) {
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
        return configuration;
    }

    @Override
    public RequestEntityRestStorageService connect(final HostKeyCallback key) throws BackgroundException {
        return new RequestEntityRestStorageService(this, this.configure(), builder, this);
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt, final CancelCallback cancel,
                      final Cache<Path> cache)
            throws BackgroundException {
        if(Scheme.isURL(host.getProtocol().getContext())) {
            try {
                client.setProviderCredentials(new S3SessionCredentialsRetriever(this, host.getProtocol().getContext()).get());
            }
            catch(ConnectionTimeoutException | ConnectionRefusedException | ResolveFailedException | NotfoundException | InteroperabilityException e) {
                log.warn(String.format("Failure to retrieve session credentials from . %s", e.getMessage()));
                throw new LoginFailureException(e.getDetail(false), e);
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
        final Path home = new S3HomeFinderService(this).find();
        cache.put(home, this.list(home, new DisabledListProgressListener() {
            @Override
            public void chunk(final Path parent, final AttributedList<Path> list) throws ListCanceledException {
                try {
                    cancel.verify();
                }
                catch(ConnectionCanceledException e) {
                    throw new ListCanceledException(list, e);
                }
            }
        }));
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(directory.isRoot()) {
            // List all buckets
            return new S3BucketListService(this, new S3LocationFeature.S3Region(host.getRegion())).list(directory, listener);
        }
        else {
            final AttributedList<Path> objects = new S3ObjectListService(this).list(directory, listener);
            try {
                for(MultipartUpload upload : new S3DefaultMultipartService(this).find(directory)) {
                    final PathAttributes attributes = new PathAttributes();
                    attributes.setDuplicate(true);
                    attributes.setVersionId(upload.getUploadId());
                    attributes.setModificationDate(upload.getInitiatedDate().getTime());
                    objects.add(new Path(directory, upload.getObjectKey(), EnumSet.of(Path.Type.file, Path.Type.upload), attributes));
                }
            }
            catch(AccessDeniedException | InteroperabilityException e) {
                log.warn(String.format("Ignore failure listing incomplete multipart uploads. %s", e.getDetail()));
            }
            return objects;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type) {
        if(type == Read.class) {
            return (T) new S3ReadFeature(this);
        }
        if(type == Write.class) {
            return (T) new S3MultipartWriteFeature(this);
        }
        if(type == Download.class) {
            return (T) new S3ThresholdDownloadService(this, trust, key, new QloudsonicTransferOption());
        }
        if(type == Upload.class) {
            return (T) new S3ThresholdUploadService(this, trust, key, new QloudsonicTransferOption());
        }
        if(type == Directory.class) {
            return (T) new S3DirectoryFeature(this);
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
            return (T) new S3MetadataFeature(this);
        }
        if(type == Touch.class) {
            return (T) new S3TouchFeature(this);
        }
        if(type == Location.class) {
            // Only for AWS
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
            if(preferences.getBoolean("s3.revisions.enable")) {
                // Only for AWS
                if(host.getHostname().endsWith(preferences.getProperty("s3.hostname.default"))) {
                    if(null == versioning) {
                        versioning = new S3VersioningFeature(this, new S3AccessControlListFeature(this));
                    }
                    return (T) versioning;
                }
            }
            return null;
        }
        if(type == Logging.class) {
            // Only for AWS
            if(host.getHostname().endsWith(preferences.getProperty("s3.hostname.default"))) {
                return (T) new S3LoggingFeature(this);
            }
            return null;
        }
        if(type == Lifecycle.class) {
            // Only for AWS
            if(host.getHostname().endsWith(preferences.getProperty("s3.hostname.default"))) {
                return (T) new S3LifecycleConfiguration(this);
            }
            return null;
        }
        if(type == Encryption.class) {
            // Only for AWS
            if(host.getHostname().endsWith(preferences.getProperty("s3.hostname.default"))) {
                return (T) new KMSEncryptionFeature(this);
            }
            return null;
        }
        if(type == Redundancy.class) {
            // Only for AWS
            if(host.getHostname().endsWith(preferences.getProperty("s3.hostname.default"))) {
                return (T) new S3StorageClassFeature(this);
            }
            return null;
        }
        if(type == IdentityConfiguration.class) {
            // Only for AWS
            if(host.getHostname().endsWith(preferences.getProperty("s3.hostname.default"))) {
                return (T) new AmazonIdentityConfiguration(host);
            }
            return null;
        }
        if(type == DistributionConfiguration.class) {
            if(null == cdn) {
                cdn = new WebsiteCloudFrontDistributionConfiguration(this, trust, key);
            }
            return (T) cdn;
        }
        if(type == UrlProvider.class) {
            return (T) new S3UrlProvider(this);
        }
        if(type == Attributes.class) {
            return (T) new S3AttributesFeature(this);
        }
        if(type == Home.class) {
            return (T) new S3HomeFinderService(this);
        }
        return super.getFeature(type);
    }
}