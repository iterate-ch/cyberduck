package ch.cyberduck.core;

/*
 *  ch.cyberduck.core.SessionException.java
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

/**
* An exception occuring when communicating with a remote host but not IO related.
 * @version $Id$
 */
public class SessionException extends Exception {

    private int code = -1;

    /**
    * @param message The description of the exception
     */
    public SessionException(String message) {
        super(message);
    }

    /**
        * @param message The description of the exception
     * @param reploycode The associated code the server replied with the error message
     */
    public SessionException(String message, int replycode) {
        super(message);
        this.code = replycode;
    }

    /**
        * @param message The description of the exception
     * @param reploycode The associated code the server replied with the error message
     */
    public SessionException(String message, String replycode) {
        super(message);
        try {
            this.code = Integer.parseInt(replycode);
        }
        catch (NumberFormatException ex) {
        }
    }

    /**
        * @return The code the server replied along with the error message
     */
    public int getReplyCode() {
        return this.code;
    }
}
