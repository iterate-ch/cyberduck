package ch.cyberduck.core.local;

/*
 *  Copyright (c) 2009 David Kocher. All rights reserved.
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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Native;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;
import ch.cyberduck.ui.cocoa.foundation.NSArray;
import ch.cyberduck.ui.cocoa.foundation.NSDate;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSDistributedNotificationCenter;
import ch.cyberduck.ui.cocoa.foundation.NSEnumerator;
import ch.cyberduck.ui.cocoa.foundation.NSFileManager;
import ch.cyberduck.ui.cocoa.foundation.NSNotification;
import ch.cyberduck.ui.cocoa.foundation.NSNumber;
import ch.cyberduck.ui.cocoa.foundation.NSObject;
import ch.cyberduck.ui.cocoa.foundation.NSString;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Rococoa;

import java.io.File;

/**
 * @version $Id$
 */
public class FinderLocal extends Local {
    private static Logger log = Logger.getLogger(FinderLocal.class);

    public FinderLocal(Local parent, String name) {
        super(parent, name);
    }

    public FinderLocal(String parent, String name) {
        super(parent, name);
    }

    public FinderLocal(String path) {
        super(path);
    }

    public FinderLocal(File path) {
        super(path);
    }

    public static void register() {
        LocalFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends LocalFactory {
        @Override
        protected Local create() {
            return new FinderLocal(System.getProperty("user.home"));
        }

        @Override
        protected Local create(final Local parent, final String name) {
            return new FinderLocal(parent, name);
        }

        @Override
        protected Local create(final String parent, final String name) {
            return new FinderLocal(parent, name);
        }

        @Override
        protected Local create(final String path) {
            return new FinderLocal(path);
        }

        @Override
        protected Local create(final File path) {
            return new FinderLocal(path);
        }
    }

    @Override
    public void setPath(final String name) {
        if(loadNative()) {
            super.setPath(this.resolveAlias(stringByExpandingTildeInPath(name)));
        }
        else {
            super.setPath(stringByExpandingTildeInPath(name));
        }
    }

    @Override
    public void setPath(final String parent, final String name) {
        super.setPath(stringByExpandingTildeInPath(parent), name);
    }

    /**
     * @return Name of the file as displayed in the Finder. E.g. a ':' is replaced with '/'.
     */
    @Override
    public String getDisplayName() {
        return NSFileManager.defaultManager().displayNameAtPath(this.getName());
    }

    /**
     * @return Path relative to the home directory denoted with a tilde.
     */
    @Override
    public String getAbbreviatedPath() {
        return stringByAbbreviatingWithTildeInPath(this.getAbsolute());
    }

    @Override
    public Local getVolume() {
        for(AbstractPath parent = this.getParent(); !parent.isRoot(); parent = parent.getParent()) {
            if(parent.getParent().getAbsolute().equals("/Volumes")) {
                return (Local) parent;
            }
        }
        return super.getVolume();
    }

    @Override
    public AttributedList<Local> list() {
        if(Preferences.instance().getBoolean("local.list.native")) {
            final AttributedList<Local> children = new AttributedList<Local>();
            final NSArray files = NSFileManager.defaultManager().contentsOfDirectoryAtPath_error(this.getAbsolute(), null);
            if(null == files) {
                log.error("Error listing children:" + this.getAbsolute());
                return children;
            }
            final NSEnumerator i = files.objectEnumerator();
            NSObject next;
            while(((next = i.nextObject()) != null)) {
                children.add(new FinderLocal(this.getAbsolute(), next.toString()));
            }
            return children;
        }
        else {
            return super.list();
        }
    }

    @Override
    public boolean exists() {
        return NSFileManager.defaultManager().fileExistsAtPath(this.getAbsolute());
    }

    @Override
    public void symlink(String target) {
        if(!loadNative()) {
            return;
        }
        final boolean success = NSFileManager.defaultManager().createSymbolicLinkAtPath_pathContent(
                this.getAbsolute(), target);
        if(!success) {
            log.error("File attribute changed failed:" + this.getAbsolute());
        }
    }

    private static boolean JNI_LOADED = false;

    private static boolean loadNative() {
        if(!JNI_LOADED) {
            JNI_LOADED = Native.load("Local");
        }
        return JNI_LOADED;
    }

    /**
     * @param absolute The absolute path of the alias file.
     * @return The absolute path this alias is pointing to.
     */
    private native String resolveAlias(String absolute);

    /**
     * Executable, readable and writable flags based on <code>NSFileManager</code>.
     */
    private class FinderLocalPermission extends Permission {
        public FinderLocalPermission(final int octal) {
            super(octal);
        }

        @Override
        public boolean isExecutable() {
            return NSFileManager.defaultManager().isExecutableFileAtPath(FinderLocal.this.getAbsolute());
        }

        @Override
        public boolean isReadable() {
            return NSFileManager.defaultManager().isReadableFileAtPath(FinderLocal.this.getAbsolute());
        }

        @Override
        public boolean isWritable() {
            return NSFileManager.defaultManager().isWritableFileAtPath(FinderLocal.this.getAbsolute());
        }
    }

    /**
     * Extending attributes with <code>NSFileManager</code>.
     *
     * @see ch.cyberduck.ui.cocoa.foundation.NSFileManager
     */
    private FinderLocalAttributes attributes;

    /**
     * Uses <code>NSFileManager</code> for reading file attributes.
     */
    private class FinderLocalAttributes extends LocalAttributes {
        /**
         * @return Null if no such file.
         */
        private NSDictionary getNativeAttributes() {
            if(!exists()) {
                return null;
            }
            // If flag is true and path is a symbolic link, the attributes of the linked-to file are returned;
            // if the link points to a nonexistent file, this method returns null. If flag is false,
            // the attributes of the symbolic link are returned.
            return NSFileManager.defaultManager().attributesOfItemAtPath_error(
                    getAbsolute(), null);
        }

        /**
         * @param name File manager attribute name
         * @return Null if no such file or attribute.
         */
        private NSObject getNativeAttribute(final String name) {
            NSDictionary dict = this.getNativeAttributes();
            if(null == dict) {
                log.error("No such file:" + getAbsolute());
                return null;
            }
            // Returns an entry’s value given its key, or null if no value is associated with key.
            return dict.objectForKey(name);
        }

        @Override
        public long getSize() {
            if(!loadNative()) {
                return super.getSize();
            }
            if(this.isDirectory()) {
                return -1;
            }
            NSObject size = this.getNativeAttribute(NSFileManager.NSFileSize);
            if(null == size) {
                return -1;
            }
            // Refer to #5503 and http://code.google.com/p/rococoa/issues/detail?id=3
            return (long) Rococoa.cast(size, NSNumber.class).doubleValue();
        }

        @Override
        public Permission getPermission() {
            if(!loadNative()) {
                return Permission.EMPTY;
            }
            try {
                NSObject object = this.getNativeAttribute(NSFileManager.NSFilePosixPermissions);
                if(null == object) {
                    return Permission.EMPTY;
                }
                String posixString = Integer.toOctalString(Rococoa.cast(object, NSNumber.class).intValue());
                return new FinderLocalPermission(Integer.parseInt(posixString.substring(posixString.length() - 3)));
            }
            catch(NumberFormatException e) {
                log.error(e.getMessage());
            }
            return Permission.EMPTY;
        }

        /**
         * Read <code>NSFileCreationDate</code>.
         *
         * @return Milliseconds since 1970
         */
        @Override
        public long getCreationDate() {
            if(!loadNative()) {
                return super.getCreationDate();
            }
            NSObject object = this.getNativeAttribute(NSFileManager.NSFileCreationDate);
            if(null == object) {
                return -1;
            }
            return (long) (Rococoa.cast(object, NSDate.class).timeIntervalSince1970() * 1000);
        }

        @Override
        public long getAccessedDate() {
            return -1;
        }

        @Override
        public String getOwner() {
            if(!loadNative()) {
                return super.getOwner();
            }
            NSObject object = this.getNativeAttribute(NSFileManager.NSFileOwnerAccountName);
            if(null == object) {
                return super.getOwner();
            }
            return object.toString();
        }

        @Override
        public String getGroup() {
            if(!loadNative()) {
                return super.getGroup();
            }
            NSObject object = this.getNativeAttribute(NSFileManager.NSFileGroupOwnerAccountName);
            if(null == object) {
                return super.getGroup();
            }
            return object.toString();
        }

        /**
         * @return The value for the key NSFileSystemFileNumber, or 0 if the receiver doesn’t have an entry for the key
         */
        public long getInode() {
            NSObject object = this.getNativeAttribute(NSFileManager.NSFileSystemFileNumber);
            if(null == object) {
                return 0;
            }
            NSNumber number = Rococoa.cast(object, NSNumber.class);
            return number.longValue();
        }

        @Override
        public boolean isBundle() {
            return NSWorkspace.sharedWorkspace().isFilePackageAtPath(getAbsolute());
        }

        @Override
        public boolean isSymbolicLink() {
            if(!loadNative()) {
                return super.isSymbolicLink();
            }
            return NSFileManager.defaultManager().destinationOfSymbolicLinkAtPath_error(getAbsolute(), null) != null;
        }
    }

    @Override
    public FinderLocalAttributes attributes() {
        if(null == attributes) {
            attributes = new FinderLocalAttributes();
        }
        return attributes;
    }

    /**
     * @return The file type for the extension of this file provided by launch services
     *         if the path is a file.
     */
    @Override
    public String kind() {
        String suffix = this.getExtension();
        if(StringUtils.isEmpty(suffix)) {
            return super.kind();
        }
        // Native file type mapping
        final String kind = kind(suffix);
        if(StringUtils.isEmpty(kind)) {
            return super.kind();
        }
        return kind;
    }

    @Override
    public AbstractPath getSymlinkTarget() {
        if(!loadNative()) {
            return super.getSymlinkTarget();
        }
        return LocalFactory.createLocal((Local) this.getParent(),
                NSFileManager.defaultManager().destinationOfSymbolicLinkAtPath_error(this.getAbsolute(), null));
    }

    public static native String kind(String extension);

    private static final Object workspace = new Object();

    @Override
    public void writeUnixPermission(final Permission permission) {
        synchronized(workspace) {
            boolean success = NSFileManager.defaultManager().setAttributes_ofItemAtPath_error(
                    NSDictionary.dictionaryWithObjectsForKeys(
                            NSArray.arrayWithObject(NSNumber.numberWithInt(Integer.valueOf(permission.getOctalString(), 8))),
                            NSArray.arrayWithObject(NSFileManager.NSFilePosixPermissions)),
                    getAbsolute(), null);
            if(!success) {
                log.error("File attribute changed failed:" + getAbsolute());
            }
        }
    }

    /**
     * Write <code>NSFileModificationDate</code>.
     *
     * @param created  Milliseconds
     * @param modified Milliseconds
     * @param accessed Milliseconds
     */
    @Override
    public void writeTimestamp(final long created, final long modified, final long accessed) {
        synchronized(workspace) {
            boolean success = NSFileManager.defaultManager().setAttributes_ofItemAtPath_error(
                    NSDictionary.dictionaryWithObjectsForKeys(
                            NSArray.arrayWithObject(NSDate.dateWithTimeIntervalSince1970(modified / 1000d)),
                            NSArray.arrayWithObject(NSFileManager.NSFileModificationDate)),
                    getAbsolute(), null);
            if(!success) {
                log.error("File attribute changed failed:" + getAbsolute());
            }
        }
    }

    /**
     * Move file to trash on main interface thread using <code>NSWorkspace.RecycleOperation</code>.
     */
    @Override
    public void trash() {
        if(this.exists()) {
            synchronized(workspace) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Move %s to Trash", this.getAbsolute()));
                }
                if(!NSWorkspace.sharedWorkspace().performFileOperation(
                        NSWorkspace.RecycleOperation,
                        this.getParent().getAbsolute(), StringUtils.EMPTY,
                        NSArray.arrayWithObject(this.getName()))) {
                    log.warn(String.format("Failed to move %s to Trash", this.getAbsolute()));
                }

            }
        }
    }

    @Override
    public boolean reveal() {
        synchronized(workspace) {
            // If a second path argument is specified, a new file viewer is opened. If you specify an
            // empty string (@"") for this parameter, the file is selected in the main viewer.
            return NSWorkspace.sharedWorkspace().selectFile(this.getAbsolute(), this.getParent().getAbsolute());
        }
    }

    /**
     * Comparing by inode if the file exists.
     *
     * @param o Other file
     * @return True if Inode is same
     */
    @Override
    public boolean equals(Object o) {
        // Case insensitive compare returned
        if(super.equals(o)) {
            // Now test with inode for case sensitive volumes
            if(!this.exists()) {
                return super.equals(o);
            }
            FinderLocal other = (FinderLocal) o;
            if(!other.exists()) {
                return super.equals(o);
            }
            return this.attributes().getInode() == other.attributes().getInode();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(this.attributes().getInode()).hashCode();
    }

    @Override
    public boolean open() {
        return NSWorkspace.sharedWorkspace().openFile(this.getAbsolute());
    }

    /**
     * Post a download finished notification to the distributed notification center. Will cause the
     * download folder to bounce just once.
     */
    @Override
    public void bounce() {
        NSDistributedNotificationCenter.defaultCenter().postNotification(
                NSNotification.notificationWithName("com.apple.DownloadFileFinished", this.getAbsolute())
        );
    }

    private static String stringByAbbreviatingWithTildeInPath(String string) {
        return NSString.stringByAbbreviatingWithTildeInPath(string);
    }

    private static String stringByExpandingTildeInPath(String string) {
        return NSString.stringByExpandingTildeInPath(string);
    }
}