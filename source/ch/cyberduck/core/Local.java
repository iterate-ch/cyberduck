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

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.NSWorkspace;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;

/**
 * A path is a local directory or file.
 *
 * @version $Id$
 */
public class Local extends File {
	private static Logger log = Logger.getLogger(Local.class);

	static {
		try {
			NSBundle bundle = NSBundle.mainBundle();
			String lib = bundle.resourcePath()+"/Java/"+"libLocal.jnilib";
			log.debug("Locating libLocal.jnilib at '"+lib+"'");
			System.load(lib);
		}
		catch(UnsatisfiedLinkError e) {
			log.error("Could not load the alias resolving library:"+e.getMessage());
		}
	}
	
	public Local(File parent, String name) {
		super(NSPathUtilities.stringByExpandingTildeInPath(parent.getAbsolutePath()), name);
	}

	public Local(String parent, String name) {
		super(NSPathUtilities.stringByExpandingTildeInPath(parent), name);
	}

	public Local(String path) {
		super(NSPathUtilities.stringByExpandingTildeInPath(path));
	}

    /**
     * @return the extension if any
     */
    public String getExtension() {
        String name = this.getName();
        int index = name.lastIndexOf(".");
        if(index != -1) {
            return name.substring(index+1, name.length());
        }
        return null;
    }
    
	public String getAbsolute() {
		return super.getAbsolutePath();
    }

    public void setProgress(int progress) {
		if(-1 == progress)
			this.setIconFromExtension(this.getAbsolute(), this.getExtension());
		else
			this.setIconFromFile(this.getAbsolute(), "download"+progress+".icns");
        NSWorkspace.sharedWorkspace().noteFileSystemChangedAtPath(this.getAbsolute());
    }

	/**
		* @param The file extension to load the appropriate default system icon for
	 */
    public native void setIconFromExtension(String path, String icon);

	/**
	 * @param icon the absolute path to the image file to use as an icon
	 */
	public native void setIconFromFile(String path, String icon);
		
//	public File getAbsoluteFile() {
//		return new Local(super.getAbsoluteFile().getAbsolutePath());
//	}
	
//	public File getCanonicalFile() throws IOException {
//		return new Local(super.getCanonicalFile().getAbsolutePath());
//	}
	
//	public File getParentFile() {
//		return new Local(super.getParent());
//	}
	
//	public boolean isFile() {
//		if(this.isAlias()) {
//			return this.resolveAlias().isFile();
//		}
//		return super.isFile();
//	}
	
//	public boolean isDirectory() {
//		if(this.isAlias()) {
//			return this.resolveAlias().isDirectory();
//		}
//		return super.isDirectory();
//	}
	
//	public boolean isAlias() {
//		return this.isAlias(this.getAbsolute());
//	}

//	public Local resolveAlias() {
//		return new Local(this.resolveAlias(this.getAbsolute()));
//	}
	
	/**
	 * @return true if the provided path is an alias.
	 */
	private native boolean isAlias(String path);

	/**
	 * Resolves an alias path.
	 *
	 * @return the same path if the provided path is not an alias.
	 */
	private native String resolveAlias(String aliasPath);

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

	public Calendar getTimestampAsCalendar() {
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone(Preferences.instance().getProperty("queue.sync.timezone")));
		c.setTime(this.getTimestamp());
		if(Preferences.instance().getBoolean("queue.sync.ignore.millisecond"))
			c.clear(Calendar.MILLISECOND);
		if(Preferences.instance().getBoolean("queue.sync.ignore.second"))
			c.clear(Calendar.SECOND);
		if(Preferences.instance().getBoolean("queue.sync.ignore.minute"))
			c.clear(Calendar.MINUTE);
		if(Preferences.instance().getBoolean("queue.sync.ignore.hour"))
			c.clear(Calendar.HOUR);
		return c;
	}

	private static final NSGregorianDateFormatter longDateFormatter = new NSGregorianDateFormatter((String)NSUserDefaults.standardUserDefaults().objectForKey(NSUserDefaults.TimeDateFormatString), false);
	private static final NSGregorianDateFormatter shortDateFormatter = new NSGregorianDateFormatter((String)NSUserDefaults.standardUserDefaults().objectForKey(NSUserDefaults.ShortTimeDateFormatString), false);

	/**
	 * @return the modification date of this file
	 */
	public String getTimestampAsString() {
		try {
			return longDateFormatter.stringForObjectValue(new NSGregorianDate((double)this.getTimestamp().getTime()/1000, NSDate.DateFor1970));
		}
		catch(NSFormatter.FormattingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getTimestampAsShortString() {
		try {
			return shortDateFormatter.stringForObjectValue(new NSGregorianDate((double)this.getTimestamp().getTime()/1000, NSDate.DateFor1970));
		}
		catch(NSFormatter.FormattingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Date getTimestamp() {
		return new Date(super.lastModified());
	}

	public long getSize() {
		if(this.isDirectory())
			return 0l;
		return super.length();
	}

	public boolean equals(Object other) {
		if(other instanceof Local) {
			return this.getAbsolutePath().equals(((Local)other).getAbsolutePath());// && this.attributes.getTimestamp().equals(((Local)other).attributes.getTimestamp());
		}
		return false;
	}
}
