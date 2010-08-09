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

import ch.cyberduck.core.ssl.CustomTrustSSLProtocolSocketFactory;

import com.enterprisedt.net.ftp.*;

import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;

/**
 * @version $Id$
 */
public class FTPSControlSocket extends FTPControlSocket {

    /**
     *
     */
    protected boolean secure;

    /**
     *
     */
    private CustomTrustSSLProtocolSocketFactory socketFactory;

    /**
     *
     */
    private X509TrustManager trustManager;

    protected FTPSControlSocket(String encoding, FTPMessageListener listener) {
        super(encoding, listener);
    }

    private CustomTrustSSLProtocolSocketFactory getSocketFactory() {
        if(null == socketFactory) {
            socketFactory = new CustomTrustSSLProtocolSocketFactory(trustManager);
        }
        return socketFactory;
    }

    public void setTrustManager(X509TrustManager trustManager) {
        this.trustManager = trustManager;
    }

    /**
     * Switch the control socket to SSL before opening the read and write streams.
     *
     * @throws IOException
     */
    protected void startHandshake() throws IOException {
        // This constructor can be used when tunneling SSL through a proxy or when negotiating the
        // use of SSL over an existing socket. The host and port refer to the logical peer destination.
        // This socket is configured using the socket options established for this factory.
        this.controlSock = this.getSocketFactory().createSocket(controlSock,
                controlSock.getInetAddress().getHostName(), controlSock.getPort(),
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
            FTPDataSocket socket = new FTPActiveDataSocket(this.getSocketFactory().createServerSocket(0));

            // get the local address to which the control socket is bound.
            InetAddress localhost = controlSock.getLocalAddress();

            // send the PORT command to the server
            this.setDataPort(localhost, (short) socket.getLocalPort());

            return socket;
        }
        return super.createDataSocketActive();
    }

    @Override
    protected FTPDataSocket createDataSocketPASV() throws IOException, FTPException {
        if(secure) {
            // PASSIVE command - tells the server to listen for
            // a connection attempt rather than initiating it
            FTPReply replyObj = sendCommand("PASV");
            validateReply(replyObj, "227");
            String reply = replyObj.getReplyText();

            // The reply to PASV is in the form:
            // 227 Entering Passive Mode (h1,h2,h3,h4,p1,p2).
            // where h1..h4 are the IP address to connect and
            // p1,p2 the port number
            // Example:
            // 227 Entering Passive Mode (128,3,122,1,15,87).
            // NOTE: PASV command in IBM/Mainframe returns the string
            // 227 Entering Passive Mode 128,3,122,1,15,87	(missing
            // brackets)
            //
            // Improvement: The first digit found after the reply code
            // is considered start of IP. End of IP can be EOL or random
            // characters. Should take care of all PASV reponse lines,
            // right?

            int parts[] = this.parsePASVResponse(reply);

            // assemble the IP address
            // we try connecting, so we don't bother checking digits etc
            String ipAddress = parts[0] + "." + parts[1] + "." +
                    parts[2] + "." + parts[3];

            // assemble the port number
            int port = (parts[4] << 8) + parts[5];

            try {
                if(InetAddress.getByName(ipAddress).isSiteLocalAddress()) {
                    // Do not trust a local address; may be a misconfigured router
                    return new FTPPassiveDataSocket(
                            this.getSocketFactory().createSocket(controlSock.getInetAddress().getHostAddress(), port)
                    );
                }

                // create the socket
                return new FTPPassiveDataSocket(this.getSocketFactory().createSocket(ipAddress, port));
            }
            catch(ConnectException e) {
                // See #15353
                throw new FTPException(e.getMessage());
            }
        }
        return super.createDataSocketPASV();
    }

    @Override
    protected FTPDataSocket createDataSocketEPSV() throws IOException {
        if(secure) {
            FTPReply replyObj = sendCommand("EPSV");
            validateReply(replyObj, "229");
            String reply = replyObj.getReplyText();

            int port = this.parseEPSVResponse(reply);

            return new FTPPassiveDataSocket(
                    this.getSocketFactory().createSocket(controlSock.getInetAddress().getHostAddress(), port));
        }
        return super.createDataSocketEPSV();
    }
}