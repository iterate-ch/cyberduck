package ch.cyberduck.core.ftps;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.ssl.AbstractX509TrustManager;

import org.apache.log4j.Logger;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPMessageListener;

import java.io.IOException;

/**
 * @version $Id$
 */
public class FTPSClient extends FTPClient {
    private static Logger log = Logger.getLogger(FTPSClient.class);

    /**
     * @param encoding
     * @param listener
     */
    public FTPSClient(String encoding, FTPMessageListener listener) {
        super(encoding, listener, new FTPSControlSocket(encoding, listener));
    }

    public void setSecureDataSocket(boolean secure) {
        ((FTPSControlSocket) this.control).setSecureDataSocket(secure);
    }

    public void setTrustManager(AbstractX509TrustManager trust) {
        ((FTPSControlSocket) this.control).setTrustManager(trust);
    }

    /**
     * @throws IOException
     */
    public void auth() throws IOException {
        lastValidReply = control.validateReply(control.sendCommand("AUTH TLS"), "234");

        ((FTPSControlSocket) this.control).startHandshake();
    }

    /**
     * The command defined in [RFC-2228] to negotiate data connection
     * security is the PROT command.
     * For TLS, the data connection can have one of two security levels.
     * 1) Clear (requested by 'PROT C')
     * 2) Private (requested by 'PROT P')
     */
    public void prot() throws IOException {
        lastValidReply = control.validateReply(control.sendCommand("PBSZ 0"), "200");
        try {
            // Default to secured data socket using PROT P
            lastValidReply = control.validateReply(control.sendCommand("PROT "
                    + Preferences.instance().getProperty("ftp.tls.datachannel")), "200");
        }
        catch(FTPException e) {
            // Compatibility mode if server does only accept clear data connections.
            log.warn("No data channel security: " + e.getMessage());
            this.setSecureDataSocket(false);
        }
    }
}
