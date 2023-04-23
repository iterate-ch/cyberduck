package ch.cyberduck.core;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.features.Location;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Protocol extends Comparable<Protocol>, Serializable {

    /**
     * Check login credentials for validity for this protocol.
     *
     * @param credentials Login credentials
     * @param options     Options
     * @return True if username is not a blank string and password is not empty ("") and not null.
     */
    boolean validate(Credentials credentials, LoginOptions options);

    /**
     * @return Configurator for resolving credentials for bookmark
     */
    CredentialsConfigurator getCredentialsFinder();

    /**
     * @return Configurator for resolving hostname from alias
     */
    HostnameConfigurator getHostnameFinder();

    /**
     * @return Configurator for resolving jump host configuration for bookmark
     */
    JumphostConfigurator getJumpHostFinder();

    /**
     * @return Case sensitivity of system
     */
    Case getCaseSensitivity();

    /**
     * @return Server support for implicit timestamp update on parent directory when modifying contents
     */
    DirectoryTimestamp getDirectoryTimestamp();

    /**
     * @return By default a protocol is considered stateless
     */
    Statefulness getStatefulness();

    /**
     * @return Comparator that matches natural sorting of results returned by list service
     */
    Comparator<String> getListComparator();

    /**
     * @return True if anonymous login is possible.
     */
    boolean isAnonymousConfigurable();

    /**
     * @return True if username is required
     */
    boolean isUsernameConfigurable();

    /**
     * @return True if password is required
     */
    boolean isPasswordConfigurable();

    boolean isTokenConfigurable();

    boolean isOAuthConfigurable();

    boolean isCertificateConfigurable();

    boolean isPrivateKeyConfigurable();

    /**
     * @return False if the hostname to connect is static.
     */
    boolean isHostnameConfigurable();

    /**
     * @return False if the port to connect is static.
     */
    boolean isPortConfigurable();

    /**
     * @return False if the path to connect is static.
     */
    boolean isPathConfigurable();

    /**
     * @return True if the character set is not defined in the protocol.
     */
    boolean isEncodingConfigurable();

    /**
     * @return True if protocol uses UTC timezone for timestamps
     */
    boolean isUTCTimezone();

    /**
     * @return Human readable short name
     */
    String getName();

    /**
     * @return Available in connection selection
     */
    boolean isEnabled();

    /**
     * @return True if protocol selection is no longer allowed
     */
    boolean isDeprecated();

    /**
     * @return True if the protocol is inherently secure.
     */
    boolean isSecure();

    /**
     * Provider identification
     *
     * @return Identifier if no vendor specific profile
     * @see #getIdentifier()
     */
    String getProvider();

    boolean isBundled();

    /**
     * @return Protocol family
     */
    Type getType();

    /**
     * Must be unique across all available protocols.
     *
     * @return The identifier for this protocol which is the scheme by default
     */
    String getIdentifier();

    String getPrefix();

    /**
     * @return Human readable description
     */
    String getDescription();


    /**
     * @return Protocol scheme
     */
    Scheme getScheme();

    /**
     * @return Protocol schemes
     */
    String[] getSchemes();

    /**
     * @return Default hostname for server
     */
    String getDefaultHostname();

    /**
     * @return Default port for server
     */
    int getDefaultPort();

    /**
     * @return Default path
     */
    String getDefaultPath();

    /**
     * @return Default Nickname
     */
    String getDefaultNickname();

    /**
     * @return Authentication context path
     */
    String getContext();

    /**
     * @return Authentication header version
     */
    String getAuthorization();

    /**
     * @return Available regions
     */
    Set<Location.Name> getRegions();

    /**
     * @param regions Available regions represented as strings from profile
     * @return Localized region set
     */
    Set<Location.Name> getRegions(List<String> regions);

    /**
     * @return Default region
     */
    String getRegion();

    /**
     * @return A mounted disk icon to display
     */
    String disk();

    /**
     * @return Replacement for small disk icon
     */
    String icon();

    String favicon();

    /**
     * @return Host label
     */
    String getHostnamePlaceholder();

    /**
     * @return Username label
     */
    String getUsernamePlaceholder();

    /**
     * @return Password label
     */
    String getPasswordPlaceholder();

    String getTokenPlaceholder();

    /**
     * @return OAuth 2 Authorization Server URL
     */
    String getOAuthAuthorizationUrl();

    /**
     * @return OAuth 2 Token Server URL
     */
    String getOAuthTokenUrl();

    /**
     * @return OAuth 2 Requested scopes
     */
    List<String> getOAuthScopes();

    /**
     * @return OAuth 2 Redirect URI
     */
    String getOAuthRedirectUrl();

    /**
     * @return Default OAuth 2.0 client id
     */
    String getOAuthClientId();

    /**
     * @return Default OAuth 2.0 client secret
     */
    String getOAuthClientSecret();

    /**
     * @return Allow use of Proof Key for Code Exchange (PKCE) for the OAuth2 Athorization Code Flow
     */
    boolean isOAuthPKCE();

    /**
     * @return Custom connection protocol properties
     */
    Map<String, String> getProperties();

    /**
     * @return Provider specific help URL
     */
    String getHelp();

    enum Type {
        ftp,
        sftp,
        s3,
        googlestorage,
        dropbox,
        googledrive,
        swift,
        dav,
        smb,
        azure,
        onedrive,
        irods,
        b2,
        file,
        dracoon,
        storegate,
        brick,
        nextcloud,
        owncloud,
        manta,
        eue,
        freenet,
        ctera,
        box,
    }

    enum Case {
        sensitive,
        insensitive
    }

    enum DirectoryTimestamp {
        /**
         * Timestamp on directory is only updated when set explicitly using API
         */
        explicit,
        /**
         * Timestamp on directory changes implicitly when its contents changes
         */
        implicit
    }

    enum Statefulness {
        stateful,
        stateless
    }

    @SuppressWarnings("unchecked")
    <T> T getFeature(final Class<T> type);
}
