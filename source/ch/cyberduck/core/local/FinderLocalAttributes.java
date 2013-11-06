package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.LocalAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.ui.cocoa.application.NSWorkspace;
import ch.cyberduck.ui.cocoa.foundation.NSDate;
import ch.cyberduck.ui.cocoa.foundation.NSDictionary;
import ch.cyberduck.ui.cocoa.foundation.NSFileManager;
import ch.cyberduck.ui.cocoa.foundation.NSNumber;
import ch.cyberduck.ui.cocoa.foundation.NSObject;

import org.apache.log4j.Logger;
import org.rococoa.ObjCObjectByReference;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSError;

/**
 * Extending attributes with <code>NSFileManager</code>.
 *
 * @see ch.cyberduck.ui.cocoa.foundation.NSFileManager
 */
public class FinderLocalAttributes extends LocalAttributes {
    private static final Logger log = Logger.getLogger(FinderLocalAttributes.class);

    private FinderLocal local;

    public FinderLocalAttributes(final FinderLocal local) {
        super(local.getAbsolute());
        this.local = local;
    }

    /**
     * @return Null if no such file.
     */
    private NSDictionary getNativeAttributes() {
        if((!local.exists())) {
            return null;
        }
        final ObjCObjectByReference error = new ObjCObjectByReference();
        // If flag is true and path is a symbolic link, the attributes of the linked-to file are returned;
        // if the link points to a nonexistent file, this method returns null. If flag is false,
        // the attributes of the symbolic link are returned.
        final NSDictionary dict = NSFileManager.defaultManager().attributesOfItemAtPath_error(
                path, error);
        if(null == dict) {
            final NSError f = error.getValueAs(NSError.class);
            log.error(String.format("failure reading attributes for %s %s", path, f));
        }
        return dict;
    }

    /**
     * @param name File manager attribute name
     * @return Null if no such file or attribute.
     */
    private NSObject getNativeAttribute(final String name) {
        final NSDictionary dict = this.getNativeAttributes();
        if(null == dict) {
            log.warn(String.format("No file at %s", path));
            return null;
        }
        // Returns an entry’s value given its key, or null if no value is associated with key.
        return dict.objectForKey(name);
    }

    @Override
    public long getSize() {
        if(this.isDirectory()) {
            return -1;
        }
        final NSObject size = this.getNativeAttribute(NSFileManager.NSFileSize);
        if(null == size) {
            return -1;
        }
        // Refer to #5503 and http://code.google.com/p/rococoa/issues/detail?id=3
        return (long) Rococoa.cast(size, NSNumber.class).doubleValue();
    }

    @Override
    public Permission getPermission() {
        try {
            final NSObject object = this.getNativeAttribute(NSFileManager.NSFilePosixPermissions);
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
        final NSObject object = this.getNativeAttribute(NSFileManager.NSFileCreationDate);
        if(null == object) {
            return -1;
        }
        return (long) (Rococoa.cast(object, NSDate.class).timeIntervalSince1970() * 1000);
    }

    @Override
    public String getOwner() {
        final NSObject object = this.getNativeAttribute(NSFileManager.NSFileOwnerAccountName);
        if(null == object) {
            return super.getOwner();
        }
        return object.toString();
    }

    @Override
    public String getGroup() {
        final NSObject object = this.getNativeAttribute(NSFileManager.NSFileGroupOwnerAccountName);
        if(null == object) {
            return super.getGroup();
        }
        return object.toString();
    }

    /**
     * @return The value for the key NSFileSystemFileNumber, or 0 if the receiver doesn’t have an entry for the key
     */
    public Long getInode() {
        final NSObject object = this.getNativeAttribute(NSFileManager.NSFileSystemFileNumber);
        if(null == object) {
            return null;
        }
        final NSNumber number = Rococoa.cast(object, NSNumber.class);
        return number.longValue();
    }

    @Override
    public boolean isBundle() {
        return NSWorkspace.sharedWorkspace().isFilePackageAtPath(path);
    }

    @Override
    public boolean isSymbolicLink() {
        final ObjCObjectByReference error = new ObjCObjectByReference();
        final String target = NSFileManager.defaultManager().destinationOfSymbolicLinkAtPath_error(path, error);
        if(null == target) {
            final NSError f = error.getValueAs(NSError.class);
            log.warn(String.format("File reading symbolic target for file %s %s", this, f));
        }
        return target != null;
    }

    /**
     * Executable, readable and writable flags based on <code>NSFileManager</code>.
     */
    private final class FinderLocalPermission extends Permission {
        public FinderLocalPermission(final int octal) {
            super(octal);
        }

        @Override
        public boolean isExecutable() {
            return NSFileManager.defaultManager().isExecutableFileAtPath(path);
        }

        @Override
        public boolean isReadable() {
            return NSFileManager.defaultManager().isReadableFileAtPath(path);
        }

        @Override
        public boolean isWritable() {
            return NSFileManager.defaultManager().isWritableFileAtPath(path);
        }
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        FinderLocalAttributes that = (FinderLocalAttributes) o;
        if(getInode() != null ? !getInode().equals(that.getInode()) : that.getInode() != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return getInode() != null ? getInode().hashCode() : 0;
    }
}
