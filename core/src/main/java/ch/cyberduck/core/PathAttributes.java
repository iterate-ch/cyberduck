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

import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.serializer.Serializer;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Map;

/**
 * Attributes of a remote directory or file.
 *
 * @version $Id$
 */
public class PathAttributes extends Attributes implements Serializable {

    public static final PathAttributes EMPTY = new PathAttributes();

    /**
     * The file length
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

    private Permission permission = Permission.EMPTY;

    private Acl acl = Acl.EMPTY;

    /**
     * MD5 checksum
     */
    private Checksum checksum;

    /**
     * ETag header in HTTP
     */
    private String etag;

    /**
     * Redundancy level if available
     */
    private String storageClass;

    /**
     * Server side encryption algorithm or null
     */
    private String encryption;

    /**
     * Unique identifier for a given version of a file
     */
    private String versionId;

    /**
     * Should be hidden in the browser by default
     */
    private boolean duplicate;

    /**
     * Revision number
     */
    private long revision;

    /**
     * Geographical location
     */
    private String region;

    /**
     * HTTP headers
     */
    private Map<String, String> metadata;

    public PathAttributes() {
        metadata = Collections.emptyMap();
    }

    @Override
    public <T> T serialize(final Serializer dict) {
        if(size != -1) {
            dict.setStringForKey(String.valueOf(size), "Size");
        }
        if(modified != -1) {
            dict.setStringForKey(String.valueOf(modified), "Modified");
        }
        if(permission != Permission.EMPTY) {
            dict.setObjectForKey(permission, "Permission");
        }
        if(StringUtils.isNotBlank(versionId)) {
            dict.setStringForKey(versionId, "Version");
        }
        dict.setStringForKey(String.valueOf(duplicate), "Duplicate");
        if(StringUtils.isNotBlank(region)) {
            dict.setStringForKey(region, "Region");
        }
        if(StringUtils.isNotBlank(storageClass)) {
            dict.setStringForKey(storageClass, "Storage Class");
        }
        return dict.getSerialized();
    }

    /**
     * @return length the size of file in bytes.
     */
    @Override
    public long getSize() {
        return size;
    }

    /**
     * @param size the size of file in bytes.
     */
    public void setSize(final long size) {
        this.size = size;
    }

    @Override
    public long getModificationDate() {
        return modified;
    }

    public void setModificationDate(final long millis) {
        this.modified = millis;
    }

    @Override
    public long getCreationDate() {
        return created;
    }

    public void setCreationDate(final long millis) {
        this.created = millis;
    }

    @Override
    public long getAccessedDate() {
        return accessed;
    }

    public void setAccessedDate(final long millis) {
        if(millis < 0) {
            return;
        }
        this.accessed = millis;
    }

    /**
     * @return UNIX permissions
     */
    @Override
    public Permission getPermission() {
        return permission;
    }

    /**
     * @param p UNIX permissions
     */
    public void setPermission(final Permission p) {
        this.permission = p;
    }

    public Acl getAcl() {
        return acl;
    }

    public void setAcl(final Acl acl) {
        this.acl = acl;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    public void setOwner(final String o) {
        this.owner = o;
    }

    @Override
    public String getGroup() {
        return group;
    }

    public void setGroup(final String g) {
        this.group = g;
    }

    @Override
    public Checksum getChecksum() {
        return checksum;
    }

    public void setChecksum(final Checksum checksum) {
        this.checksum = checksum;
    }

    public String getETag() {
        return etag;
    }

    public void setETag(final String etag) {
        this.etag = etag;
    }

    /**
     * @return Storage redundancy identifier.
     */
    public String getStorageClass() {
        return storageClass;
    }

    /**
     * @param storageClass Storage redundancy identifier.
     */
    public void setStorageClass(final String storageClass) {
        this.storageClass = storageClass;
    }

    public String getEncryption() {
        return encryption;
    }

    public void setEncryption(final String encryption) {
        this.encryption = encryption;
    }

    /**
     * A version identifying a particular revision of a file
     * with the same path.
     *
     * @return Version Identifier or null if not versioned.
     */
    public String getVersionId() {
        return versionId;
    }

    /**
     * Set a unique version identifier for the revision of a file.
     *
     * @param versionId Revision
     */
    public void setVersionId(final String versionId) {
        this.versionId = versionId;
    }

    /**
     * @return The incrementing revision number of the file or null if not versioned.
     */
    public String getRevision() {
        return String.valueOf(revision);
    }

    public void setRevision(final long revision) {
        this.revision = revision;
    }

    /**
     * If the path should not be displayed in a browser by default unless the user
     * explicitly chooses to show hidden files.
     *
     * @return True if hidden by default.
     */
    public boolean isDuplicate() {
        return duplicate;
    }

    /**
     * Attribute to mark a file as hidden by default in addition to a filename convention.
     *
     * @param duplicate Flag
     */
    public void setDuplicate(final boolean duplicate) {
        this.duplicate = duplicate;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(final Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof PathAttributes)) {
            return false;
        }
        PathAttributes that = (PathAttributes) o;
        if(modified != that.modified) {
            return false;
        }
        if(size != that.size) {
            return false;
        }
        if(checksum != null ? !checksum.equals(that.checksum) : that.checksum != null) {
            return false;
        }
        if(etag != null ? !etag.equals(that.etag) : that.etag != null) {
            return false;
        }
        if(permission != null ? !permission.equals(that.permission) : that.permission != null) {
            return false;
        }
        if(versionId != null ? !versionId.equals(that.versionId) : that.versionId != null) {
            return false;
        }
        if(region != null ? !region.equals(that.region) : that.region != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (size ^ (size >>> 32));
        result = 31 * result + (int) (modified ^ (modified >>> 32));
        result = 31 * result + (permission != null ? permission.hashCode() : 0);
        result = 31 * result + (checksum != null ? checksum.hashCode() : 0);
        result = 31 * result + (etag != null ? etag.hashCode() : 0);
        result = 31 * result + (versionId != null ? versionId.hashCode() : 0);
        result = 31 * result + (region != null ? region.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PathAttributes{");
        sb.append("accessed=").append(accessed);
        sb.append(", size=").append(size);
        sb.append(", modified=").append(modified);
        sb.append(", created=").append(created);
        sb.append(", owner='").append(owner).append('\'');
        sb.append(", group='").append(group).append('\'');
        sb.append(", permission=").append(permission);
        sb.append(", acl=").append(acl);
        sb.append(", checksum='").append(checksum).append('\'');
        sb.append(", etag='").append(etag).append('\'');
        sb.append(", storageClass='").append(storageClass).append('\'');
        sb.append(", encryption='").append(encryption).append('\'');
        sb.append(", versionId='").append(versionId).append('\'');
        sb.append(", duplicate=").append(duplicate);
        sb.append(", revision=").append(revision);
        sb.append(", region='").append(region).append('\'');
        sb.append(", metadata=").append(metadata);
        sb.append('}');
        return sb.toString();
    }
}
