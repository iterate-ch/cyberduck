package ch.cyberduck.core;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;

import java.text.DateFormat;
import java.util.Date;
import java.util.Observable;

import org.apache.log4j.Logger;

/**
 * Attributes of a remote directory or file.
 *
 * @version $Id$
 */
public class Attributes extends Observable {
    private static Logger log = Logger.getLogger(Attributes.class);

    private Date modified = new Date();
    private String owner = "Unknown";
    private String group = "Unknown";
    private int type = Path.FILE_TYPE;
    protected Permission permission = new Permission();

    public Attributes() {
        super();
    }

    public Attributes(NSDictionary dict) {
        log.debug("Attributes");
        Object typeObj = dict.objectForKey("Type");
        if (typeObj != null) {
            this.type = Integer.parseInt((String)typeObj);
        }
        Object permObj = dict.objectForKey("Permission");
        if (permObj != null) {
            this.permission = new Permission((NSDictionary)permObj);
        }
    }

    public NSDictionary getAsDictionary() {
        NSMutableDictionary dict = new NSMutableDictionary();
        dict.setObjectForKey(this.type+"", "Type");
		dict.setObjectForKey(this.permission.getAsDictionary(), "Permission");
		return dict;
    }

    /**
     * Set the modfication returned by ftp directory listings
     */
    public void setTimestamp(long m) {
        this.modified = new Date(m);
    }

    public void setTimestamp(Date d) {
        this.modified = d;
    }

    /**
     * @return the modification date of this file
     */
    public String getTimestampAsString() {
        return (DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT)).format(this.modified);
    }

    public Date getTimestamp() {
        return this.modified;
    }

    public void setPermission(Permission p) {
        this.permission = p;
    }

    public Permission getPermission() {
        return this.permission;
    }

    public void setType(int type) {
		log.debug("setType:"+type);
        this.type = type;
    }

    public int getType() {
		log.debug("getType:"+this.type);
        return this.type;
    }

    public boolean isDirectory() {
        return (this.type & Path.DIRECTORY_TYPE) == (Path.DIRECTORY_TYPE);
    }

    public boolean isFile() {
        return (this.type & Path.FILE_TYPE) == (Path.FILE_TYPE);
    }
	
    public boolean isSymbolicLink() {
        return (this.type & Path.SYMBOLIC_LINK_TYPE) == (Path.SYMBOLIC_LINK_TYPE);
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
