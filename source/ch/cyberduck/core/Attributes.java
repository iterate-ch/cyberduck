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

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSDictionary;
import com.apple.cocoa.foundation.NSMutableDictionary;

import org.apache.log4j.Logger;

/**
 * Attributes of a remote directory or file.
 *
 * @version $Id$
 */
public class Attributes implements IAttributes {
    private static Logger log = Logger.getLogger(Attributes.class);

    /**
     * The file length
     */
    private double size = -1;
    /**
     *
     */
    private long modified = -1;
    private String owner = null;
    private String group = null;
    /**
     * The file type
     */
    private int type = Path.FILE_TYPE;
    protected Permission permission = null;

    public Attributes() {
        super();
    }

    public Object clone() {
        Attributes copy = new Attributes(this.getAsDictionary());
        copy.size = this.getSize();
        copy.permission = (Permission) this.getPermission().clone();
        copy.modified = this.getTimestamp();
        return copy;
    }

    public boolean isUndefined() {
        boolean defined = -1 == this.modified || -1 == this.size;
        if(!defined)
            log.info("Undefined file attributes");
        return defined;
    }

    public Attributes(NSDictionary dict) {
        Object typeObj = dict.objectForKey("Type");
        if(typeObj != null) {
            this.type = Integer.parseInt((String) typeObj);
        }
    }

    public NSDictionary getAsDictionary() {
        NSMutableDictionary dict = new NSMutableDictionary();
        dict.setObjectForKey(String.valueOf(this.type), "Type");
        return dict;
    }

    /**
     * @param size the size of file in bytes.
     */
    public void setSize(double size) {
        this.size = size;
    }

    /**
     * @return length the size of file in bytes.
     */
    public double getSize() {
        return this.size;
    }

    /**
     * Set the modfication date iN UTC milliseconds
     */
    public void setTimestamp(long m) {
        this.modified = m;
    }

    /**
     * @return in milliseconds
     */
    public long getTimestamp() {
        return this.modified;
    }

    /**
     * @param p
     */
    public void setPermission(Permission p) {
        this.permission = p;
    }

    /**
     * @return
     */
    public Permission getPermission() {
        if(null == this.permission)
            return new Permission();
        return this.permission;
    }

    /**
     *
     * @return true if executable for user, group and world
     */
    public boolean isExecutable() {
        Permission perm = this.getPermission();
        if(null == perm) {
            return false;
        }
        return perm.getOwnerPermissions()[Permission.EXECUTE]
                || perm.getGroupPermissions()[Permission.EXECUTE]
                || perm.getOtherPermissions()[Permission.EXECUTE];
    }

    /**
     *
     * @return true if readable for user, group and world
     */
    public boolean isReadable() {
        Permission perm = this.getPermission();
        if(null == perm) {
            return false;
        }
        return perm.getOwnerPermissions()[Permission.READ]
                || perm.getGroupPermissions()[Permission.READ]
                || perm.getOtherPermissions()[Permission.READ];
    }

    /**
     *
     * @return true if writable for user, group and world
     */
    public boolean isWritable() {
        Permission perm = this.getPermission();
        if(null == perm) {
            return false;
        }
        return perm.getOwnerPermissions()[Permission.WRITE]
                || perm.getGroupPermissions()[Permission.WRITE]
                || perm.getOtherPermissions()[Permission.WRITE];
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
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

    /**
     * @return The owner of the file or 'Unknown' if not set
     */
    public String getOwner() {
        if(null == this.owner)
            return NSBundle.localizedString("Unknown", "");
        return this.owner;
    }

    public void setGroup(String g) {
        this.group = g;
    }

    /**
     * @return
     */
    public String getGroup() {
        if(null == this.group)
            return NSBundle.localizedString("Unknown", "");
        return this.group;
    }
}
