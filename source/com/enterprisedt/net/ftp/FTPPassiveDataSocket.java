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
import java.net.Socket;

/**
 * Passive data socket handling class
 *
 * @author Bruce Blackshaw
 * @version $Revision$
 */
public class FTPPassiveDataSocket implements FTPDataSocket {

    /**
     * Revision control id
     */
    private static String cvsId = "@(#)$Id$";

    /**
     * The underlying socket
     */
    private Socket sock = null;

    /**
     * Constructor
     *
     * @param sock client socket to use
     */
    FTPPassiveDataSocket(Socket sock) {
        this.sock = sock;
    }

    /**
     * Set the TCP timeout on the underlying control socket.
     * <p/>
     * If a timeout is set, then any operation which
     * takes longer than the timeout value will be
     * killed with a java.io.InterruptedException.
     *
     * @param millis The length of the timeout, in milliseconds
     */
    public void setTimeout(int millis) throws IOException {
        sock.setSoTimeout(millis);
    }

    /**
     * If active mode, accepts the FTP server's connection - in PASV,
     * we are already connected. Then gets the output stream of
     * the connection
     *
     * @return output stream for underlying socket.
     */
    public OutputStream getOutputStream() throws IOException {
        return sock.getOutputStream();
    }

    /**
     * If active mode, accepts the FTP server's connection - in PASV,
     * we are already connected. Then gets the input stream of
     * the connection
     *
     * @return input stream for underlying socket.
     */
    public InputStream getInputStream() throws IOException {
        return sock.getInputStream();
    }

    /**
     * Closes underlying socket
     */
    public void close() throws IOException {
        sock.close();
    }
}
