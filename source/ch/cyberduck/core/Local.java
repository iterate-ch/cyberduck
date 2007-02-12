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
import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSPathUtilities;
import com.apple.cocoa.foundation.NSDate;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * @version $Id$
 */
public class Local extends File implements IAttributes {
    private static Logger log = Logger.getLogger(Local.class);

    private static boolean JNI_LOADED = false;

    public IAttributes attributes = this;

    private boolean jni_load() {
        synchronized(lock) {
            if(!JNI_LOADED) {
                try {
                    NSBundle bundle = NSBundle.mainBundle();
                    String lib = bundle.resourcePath() + "/Java/" + "libLocal.dylib";
                    log.info("Locating libLocal.dylib at '" + lib + "'");
                    System.load(lib);
                    JNI_LOADED = true;
                    log.info("libLocal.dylib loaded");
                }
                catch(UnsatisfiedLinkError e) {
                    log.error("Could not load the libLocal.dylib library:" + e.getMessage());
                }
            }
            return JNI_LOADED;
        }
    }

    public Local(File parent, String name) {
        // See trac #933
        super(NSPathUtilities.stringByExpandingTildeInPath(parent.getAbsolutePath()),
                name.replace('/', ':'));
    }

    public Local(String parent, String name) {
        // See trac #933
        super(NSPathUtilities.stringByExpandingTildeInPath(parent),
                name.replace('/', ':'));
    }

    public Local(String path) {
        super(NSPathUtilities.stringByExpandingTildeInPath(path));
    }

    public Local(File path) {
        super(NSPathUtilities.stringByExpandingTildeInPath(path.getAbsolutePath()));
    }

//    private FileWatcher uk;
//
//    /**
//     *
//     * @param listener
//     */
//    public void watch(FileWatcherListener listener) {
//        if(null == uk) {
//            uk = FileWatcher.instance(this);
//        }
//        uk.watch(listener);
//    }

    /**
     * Creates a new file and sets its resource fork to feature a custom progress icon
     * @return
     */
    public boolean createNewFile() {
        try {
            if(super.createNewFile()) {
                this.setIcon(0);
            }
        }
        catch(IOException e) {
            log.error(e.getMessage());
        }
        return false;
    }

//    /**
//     * @param time     Modification date measured in milliseconds since 00:00:00 <code>timezone</code>, January 1, 1970
//     * @param timezone
//     * @return <code>true</code> if and only if the operation succeeded;
//     *         <code>false</code> otherwise
//     */
//    public boolean setLastModified(final long time, final TimeZone timezone) {
//        super.setLastModified(time);
//        int offset = TimeZone.getDefault().getRawOffset() /*amount of raw offset time in milliseconds to add to UTC*/
//                - timezone.getOffset(time); /*amount of time in milliseconds to add to UTC to get local time*/
//        return super.setLastModified(time + offset);
//    }

    /**
     * @param recursively If true, descend into directories and delete recursively
     * @return  <code>true</code> if and only if the file or directory is
     *          successfully deleted; <code>false</code> otherwise
     */
    public boolean delete(boolean recursively) {
        if(!recursively) {
            return this.delete();
        }
        return this.deleteImpl(this);
    }

    /**
     * Recursively deletes this file
     * @return  <code>true</code> if and only if the file or directory is
     *          successfully deleted; <code>false</code> otherwise
     */
    private boolean deleteImpl(Local f) {
        if(f.attributes.isDirectory()) {
            File[] files = f.listFiles();
            for(int i = 0; i < files.length; i++) {
                this.deleteImpl(new Local(files[i]));
            }
        }
        return f.delete();
    }

    /**
     * @return the extension if any or null
     */
    public String getExtension() {
        String name = this.getName();
        int index = name.lastIndexOf(".");
        if(index != -1) {
            return name.substring(index + 1, name.length());
        }
        return null;
    }

    /**
     * Checks whether a given file is a symbolic link.
     * <p/>
     * <p>It doesn't really test for symbolic links but whether the
     * canonical and absolute paths of the file are identical - this
     * may lead to false positives on some platforms.</p>
     *
     * @return true if the file is a symbolic link.
     */
    public boolean isSymbolicLink() {
        if(!this.exists()) {
            return false;
        }
        // For a link that actually points to something (either a file or a directory),
        // the absolute path is the path through the link, whereas the canonical path
        // is the path the link references.
        try {
            return !this.getAbsolutePath().equals(this.getCanonicalPath());
        }
        catch(IOException e) {
            return false;
        }
    }

    /**
     * @return the file type for the extension of this file provided by launch services
     */
    public String kind() {
        if(this.attributes.isDirectory()) {
            return NSBundle.localizedString("Folder", "");
        }
        final String extension = this.getExtension();
        if(null == extension) {
            return NSBundle.localizedString("Unknown", "");
        }
        this.jni_load();
        return this.kind(this.getExtension());
    }

    /**
     * @param extension
     * @return
     */
    private native String kind(String extension);

    public String getAbsolute() {
        return super.getAbsolutePath();
    }

    private final static Object lock = new Object();

    /**
     * Update the custom icon for the file in the Finder
     * @param progress An integer from -1 and 9. If -1 is passed,
     * the resource fork with the custom icon is removed from the file.
     */
    public void setIcon(int progress) {
        if(progress > 9 || progress < -1) {
            log.warn("Local#setIcon:"+progress);
            return;
        }
        if(Preferences.instance().getBoolean("queue.download.updateIcon")) {
            synchronized(lock) {
                this.jni_load();
                if(-1 == progress) {
                    this.removeResourceFork();
                }
                else {
                    this.setIconFromFile(this.getAbsolute(), "download" + progress + ".icns");
                }
            }
        }
        NSWorkspace.sharedWorkspace().noteFileSystemChangedAtPath(this.getAbsolute());
    }

    /**
     * Removes the resource fork from the file alltogether
     */
    private void removeResourceFork() {
        try {
            this.removeCustomIcon();
            FileForker forker = new MacOSXForker();
            forker.usePathname(new Pathname(this.getAbsoluteFile()));
            forker.makeForkOutputStream(true, false).close();
        }
        catch(IOException e) {
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
        NSDictionary fileAttributes = NSPathUtilities.fileAttributes(this.getAbsolutePath(), true);
        if(null == fileAttributes) {
            log.error("No such file:"+this.getAbsolute());
            return null;
        }
        Object posix = fileAttributes.objectForKey(NSPathUtilities.FilePosixPermissions);
        if(null == posix) {
            log.error("No such file:"+this.getAbsolute());
            return null;
        }
        return new Permission(Integer.parseInt(Integer.toOctalString(((Number) posix).intValue())));
    }

    public void setPermission(Permission p) {
        boolean success = NSPathUtilities.setFileAttributes(this.getAbsolutePath(),
                new NSDictionary(new Integer(p.getDecimalCode()),
                        NSPathUtilities.FilePosixPermissions));
        if(!success) {
            log.error("File attribute changed failed:"+this.getAbsolute());
        }
    }

    public long getModificationDate() {
        return super.lastModified();
    }

    public void setModificationDate(long millis) {
        boolean success = NSPathUtilities.setFileAttributes(this.getAbsolutePath(),
                new NSDictionary(new NSDate(NSDate.millisecondsToTimeInterval(millis)),
                        NSPathUtilities.FileModificationDate));
        if(!success) {
            log.error("File attribute changed failed:"+this.getAbsolute());
        }
    }

    public long getCreationDate() {
        NSDictionary fileAttributes = NSPathUtilities.fileAttributes(this.getAbsolutePath(), true);
        // If flag is true and path is a symbolic link, the attributes of the linked-to file are returned;
        // if the link points to a nonexistent file, this method returns null. If flag is false,
        // the attributes of the symbolic link are returned.
        if(null == fileAttributes) {
            log.error("No such file:"+this.getAbsolute());
            return -1;
        }
        Object date = fileAttributes.objectForKey(NSPathUtilities.FileCreationDate);
        if(null == date) {
            // Returns an entry’s value given its key, or null if no value is associated with key.
            log.error("No such file:"+this.getAbsolute());
            return -1;
        }
        return NSDate.timeIntervalToMilliseconds(((NSDate)date).timeIntervalSinceDate(NSDate.DateFor1970));
    }

    public void setCreationDate(long millis) {
        boolean success = NSPathUtilities.setFileAttributes(this.getAbsolutePath(),
                new NSDictionary(new NSDate(NSDate.millisecondsToTimeInterval(millis)),
                        NSPathUtilities.FileCreationDate));
        if(!success) {
            log.error("File attribute changed failed:"+this.getAbsolute());
        }
    }

    public long getAccessedDate() {
        return this.getModificationDate();
    }

    public void setAccessedDate(long millis) {
        ;
    }

    public double getSize() {
        if(this.isDirectory()) {
            return 0;
        }
        return super.length();
    }

    public int hashCode() {
        return this.getAbsolutePath().hashCode();
    }

    public boolean equals(Object other) {
        if(other instanceof Local) {
            return this.getAbsolute().equalsIgnoreCase(((Local) other).getAbsolute());
        }
        return false;
    }
}
