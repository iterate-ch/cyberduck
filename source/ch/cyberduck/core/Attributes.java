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

import java.util.Observable;
import org.apache.log4j.Logger;

/**
* Attributes of a remote directory or file.
 * @version $Id$
 */
public class Attributes extends Observable {
    private static Logger log = Logger.getLogger(Path.class);

    private int size;
    private long modified;

    private String owner;
    private String group;
    private String access;
    private Permission permission;
    //	private boolean visible = true;

    /**
	* @param visible If this path should be shown in the directory listing
     */
    /*
     public void setVisible(boolean visible) {
	 this.visible = visible;
     }
     */
    /**
	* @return If this path is shown in the directory listing
     */
    /*
     public boolean isVisible() {
	 return this.visible;
     }
     */

    /**
	* @ param size the size of file in bytes.
     */
    public void setSize(int size) {
	//	log.debug("setSize:"+size);
	this.size = size;
    }

    /**
	* @ return length the size of file in bytes.
     */
    public int getSize() {
	return size;
    }

    private static final int KILO = 1024; //2^10
    private static final int MEGA = 1048576; // 2^20
    private static final int GIGA = 1073741824; // 2^30

    /**
	* @return The size of the file
     */
    public String getSizeAsString() {
	if(size < KILO) {
	    return size + " B";
	}
	else if(size < MEGA) {
	    return new Double(size/KILO).intValue() + " KB";
	}
	else if(size < GIGA) {
	    return new Double(size/MEGA).intValue() + " MB";
	}
	else {
	    return new Double(size/GIGA).intValue() + " GB";
	}
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

    public void setMode(String access) {
	this.access = access;
    }

    /**
	* @return The unix access permissions including the the first bit
     */
    protected String getMode() {
	return this.access;
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

    public String getKind() {
	if(Path.this.isFile())
	    return "File";
	if(Path.this.isDirectory())
	    return "Folder";
	if(Path.this.isLink())
	    return "Link";
	return "Unknown";
    }
}
