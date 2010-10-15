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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Map;

/**
 * Attributes of a remote directory or file.
 *
 * @version $Id$
 */
public class PathAttributes extends Attributes implements Serializable {
    private static Logger log = Logger.getLogger(PathAttributes.class);

    /**
     * The file length
     *
     * @see Path#readSize()
     */
    private long size = -1;

    /**
     * The file modification date in milliseconds
     */
    private long modified = -1;
    /**
     * Last accessed timestamp in millliseconds
     */
    private long accessed = -1;
    /**
     * When this file was originally created in milliseconds
     */
    private long created = -1;

    private String owner;
    private String group;

    /**
     * The file type
     */
    private int type = Path.FILE_TYPE;

    /**
     * @see ch.cyberduck.core.Path#readUnixPermission()
     */
    private Permission permission = Permission.EMPTY;

    /**
     * @see ch.cyberduck.core.Path#readAcl() ()
     */
    private Acl acl = Acl.EMPTY;

    /**
     *
     */
    private String checksum;

    /**
     * Redundany level if available
     *
     * @see ch.cyberduck.core.cloud.CloudSession#getSupportedStorageClasses()
     */
    private String storageClass;

    /**
     * Unique identifier for a given version of a file.
     *
     * @see ch.cyberduck.core.s3.S3Session#isVersioningSupported()
     */
    private String versionId;

    /**
     * Should be hidden in the browser by default.
     */
    private boolean duplicate;

    /**
     * Revision number.
     */
    private long revision;

    /**
     * Directory placeholder in Cloud Storage.
     */
    private boolean placeholder;

    /**
     * HTTP headers.
     */
    private Map<String, String> metadata = Collections.emptyMap();

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
            type = Integer.parseInt(typeObj);
        }
        String sizeObj = dict.stringForKey("Size");
        if(sizeObj != null) {
            size = Long.parseLong(sizeObj);
        }
        String modifiedObj = dict.stringForKey("Modified");
        if(modifiedObj != null) {
            modified = Long.parseLong(modifiedObj);
        }
        Object permissionObj = dict.objectForKey("Permission");
        if(permissionObj != null) {
            permission = new Permission(permissionObj);
        }
        Object versionObj = dict.objectForKey("Version");
        if(versionObj != null) {
            versionId = versionObj.toString();
        }
        Object duplicateObj = dict.objectForKey("Duplicate");
        if(duplicateObj != null) {
            duplicate = Boolean.valueOf(duplicateObj.toString());
        }
    }

    public <T> T getAsDictionary() {
        final Serializer dict = SerializerFactory.createSerializer();
        dict.setStringForKey(String.valueOf(type), "Type");
        if(size != -1) {
            dict.setStringForKey(String.valueOf(size), "Size");
        }
        if(modified != -1) {
            dict.setStringForKey(String.valueOf(modified), "Modified");
        }
        if(StringUtils.isNotBlank(versionId)) {
            dict.setStringForKey(versionId, "Version");
        }
        dict.setStringForKey(String.valueOf(duplicate), "Duplicate");
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
    @Override
    public long getSize() {
        return this.size;
    }

    @Override
    public long getModificationDate() {
        return this.modified;
    }

    public void setModificationDate(long millis) {
        this.modified = millis;
    }

    @Override
    public long getCreationDate() {
        return this.created;
    }

    public void setCreationDate(long millis) {
        this.created = millis;
    }

    @Override
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
    @Override
    public Permission getPermission() {
        return this.permission;
    }

    public Acl getAcl() {
        return acl;
    }

    public void setAcl(Acl acl) {
        this.acl = acl;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int getType() {
        return this.type;
    }

    @Override
    public boolean isVolume() {
        return (this.type & Path.VOLUME_TYPE) == Path.VOLUME_TYPE;
    }

    @Override
    public boolean isDirectory() {
        return (this.type & Path.DIRECTORY_TYPE) == Path.DIRECTORY_TYPE
                || this.isVolume();
    }

    @Override
    public boolean isFile() {
        return (this.type & Path.FILE_TYPE) == Path.FILE_TYPE;
    }

    @Override
    public boolean isSymbolicLink() {
        return (this.type & Path.SYMBOLIC_LINK_TYPE) == Path.SYMBOLIC_LINK_TYPE;
    }

    public void setOwner(String o) {
        this.owner = o;
    }

    /**
     * @return The owner of the file or 'Unknown' if not set
     */
    @Override
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
    @Override
    public String getGroup() {
        if(null == this.group) {
            return Locale.localizedString("Unknown");
        }
        return this.group;
    }

    @Override
    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    /**
     * @return Storage redundancy identifier.
     */
    public String getStorageClass() {
        return storageClass;
    }

    /**
     * @param redundancy Storage redundancy identifier.
     */
    public void setStorageClass(String redundancy) {
        this.storageClass = redundancy;
    }

    /**
     * @return Unique version identifier
     */
    @Override
    public String getVersionId() {
        return versionId;
    }

    /**
     * Set a unique version identifier for the revision of a file.
     *
     * @param versionId Revision
     */
    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public void setRevision(long revision) {
        this.revision = revision;
    }

    /**
     * Incremental revision number of document.
     *
     * @return Revision number
     */
    @Override
    public String getRevision() {
        return String.valueOf(revision);
    }

    /**
     * @return True if hidden by default
     */
    @Override
    public boolean isDuplicate() {
        return duplicate;
    }

    /**
     * Attribute to mark a file as hidden by default in addition to a filename convention.
     *
     * @param duplicate
     */
    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }


    public boolean isPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(boolean placeholder) {
        this.placeholder = placeholder;
    }

    /**
     *
     */
    public void clear() {
        this.clear(true, true, true, true);
    }

    /**
     * @param timestamp   Clear modification, creation and last access date
     * @param size        Clear content length
     * @param permissions Clear permissions and ACLs
     * @param metadata
     */
    public void clear(boolean timestamp, boolean size, boolean permissions, boolean metadata) {
        if(timestamp) {
            this.setModificationDate(-1);
            this.setCreationDate(-1);
            this.setAccessedDate(-1);
        }
        if(size) {
            this.setSize(-1);
        }
        if(permissions) {
            this.setPermission(Permission.EMPTY);
            this.setAcl(Acl.EMPTY);
        }
        if(metadata) {
            this.setMetadata(Collections.<String, String>emptyMap());
        }
    }

    @Override
    public boolean equals(Object other) {
        if(other instanceof PathAttributes) {
            PathAttributes attr = ((PathAttributes) other);
            if(this.getType() != attr.getType()) {
                return false;
            }
            else if(this.getSize() != attr.getSize()) {
                return false;
            }
            else if(this.getModificationDate() != attr.getModificationDate()) {
                return false;
            }
            else if(!this.getPermission().equals(attr.getPermission())) {
                return false;
            }
            return true;
        }
        return super.equals(other);
    }
}