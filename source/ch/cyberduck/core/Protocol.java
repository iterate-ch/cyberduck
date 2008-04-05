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

import com.apple.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

/**
 * @version $Id:$
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

    public boolean equals(Object other) {
        if(other instanceof Protocol) {
            return ((Protocol) other).getIdentifier().equals(this.getIdentifier());
        }
        return false;
    }

    public String toString() {
        return this.getIdentifier();
    }

    /**
     * @return
     */
    public boolean isSecure() {
        return false;
    }

    /**
     * @return The default port this protocol connects to
     */
    public abstract int getDefaultPort();

    public static final Protocol SFTP = new Protocol() {
        public String getDescription() {
            return NSBundle.localizedString("SFTP (SSH File Transfer Protocol)", "");
        }

        public int getDefaultPort() {
            return 22;
        }

        public String getScheme() {
            return "sftp";
        }

        public boolean isSecure() {
            return true;
        }
    };

    public static final Protocol SCP = new Protocol() {
        public String getDescription() {
            return NSBundle.localizedString("SCP (Secure Copy)", "");
        }

        public int getDefaultPort() {
            return 22;
        }

        public String getScheme() {
            return "scp";
        }

        public boolean isSecure() {
            return true;
        }
    };

    public static final Protocol FTP = new Protocol() {
        public String getDescription() {
            return NSBundle.localizedString("FTP (File Transfer Protocol)", "");
        }

        public int getDefaultPort() {
            return 21;
        }

        public String getScheme() {
            return "ftp";
        }
    };

    public static final Protocol FTP_TLS = new Protocol() {
        public String getName() {
            return "FTP-SSL";
        }

        public String getDescription() {
            return NSBundle.localizedString("FTPS (FTP/SSL)", "");
        }

        public int getDefaultPort() {
            return 21;
        }

        public String getScheme() {
            return "ftps";
        }

        public boolean isSecure() {
            return true;
        }
    };

    public static final Protocol S3 = new Protocol() {
        public String getDescription() {
            return NSBundle.localizedString("S3 (Amazon Simple Storage Service)", "S3", "");
        }

        public String getIdentifier() {
            return "s3";
        }

        public int getDefaultPort() {
            return 443;
        }

        public String getScheme() {
            return "https";
        }

        public String[] getSchemes() {
            return new String[]{this.getScheme(), "s3"};
        }

        public boolean isSecure() {
            return true;
        }
    };

    public static final Protocol WEBDAV = new Protocol() {
        public String getName() {
            return "WebDAV (HTTP)";
        }

        public String getDescription() {
            return NSBundle.localizedString("WebDAV (Web-based Distributed Authoring and Versioning)", "");
        }

        public String getIdentifier() {
            return "dav";
        }

        public int getDefaultPort() {
            return 80;
        }

        public String getScheme() {
            return "http";
        }

        public String[] getSchemes() {
            return new String[]{this.getScheme(), "dav"};
        }
    };

    public static final Protocol WEBDAV_SSL = new Protocol() {
        public String getName() {
            return "WebDAV (HTTPS)";
        }

        public String getDescription() {
            return NSBundle.localizedString("WebDAV (HTTP/SSL)", "");
        }

        public String getIdentifier() {
            return "davs";
        }

        public int getDefaultPort() {
            return 443;
        }

        public String getScheme() {
            return "https";
        }

        public boolean isSecure() {
            return true;
        }

        public String[] getSchemes() {
            return new String[]{this.getScheme(), "davs"};
        }
    };

    /**
     * @param port
     * @return The standard protocol for this port number
     */
    public static Protocol getDefaultProtocol(int port) {
        final Protocol[] protocols = getKnownProtocols();
        for(int i = 0; i < protocols.length; i++) {
            if(protocols[i].getDefaultPort() == port) {
                return protocols[i];
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
     * @param protocol
     * @return
     */
    public static Protocol forName(final String protocol) {
        final Protocol[] protocols = getKnownProtocols();
        for(int i = 0; i < protocols.length; i++) {
            if(protocols[i].getIdentifier().equals(protocol)) {
                return protocols[i];
            }
        }
        log.fatal("Unknown protocol:" + protocol);
        return Protocol.forName(
                Preferences.instance().getProperty("connection.protocol.default"));
    }

    /**
     * @param scheme
     * @return
     */
    public static Protocol forScheme(final String scheme) {
        final Protocol[] protocols = getKnownProtocols();
        for(int i = 0; i < protocols.length; i++) {
            for(int k = 0; k < protocols[i].getSchemes().length; k++) {
                if(protocols[i].getSchemes()[k].equals(scheme)) {
                    return protocols[i];
                }
            }
        }
        log.fatal("Unknown scheme:" + scheme);
        return Protocol.forName(
                Preferences.instance().getProperty("connection.protocol.default"));
    }

    public static Protocol[] getKnownProtocols() {
        return new Protocol[]{
                FTP, FTP_TLS, SFTP, WEBDAV, WEBDAV_SSL, S3};
    }
}
