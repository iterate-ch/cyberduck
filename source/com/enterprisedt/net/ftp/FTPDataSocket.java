/**
 *
 *  Java FTP client library.
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
 *
 *  Change Log:
 *
 *        $Log$
 *        Revision 1.9  2004/11/02 12:26:27  dkocher
 *        *** empty log message ***
 *
 *        Revision 1.6  2003/11/15 11:23:55  bruceb
 *        changes required for ssl subclasses
 *
 *        Revision 1.4  2003/11/02 21:50:14  bruceb
 *        changed FTPDataSocket to an interface
 *
 *        Revision 1.3  2003/05/31 14:53:44  bruceb
 *        1.2.2 changes
 *
 *        Revision 1.2  2002/11/19 22:01:25  bruceb
 *        changes for 1.2
 *
 *        Revision 1.1  2001/10/09 20:53:46  bruceb
 *        Active mode changes
 *
 *        Revision 1.1  2001/10/05 14:42:03  bruceb
 *        moved from old project
 *
 */

package com.enterprisedt.net.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *  Interface for data socket classes, whether active or passive
 *
 *  @author      Bruce Blackshaw
 *  @version     $Revision$
 */
public interface FTPDataSocket {

    /**
     *  Revision control id
     */
    public static String cvsId = "@(#)$Id$";

    /**
     *   Set the TCP timeout on the underlying control socket.
     *
     *   If a timeout is set, then any operation which
     *   takes longer than the timeout value will be
     *   killed with a java.io.InterruptedException.
     *
     *   @param millis The length of the timeout, in milliseconds
     */
    public void setTimeout(int millis) throws IOException;
    
    /**
     * Returns the local port to which this socket is bound. 
     * 
     * @return the local port number to which this socket is bound
     */
    public int getLocalPort();

    /**
     *  Get the appropriate output stream for writing to
     *
     *  @return  output stream for underlying socket.
     */
    public OutputStream getOutputStream() throws IOException;

    /**
     *  Get the appropriate input stream for reading from
     *
     *  @return  input stream for underlying socket.
     */
    public InputStream getInputStream() throws IOException;

     /**
      *  Closes underlying socket(s)
      */
    public void close() throws IOException;
}

