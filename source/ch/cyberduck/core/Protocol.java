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

import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @version $Id$
 */
public interface Protocol {

    public enum Type {
        ftp {
            /**
             * Allows empty string for password.
             *
             * @return True if username is not blank and password is not null
             */
            @Override
            public boolean validate(final Credentials credentials, final LoginOptions options) {
                // Allow empty passwords
                return StringUtils.isNotBlank(credentials.getUsername()) && null != credentials.getPassword();
            }
        },
        ssh {
            @Override
            public boolean validate(Credentials credentials, final LoginOptions options) {
                if(credentials.isPublicKeyAuthentication()) {
                    return StringUtils.isNotBlank(credentials.getUsername());
                }
                return super.validate(credentials, options);
            }
        },
        s3,
        googlestorage {
            @Override
            public boolean validate(final Credentials credentials, final LoginOptions options) {
                // OAuth only requires the project token
                return StringUtils.isNotBlank(credentials.getUsername());
            }
        },
        swift,
        dav;

        /**
         * Check login credentials for validity for this protocol.
         *
         * @param credentials Login credentials
         * @param options     Options
         * @return True if username is not a blank string and password is not empty ("") and not null.
         */
        public boolean validate(Credentials credentials, final LoginOptions options) {
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
     * @return True if anonymous logins are possible.
     */
    boolean isAnonymousConfigurable();

    boolean isHostnameConfigurable();

    /**
     * @return False if the port to connect is static.
     */
    boolean isPortConfigurable();

    boolean isWebUrlConfigurable();

    /**
     * @return True if the character set is not defined in the protocol.
     */
    boolean isEncodingConfigurable();

    /**
     * @return True if protocol uses UTC timezone for timestamps
     */
    boolean isUTCTimezone();

    Set<String> getLocations();

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
     * @return Include legacy names
     */
    Set<String> getProviders();

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

    public static final Protocol SFTP = new AbstractProtocol() {
        @Override
        public Type getType() {
            return Type.ssh;
        }

        @Override
        public String getIdentifier() {
            return this.getScheme().name();
        }

        @Override
        public String getDescription() {
            return Locale.localizedString("SFTP (SSH File Transfer Protocol)");
        }

        @Override
        public Scheme getScheme() {
            return Scheme.sftp;
        }

        @Override
        public boolean isEncodingConfigurable() {
            return true;
        }

        @Override
        public boolean isAnonymousConfigurable() {
            return false;
        }

        @Override
        public String disk() {
            return FTP_TLS.disk();
        }
    };

    public static final Protocol SCP = new AbstractProtocol() {
        @Override
        public Type getType() {
            return Type.ssh;
        }

        @Override
        public String getIdentifier() {
            return "scp";
        }

        @Override
        public String getDescription() {
            return Locale.localizedString("SCP (Secure Copy)");
        }

        @Override
        public Scheme getScheme() {
            return Scheme.sftp;
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    };

    public static final Protocol FTP = new AbstractProtocol() {
        @Override
        public String getIdentifier() {
            return this.getScheme().name();
        }

        @Override
        public String getDescription() {
            return Locale.localizedString("FTP (File Transfer Protocol)");
        }

        @Override
        public Scheme getScheme() {
            return Scheme.ftp;
        }

        @Override
        public boolean isUTCTimezone() {
            return false;
        }

        @Override
        public boolean isEncodingConfigurable() {
            return true;
        }
    };

    public static final Protocol FTP_TLS = new AbstractProtocol() {
        @Override
        public String getIdentifier() {
            return this.getScheme().name();
        }

        @Override
        public Type getType() {
            return Type.ftp;
        }

        @Override
        public String getName() {
            return "FTP-SSL";
        }

        @Override
        public String getDescription() {
            return Locale.localizedString("FTP-SSL (Explicit AUTH TLS)");
        }

        @Override
        public Scheme getScheme() {
            return Scheme.ftps;
        }

        @Override
        public String disk() {
            return FTP.disk();
        }

        @Override
        public boolean isUTCTimezone() {
            return false;
        }

        @Override
        public boolean isEncodingConfigurable() {
            return true;
        }
    };

    public static final Protocol S3_SSL = new AbstractProtocol() {
        @Override
        public String getName() {
            return "S3";
        }

        @Override
        public String getDescription() {
            return Locale.localizedString("S3 (Amazon Simple Storage Service)", "S3");
        }

        @Override
        public String getIdentifier() {
            return "s3";
        }

        @Override
        public boolean isPortConfigurable() {
            return false;
        }

        @Override
        public Scheme getScheme() {
            return Scheme.https;
        }

        @Override
        public String[] getSchemes() {
            return new String[]{this.getScheme().name(), "s3"};
        }

        @Override
        public boolean isHostnameConfigurable() {
            return true;
        }

        @Override
        public String getDefaultHostname() {
            return "s3.amazonaws.com";
        }

        @Override
        public Set<String> getLocations() {
            return new HashSet<String>(Arrays.asList(
                    "US",
                    "EU",
                    "us-west-1",
                    "us-west-2",
                    "ap-southeast-1",
                    "ap-southeast-2",
                    "ap-northeast-1",
                    "sa-east-1",
                    "s3-us-gov-west-1",
                    "s3-fips-us-gov-west-1"
            ));
        }

        @Override
        public boolean isWebUrlConfigurable() {
            return false;
        }

        @Override
        public String getUsernamePlaceholder() {
            return Locale.localizedString("Access Key ID", "S3");
        }

        @Override
        public String getPasswordPlaceholder() {
            return Locale.localizedString("Secret Access Key", "S3");
        }

        @Override
        public String favicon() {
            // Return static icon as endpoint has no favicon configured
            return this.icon();
        }
    };

    public static final Protocol CLOUDFRONT = new AbstractProtocol() {
        @Override
        public Type getType() {
            return Type.s3;
        }

        @Override
        public String getName() {
            return "Cloudfront";
        }

        @Override
        public String getDescription() {
            return Locale.localizedString("Amazon CloudFront", "S3");
        }

        @Override
        public String getIdentifier() {
            return "cloudfront";
        }

        @Override
        public boolean isPortConfigurable() {
            return false;
        }

        @Override
        public Scheme getScheme() {
            return Scheme.https;
        }

        @Override
        public String getDefaultHostname() {
            return "cloudfront.amazonaws.com";
        }

        @Override
        public String getUsernamePlaceholder() {
            return Locale.localizedString("Access Key ID", "S3");
        }

        @Override
        public String getPasswordPlaceholder() {
            return Locale.localizedString("Secret Access Key", "S3");
        }
    };

    public static final Protocol WEBDAV = new AbstractProtocol() {
        @Override
        public String getName() {
            return "WebDAV (HTTP)";
        }

        @Override
        public String getDescription() {
            return Locale.localizedString("WebDAV (Web-based Distributed Authoring and Versioning)");
        }

        @Override
        public String getIdentifier() {
            return "dav";
        }

        @Override
        public Scheme getScheme() {
            return Scheme.http;
        }

        @Override
        public String[] getSchemes() {
            return new String[]{this.getScheme().name(), "dav"};
        }

        @Override
        public String disk() {
            return FTP.disk();
        }
    };

    public static final Protocol WEBDAV_SSL = new AbstractProtocol() {
        @Override
        public String getName() {
            return "WebDAV (HTTPS)";
        }

        @Override
        public String getDescription() {
            return Locale.localizedString("WebDAV (HTTP/SSL)");
        }

        @Override
        public String getIdentifier() {
            return "davs";
        }

        @Override
        public Type getType() {
            return Type.dav;
        }

        @Override
        public Scheme getScheme() {
            return Scheme.https;
        }

        @Override
        public String[] getSchemes() {
            return new String[]{this.getScheme().name(), "davs"};
        }

        @Override
        public String disk() {
            return FTP_TLS.disk();
        }
    };

    public static final Protocol CLOUDFILES = new AbstractProtocol() {
        @Override
        public String getName() {
            return Locale.localizedString("Cloud Files", "Mosso");
        }

        @Override
        public String getDescription() {
            return Locale.localizedString("Rackspace Cloud Files", "Mosso");
        }

        @Override
        public String getIdentifier() {
            return "cf";
        }

        @Override
        public Type getType() {
            return Type.swift;
        }

        @Override
        public boolean isPortConfigurable() {
            return false;
        }

        @Override
        public Scheme getScheme() {
            return Scheme.https;
        }

        @Override
        public String disk() {
            return SWIFT.disk();
        }

        @Override
        public String[] getSchemes() {
            return new String[]{this.getScheme().name(), "mosso", "cloudfiles", "cf"};
        }

        @Override
        public boolean isHostnameConfigurable() {
            return false;
        }

        @Override
        public String getDefaultHostname() {
            return "identity.api.rackspacecloud.com";
        }

        @Override
        public String getContext() {
            return "/v2.0/tokens";
        }

        @Override
        public boolean isWebUrlConfigurable() {
            return false;
        }

        @Override
        public boolean isAnonymousConfigurable() {
            return false;
        }

        @Override
        public String getPasswordPlaceholder() {
            return Locale.localizedString("API Access Key", "Mosso");
        }
    };

    public static final Protocol SWIFT = new AbstractProtocol() {
        @Override
        public String getName() {
            return Locale.localizedString("Swift", "Mosso");
        }

        @Override
        public String getDescription() {
            return Locale.localizedString("Swift (OpenStack Object Storage)", "Mosso");
        }

        @Override
        public String getIdentifier() {
            return "swift";
        }

        @Override
        public Set<String> getProviders() {
            // Include legacy Rackspace identifier
            return new HashSet<String>(Arrays.asList(this.getIdentifier(), "cf"));
        }

        @Override
        public Scheme getScheme() {
            return Scheme.https;
        }

        @Override
        public String[] getSchemes() {
            return new String[]{this.getScheme().name(), "swift"};
        }

        @Override
        public boolean isHostnameConfigurable() {
            return true;
        }

        @Override
        public boolean isWebUrlConfigurable() {
            return false;
        }

        @Override
        public boolean isAnonymousConfigurable() {
            return false;
        }

        @Override
        public String getPasswordPlaceholder() {
            return Locale.localizedString("API Access Key", "Mosso");
        }
    };

    public static final Protocol GOOGLESTORAGE_SSL = new AbstractProtocol() {
        @Override
        public String getName() {
            return "Google Cloud Storage";
        }

        @Override
        public String getDescription() {
            return Locale.localizedString("Google Cloud Storage", "S3");
        }

        @Override
        public String getIdentifier() {
            return "gs";
        }

        @Override
        public Type getType() {
            return Type.googlestorage;
        }

        @Override
        public String disk() {
            return "googlestorage";
        }

        @Override
        public boolean isHostnameConfigurable() {
            return false;
        }

        @Override
        public String getDefaultHostname() {
            return "storage.googleapis.com";
        }

        @Override
        public Set<String> getLocations() {
            return new HashSet<String>(Arrays.asList(
                    "US", "EU"
            ));
        }

        @Override
        public Scheme getScheme() {
            return Scheme.https;
        }

        @Override
        public boolean isPortConfigurable() {
            return false;
        }

        @Override
        public boolean isWebUrlConfigurable() {
            return false;
        }

        @Override
        public boolean isAnonymousConfigurable() {
            return false;
        }

        @Override
        public String getUsernamePlaceholder() {
            return Locale.localizedString("x-goog-project-id", "Credentials");
        }

        @Override
        public String getPasswordPlaceholder() {
            return Locale.localizedString("Authorization code", "Credentials");
        }

        @Override
        public String favicon() {
            // Return static icon as endpoint has no favicon configured
            return this.icon();
        }
    };
}