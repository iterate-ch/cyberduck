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

import org.apache.log4j.Logger;

import java.text.DateFormat;
import java.util.Date;
import java.util.Observable;

/**
* Attributes of a remote directory or file.
 * @version $Id$
 */
public class Attributes extends Observable {
    private static Logger log = Logger.getLogger(Path.class);

    private long modified;

    private String owner;
    private String group;
    private String mode = "-rwxrwxrwx"; //defaulting to a file
    private Permission permission;
    private boolean visible = true;

    /**
	* @param visible If this path should be shown in the directory listing
     */
     public void setVisible(boolean visible) {
	 this.visible = visible;
     }
    /**
	* @return If this path is shown in the directory listing
     */
     public boolean isVisible() {
	 return this.visible;
     }

    /**
	* Set the modfication returned by ftp directory listings
     */
    public void setModified(long m) {
	this.modified = m;
    }

    /**
	* @return the modification date of this file
     */
    public String getModified() {
	return (DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)).format(new Date(this.modified));
    }

    /**
	* @param access unix access permitions, i.e. -rwxrwxrwx
     */

    public void setMode(String mode) {
	this.mode = mode;
    }

    /**
	* @return The unix access permissions including the the first bit
     */
    public String getMode() {
	return this.mode;
    }

    public void setPermission(Permission p) {
	this.permission = p;
    }

    public Permission getPermission() {
	return this.permission;
    }

    public void setOwner(String o) {
	this.owner = o;
    }

    public String getOwner() {
	return this.owner;
    }

    public void setGroup(String g) {
	this.group = g;
    }

    public String getGroup() {
	return this.group;
    }
}
