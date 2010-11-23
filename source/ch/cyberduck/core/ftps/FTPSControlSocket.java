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

import ch.cyberduck.core.ssl.AbstractX509TrustManager;
import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;

import com.enterprisedt.net.ftp.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @version $Id$
 */
public class FTPSControlSocket extends FTPControlSocket {

    /**
     * Secure data channel using TLS.
     */
    protected boolean secure;

    /**
     *
     */
    private CustomTrustSSLProtocolSocketFactory factory;

    protected FTPSControlSocket(String encoding, FTPMessageListener listener) {
        super(encoding, listener);
    }

    public void setTrustManager(AbstractX509TrustManager trust) {
        factory = new CustomTrustSSLProtocolSocketFactory(trust);
    }

    /**
     * Switch the control socket to SSL before opening the read and write streams.
     *
     * @throws IOException
     */
    protected void startHandshake() throws IOException {
        Socket plainSocket = controlSocket;
        // This constructor can be used when tunneling SSL through a proxy or when negotiating the
        // use of SSL over an existing socket. The host and port refer to the logical peer destination.
        // This socket is configured using the socket options established for this factory.
        this.controlSocket = factory.createSocket(plainSocket,
                plainSocket.getInetAddress().getHostAddress(), plainSocket.getPort(),
                true); //close the underlying socket when this socket is closed
        this.initStreams();
    }

    /**
     * Secure data channel using TLS.
     *
     * @param b If true data socket is tunneled through TLS.
     */
    public void setSecureDataSocket(boolean b) {
        this.secure = b;
    }

    @Override
    protected FTPDataSocket createDataSocketActive()
            throws IOException, FTPException {

        if(secure) {
            // use any available port
            FTPDataSocket socket = new FTPActiveDataSocket(factory.createServerSocket(0));

            // get the local address to which the control socket is bound.
            InetAddress localhost = controlSocket.getLocalAddress();

            // send the PORT command to the server
            this.setDataPort(localhost, (short) socket.getLocalPort());

            return socket;
        }
        return super.createDataSocketActive();
    }

    /**
     * Tells the server to listen for a connection attempt rather than initiating it
     *
     * @return
     * @throws IOException
     * @throws FTPException
     */
    @Override
    protected FTPDataSocket createDataSocketPASV() throws IOException, FTPException {
        if(secure) {
            FTPReply replyObj = sendCommand("PASV");
            validateReply(replyObj, "227");
            String reply = replyObj.getReplyText();

            int port = this.parsePASVResponse(reply);
            try {
                // Connect to port number returned by PASV
                return new FTPPassiveDataSocket(
                        factory.createSocket(controlSocket.getInetAddress().getHostAddress(), port));
            }
            catch(ConnectException e) {
                log.error("Failed to open socket to " + controlSocket.getInetAddress().getHostAddress() + ":" + port);
                throw new FTPException(e.getMessage());
            }
        }
        else {
            return super.createDataSocketPASV();
        }
    }

    @Override
    protected FTPDataSocket createDataSocketEPSV() throws IOException {
        if(secure) {
            FTPReply replyObj = sendCommand("EPSV");
            validateReply(replyObj, "229");
            String reply = replyObj.getReplyText();

            int port = this.parseEPSVResponse(reply);

            return new FTPPassiveDataSocket(
                    factory.createSocket(controlSocket.getInetAddress().getHostAddress(), port));
        }
        else {
            return super.createDataSocketEPSV();
        }
    }
}