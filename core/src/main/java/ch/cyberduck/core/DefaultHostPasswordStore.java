/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

package ch.cyberduck.core;

import ch.cyberduck.core.exception.LocalAccessDeniedException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class DefaultHostPasswordStore implements HostPasswordStore {
    private static final Logger log = LogManager.getLogger(DefaultHostPasswordStore.class);

    /**
     * Find password for login
     *
     * @param bookmark Hostname
     * @return the password fetched from the keychain or null if it was not found
     */
    @Override
    public String findLoginPassword(final Host bookmark) {
        if(StringUtils.isEmpty(bookmark.getHostname())) {
            log.warn("No hostname given");
            return null;
        }
        final Credentials credentials = bookmark.getCredentials();
        if(StringUtils.isEmpty(credentials.getUsername())) {
            log.warn("No username given");
            return null;
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("Fetching login password from keychain for %s", bookmark));
        }
        final String password;
        try {
            password = this.getPassword(bookmark.getProtocol().getScheme(), bookmark.getPort(),
                    bookmark.getHostname(), credentials.getUsername());
        }
        catch(LocalAccessDeniedException e) {
            log.warn(String.format("Failure %s searching in keychain", e));
            return null;
        }
        if(null == password) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Password not found in keychain for %s", bookmark));
            }
        }
        return password;
    }

    @Override
    public String findLoginToken(final Host bookmark) {
        if(StringUtils.isEmpty(bookmark.getHostname())) {
            log.warn("No hostname given");
            return null;
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("Fetching login token from keychain for %s", bookmark));
        }
        final Credentials credentials = bookmark.getCredentials();
        // Find token named like "Shared Access Signature (SAS) Token"
        final String token;
        try {
            token = this.getPassword(bookmark.getProtocol().getScheme(), bookmark.getPort(),
                    bookmark.getHostname(), StringUtils.isEmpty(credentials.getUsername()) ?
                            bookmark.getProtocol().getTokenPlaceholder() : String.format("%s (%s)", bookmark.getProtocol().getTokenPlaceholder(), credentials.getUsername()));
        }
        catch(LocalAccessDeniedException e) {
            log.warn(String.format("Failure %s searching in keychain", e));
            return null;
        }
        if(null == token) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Token not found in keychain for %s", bookmark));
            }
        }
        return token;
    }

    /**
     * Find passphrase for private key
     *
     * @param bookmark Hostname
     * @return the password fetched from the keychain or null if it was not found
     */
    @Override
    public String findPrivateKeyPassphrase(final Host bookmark) {
        if(StringUtils.isEmpty(bookmark.getHostname())) {
            log.warn("No hostname given");
            return null;
        }
        final Credentials credentials = bookmark.getCredentials();
        if(StringUtils.isEmpty(credentials.getUsername())) {
            log.warn("No username given");
            return null;
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("Fetching private key passphrase from keychain for %s", bookmark));
        }
        if(credentials.isPublicKeyAuthentication()) {
            final Local key = credentials.getIdentity();
            try {
                String passphrase = this.getPassword(bookmark.getHostname(), key.getAbbreviatedPath());
                if(null == passphrase) {
                    // Interoperability with OpenSSH (ssh, ssh-agent, ssh-add)
                    passphrase = this.getPassword("SSH", key.getAbsolute());
                }
                if(null == passphrase) {
                    // Backward compatibility
                    passphrase = this.getPassword("SSHKeychain", key.getAbbreviatedPath());
                }
                if(null == passphrase) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Passphrase not found in keychain for %s", key));
                    }
                }
                return passphrase;
            }
            catch(LocalAccessDeniedException e) {
                log.warn(String.format("Failure %s searching in keychain", e));
                return null;
            }
        }
        else {
            return null;
        }
    }

    @Override
    public OAuthTokens findOAuthTokens(final Host bookmark) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Fetching OAuth tokens from keychain for %s", bookmark));
        }
        final PasswordStorePrefixService service = new PasswordStorePrefixServiceFactory().create(bookmark);

        final String prefix = service.getPrefix();
        final String hostname = service.getHostname();
        final int port = service.getPort();
        final Scheme scheme = service.getScheme();
        try {
            final String expiry = this.getPassword(hostname, String.format("%s OAuth2 Token Expiry", prefix));
            return (new OAuthTokens(
                    this.getPassword(scheme, port, hostname,
                            String.format("%s OAuth2 Access Token", prefix)),
                    this.getPassword(scheme, port, hostname,
                            String.format("%s OAuth2 Refresh Token", prefix)),
                    expiry != null ? Long.parseLong(expiry) : -1L,
                    this.getPassword(scheme, port, hostname,
                            String.format("%s OIDC Id Token", prefix))));
        }
        catch(LocalAccessDeniedException e) {
            log.warn(String.format("Failure %s searching in keychain", e));
            return OAuthTokens.EMPTY;
        }
    }

    @Override
    public void save(final Host bookmark) throws LocalAccessDeniedException {
        if(StringUtils.isEmpty(bookmark.getHostname())) {
            log.warn("No hostname given");
            return;
        }
        final Credentials credentials = bookmark.getCredentials();
        if(log.isInfoEnabled()) {
            log.info(String.format("Save credentials %s for bookmark %s", credentials, bookmark));
        }
        if(credentials.isPublicKeyAuthentication()) {
            this.addPassword(bookmark.getHostname(), credentials.getIdentity().getAbbreviatedPath(),
                    credentials.getIdentityPassphrase());
        }
        if(credentials.isPasswordAuthentication()) {
            if(StringUtils.isEmpty(credentials.getUsername())) {
                log.warn(String.format("No username in credentials for bookmark %s", bookmark.getHostname()));
                return;
            }
            if(StringUtils.isEmpty(credentials.getPassword())) {
                log.warn(String.format("No password in credentials for bookmark %s", bookmark.getHostname()));
                return;
            }
            this.addPassword(bookmark.getProtocol().getScheme(), bookmark.getPort(),
                    bookmark.getHostname(), credentials.getUsername(), credentials.getPassword());
        }
        if(credentials.isTokenAuthentication()) {
            this.addPassword(bookmark.getProtocol().getScheme(), bookmark.getPort(),
                    bookmark.getHostname(), StringUtils.isEmpty(credentials.getUsername()) ?
                            bookmark.getProtocol().getTokenPlaceholder() : String.format("%s (%s)", bookmark.getProtocol().getTokenPlaceholder(), credentials.getUsername()),
                    credentials.getToken());
        }
        if(credentials.isOAuthAuthentication()) {
            final PasswordStorePrefixService oAuthPrefix = new PasswordStorePrefixServiceFactory().create(bookmark);

            final String prefix = oAuthPrefix.getPrefix();
            final String hostname = oAuthPrefix.getHostname();
            final int port = oAuthPrefix.getPort();
            final Scheme scheme = oAuthPrefix.getScheme();
            if(StringUtils.isNotBlank(credentials.getOauth().getAccessToken())) {
                this.addPassword(scheme, port, hostname,
                        String.format("%s OAuth2 Access Token", prefix), credentials.getOauth().getAccessToken());
            }
            if(StringUtils.isNotBlank(credentials.getOauth().getRefreshToken())) {
                this.addPassword(scheme, port, hostname,
                        String.format("%s OAuth2 Refresh Token", prefix), credentials.getOauth().getRefreshToken());
            }
            // Save expiry
            if(credentials.getOauth().getExpiryInMilliseconds() != null) {
                this.addPassword(hostname, String.format("%s OAuth2 Token Expiry", prefix),
                        String.valueOf(credentials.getOauth().getExpiryInMilliseconds()));
            }
            if(StringUtils.isNotBlank(credentials.getOauth().getIdToken())) {
                this.addPassword(scheme, port, hostname,
                        String.format("%s OIDC Id Token", prefix), credentials.getOauth().getIdToken());
            }
        }
    }

    @Override
    public void delete(final Host bookmark) throws LocalAccessDeniedException {
        if(log.isInfoEnabled()) {
            log.info(String.format("Delete password for bookmark %s", bookmark));
        }
        final Credentials credentials = bookmark.getCredentials();
        final Protocol protocol = bookmark.getProtocol();
        if(protocol.isPrivateKeyConfigurable()) {
            this.deletePassword(bookmark.getHostname(), credentials.getIdentity().getAbbreviatedPath());
        }
        if(protocol.isPasswordConfigurable()) {
            if(StringUtils.isEmpty(credentials.getUsername())) {
                log.warn(String.format("No username in credentials for bookmark %s", bookmark.getHostname()));
                return;
            }
            this.deletePassword(protocol.getScheme(), bookmark.getPort(), bookmark.getHostname(),
                    credentials.getUsername());
        }
        if(protocol.isTokenConfigurable()) {
            this.deletePassword(protocol.getScheme(), bookmark.getPort(), bookmark.getHostname(),
                    StringUtils.isEmpty(credentials.getUsername()) ?
                            protocol.getTokenPlaceholder() : String.format("%s (%s)", protocol.getTokenPlaceholder(), credentials.getUsername()));
        }
        if(protocol.isOAuthConfigurable()) {
            final PasswordStorePrefixService oAuthPrefix = new PasswordStorePrefixServiceFactory().create(bookmark);

            final String prefix = oAuthPrefix.getPrefix();
            final String hostname = oAuthPrefix.getHostname();
            final int port = oAuthPrefix.getPort();
            final Scheme scheme = oAuthPrefix.getScheme();
            if(StringUtils.isNotBlank(credentials.getOauth().getAccessToken())) {
                this.deletePassword(scheme, port, hostname,
                        String.format("%s OAuth2 Access Token", prefix));
            }
            if(StringUtils.isNotBlank(credentials.getOauth().getRefreshToken())) {
                this.deletePassword(scheme, port, hostname,
                        String.format("%s OAuth2 Refresh Token", prefix));
            }
            // Save expiry
            if(credentials.getOauth().getExpiryInMilliseconds() != null) {
                this.deletePassword(hostname, String.format("%s OAuth2 Token Expiry", prefix));
            }
            if(StringUtils.isNotBlank(credentials.getOauth().getIdToken())) {
                this.deletePassword(scheme, port, hostname,
                        String.format("%s OIDC Id Token", prefix));
            }
        }
    }
}
