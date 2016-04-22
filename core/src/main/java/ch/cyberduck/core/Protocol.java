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

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

public interface Protocol extends Comparable<Protocol> {

    boolean validate(Credentials credentials, LoginOptions options);

    enum Type {
        ftp {
            /**
             * Allows empty string for password.
             *
             * @return True if username is not blank and password is not null
             */
            @Override
            public boolean validate(final Credentials credentials, final LoginOptions options) {
                if(options.user) {
                    if(StringUtils.isBlank(credentials.getUsername())) {
                        return false;
                    }
                }
                if(options.password) {
                    // Allow empty passwords
                    return credentials.getPassword() != null;
                }
                return true;
            }
        },
        sftp {
            @Override
            public boolean validate(final Credentials credentials, final LoginOptions options) {
                if(options.user) {
                    return StringUtils.isNotBlank(credentials.getUsername());
                }
                return true;
            }
        },
        s3,
        googlestorage {
            @Override
            public boolean validate(final Credentials credentials, final LoginOptions options) {
                // OAuth only requires the project token
                return true;
            }
        },
        googledrive {
            @Override
            public boolean validate(final Credentials credentials, final LoginOptions options) {
                // OAuth only requires the project token
                return true;
            }
        },
        swift,
        dav,
        azure {
            @Override
            public boolean validate(final Credentials credentials, final LoginOptions options) {
                if(super.validate(credentials, options)) {
                    if(Base64.isBase64(credentials.getPassword())) {
                        return true;
                    }
                    // Storage Key is not a valid base64 encoded string
                }
                return false;
            }
        },
        irods,
        spectra,
        b2;

        /**
         * Check login credentials for validity for this protocol.
         *
         * @param credentials Login credentials
         * @param options     Options
         * @return True if username is not a blank string and password is not empty ("") and not null.
         */
        public boolean validate(final Credentials credentials, final LoginOptions options) {
            if(options.user) {
                if(StringUtils.isBlank(credentials.getUsername())) {
                    return false;
                }
            }
            if(options.password) {
                if(StringUtils.isEmpty(credentials.getPassword())) {
                    return false;
                }
            }
            return true;
        }
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

    /**
     * @return False if the hostname to connect is static.
     */
    boolean isHostnameConfigurable();

    /**
     * @return False if the port to connect is static.
     */
    boolean isPortConfigurable();

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
     * @return Authentication context path
     */
    String getContext();

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
}