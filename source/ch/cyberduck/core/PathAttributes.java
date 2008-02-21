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
public class PathAttributes extends Attributes {
    private static Logger log = Logger.getLogger(PathAttributes.class);

    /**
     * The file length
     */
    private double size = -1;
    /**
     * The file modification date
     */
    private long modified = -1;
    private long accessed = -1;
    private long created = -1;
    private String owner = null;
    private String group = null;
    /**
     * The file type
     */
    private int type = Path.FILE_TYPE;

    protected Permission permission = null;

    public PathAttributes() {
        super();
    }

    public Object clone() {
        PathAttributes copy = new PathAttributes(this.getAsDictionary());
        copy.size = this.getSize();
        if(null != this.getPermission()) {
            copy.permission = (Permission) this.getPermission().clone();
        }
        copy.modified = this.getModificationDate();
        return copy;
    }

    private static final String TYPE = "Type";

    public PathAttributes(NSDictionary dict) {
        Object typeObj = dict.objectForKey(TYPE);
        if(typeObj != null) {
            this.type = Integer.parseInt((String) typeObj);
        }
    }

    public NSDictionary getAsDictionary() {
        NSMutableDictionary dict = new NSMutableDictionary();
        dict.setObjectForKey(String.valueOf(this.type), TYPE);
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

    public long getModificationDate() {
        return this.modified;
    }

    public void setModificationDate(long millis) {
        this.modified = millis;
    }

    public long getCreationDate() {
        return this.created;
    }

    public void setCreationDate(long millis) {
        this.created = millis;
    }

    public long getAccessedDate() {
        return this.accessed;
    }

    public void setAccessedDate(long millis) {
        this.accessed = millis;
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
        return this.permission;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }

    public boolean isVolume() {
        return (this.type & Path.VOLUME_TYPE) == Path.VOLUME_TYPE;
    }

    public boolean isDirectory() {
        return (this.type & Path.DIRECTORY_TYPE) == Path.DIRECTORY_TYPE
                || this.isVolume();
    }

    public boolean isFile() {
        return (this.type & Path.FILE_TYPE) == Path.FILE_TYPE;
    }

    public boolean isSymbolicLink() {
        return (this.type & Path.SYMBOLIC_LINK_TYPE) == Path.SYMBOLIC_LINK_TYPE;
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
