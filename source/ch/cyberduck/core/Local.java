package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import glguerin.io.FileForker;
import glguerin.io.Pathname;
import glguerin.io.imp.mac.macosx.MacOSXForker;

import com.apple.cocoa.application.NSWorkspace;
import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @version $Id$
 */
public class Local extends File implements IAttributes {
    private static Logger log = Logger.getLogger(Local.class);

    static boolean JNI_LOADED = false;

    public IAttributes attributes = this;

    private boolean jni_load() {
        if(!JNI_LOADED) {
            try {
                NSBundle bundle = NSBundle.mainBundle();
                String lib = bundle.resourcePath() + "/Java/" + "libLocal.dylib";
                log.info("Locating libLocal.dylib at '" + lib + "'");
                System.load(lib);
                JNI_LOADED = true;

            }
            catch (UnsatisfiedLinkError e) {
                log.error("Could not load the libLocal.dylib library:" + e.getMessage());
            }
        }
        return JNI_LOADED;
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

    public boolean createNewFile() {
        try {
            if (super.createNewFile()) {
                this.setProgress(0);
            }
        }
        catch (IOException e) {
            log.error(e.getMessage());
        }
        return false;
    }

    /**
     * @return the extension if any or null
     */
    public String getExtension() {
        String name = this.getName();
        int index = name.lastIndexOf(".");
        if (index != -1) {
            return name.substring(index + 1, name.length());
        }
        return null;
    }

    public String getAbsolute() {
        return super.getAbsolutePath();
    }

	private final Object lock = new Object();
		
    public void setProgress(int progress) {
        if (Preferences.instance().getBoolean("queue.download.updateIcon")) {
			synchronized(lock) {
	            if (-1 == progress) {
	                this.removeResourceFork();
	            }
	            else {
                    this.jni_load();
                    this.setIconFromFile(this.getAbsolute(), "download" + progress + ".icns");
	            }
	        }
		}
		NSWorkspace.sharedWorkspace().noteFileSystemChangedAtPath(this.getAbsolute());
    }

    private void removeResourceFork() {
        try {
            this.removeCustomIcon();
            FileForker forker = new MacOSXForker();
            forker.usePathname(new Pathname(this.getAbsoluteFile()));
            forker.makeForkOutputStream(true, false).close();
        }
        catch (IOException e) {
            log.error("Failed to remove resource fork from file:" + e.getMessage());
        }
    }

    /**
     * @param icon the absolute path to the image file to use as an icon
     */
    private native void setIconFromFile(String path, String icon);

    private void removeCustomIcon() {
        this.jni_load();
        this.removeCustomIcon(this.getAbsolute());
    }

    private native void removeCustomIcon(String path);

    public Permission getPermission() {
        try {
            NSDictionary fileAttributes = NSPathUtilities.fileAttributes(this.getAbsolutePath(), true);
            Object posix = fileAttributes.objectForKey(NSPathUtilities.FilePosixPermissions);
            if(null == posix) {
                //The file may have desappeared since
                throw new IllegalArgumentException("No such file.");
            }
            return new Permission(((Integer) posix).intValue());
        }
        catch(IllegalArgumentException e) {
            log.error(this.getAbsolute()+":"+e.getMessage());
            return new Permission();
        }
    }

    public void setPermission(Permission p) {
        boolean success = NSPathUtilities.setFileAttributes(this.getAbsolutePath(),
                new NSDictionary(new Integer(p.getDecimalCode()),
                        NSPathUtilities.FilePosixPermissions));
        log.debug("Setting permissions on local file suceeded:" + success);
    }

    public long getTimestamp() {
        return super.lastModified();
    }

    public double getSize() {
        if (this.isDirectory()) {
            return 0;
        }
        return super.length();
    }

    public int hashCode() {
        return this.getAbsolutePath().hashCode();
    }

    public boolean equals(Object other) {
        if (other instanceof Local) {
            return this.getAbsolutePath().equalsIgnoreCase(((Local) other).getAbsolutePath());
        }
        return false;
    }
}
