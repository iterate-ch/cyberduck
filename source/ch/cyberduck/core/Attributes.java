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

import java.text.DateFormat;
import java.util.Date;
import java.util.Observable;

import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;

import org.apache.log4j.Logger;

/**
 * Attributes of a remote directory or file.
 * @version $Id$
 */
public class Attributes extends Observable {
	private static Logger log = Logger.getLogger(Attributes.class);

	private Date modified = new Date();
	private String owner = null;
	private String group = null;
	protected Permission permission = new Permission();
	private boolean visible = true;
	
	public Attributes() {
		super();
	}

	public Attributes(NSDictionary dict) {
		log.debug("Attributes");
		this.permission = new Permission((NSDictionary)dict.objectForKey("Permission"));
	}
	
	
	public NSDictionary getAsDictionary() {
		NSMutableDictionary dict = new NSMutableDictionary();
		dict.setObjectForKey(this.permission.getAsDictionary(), "Permission");
		return dict;
	}
	
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
		this.modified = new Date(m);
	}

	/**
	 * @return the modification date of this file
	 */
	public String getModified() {
		return (DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)).format(this.modified);
	}

	public Date getModifiedDate() {
		return this.modified;
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
