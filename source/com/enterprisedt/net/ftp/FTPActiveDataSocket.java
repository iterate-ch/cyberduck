/**
 *
 *  Copyright (C) 2000-2003  Enterprise Distributed Technologies Ltd
 *
 *  www.enterprisedt.com
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Bug fixes, suggestions and comments should be sent to bruce@enterprisedt.com
 */

package com.enterprisedt.net.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Active data socket handling class
 *
 * @author Bruce Blackshaw
 * @version $Revision$
 */
public class FTPActiveDataSocket implements FTPDataSocket {

    /**
     * The underlying socket for Active connection.
     */
    protected ServerSocket sock = null;

    /**
     * The socket accepted from server.
     */
    protected Socket acceptedSock = null;

    /**
     * Constructor
     *
     * @param sock the server socket to use
     */
    public FTPActiveDataSocket(ServerSocket sock) {
        this.sock = sock;
    }

    /**
     * Set the TCP timeout on the underlying data socket(s).
     * If a timeout is set, then any operation which
     * takes longer than the timeout value will be
     * killed with a java.io.InterruptedException.
     *
     * @param millis The length of the timeout, in milliseconds
     */
    public void setTimeout(int millis) throws IOException {
        sock.setSoTimeout(millis);
        if(acceptedSock != null) {
            acceptedSock.setSoTimeout(millis);
        }
    }

    /**
     * Returns the local port to which this socket is bound.
     *
     * @return the local port number to which this socket is bound
     */
    public int getLocalPort() {
        return sock.getLocalPort();
    }

    /**
     * Waits for a connection from the server and then sets the timeout
     * when the connection is made.
     *
     * @throws IOException There was an error while waiting for or accepting
     *                     a connection from the server.
     */
    protected void acceptConnection() throws IOException {
        if(null == acceptedSock) {
            acceptedSock = sock.accept();
            try {
                acceptedSock.setSoTimeout(sock.getSoTimeout());
            }
            catch(SocketException e) {
                //log.error(e.getMessage());
            }
            try {
                acceptedSock.setKeepAlive(true);
            }
            catch(SocketException e) {
                //log.error(e.getMessage());
            }
        }
    }

    /**
     * If active mode, accepts the FTP server's connection - in PASV,
     * we are already connected. Then gets the output stream of
     * the connection
     *
     * @return output stream for underlying socket.
     */
    public OutputStream getOutputStream() throws IOException {
        // accept socket from server
        acceptConnection();
        return acceptedSock.getOutputStream();
    }

    /**
     * If active mode, accepts the FTP server's connection - in PASV,
     * we are already connected. Then gets the input stream of
     * the connection
     *
     * @return input stream for underlying socket.
     */
    public InputStream getInputStream() throws IOException {
        // accept socket from server
        acceptConnection();
        return acceptedSock.getInputStream();
    }

    /**
     * Closes underlying sockets
     */
    public void close() throws IOException {
        if(acceptedSock != null) {
            acceptedSock.close();
            acceptedSock = null;
        }
        sock.close();
    }
}
