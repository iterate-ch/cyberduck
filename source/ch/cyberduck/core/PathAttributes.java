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

import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.DeserializerFactory;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.serializer.SerializerFactory;

import org.apache.log4j.Logger;

/**
 * Attributes of a remote directory or file.
 *
 * @version $Id$
 */
public class PathAttributes implements Attributes, Serializable {
    private static Logger log = Logger.getLogger(PathAttributes.class);

    /**
     * The file length
     */
    private long size = -1;
    /**
     * The file modification date
     */
    private long modified = -1;
    private long accessed = -1;
    private long created = -1;
    private String owner;
    private String group;
    /**
     * The file type
     */
    private int type = Path.FILE_TYPE;

    protected Permission permission;
    private String checksum;

    /**
     * Redundany level if available
     */
    private String storageClass;
    private String versionId;

    public PathAttributes() {
        super();
    }

    public <T> PathAttributes(T dict) {
        this.init(dict);
    }

    public <T> void init(T serialized) {
        final Deserializer dict = DeserializerFactory.createDeserializer(serialized);
        String typeObj = dict.stringForKey("Type");
        if(typeObj != null) {
            this.type = Integer.parseInt(typeObj);
        }
        String sizeObj = dict.stringForKey("Size");
        if(sizeObj != null) {
            this.size = Long.parseLong(sizeObj);
        }
        String modifiedObj = dict.stringForKey("Modified");
        if(modifiedObj != null) {
            this.modified = Long.parseLong(modifiedObj);
        }
        Object permissionObj = dict.objectForKey("Permission");
        if(permissionObj != null) {
            this.permission = new Permission(permissionObj);
        }
    }

    public <T> T getAsDictionary() {
        final Serializer dict = SerializerFactory.createSerializer();
        dict.setStringForKey(String.valueOf(this.type), "Type");
        if(this.size != -1) {
            dict.setStringForKey(String.valueOf(this.size), "Size");
        }
        if(this.modified != -1) {
            dict.setStringForKey(String.valueOf(this.modified), "Modified");
        }
        if(null != permission) {
            dict.setObjectForKey(permission, "Permission");
        }
        return dict.<T>getSerialized();
    }

    /**
     * @param size the size of file in bytes.
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * @return length the size of file in bytes.
     */
    public long getSize() {
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
        if(null == this.owner) {
            return Locale.localizedString("Unknown");
        }
        return this.owner;
    }

    public void setGroup(String g) {
        this.group = g;
    }

    /**
     * @return
     */
    public String getGroup() {
        if(null == this.group) {
            return Locale.localizedString("Unknown");
        }
        return this.group;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String redundancy) {
        this.storageClass = redundancy;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }
}
