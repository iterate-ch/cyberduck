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

import java.io.File;
import java.util.Date;

import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

/**
 * A path is a local directory or file.
 *
 * @version $Id$
 */
public class Local extends File {
	private static Logger log = Logger.getLogger(Local.class);

	public Local(File parent, String name) {
		super(parent, name);
	}

	public Local(String parent, String name) {
		super(parent, name);
	}

	public Local(String path) {
		super(path);
	}

	public long size() {
		return this.length();
	}

	public File getTemp() {
		return this;
//		return new File(super.getAbsolutePath()+".part");
	}

	public String getAbsolute() {
		return super.getAbsolutePath();
	}
	
//	public NSFileWrapper getWrapper() {
	// @todo jnilib to access file wrapper
//		return new NSFileWrapper(this.getTemp().getAbsolutePath(), false);
//		//this.getWrapper().setIcon(NSImage.imageNamed(img));
//	}

	public Permission getPermission() {
		NSDictionary fileAttributes = NSPathUtilities.fileAttributes(this.getAbsolutePath(), true);
		return new Permission(((Integer)fileAttributes.objectForKey(NSPathUtilities.FilePosixPermissions)).intValue());
	}

	public void setPermission(Permission p) {
		boolean success = NSPathUtilities.setFileAttributes(this.getAbsolutePath(),
		    new NSDictionary(new Integer(p.getDecimalCode()),
		        NSPathUtilities.FilePosixPermissions));
		log.debug("Setting permissions on local file suceeded:"+success);
	}

	private static final NSGregorianDateFormatter formatter = new NSGregorianDateFormatter((String)NSUserDefaults.standardUserDefaults().objectForKey(NSUserDefaults.TimeDateFormatString), false);

	/**
	 * @return the modification date of this file
	 */
	public String getTimestampAsString() {
		try {
			return formatter.stringForObjectValue(new NSGregorianDate((double)this.getTimestamp().getTime()/1000, NSDate.DateFor1970));
			//        return (DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT)).format(new Date(super.lastModified()));
		}
		catch(NSFormatter.FormattingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Date getTimestamp() {
		return new Date(super.lastModified());
	}

	public boolean equals(Object other) {
		if(other instanceof Local) {
			return this.getAbsolutePath().equals(((Local)other).getAbsolutePath());// && this.attributes.getTimestamp().equals(((Local)other).attributes.getTimestamp());
		}
		return false;
	}
}
