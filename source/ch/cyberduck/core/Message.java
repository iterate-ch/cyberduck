package ch.cyberduck.core;
/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

/**
* A message is sent to the observers of a <code>Bookmark</code>
 * to inform about the current status. There are many different types
 * of messages.
 * @version $Id$
 */
public class Message {//{implements java.io.Serializable {
	
    private String title;
    private Object content;
	
    public static final String TRANSCRIPT = "TRANSCRIPT";
    public static final String PROGRESS = "PROGRESS";
    public static final String ERROR = "ERROR";
    public static final String DATA = "DATA";
    public static final String CLOCK = "CLOCK";
	
    // connection state of host
    public static final String OPEN = "OPEN";
    public static final String CLOSE = "CLOSE";
	
    // connection state of file
    public static final String START = "START";
    public static final String STOP = "STOP";
//	public static final String COMPLETE = "COMPLETE";
//    public static final String INCOMPLETE = "INCOMPLETE";

	public static final String QUEUE_START = "QUEUE_START";
    public static final String QUEUE_STOP = "QUEUE_STOP";

    public static final String RENDEZVOUS_ADD = "RENDEZVOUS_ADD";
    public static final String RENDEZVOUS_REMOVE = "RENDEZVOUS_REMOVE";
    
    /**
		* @param t The title of the mesage
	 * @param d The description of the mesage
     */
    public Message(String title, Object content) {
        this.title = title;
        this.content = content;
    }
	
    /**
		* @param d The title of the mesage
     */
    public Message(String title) {
        this.title = title;
		this.content = null;
    }
	
    public String getTitle() {
        return title;
    }
	
    public Object getContent() {
        return content;
    }
    
    public String toString() {
        return this.getTitle()+":"+this.getContent();
    }
}
