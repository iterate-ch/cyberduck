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
import org.apache.log4j.Logger;
import org.jets3t.service.Constants;

/**
 * @version $Id$
 */
public abstract class Protocol {
    private static Logger log = Logger.getLogger(Protocol.class);

    /**
     * @return The identifier for this protocol which is the scheme by default
     */
    public String getIdentifier() {
        return this.getScheme();
    }

    public String getName() {
        return this.getScheme().toUpperCase();
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
        return this.getIdentifier() + ".icns";
    }

    /**
     * @return A small icon to display
     */
    public String icon() {
        return this.getIdentifier() + "-icon.png";
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

    public String getDefaultHostname() {
        return Preferences.instance().getProperty("connection.hostname.default");
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
    };

    public static final Protocol S3 = new Protocol() {
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
            return Constants.S3_HOSTNAME;
        }

        @Override
        public boolean isWebUrlConfigurable() {
            return false;
        }
    };

    public static final Protocol EUCALYPTUS = new Protocol() {
        @Override
        public String getName() {
            return "Walrus";
        }

        @Override
        public String getDescription() {
            return Locale.localizedString("Eucalyptus Walrus", "S3");
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
        public String getDefaultHostname() {
            return "mayhem9.cs.ucsb.edu";
        }

        @Override
        public boolean isWebUrlConfigurable() {
            return false;
        }

        @Override
        public String disk() {
            return S3.disk();
        }

        @Override
        public String icon() {
            return "eucalyptus-icon.png";
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
        public String icon() {
            return "NSDotMac";
        }
    };

    public static final Protocol MOSSO = new Protocol() {
        @Override
        public String getName() {
            return "Cloud Files";
        }

        @Override
        public String getDescription() {
            return "Rackspace Cloud Files";
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
    };

    /**
     * @param port
     * @return The standard protocol for this port number
     */
    public static Protocol getDefaultProtocol(int port) {
        final Protocol[] protocols = getKnownProtocols();
        for(Protocol protocol : protocols) {
            if(protocol.getDefaultPort() == port) {
                return protocol;
            }
        }
        log.warn("Cannot find default protocol for port:" + port);
        return Protocol.forName(
                Preferences.instance().getProperty("connection.protocol.default"));
    }

    public static String[] getProtocolDescriptions() {
        final Protocol[] protocols = getKnownProtocols();
        final String[] descriptions = new String[protocols.length];
        for(int i = 0; i < protocols.length; i++) {
            descriptions[i] = protocols[i].getDescription();
        }
        return descriptions;
    }

    /**
     * @param identifier
     * @return
     */
    public static Protocol forName(final String identifier) {
        final Protocol[] protocols = getKnownProtocols();
        for(Protocol protocol : protocols) {
            if(protocol.getIdentifier().equals(identifier)) {
                return protocol;
            }
        }
        log.fatal("Unknown protocol:" + identifier);
        return Protocol.forScheme(identifier);
    }

    /**
     * @param scheme
     * @return
     */
    public static Protocol forScheme(final String scheme) {
        final Protocol[] protocols = getKnownProtocols();
        for(Protocol protocol : protocols) {
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

    public static Protocol[] getKnownProtocols() {
        return new Protocol[]{
                FTP, FTP_TLS, SFTP, WEBDAV, WEBDAV_SSL, IDISK, S3, MOSSO};
    }

    /**
     * @param str
     * @return
     */
    public static boolean isURL(String str) {
        if(StringUtils.isNotBlank(str)) {
            Protocol[] protocols = getKnownProtocols();
            for(Protocol protocol : protocols) {
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
