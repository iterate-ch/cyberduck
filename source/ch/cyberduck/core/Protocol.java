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

import ch.cyberduck.core.azure.AzurePath;
import ch.cyberduck.core.azure.AzureSession;
import ch.cyberduck.core.cf.CFPath;
import ch.cyberduck.core.cf.CFSession;
import ch.cyberduck.core.dav.DAVPath;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.dropbox.DropboxPath;
import ch.cyberduck.core.dropbox.DropboxSession;
import ch.cyberduck.core.eucalyptus.ECPath;
import ch.cyberduck.core.eucalyptus.ECSession;
import ch.cyberduck.core.ftp.FTPPath;
import ch.cyberduck.core.ftp.FTPSession;
import ch.cyberduck.core.gdocs.GDPath;
import ch.cyberduck.core.gdocs.GDSession;
import ch.cyberduck.core.gstorage.GSPath;
import ch.cyberduck.core.gstorage.GSSession;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.idisk.IDiskPath;
import ch.cyberduck.core.idisk.IDiskSession;
import ch.cyberduck.core.s3.S3Path;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.sftp.SFTPPath;
import ch.cyberduck.core.sftp.SFTPSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.Constants;
import org.soyatec.windows.azure.authenticate.Base64;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * @version $Id$
 */
public abstract class Protocol {
    protected static Logger log = Logger.getLogger(Protocol.class);

    /**
     * Must be unique across all available protocols.
     *
     * @return The identifier for this protocol which is the scheme by default
     */
    public String getIdentifier() {
        return this.getScheme();
    }

    public String getName() {
        return this.getScheme().toUpperCase();
    }

    public String favicon() {
        return null;
    }

    public boolean isEnabled() {
        return Preferences.instance().getBoolean("protocol." + this.getIdentifier() + ".enable");
    }

    /**
     * Statically register protocol implementations.
     */
    public void register() {
        if(log.isDebugEnabled()) {
            log.debug("Register protocol:" + this);
        }
        SessionFactory.addFactory(this, this.getSessionFactory());
        PathFactory.addFactory(this, this.getPathFactory());
    }

    /**
     * @return
     */
    public abstract String getDescription();

    /**
     * @return
     */
    public abstract String getScheme();

    public String[] getSchemes() {
        return new String[]{this.getScheme()};
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof Protocol) {
            return ((Protocol) other).getIdentifier().equals(this.getIdentifier());
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getIdentifier();
    }

    /**
     * @return A mounted disk icon to display
     */
    public String disk() {
        return this.getIdentifier();
    }

    /**
     * @return A small icon to display
     */
    public String icon() {
        return this.getIdentifier() + "-icon";
    }

    /**
     * @return
     */
    public boolean isSecure() {
        return false;
    }

    public boolean isHostnameConfigurable() {
        return true;
    }

    public boolean isPortConfigurable() {
        return true;
    }

    public boolean isWebUrlConfigurable() {
        return true;
    }

    public boolean isEncodingConfigurable() {
        return false;
    }

    public boolean isConnectModeConfigurable() {
        return false;
    }

    public boolean isAnonymousConfigurable() {
        return true;
    }

    public boolean isUTCTimezone() {
        return true;
    }

    public String getUsernamePlaceholder() {
        return Locale.localizedString("Username", "Credentials");
    }

    public String getPasswordPlaceholder() {
        return Locale.localizedString("Password", "Credentials");
    }

    public String getDefaultHostname() {
        return Preferences.instance().getProperty("connection.hostname.default");
    }

    /**
     * @return
     */
    public abstract SessionFactory getSessionFactory();

    /**
     * @return
     */
    public abstract PathFactory getPathFactory();

    /**
     * Check login credentials for validity for this protocol.
     *
     * @param credentials
     * @return True if username and password is not a blank string and password
     */
    public boolean validate(Credentials credentials) {
        return StringUtils.isNotBlank(credentials.getUsername())
                && StringUtils.isNotEmpty(credentials.getPassword());
    }

    /**
     * @return The default port this protocol connects to
     */
    public abstract int getDefaultPort();

    public static final Protocol SFTP = new Protocol() {
        @Override
        public String getDescription() {
            return Locale.localizedString("SFTP (SSH File Transfer Protocol)");
        }

        @Override
        public int getDefaultPort() {
            return 22;
        }

        @Override
        public String getScheme() {
            return "sftp";
        }

        @Override
        public boolean isSecure() {
            return true;
        }

        @Override
        public boolean isEncodingConfigurable() {
            return true;
        }

        @Override
        public boolean validate(Credentials credentials) {
            if(credentials.isPublicKeyAuthentication()) {
                return StringUtils.isNotBlank(credentials.getUsername());
            }
            return FTP.validate(credentials);
        }

        @Override
        public SessionFactory getSessionFactory() {
            return SFTPSession.factory();
        }

        @Override
        public PathFactory getPathFactory() {
            return SFTPPath.factory();
        }
    };

    public static final Protocol SCP = new Protocol() {
        @Override
        public String getDescription() {
            return Locale.localizedString("SCP (Secure Copy)");
        }

        @Override
        public int getDefaultPort() {
            return 22;
        }

        @Override
        public String getScheme() {
            return "scp";
        }

        @Override
        public boolean isSecure() {
            return true;
        }

        @Override
        public SessionFactory getSessionFactory() {
            throw new RuntimeException();
        }

        @Override
        public PathFactory getPathFactory() {
            throw new RuntimeException();
        }

        @Override
        public boolean isEnabled() {
            return false;
        }
    };

    public static final Protocol FTP = new Protocol() {
        @Override
        public String getDescription() {
            return Locale.localizedString("FTP (File Transfer Protocol)");
        }

        @Override
        public int getDefaultPort() {
            return 21;
        }

        @Override
        public String getScheme() {
            return "ftp";
        }

        @Override
        public boolean isUTCTimezone() {
            return false;
        }

        @Override
        public boolean isEncodingConfigurable() {
            return true;
        }

        @Override
        public boolean isConnectModeConfigurable() {
            return true;
        }

        /**
         * Allows empty string for password.
         * @param credentials
         * @return True if username is given and password is not null
         */
        @Override
        public boolean validate(Credentials credentials) {
            return StringUtils.isNotBlank(credentials.getUsername())
                    && null != credentials.getPassword();
        }

        @Override
        public SessionFactory getSessionFactory() {
            return FTPSession.factory();
        }

        @Override
        public PathFactory getPathFactory() {
            return FTPPath.factory();
        }
    };

    public static final Protocol FTP_TLS = new Protocol() {
        @Override
        public String getName() {
            return "FTP-SSL";
        }

        @Override
        public String getDescription() {
            return Locale.localizedString("FTP-SSL (Explicit AUTH TLS)");
        }

        @Override
        public int getDefaultPort() {
            return 21;
        }

        @Override
        public String getScheme() {
            return "ftps";
        }

        @Override
        public boolean isSecure() {
            return true;
        }

        @Override
        public String disk() {
            return SFTP.disk();
        }

        @Override
        public String icon() {
            return SFTP.icon();
        }

        @Override
        public boolean isUTCTimezone() {
            return false;
        }

        @Override
        public boolean isEncodingConfigurable() {
            return true;
        }

        @Override
        public boolean isConnectModeConfigurable() {
            return true;
        }

        @Override
        public SessionFactory getSessionFactory() {
            return FTPSession.factory();
        }

        @Override
        public PathFactory getPathFactory() {
            return FTPPath.factory();
        }
    };

    public static final Protocol S3_SSL = new Protocol() {
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
        public int getDefaultPort() {
            return 443;
        }

        @Override
        public boolean isPortConfigurable() {
            return false;
        }

        @Override
        public String getScheme() {
            return "https";
        }

        @Override
        public String[] getSchemes() {
            return new String[]{this.getScheme(), "s3"};
        }

        @Override
        public boolean isSecure() {
            return true;
        }

        @Override
        public String getDefaultHostname() {
            return Constants.S3_DEFAULT_HOSTNAME;
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
            return this.icon();
        }

        @Override
        public SessionFactory getSessionFactory() {
            return S3Session.factory();
        }

        @Override
        public PathFactory getPathFactory() {
            return S3Path.factory();
        }
    };

    public static final Protocol S3 = new Protocol() {
        @Override
        public String getName() {
            return "S3";
        }

        @Override
        public String getDescription() {
            return Locale.localizedString("S3/HTTP (Amazon Simple Storage Service)", "S3");
        }

        @Override
        public String getIdentifier() {
            return "s3h";
        }

        @Override
        public int getDefaultPort() {
            return 80;
        }

        @Override
        public boolean isPortConfigurable() {
            return false;
        }

        @Override
        public String getScheme() {
            return "http";
        }

        @Override
        public String[] getSchemes() {
            return new String[]{this.getScheme(), "s3"};
        }

        @Override
        public String getDefaultHostname() {
            return Constants.S3_DEFAULT_HOSTNAME;
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
        public String disk() {
            return S3_SSL.disk();
        }

        @Override
        public String icon() {
            return S3_SSL.icon();
        }

        @Override
        public String favicon() {
            return this.icon();
        }

        @Override
        public SessionFactory getSessionFactory() {
            return S3Session.factory();
        }

        @Override
        public PathFactory getPathFactory() {
            return S3Path.factory();
        }
    };

    public static final Protocol EUCALYPTUS = new Protocol() {
        @Override
        public String getName() {
            return "S3";
        }

        @Override
        public String getDescription() {
            return Locale.localizedString("Eucalyptus Walrus S3", "S3");
        }

        @Override
        public String getIdentifier() {
            return "ec";
        }

        @Override
        public String getDefaultHostname() {
            return "ecc.eucalyptus.com";
        }

        @Override
        public int getDefaultPort() {
            return 8443;
        }

        @Override
        public String getScheme() {
            return "https";
        }

        @Override
        public String[] getSchemes() {
            return new String[]{this.getScheme(), "walrus"};
        }

        @Override
        public boolean isSecure() {
            return true;
        }

        @Override
        public boolean isHostnameConfigurable() {
            return true;
        }

        @Override
        public String disk() {
            return S3_SSL.disk();
        }

        @Override
        public String icon() {
            return "eucalyptus-icon";
        }

        @Override
        public SessionFactory getSessionFactory() {
            return ECSession.factory();
        }

        @Override
        public PathFactory getPathFactory() {
            return ECPath.factory();
        }
    };

    public static final Protocol WEBDAV = new Protocol() {
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
        public int getDefaultPort() {
            return 80;
        }

        @Override
        public String getScheme() {
            return "http";
        }

        @Override
        public String[] getSchemes() {
            return new String[]{this.getScheme(), "dav"};
        }

        @Override
        public String disk() {
            return FTP.disk();
        }

        @Override
        public String icon() {
            return FTP.icon();
        }

        @Override
        public SessionFactory getSessionFactory() {
            return DAVSession.factory();
        }

        @Override
        public PathFactory getPathFactory() {
            return DAVPath.factory();
        }
    };

    public static final Protocol WEBDAV_SSL = new Protocol() {
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
        public int getDefaultPort() {
            return 443;
        }

        @Override
        public String getScheme() {
            return "https";
        }

        @Override
        public boolean isSecure() {
            return true;
        }

        @Override
        public String[] getSchemes() {
            return new String[]{this.getScheme(), "davs"};
        }

        @Override
        public String disk() {
            return FTP_TLS.disk();
        }

        @Override
        public String icon() {
            return FTP_TLS.icon();
        }

        @Override
        public SessionFactory getSessionFactory() {
            return DAVSession.factory();
        }

        @Override
        public PathFactory getPathFactory() {
            return DAVPath.factory();
        }
    };

    public static final Protocol IDISK = new Protocol() {
        @Override
        public String getName() {
            return "MobileMe";
        }

        @Override
        public String getDescription() {
            return Locale.localizedString("MobileMe iDisk (WebDAV)");
        }

        @Override
        public String getIdentifier() {
            return "me";
        }

        @Override
        public int getDefaultPort() {
            return 443;
        }

        @Override
        public boolean isPortConfigurable() {
            return false;
        }

        @Override
        public String getScheme() {
            return "https";
        }

        @Override
        public boolean isSecure() {
            return true;
        }

        @Override
        public String[] getSchemes() {
            return new String[]{this.getScheme(), "idisk"};
        }

        @Override
        public boolean isHostnameConfigurable() {
            return false;
        }

        @Override
        public String getDefaultHostname() {
            return "idisk.me.com";
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
        public String disk() {
            return WEBDAV_SSL.disk();
        }

        @Override
        public String icon() {
            return "NSDotMac";
        }

        @Override
        public String getUsernamePlaceholder() {
            return Locale.localizedString("MobileMe Member Name", "IDisk");
        }

        @Override
        public SessionFactory getSessionFactory() {
            return IDiskSession.factory();
        }

        @Override
        public PathFactory getPathFactory() {
            return IDiskPath.factory();
        }
    };

    public static final Protocol CLOUDFILES = new Protocol() {
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
        public int getDefaultPort() {
            return 443;
        }

        @Override
        public boolean isPortConfigurable() {
            return false;
        }

        @Override
        public String getScheme() {
            return "https";
        }

        @Override
        public String[] getSchemes() {
            return new String[]{this.getScheme(), "mosso", "cloudfiles", "cf"};
        }

        @Override
        public boolean isSecure() {
            return true;
        }

        @Override
        public boolean isHostnameConfigurable() {
            return false;
        }

        @Override
        public String getDefaultHostname() {
            return "storage.clouddrive.com";
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

        @Override
        public SessionFactory getSessionFactory() {
            return CFSession.factory();
        }

        @Override
        public PathFactory getPathFactory() {
            return CFPath.factory();
        }
    };

    public static final Protocol SWIFT = new Protocol() {
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
        public int getDefaultPort() {
            return 443;
        }

        @Override
        public String getScheme() {
            return "https";
        }

        @Override
        public String[] getSchemes() {
            return new String[]{this.getScheme(), "swift"};
        }

        @Override
        public boolean isSecure() {
            return true;
        }

        @Override
        public boolean isHostnameConfigurable() {
            return true;
        }

        @Override
        public String getDefaultHostname() {
            return "auth.api.rackspacecloud.com";
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

        @Override
        public SessionFactory getSessionFactory() {
            return CFSession.factory();
        }

        @Override
        public PathFactory getPathFactory() {
            return CFPath.factory();
        }
    };

    public static final Protocol GDOCS_SSL = new Protocol() {
        @Override
        public String getName() {
            return Locale.localizedString("Google Docs");
        }

        @Override
        public String getDescription() {
            return this.getName();
        }

        @Override
        public String getIdentifier() {
            return "gd";
        }

        @Override
        public String disk() {
            return "google";
        }

        @Override
        public String icon() {
            return "google-icon";
        }

        @Override
        public boolean isSecure() {
            return true;
        }

        @Override
        public boolean isHostnameConfigurable() {
            return false;
        }

        @Override
        public String getDefaultHostname() {
            return "docs.google.com";
        }

        @Override
        public String getScheme() {
            return "https";
        }

        @Override
        public int getDefaultPort() {
            return 443;
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
            return Locale.localizedString("Google Account Email", "S3");
        }

        @Override
        public String getPasswordPlaceholder() {
            return Locale.localizedString("Google Account Password", "S3");
        }

        @Override
        public String favicon() {
            return this.icon();
        }

        @Override
        public boolean validate(Credentials credentials) {
            if(super.validate(credentials)) {
                try {
                    new InternetAddress(credentials.getUsername()).validate();
                    return true;
                }
                catch(AddressException e) {
                    log.warn(e.getMessage());
                }
            }
            return false;
        }

        @Override
        public SessionFactory getSessionFactory() {
            return GDSession.factory();
        }

        @Override
        public PathFactory getPathFactory() {
            return GDPath.factory();
        }
    };

    public static final Protocol GOOGLESTORAGE_SSL = new Protocol() {
        @Override
        public String getName() {
            return "Google Storage";
        }

        @Override
        public String getDescription() {
            return Locale.localizedString("Google Storage", "S3");
        }

        @Override
        public String getIdentifier() {
            return "gs";
        }

        @Override
        public String disk() {
            return "google";
        }

        @Override
        public String icon() {
            return "google-icon";
        }

        @Override
        public boolean isSecure() {
            return true;
        }

        @Override
        public boolean isHostnameConfigurable() {
            return false;
        }

        @Override
        public String getDefaultHostname() {
            return "commondatastorage.googleapis.com";
        }

        @Override
        public String getScheme() {
            return "https";
        }

        @Override
        public int getDefaultPort() {
            return 443;
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
            return Locale.localizedString("Access Key", "S3");
        }

        @Override
        public String getPasswordPlaceholder() {
            return Locale.localizedString("Secret", "S3");
        }

        @Override
        public String favicon() {
            return this.icon();
        }

        @Override
        public SessionFactory getSessionFactory() {
            return GSSession.factory();
        }

        @Override
        public PathFactory getPathFactory() {
            return GSPath.factory();
        }
    };

    public static final Protocol AZURE_SSL = new Protocol() {
        @Override
        public String getName() {
            return "Azure";
        }

        @Override
        public String getDescription() {
            return "Windows Azure Cloud Storage";
        }

        @Override
        public String getIdentifier() {
            return "azure";
        }

        @Override
        public boolean isSecure() {
            return true;
        }

        @Override
        public boolean isHostnameConfigurable() {
            return false;
        }

        @Override
        public String getDefaultHostname() {
            return "blob.core.windows.net";
        }

        @Override
        public String getScheme() {
            return "https";
        }

        @Override
        public int getDefaultPort() {
            return 443;
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
            return Locale.localizedString(" Storage Account Name", "Azure");
        }

        @Override
        public String getPasswordPlaceholder() {
            return Locale.localizedString("Primary Access Key", "Azure");
        }

        @Override
        public boolean validate(Credentials credentials) {
            if(super.validate(credentials)) {
                return null != Base64.decode(credentials.getPassword());
            }
            return false;
        }

        @Override
        public String favicon() {
            return this.icon();
        }

        @Override
        public SessionFactory getSessionFactory() {
            return AzureSession.factory();
        }

        @Override
        public PathFactory getPathFactory() {
            return AzurePath.factory();
        }
    };

    public static final Protocol DROPBOX_SSL = new Protocol() {
        @Override
        public String getName() {
            return "Dropbox";
        }

        @Override
        public String getDescription() {
            return "Dropbox";
        }

        @Override
        public String getIdentifier() {
            return "dropbox";
        }

        @Override
        public boolean isSecure() {
            return true;
        }

        @Override
        public boolean isHostnameConfigurable() {
            return false;
        }

        @Override
        public String getDefaultHostname() {
            return "api.getdropbox.com";
        }

        @Override
        public String getScheme() {
            return "https";
        }

        @Override
        public int getDefaultPort() {
            return 443;
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
            return Locale.localizedString("Email Address", "S3");
        }

        @Override
        public String favicon() {
            return this.icon();
        }

        @Override
        public SessionFactory getSessionFactory() {
            return DropboxSession.factory();
        }

        @Override
        public PathFactory getPathFactory() {
            return DropboxPath.factory();
        }
    };
}