package ch.cyberduck.core.gstorage;

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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Lifecycle;
import ch.cyberduck.core.features.Logging;
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.identity.DefaultCredentialsIdentityConfiguration;
import ch.cyberduck.core.identity.IdentityConfiguration;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.s3.S3BucketListService;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.s3.S3SingleUploadService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.log4j.Logger;
import org.jets3t.service.Jets3tProperties;
import org.jets3t.service.ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.acl.gs.GSAccessControlList;
import org.jets3t.service.impl.rest.AccessControlListHandler;
import org.jets3t.service.impl.rest.GSAccessControlListHandler;
import org.jets3t.service.impl.rest.XmlResponsesSaxParser;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.model.WebsiteConfig;
import org.jets3t.service.security.OAuth2Credentials;
import org.jets3t.service.security.OAuth2Tokens;
import org.jets3t.service.security.ProviderCredentials;
import org.jets3t.service.utils.oauth.OAuthConstants;
import org.jets3t.service.utils.oauth.OAuthUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Google Storage for Developers is a new service for developers to store and
 * access data in Google's cloud. It offers developers direct access to Google's
 * scalable storage and networking infrastructure as well as powerful authentication
 * and data sharing mechanisms.
 *
 * @version $Id$
 */
public class GoogleStorageSession extends S3Session {
    private static final Logger log = Logger.getLogger(GoogleStorageSession.class);

    public GoogleStorageSession(Host h) {
        super(h);
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
    public void login(final PasswordStore keychain, final LoginController controller) throws BackgroundException {
        if(NumberUtils.isNumber(host.getCredentials().getUsername())) {
            // Project ID needs OAuth2 authentication
            final OAuth2Credentials oauth = new OAuth2Credentials(
                    new OAuthUtils(route,
                            OAuthUtils.OAuthImplementation.GOOGLE_STORAGE_OAUTH2_10,
                            Preferences.instance().getProperty("google.storage.oauth.clientid"),
                            Preferences.instance().getProperty("google.storage.oauth.secret")),
                    Preferences.instance().getProperty("application.name"));
            final String accesstoken = keychain.getPassword(host.getProtocol().getScheme(),
                    host.getPort(), URI.create(OAuthConstants.GSOAuth2_10.Endpoints.Token).getHost(), "Google OAuth2 Access Token");
            final String refreshtoken = keychain.getPassword(host.getProtocol().getScheme(),
                    host.getPort(), URI.create(OAuthConstants.GSOAuth2_10.Endpoints.Token).getHost(), "Google OAuth2 Refresh Token");
            if(StringUtils.isEmpty(accesstoken) || StringUtils.isEmpty(refreshtoken)) {
                // Query access token from URL to visit in browser
                final String url = oauth.generateBrowserUrlToAuthorizeNativeApplication(
                        OAuthConstants.GSOAuth2_10.Scopes.FullControl
                );
                final LoginOptions options = new LoginOptions();
                options.keychain = false;
                controller.prompt(host.getProtocol(), host.getCredentials(),
                        Locale.localizedString("OAuth2 Authentication", "Credentials"), url, options);

                try {
                    // Swap the given authorization token for access/refresh tokens
                    oauth.retrieveOAuth2TokensFromAuthorization(host.getCredentials().getPassword());
                    final OAuth2Tokens tokens = oauth.getOAuth2Tokens();
                    // Save for future use
                    keychain.addPassword(host.getProtocol().getScheme(),
                            host.getPort(), URI.create(OAuthConstants.GSOAuth2_10.Endpoints.Token).getHost(),
                            "Google OAuth2 Access Token", tokens.getAccessToken());
                    keychain.addPassword(host.getProtocol().getScheme(),
                            host.getPort(), URI.create(OAuthConstants.GSOAuth2_10.Endpoints.Token).getHost(),
                            "Google OAuth2 Refresh Token", tokens.getRefreshToken());

                    // Save expiry
                    Preferences.instance().setProperty("google.storage.oauth.expiry", tokens.getExpiry().getTime());
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map(e);
                }
            }
            else {
                // Re-use authentication tokens from last use
                oauth.setOAuth2Tokens(new OAuth2Tokens(accesstoken, refreshtoken,
                        new Date(Preferences.instance().getLong("google.storage.oauth.expiry"))));
            }
            client.setProviderCredentials(oauth);
            try {
                new S3BucketListService().list(this);
            }
            catch(BackgroundException e) {
                throw new LoginFailureException(e.getMessage(), e);
            }
        }
        else {
            super.login(keychain, controller);
        }
    }

    @Override
    public void upload(final Path file, final BandwidthThrottle throttle, final StreamListener listener,
                       final TransferStatus status) throws BackgroundException {
        final StorageObject object = this.createObjectDetails(file);
        new S3SingleUploadService(this).upload(file, throttle, listener, status, object);
    }

    @Override
    protected AccessControlList getPrivateCannedAcl() {
        return GSAccessControlList.REST_CANNED_PRIVATE;
    }

    @Override
    protected AccessControlList getPublicCannedReadAcl() {
        return GSAccessControlList.REST_CANNED_PUBLIC_READ;
    }

    @Override
    public List<Acl.User> getAvailableAclUsers() {
        final List<Acl.User> users = new ArrayList<Acl.User>(Arrays.asList(
                new Acl.CanonicalUser(),
                new Acl.GroupUser("AllAuthenticatedUsers", false),
                new Acl.GroupUser("AllUsers", false))
        );
        users.add(new Acl.EmailUser() {
            @Override
            public String getPlaceholder() {
                return Locale.localizedString("Google Account Email Address", "S3");
            }
        });
        // Google Apps customers can associate their email accounts with an Internet domain name. When you do
        // this, each email account takes the form username@yourdomain.com. You can specify a scope by using
        // any Internet domain name that is associated with a Google Apps account.
        users.add(new Acl.DomainUser(StringUtils.EMPTY) {
            @Override
            public String getPlaceholder() {
                return Locale.localizedString("Google Apps Domain", "S3");
            }
        });
        users.add(new Acl.EmailGroupUser(StringUtils.EMPTY, true) {
            @Override
            public String getPlaceholder() {
                return Locale.localizedString("Google Group Email Address", "S3");
            }
        });
        return users;
    }

    @Override
    public List<Acl.Role> getAvailableAclRoles(List<Path> files) {
        List<Acl.Role> roles = new ArrayList<Acl.Role>(Arrays.asList(
                new Acl.Role(org.jets3t.service.acl.Permission.PERMISSION_FULL_CONTROL.toString()),
                new Acl.Role(org.jets3t.service.acl.Permission.PERMISSION_READ.toString()))
        );
        for(Path file : files) {
            if(file.attributes().isVolume()) {
                // When applied to a bucket, this permission lets a user create objects, overwrite objects, and
                // delete objects in a bucket. This permission also lets a user list the contents of a bucket.
                // You cannot apply this permission to objects because bucket ACLs control who can upload,
                // overwrite, and delete objects. Also, you must grant READ permission if you grant WRITE permission.
                roles.add(new Acl.Role(org.jets3t.service.acl.Permission.PERMISSION_WRITE.toString()));
                break;
            }
        }
        return roles;
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
    public DescriptiveUrl toSignedUrl(final Path file, int seconds) {
        return DescriptiveUrl.EMPTY;
    }

    /**
     * Torrent links are not supported.
     *
     * @return Always null.
     */
    @Override
    public DescriptiveUrl toTorrentUrl(final Path path) {
        return DescriptiveUrl.EMPTY;
    }


    /**
     * This creates an URL that uses Cookie-based Authentication. The ACLs for the given Google user account
     * has to be setup first.
     * <p/>
     * Google Storage lets you provide browser-based authenticated downloads to users who do not have
     * Google Storage accounts. To do this, you apply Google account-based ACLs to the object and then
     * you provide users with a URL that is scoped to the object.
     *
     * @return URL to be displayed in browser
     */
    @Override
    public DescriptiveUrl toAuthenticatedUrl(final Path path) {
        if(path.attributes().isFile()) {
            // Authenticated browser download using cookie-based Google account authentication in conjunction with ACL
            return new DescriptiveUrl(String.format("https://sandbox.google.com/storage%s", path.getAbsolute()));
        }
        return DescriptiveUrl.EMPTY;
    }

    @Override
    public Set<DescriptiveUrl> getHttpURLs(final Path path) {
        Set<DescriptiveUrl> urls = super.getHttpURLs(path);
        DescriptiveUrl url = this.toAuthenticatedUrl(path);
        if(StringUtils.isNotBlank(url.getUrl())) {
            urls.add(new DescriptiveUrl(url.getUrl(),
                    MessageFormat.format(Locale.localizedString("{0} URL"), Locale.localizedString("Authenticated"))));
        }
        return urls;
    }

    @Override
    public <T> T getFeature(final Class<T> type, final LoginController prompt) {
        if(type == Delete.class) {
            return (T) new S3DefaultDeleteFeature(this);
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
        return super.getFeature(type, prompt);
    }
}
