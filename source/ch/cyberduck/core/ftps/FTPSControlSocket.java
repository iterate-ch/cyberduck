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

import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.InetAddress;

import com.enterprisedt.net.ftp.FTPControlSocket;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPMessageListener;

/**
 * @version $Id$
 */
public class FTPSControlSocket extends FTPControlSocket {

    protected FTPSControlSocket(InetAddress remoteAddr, int controlPort, int timeout, String encoding, FTPMessageListener listener) throws IOException, FTPException {

        super(remoteAddr, controlPort, timeout, encoding, listener);
    }

    protected void startHandshake() throws IOException {
        // This constructor can be used when tunneling SSL through a proxy or when negotiating the use of SSL over an existing socket. The host and port refer to the logical peer destination. This socket is configured using the socket options established for this factory.
        this.controlSock = new SSLProtocolSocketFactory().createSocket(this.getSocket(),
                this.getSocket().getInetAddress().getHostName(),
                this.getSocket().getPort(),
                true); //close the underlying socket when this socket is closed
        ((SSLSocket) this.controlSock).addHandshakeCompletedListener(new HandshakeCompletedListener() {
            public void handshakeCompleted(HandshakeCompletedEvent event) {
                log.info("SSL Handshake completed");
            }
        });

        this.initStreams(this.encoding);
    }
}