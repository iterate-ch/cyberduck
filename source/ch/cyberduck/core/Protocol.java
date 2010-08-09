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
import ch.cyberduck.core.eucalyptus.EucalyptusPath;
import ch.cyberduck.core.eucalyptus.EucalyptusSession;
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

import java.util.List;

/**
 * @version $Id$
 */
public abstract class Protocol {
    private static Logger log = Logger.getLogger(Protocol.class);

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

    public boolean isWebUrlConfigurable() {
        return true;
    }

    public boolean isEncodingConfigurable() {
        return false;
    }

    public boolean isConnectModeConfigurable() {
        return false;
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
     * Check login credentials for validity for this protocol.
     *
     * @param credentials
     * @return
     */
    public boolean validate(Credentials credentials) {
        return StringUtils.isNotBlank(credentials.getUsername())
                && StringUtils.isNotBlank(credentials.getPassword());
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
         * @return
         */
        @Override
        public boolean validate(Credentials credentials) {
            return StringUtils.isNotBlank(credentials.getUsername())
                    && null != credentials.getPassword();
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
        public int getDefaultPort() {
            return 8773;
        }

        @Override
        public String getScheme() {
            return "http";
        }

        @Override
        public String[] getSchemes() {
            return new String[]{this.getScheme(), "walrus"};
        }

        @Override
        public boolean isSecure() {
            return false;
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
            return WEBDAV.disk();
        }

        @Override
        public String icon() {
            return WEBDAV.icon();
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
        public String getUsernamePlaceholder() {
            return Locale.localizedString("API Access Key", "Mosso");
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
        public boolean isWebUrlConfigurable() {
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
        public boolean isWebUrlConfigurable() {
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
    };

    public static final Protocol AZURE_SSL = new Protocol() {
        @Override
        public String getName() {
            return "Windows Azure Cloud Storage";
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
        public String disk() {
            return FTP_TLS.disk();
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
        public boolean isWebUrlConfigurable() {
            return false;
        }

        @Override
        public String getUsernamePlaceholder() {
            return Locale.localizedString("Public Storage Account Name", "Azure");
        }

        @Override
        public String getPasswordPlaceholder() {
            return Locale.localizedString("Primary Access Key", "Azure");
        }

        @Override
        public String favicon() {
            return this.icon();
        }
    };

    static {
        if(Preferences.instance().getBoolean("protocol.ftp.enable")) {
            SessionFactory.addFactory(
                    Protocol.FTP, FTPSession.factory());
            PathFactory.addFactory(
                    Protocol.FTP, FTPPath.factory());
        }
        if(Preferences.instance().getBoolean("protocol.ftp.tls.enable")) {
            SessionFactory.addFactory(
                    Protocol.FTP_TLS, FTPSession.factory());
            PathFactory.addFactory(
                    Protocol.FTP_TLS, FTPPath.factory());
        }
        if(Preferences.instance().getBoolean("protocol.sftp.enable")) {
            SessionFactory.addFactory(
                    Protocol.SFTP, SFTPSession.factory());
            PathFactory.addFactory(
                    Protocol.SFTP,
                    SFTPPath.factory());
        }
        if(Preferences.instance().getBoolean("protocol.webdav.enable")) {
            SessionFactory.addFactory(
                    Protocol.WEBDAV, DAVSession.factory());
            PathFactory.addFactory(
                    Protocol.WEBDAV, DAVPath.factory());
        }
        if(Preferences.instance().getBoolean("protocol.webdav.tls.enable")) {
            SessionFactory.addFactory(
                    Protocol.WEBDAV_SSL, DAVSession.factory());
            PathFactory.addFactory(
                    Protocol.WEBDAV_SSL, DAVPath.factory());
        }
        if(Preferences.instance().getBoolean("protocol.idisk.enable")) {
            SessionFactory.addFactory(
                    Protocol.IDISK, IDiskSession.factory());
            PathFactory.addFactory(
                    Protocol.IDISK, IDiskPath.factory());
        }
        if(Preferences.instance().getBoolean("protocol.s3.tls.enable")) {
            SessionFactory.addFactory(
                    Protocol.S3_SSL, S3Session.factory());
            PathFactory.addFactory(
                    Protocol.S3_SSL, S3Path.factory());
        }
        if(Preferences.instance().getBoolean("protocol.s3.enable")) {
            SessionFactory.addFactory(
                    Protocol.S3, S3Session.factory());
            PathFactory.addFactory(
                    Protocol.S3, S3Path.factory());
        }
        if(Preferences.instance().getBoolean("protocol.gstorage.tls.enable")) {
            SessionFactory.addFactory(
                    Protocol.GOOGLESTORAGE_SSL, GSSession.factory());
            PathFactory.addFactory(
                    Protocol.GOOGLESTORAGE_SSL, GSPath.factory());
        }
        if(Preferences.instance().getBoolean("protocol.s3.eucalyptus.enable")) {
            SessionFactory.addFactory(
                    Protocol.EUCALYPTUS, EucalyptusSession.factory());
            PathFactory.addFactory(
                    Protocol.EUCALYPTUS, EucalyptusPath.factory());
        }
        if(Preferences.instance().getBoolean("protocol.cf.enable")) {
            SessionFactory.addFactory(
                    Protocol.CLOUDFILES, CFSession.factory());
            PathFactory.addFactory(
                    Protocol.CLOUDFILES, CFPath.factory());
        }
        if(Preferences.instance().getBoolean("protocol.gdocs.enable")) {
            SessionFactory.addFactory(
                    Protocol.GDOCS_SSL, GDSession.factory());
            PathFactory.addFactory(
                    Protocol.GDOCS_SSL, GDPath.factory());
        }
        if(Preferences.instance().getBoolean("protocol.azure.tls.enable")) {
            SessionFactory.addFactory(
                    Protocol.AZURE_SSL, AzureSession.factory());
            PathFactory.addFactory(
                    Protocol.AZURE_SSL, AzurePath.factory());
        }
    }

    public static List<Protocol> getKnownProtocols() {
        return SessionFactory.getRegisteredProtocols();
    }

    /**
     * @param port
     * @return The standard protocol for this port number
     */
    public static Protocol getDefaultProtocol(int port) {
        for(Protocol protocol : Protocol.getKnownProtocols()) {
            if(protocol.getDefaultPort() == port) {
                return protocol;
            }
        }
        log.warn("Cannot find default protocol for port:" + port);
        return Protocol.forName(
                Preferences.instance().getProperty("connection.protocol.default"));
    }

    /**
     * @param identifier
     * @return
     */
    public static Protocol forName(final String identifier) {
        for(Protocol protocol : Protocol.getKnownProtocols()) {
            if(protocol.getIdentifier().equals(identifier)) {
                return protocol;
            }
        }
        log.fatal("Unknown protocol:" + identifier);
        return Protocol.forName(
                Preferences.instance().getProperty("connection.protocol.default"));
    }

    /**
     * @param scheme
     * @return
     */
    public static Protocol forScheme(final String scheme) {
        for(Protocol protocol : Protocol.getKnownProtocols()) {
            for(int k = 0; k < protocol.getSchemes().length; k++) {
                if(protocol.getSchemes()[k].equals(scheme)) {
                    return protocol;
                }
            }
        }
        log.fatal("Unknown scheme:" + scheme);
        return Protocol.forName(
                Preferences.instance().getProperty("connection.protocol.default"));
    }

    /**
     * @param str
     * @return
     */
    public static boolean isURL(String str) {
        if(StringUtils.isNotBlank(str)) {
            for(Protocol protocol : Protocol.getKnownProtocols()) {
                String[] schemes = protocol.getSchemes();
                for(String scheme : schemes) {
                    if(str.startsWith(scheme + "://")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
