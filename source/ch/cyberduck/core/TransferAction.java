package ch.cyberduck.core;

/*
 *  ch.cyberduck.core.TransferAction.java
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

import org.apache.log4j.Logger;


/**
 * The TransferAction is passed to the <code>transfer()</code> method
 * of the <code>Bookmark</code> class as a param.
 * @see ch.cyberduck.core.Bookmark#transfer
 * @version $Id$
 */
public class TransferAction implements java.io.Serializable {

    private static Logger log = Logger.getLogger(TransferAction.class);

    /** download */
    public static final String GET = "GET";
    /** upload */
    public static final String PUT = "PUT";

    // ftp protocol only
    /** delete */
    public static final String LIST = "LIST";
    /** make directory */
    public static final String MKDIR = "MKDIR";
    /** change directory */
    public static final String CHDIR = "CHDIR";
    /** delete */
    public static final String DELE = "DELE";
    /** rename */
    public static final String RNFR = "RNFR";
	/** system command*/
    public static final String SITE = "SITE";
    //public static final String ABORT = "ABORT";
    /** close connection */
    public static final String QUIT = "QUIT";

    private String action;
    //@todo replace this with a map - key value pairs
    private Object param1 = null;
    private Object param2 = null;

    /**
     * @param action The String indicating the action (GET, PUT, ...) to pass to Bookmark.transfer()
     */
    public TransferAction(String action, Object param1, Object param2) {
        //log.debug("[TransferAction] TransferAction(" + action + "," +  param2 + ")");
        this.action = action;
        this.param1 = param1;
        this.param2 = param2;
    }

    /**
     * @param action The String indicating the action (GET, PUT, ...) to pass to Bookmark.transfer()
     */
    public TransferAction(String action, Object param1) {
        //log.debug("[TransferAction] TransferAction(" + action + "," +  param1 + ")");
        this.action = action;
        this.param1 = param1;
    }

    /**
        * @param action The String indicating the action (GET, PUT, ...) to pass to Bookmark.transfer()
     */
    public TransferAction(String action) {
        this.action = action;
    }

    /**
     * @return the first parameter of this action or null if not set
     */
    public Object getParam() {
        return param1;
    }

    /**
        * @return the second parameter of this action or null if not set
     */
    public Object getParam2() {
        return param2;
    }

    /**
        * @return String represantation of this action like "GET"
     */
    public String toString() {
        return this.action;
    }
}
