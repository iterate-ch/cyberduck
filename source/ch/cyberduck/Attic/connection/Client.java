package ch.cyberduck.connection;

/*
 *  ch.cyberduck.connection.Client.java
 *  Cyberduck
 *
 *  $Header$
 *  $Revision$
 *  $Date$
 *
 *  Copyright (c) 2003 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
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

/*
 *  This is just a marker interface, the current implementation has no methods.
 * @version $Id$
 */
public interface Client {

    //@todo now obsolete
    
    /**
     * @return true if the connection is open.
     */
     public abstract boolean isAlive();
    
    /**
     * Start session
     * @param remoteHost The server
     * @param controlPort the port to connect to
     */
    public abstract void connect(String remoteHost, int controlPort) throws IOException, SessionException;
    /**
        * Start session
     * @param remoteHost The server
     * @param controlPort the port to connect to
     * @param secure true if a secure socket should be used
     */
    public abstract void connect(String remoteHost, int controlPort, boolean secure) throws IOException, SessionException;
    /**
     * Close connection to remote host
     */
    public abstract void quit() throws IOException, SessionException;
}
