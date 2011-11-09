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

import ch.cyberduck.core.*;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.s3.S3Session;

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
import org.jets3t.service.model.GSBucketLoggingStatus;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.OAuth2Credentials;
import org.jets3t.service.security.OAuth2Tokens;
import org.jets3t.service.security.ProviderCredentials;
import org.jets3t.service.utils.oauth.OAuthConstants;
import org.jets3t.service.utils.oauth.OAuthUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.*;

/**
 * Google Storage for Developers is a new service for developers to store and
 * access data in Google's cloud. It offers developers direct access to Google's
 * scalable storage and networking infrastructure as well as powerful authentication
 * and data sharing mechanisms.
 *
 * @version $Id$
 */
public class GSSession extends S3Session {
    private static Logger log = Logger.getLogger(GSSession.class);

    private static class Factory extends SessionFactory {
        @Override
        protected Session create(Host h) {
            return new GSSession(h);
        }
    }

    public static SessionFactory factory() {
        return new Factory();
    }

    protected GSSession(Host h) {
        super(h);
    }

    @Override
    protected void configure(String hostname) {
        super.configure(hostname);
        Jets3tProperties configuration = super.getProperties();
        configuration.setProperty("s3service.enable-storage-classes", String.valueOf(false));
    }

    @Override
    protected boolean authorize(HttpUriRequest httpMethod, ProviderCredentials credentials)
            throws IOException, ServiceException {
        if(credentials instanceof OAuth2Credentials) {
            OAuth2Tokens tokens = ((OAuth2Credentials) credentials).getOAuth2Tokens();
            if(tokens == null) {
                throw new ServiceException("Cannot authenticate using OAuth2 until initial tokens are provided");
            }
            log.debug("Authorizing service request with OAuth2 access token: " + tokens.getAccessToken());
            httpMethod.setHeader("Authorization", "OAuth " + tokens.getAccessToken());
            if(httpMethod.getURI().getHost().equals(this.getHost().getProtocol().getDefaultHostname())) {
                httpMethod.setHeader("x-goog-project-id", this.getHost().getCredentials().getUsername());
            }
            return true;
        }
        return false;
    }

    @Override
    protected void prompt(LoginController controller) throws IOException {
        final Credentials credentials = this.getHost().getCredentials();
        final ProviderCredentials provider = this.getProviderCredentials(controller, credentials);
        if(provider instanceof OAuth2Credentials) {
            final OAuth2Credentials oauth = (OAuth2Credentials) provider;
            final String acccesstoken = KeychainFactory.instance().getPassword(this.getHost().getProtocol().getScheme().name(),
                    this.getHost().getPort(), OAuthConstants.GSOAuth2_10.Endpoints.Token, "Google OAuth2 Access Token");
            final String refreshtoken = KeychainFactory.instance().getPassword(this.getHost().getProtocol().getScheme().name(),
                    this.getHost().getPort(), OAuthConstants.GSOAuth2_10.Endpoints.Token, "Google OAuth2 Refresh Token");
            if(StringUtils.isEmpty(acccesstoken) || StringUtils.isEmpty(refreshtoken)) {
                final String url = ((OAuth2Credentials) provider).generateBrowserUrlToAuthorizeNativeApplication(
                        OAuthConstants.GSOAuth2_10.Scopes.FullControl
                );
                final Credentials placeholder = new Credentials(credentials.getUsername(), null, false) {
                    @Override
                    public String getUsernamePlaceholder() {
                        return Locale.localizedString("x-goog-project-id", "Credentials");
                    }

                    @Override
                    public String getPasswordPlaceholder() {
                        return Locale.localizedString("Authorization code", "Credentials");
                    }
                };

                // Query access token from URL to visit in browser
                controller.prompt(this.getHost().getProtocol(), placeholder,
                        Locale.localizedString("OAuth2 Authentication", "Credentials"), url,
                        false, false, false);

                // Project ID
                credentials.setUsername(placeholder.getUsername());
                // Authorization code
                credentials.setPassword(placeholder.getPassword());

                this.message(MessageFormat.format(Locale.localizedString("Authenticating as {0}", "Status"),
                        credentials.getUsername()));

                // Swap the given authorization token for access/refresh tokens
                oauth.retrieveOAuth2TokensFromAuthorization(credentials.getPassword());

                final OAuth2Tokens tokens = oauth.getOAuth2Tokens();

                // Save for future use
                KeychainFactory.instance().addPassword(this.getHost().getProtocol().getScheme().name(),
                        this.getHost().getPort(), OAuthConstants.GSOAuth2_10.Endpoints.Token, "Google OAuth2 Access Token", tokens.getAccessToken());
                KeychainFactory.instance().addPassword(this.getHost().getProtocol().getScheme().name(),
                        this.getHost().getPort(), OAuthConstants.GSOAuth2_10.Endpoints.Token, "Google OAuth2 Refresh Token", tokens.getRefreshToken());

                // Save expiry
                Preferences.instance().setProperty("google.storage.oauth.expiry", tokens.getExpiry().getTime());
            }
            else {
                // Re-use authentication tokens from last use
                oauth.setOAuth2Tokens(new OAuth2Tokens(acccesstoken, refreshtoken,
                        new Date(Preferences.instance().getLong("google.storage.oauth.expiry"))));
            }
        }
        else {
            super.prompt(controller);
        }
    }

    private OAuth2Credentials oauth;

    @Override
    protected ProviderCredentials getProviderCredentials(final LoginController controller, final Credentials credentials) {
        if(credentials.isAnonymousLogin()) {
            return null;
        }
        if(NumberUtils.isNumber(credentials.getUsername())) {
            // Project ID needs OAuth2 authentication
            if(null == oauth) {
                oauth = new OAuth2Credentials(
                        new OAuthUtils(http(),
                                OAuthUtils.OAuthImplementation.GOOGLE_STORAGE_OAUTH2_10,
                                Preferences.instance().getProperty("google.storage.oauth.clientid"),
                                Preferences.instance().getProperty("google.storage.oauth.secret")),
                        Preferences.instance().getProperty("application.name"));
            }
            return oauth;
        }
        return super.getProviderCredentials(controller, credentials);
    }

    @Override
    public List<String> getSupportedStorageClasses() {
        return Arrays.asList(S3Object.STORAGE_CLASS_STANDARD);
    }

    @Override
    public List<String> getSupportedEncryptionAlgorithms() {
        return Collections.emptyList();
    }

    @Override
    public boolean isLoggingSupported() {
        return true;
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
                final GSBucketLoggingStatus status = new GSBucketLoggingStatus(
                        StringUtils.isNotBlank(destination) ? destination : container, null);
                if(enabled) {
                    status.setLogfilePrefix(Preferences.instance().getProperty("google.logging.prefix"));
                }
                this.check();
                this.getClient().setBucketLoggingStatusImpl(container, status);
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

    @Override
    public boolean isVersioningSupported() {
        return false;
    }

    @Override
    public boolean isMultipartUploadSupported() {
        return false;
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
    public Acl getPublicAcl(String container, boolean readable, boolean writable) {
        Acl acl = this.getPrivateAcl(container);
        if(readable) {
            acl.addAll(new Acl.GroupUser("AllUsers"),
                    new Acl.Role(org.jets3t.service.acl.Permission.PERMISSION_READ.toString()));
        }
        if(writable) {
            acl.addAll(new Acl.GroupUser("AllUsers"),
                    new Acl.Role(org.jets3t.service.acl.Permission.PERMISSION_WRITE.toString()));
        }
        return acl;
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
        return new XmlResponsesSaxParser(configuration, false) {
            @Override
            public AccessControlListHandler parseAccessControlListResponse(InputStream inputStream) throws ServiceException {
                return this.parseAccessControlListResponse(inputStream, new GSAccessControlListHandler());
            }

            @Override
            public BucketLoggingStatusHandler parseLoggingStatusResponse(InputStream inputStream) throws ServiceException {
                return super.parseLoggingStatusResponse(inputStream, new GSBucketLoggingStatusHandler());
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
    public boolean isCDNSupported() {
        return false;
    }
}
