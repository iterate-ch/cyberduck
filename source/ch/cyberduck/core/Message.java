package ch.cyberduck.core;
/*
 *  Copyright (c) 2002 David Kocher. All rights reserved.
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
 * A message is sent to the observers of a <code>Bookmark</code>
 * to inform about the current status. There are many different types
 * of messages.
 * @version $Id$
 */
public class Message {//{implements java.io.Serializable {

    private String title;
    private String description;

    public static final String TRANSCRIPT = "TRANSCRIPT";
    public static final String PROGRESS = "PROGRESS";
    public static final String ERROR = "ERROR";
    public static final String DATA = "DATA";
    /**
    * 
    */
//    public static final String TIME = "TIME";

    public static final String OPEN = "OPEN";
    public static final String CLOSE = "CLOSE";

    public static final String SELECTION="SELECTION";

    public static final String START = "START";
    public static final String STOP = "STOP";
    public static final String COMPLETE = "COMPLETE";
//    public static final String CURRENT = new String("CURRENT");
    
    /**
    * @param t The title of the mesage
    * @param d The description of the mesage
     */
    public Message(String title, String description) {
        this.title = title;
        this.description = description;
    }

    /**
     * @param d The title of the mesage
     */
//    public Message(String title) {
//        this.title = title;
//	this.description = null;
//    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
    
    public String toString() {
        return this.getTitle()+":"+this.getDescription();
    }
}
