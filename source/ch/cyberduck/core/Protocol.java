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
     *
     * @return The identifier for this protocol which is the scheme by default
     */
    public String getName() {
        return this.getScheme();
    }

    public abstract String getDescription();

    public abstract String getScheme();

    public boolean equals(Object other) {
        if(other instanceof Protocol) {
            return ((Protocol)other).getName().equals(this.getName());
        }
        return false;
    }

    public String toString() {
        return this.getName();
    }

    /**
     *
     * @return The default port this protocol connects to
     */
    public abstract int getDefaultPort();

    /**
     * 
     * @param protocol
     * @return
     */
    public static Protocol forName(String protocol) {
        if(protocol.equals(FTP.getName())) {
            return FTP;
        }
        if(protocol.equals(FTP_TLS.getName())) {
            return FTP_TLS;
        }
        if(protocol.equals(SFTP.getName())) {
            return SFTP;
        }
        if(protocol.equals(SCP.getName())) {
            return SCP;
        }
        if(protocol.equals(S3.getName())) {
            return S3;
        }
        if(protocol.equals(WEBDAV.getName())) {
            return WEBDAV;
        }
        throw new RuntimeException();
    }

    public static final Protocol SFTP = new Protocol() {
        public String getDescription() {
            return NSBundle.localizedString("SFTP (SSH Secure File Transfer)", "");
        }

        public int getDefaultPort() {
            return 22;
        }

        public String getScheme() {
            return "sftp";
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
        public String getDescription() {
            return NSBundle.localizedString("FTP-SSL (FTP over TLS/SSL)", "");
        }

        public int getDefaultPort() {
            return 21;
        }

        public String getScheme() {
            return "ftps";
        }
    };
    public static final Protocol S3 = new Protocol() {
        public String getDescription() {
            return NSBundle.localizedString("S3 (Amazon Simple Storage Service)", "");
        }

        public String getName() {
            return "s3";
        }

        public int getDefaultPort() {
            return 443;
        }

        public String getScheme() {
            return "https";
        }
    };
    public static final Protocol WEBDAV = new Protocol() {
        public String getDescription() {
            return NSBundle.localizedString("WebDAV (Web-based Distributed Authoring and Versioning)", "");
        }

        public String getName() {
            return "webdav";
        }

        public int getDefaultPort() {
            return 443;
        }

        public String getScheme() {
            return "https";
        }
    };

    public static String getDefaultScheme(String protocol) {
        return Protocol.forName(protocol).getScheme();
    }

    /**
     * @param port
     * @return The standard protocol for this port number
     */
    public static Protocol getDefaultProtocol(int port) {
        if(port == FTP.getDefaultPort())
            return FTP;
        if(port == SFTP.getDefaultPort())
            return SFTP;
        if(port == S3.getDefaultPort())
            return S3;
        if(port == WEBDAV.getDefaultPort())
            return WEBDAV;

        log.warn("Cannot find default protocol for port number " + port);
        return Protocol.forName(
                Preferences.instance().getProperty("connection.protocol.default"));
    }
}
