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

import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSPathUtilities;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * A path is a local directory or file.
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

	public Permission getPermission() {
		NSDictionary fileAttributes = NSPathUtilities.fileAttributes(this.getAbsolutePath(), true);
		return new Permission(((Integer) fileAttributes.objectForKey(NSPathUtilities.FilePosixPermissions)).intValue());
	}

	public String getModified() {
		return (DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)).format(new Date(super.lastModified()));
	}

	public Date getModifiedDate() {
		return new Date(super.lastModified());
	}

	public boolean equals(Object other) {
		if (other instanceof Local) {
			Local local = (Local) other;
			return this.getAbsolutePath().equals(local.getAbsolutePath());
		}
		if (other instanceof Path) {
			Path remote = (Path) other;
			return this.getName().equals(remote.getName()) && this.getModified().equals(remote.attributes.getModified());
		}
		return false;
	}
}