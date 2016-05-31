package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Lifecycle;
import ch.cyberduck.core.features.Logging;
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.identity.DefaultCredentialsIdentityConfiguration;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.oauth.OAuth2AuthorizationService;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.ProxyFinder;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.s3.S3SingleUploadService;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.http.client.methods.HttpUriRequest;
import org.apache.log4j.Logger;
import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.AccessControlListHandler;
import org.jets3t.service.impl.rest.GSAccessControlListHandler;
import org.jets3t.service.impl.rest.XmlResponsesSaxParser;
import org.jets3t.service.model.WebsiteConfig;
import org.jets3t.service.security.OAuth2Credentials;
import org.jets3t.service.security.OAuth2Tokens;
import org.jets3t.service.security.ProviderCredentials;
import org.jets3t.service.utils.oauth.OAuthConstants;
import org.jets3t.service.utils.oauth.OAuthUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;

import com.google.api.client.auth.oauth2.Credential;

public class GoogleStorageSession extends S3Session {
    private static final Logger log = Logger.getLogger(GoogleStorageSession.class);

    private Preferences preferences
            = PreferencesFactory.get();

    public final OAuth2AuthorizationService tokens = new OAuth2AuthorizationService(OAuthConstants.GSOAuth2_10.Endpoints.Token,
            OAuthConstants.GSOAuth2_10.Endpoints.Authorization,
            preferences.getProperty("google.storage.oauth.clientid"),
            preferences.getProperty("google.storage.oauth.secret"),
            Collections.singletonList(OAuthConstants.GSOAuth2_10.Scopes.FullControl.toString())
    ).withLegacyPrefix("Google");

    public GoogleStorageSession(final Host h) {
        super(h);
    }

    public GoogleStorageSession(final Host host, final X509TrustManager trust, final X509KeyManager key) {
        super(host, trust, key);
    }

    public GoogleStorageSession(final Host host, final X509TrustManager trust, final X509KeyManager key, final ProxyFinder proxy) {
        super(host, trust, key, proxy);
    }

    @Override
    protected Jets3tProperties configure() {
        final Jets3tProperties configuration = super.configure();
        configuration.setProperty("s3service.enable-storage-classes", String.valueOf(false));
        configuration.setProperty("s3service.disable-dns-buckets", String.valueOf(true));
        return configuration;
    }

    @Override
    protected boolean authorize(final HttpUriRequest request, final ProviderCredentials credentials)
            throws ServiceException {
        if(credentials instanceof OAuth2Credentials) {
            request.setHeader("x-goog-api-version", "2");
            OAuth2Tokens tokens;
            try {
                tokens = ((OAuth2Credentials) credentials).getOAuth2Tokens();
            }
            catch(IOException e) {
                throw new ServiceException(e.getMessage(), e);
            }
            if(tokens == null) {
                throw new ServiceException("Cannot authenticate using OAuth2 until initial tokens are provided");
            }
            log.debug("Authorizing service request with OAuth2 access token: " + tokens.getAccessToken());
            request.setHeader("Authorization", "OAuth " + tokens.getAccessToken());
            return true;
        }
        return false;
    }

    @Override
    public void login(final HostPasswordStore keychain, final LoginCallback prompt,
                      final CancelCallback cancel, final Cache<Path> cache) throws BackgroundException {
        // Project ID needs OAuth2 authentication
        final OAuth2Credentials oauth = new OAuth2Credentials(
                new OAuthUtils(client.getHttpClient(),
                        OAuthUtils.OAuthImplementation.GOOGLE_STORAGE_OAUTH2_10,
                        preferences.getProperty("google.storage.oauth.clientid"),
                        preferences.getProperty("google.storage.oauth.secret")),
                preferences.getProperty("application.name"));

        final Credential tokens = this.tokens.authorize(this, keychain, prompt);
        oauth.setOAuth2Tokens(new OAuth2Tokens(tokens.getAccessToken(), tokens.getRefreshToken(), new Date(tokens.getExpirationTimeMilliseconds())));

        client.setProviderCredentials(oauth);

        if(host.getCredentials().isPassed()) {
            log.warn(String.format("Skip verifying credentials with previous successful authentication event for %s", this));
            return;
        }
        // List all buckets and cache
        try {
            // List all buckets and cache
            final Path root = new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.directory, Path.Type.volume));
            cache.put(root, this.list(root, new DisabledListProgressListener()));
        }
        catch(BackgroundException e) {
            throw new LoginFailureException(e.getDetail(false), e);
        }
    }

    @Override
    protected XmlResponsesSaxParser getXmlResponseSaxParser() throws ServiceException {
        return new XmlResponsesSaxParser(this.configure(), false) {
            @Override
            public AccessControlListHandler parseAccessControlListResponse(InputStream inputStream) throws ServiceException {
                return this.parseAccessControlListResponse(inputStream, new GSAccessControlListHandler());
            }

            @Override
            public BucketLoggingStatusHandler parseLoggingStatusResponse(InputStream inputStream) throws ServiceException {
                return super.parseLoggingStatusResponse(inputStream, new GSBucketLoggingStatusHandler());
            }

            @Override
            public WebsiteConfig parseWebsiteConfigurationResponse(InputStream inputStream) throws ServiceException {
                return super.parseWebsiteConfigurationResponse(inputStream, new GSWebsiteConfigurationHandler());
            }
        };
    }

    /**
     * @return the identifier for the signature algorithm.
     */
    @Override
    protected String getSignatureIdentifier() {
        return "GOOG1";
    }

    /**
     * @return header prefix for general Google Storage headers: x-goog-.
     */
    @Override
    protected String getRestHeaderPrefix() {
        return "x-goog-";
    }

    /**
     * @return header prefix for Google Storage metadata headers: x-goog-meta-.
     */
    @Override
    protected String getRestMetadataPrefix() {
        return "x-goog-meta-";
    }

    @Override
    protected String getProjectId() {
        if(client.getProviderCredentials() instanceof OAuth2Credentials) {
            return host.getCredentials().getUsername();
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type) {
        if(type == Upload.class) {
            return (T) new S3SingleUploadService(this);
        }
        if(type == Write.class) {
            return (T) new S3WriteFeature(this);
        }
        if(type == Delete.class) {
            return (T) new S3DefaultDeleteFeature(this);
        }
        if(type == Directory.class) {
            return (T) new GoogleStorageDirectoryFeature(this);
        }
        if(type == AclPermission.class) {
            return (T) new GoogleStorageAccessControlListFeature(this);
        }
        if(type == DistributionConfiguration.class) {
            return (T) new GoogleStorageWebsiteDistributionConfiguration(this);
        }
        if(type == IdentityConfiguration.class) {
            return (T) new DefaultCredentialsIdentityConfiguration(host);
        }
        if(type == Logging.class) {
            return (T) new GoogleStorageLoggingFeature(this);
        }
        if(type == Lifecycle.class) {
            return null;
        }
        if(type == Versioning.class) {
            return null;
        }
        if(type == Encryption.class) {
            return null;
        }
        if(type == Redundancy.class) {
            return null;
        }
        if(type == UrlProvider.class) {
            return (T) new GoogleStorageUrlProvider(this);
        }
        return super.getFeature(type);
    }
}
