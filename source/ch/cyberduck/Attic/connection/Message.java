package ch.cyberduck.connection;

/*
 *  ch.cyberduck.connection.Message.java
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
 * A message is sent to the observers of a <code>Bookmark</code>
 * to inform about the current status. There are many different types
 * of messages.
 * @version $Id$
 */
public class Message implements java.io.Serializable {

    private String title;
    private String description;

    /**
    * @param t The title of the mesage
    * @param d The description of the mesage
     */
    public Message(String t, String d) {
        this.title = t;
        this.description = d;
    }

    /**
     * @param d The description of the mesage
     */
    public Message(String d) {
        this.description = d;
    }

    public String getTitle() {
        return title;
    }

    public String getDesription() {
        return description;
    }
    
    public String toString() {
        return this.description;
    }
}
