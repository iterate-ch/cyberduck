package ch.cyberduck.core.ftps;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import java.io.IOException;
import java.net.InetAddress;

import com.enterprisedt.net.ftp.*;

/**
 * @version $Id$
 */
public class FTPSClient extends FTPClient {

    public FTPSClient(String remoteHost, int controlPort, int timeout, String encoding, FTPMessageListener listener) throws IOException, FTPException {
        this(InetAddress.getByName(remoteHost), controlPort, timeout, encoding, listener);
    }

    public FTPSClient(InetAddress remoteAddr, int controlPort, int timeout, String encoding, FTPMessageListener listener) throws IOException, FTPException {

        if (controlPort < 0) {
            controlPort = FTPControlSocket.CONTROL_PORT;
        }
        this.messageListener = listener;
        this.initialize(new FTPSControlSocket(remoteAddr, controlPort, timeout, encoding, listener));
    }

    public void auth(char datachannel_security) throws IOException, FTPException {
        this.checkConnection(true);

        FTPReply reply = control.sendCommand("AUTH TLS");
        lastValidReply = control.validateReply(reply, "234");

        ((FTPSControlSocket) this.control).startHandshake();

        this.prot(datachannel_security);
    }

    /**
     * For TLS, the data connection can have one of two security levels.
     * 1) Clear (requested by 'PROT C')
     * 2) Private (requested by 'PROT P')
     */
    private void prot(char security) throws IOException, FTPException {
        control.sendCommand("PBSZ 0");
        FTPReply reply = control.sendCommand("PROT C");
        lastValidReply = control.validateReply(reply, "200");
    }
}
