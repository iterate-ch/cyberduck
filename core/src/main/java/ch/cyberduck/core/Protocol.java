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

import java.util.List;
import java.util.Set;

public interface Protocol extends Comparable<Protocol> {

    /**
     * Check login credentials for validity for this protocol.
     *
     * @param credentials Login credentials
     * @param options     Options
     * @return True if username is not a blank string and password is not empty ("") and not null.
     */
    boolean validate(Credentials credentials, LoginOptions options);

    CredentialsConfigurator getCredentialsFinder();

    HostnameConfigurator getHostnameFinder();

    enum Type {
        ftp,
        sftp,
        s3,
        googlestorage,
        dropbox,
        googledrive,
        swift,
        dav,
        azure,
        onedrive,
        irods,
        b2,
        file,
        dracoon,
        manta
    }

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
    Scheme[] getSchemes();

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
     * @return Username label
     */
    String getUsernamePlaceholder();

    /**
     * @return Password label
     */
    String getPasswordPlaceholder();

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
}
