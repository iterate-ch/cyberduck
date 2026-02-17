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

import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Attributes of a remote directory or file.
 */
public class DefaultPathAttributes implements PathAttributes, Attributes, Serializable {
    private static final Logger log = LogManager.getLogger(DefaultPathAttributes.class);

    /**
     * The file length
     */
    private long size = TransferStatus.UNKNOWN_LENGTH;

    /**
     * Quota of folder
     */
    private Quota.Space quota = Quota.unknown;

    /**
     * The file modification date in milliseconds
     */
    private long modified = -1;
    /**
     * Last accessed timestamp in milliseconds
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
    private Checksum checksum = Checksum.NONE;

    /**
     * ETag header in HTTP
     */
    private String etag;

    /**
     * Redundancy level if available
     */
    private String storageClass;

    /**
     * Server side encryption (SSE) algorithm and key or null
     */
    private Encryption.Algorithm encryption = Encryption.Algorithm.NONE;

    /**
     * Unique identifier for a given file. Must remain constant even after updating the file.
     */
    private String fileId;

    /**
     * Unique identifier for a given version of a file
     */
    private String versionId;

    /**
     * Lock id
     */
    private String lockId;

    /**
     * Should be hidden in the browser by default
     */
    private Boolean duplicate;

    /**
     * Hidden flag set on server
     */
    private Boolean hidden;

    /**
     * Trashed
     */
    private Boolean trashed;

    /**
     * Revision number
     */
    private Long revision;

    /**
     * Geographical location
     */
    private String region;

    /**
     *
     */
    private String displayname;

    private DescriptiveUrl link = DescriptiveUrl.EMPTY;

    /**
     * HTTP headers
     */
    private Map<String, String> metadata = Collections.emptyMap();

    /**
     * Cryptomator vault
     */
    private Path vault;
    /**
     * Cryptomator decrypted path
     */
    private Path decrypted;
    /**
     * Cryptomator encrypted path.
     */
    private Path encrypted;
    /**
     * Unique identifier for cryptomator
     */
    private String directoryId;

    private Map<String, String> custom = Collections.emptyMap();

    private Verdict verdict;

    public DefaultPathAttributes() {
    }

    public DefaultPathAttributes(final PathAttributes copy) {
        size = copy.getSize();
        quota = copy.getQuota();
        modified = copy.getModificationDate();
        accessed = copy.getAccessedDate();
        created = copy.getCreationDate();
        owner = copy.getOwner();
        group = copy.getGroup();
        permission = Permission.EMPTY == copy.getPermission() ? Permission.EMPTY : new Permission(copy.getPermission());
        acl = Acl.EMPTY == copy.getAcl() ? Acl.EMPTY : new Acl(copy.getAcl());
        checksum = Checksum.NONE == copy.getChecksum() ? Checksum.NONE : new Checksum(copy.getChecksum());
        etag = copy.getETag();
        storageClass = copy.getStorageClass();
        encryption = copy.getEncryption();
        fileId = copy.getFileId();
        versionId = copy.getVersionId();
        lockId = copy.getLockId();
        duplicate = copy.isDuplicate();
        hidden = copy.isHidden();
        trashed = copy.isTrashed();
        revision = copy.getRevision();
        region = copy.getRegion();
        displayname = copy.getDisplayname();
        link = DescriptiveUrl.EMPTY == copy.getLink() ? DescriptiveUrl.EMPTY : new DescriptiveUrl(copy.getLink());
        metadata = new HashMap<>(copy.getMetadata());
        custom = new HashMap<>(copy.getCustom());
        verdict = copy.getVerdict();
        vault = copy.getVault();
        decrypted = copy.getDecrypted();
        encrypted = copy.getEncrypted();
        directoryId = copy.getDirectoryId();
    }

    @Override
    public <T> T serialize(final Serializer<T> dict) {
        if(size != -1) {
            dict.setStringForKey(String.valueOf(size), "Size");
        }
        if(quota != Quota.unknown) {
            // Set remaining quota
            dict.setStringForKey(String.valueOf(quota.available), "Quota");
        }
        if(modified != -1) {
            dict.setStringForKey(String.valueOf(modified), "Modified");
        }
        if(created != -1) {
            dict.setStringForKey(String.valueOf(created), "Created");
        }
        if(revision != null) {
            dict.setStringForKey(String.valueOf(revision), "Revision");
        }
        if(etag != null) {
            dict.setStringForKey(etag, "ETag");
        }
        if(permission != Permission.EMPTY) {
            dict.setObjectForKey(permission, "Permission");
        }
        if(owner != null) {
            dict.setStringForKey(owner, "Owner");
        }
        if(group != null) {
            dict.setStringForKey(group, "Group");
        }
        if(acl != Acl.EMPTY) {
            dict.setObjectForKey(acl, "Acl");
        }
        if(link != DescriptiveUrl.EMPTY) {
            final Map<String, String> wrapper = new HashMap<>();
            wrapper.put("Url", link.getUrl());
            wrapper.put("Type", link.getType().name());
            dict.setMapForKey(wrapper, "Link");
        }
        if(checksum != Checksum.NONE) {
            final Map<String, String> wrapper = new HashMap<>();
            wrapper.put("Algorithm", checksum.algorithm.name());
            wrapper.put("Hash", checksum.hash);
            if(null != checksum.base64) {
                wrapper.put("Base64", checksum.base64);
            }
            dict.setMapForKey(wrapper, "Checksum");
        }
        if(StringUtils.isNotBlank(versionId)) {
            dict.setStringForKey(versionId, "Version");
        }
        if(StringUtils.isNotBlank(fileId)) {
            dict.setStringForKey(fileId, "File Id");
        }
        if(StringUtils.isNotBlank(displayname)) {
            dict.setStringForKey(displayname, "Display Name");
        }
        if(StringUtils.isNotBlank(lockId)) {
            dict.setStringForKey(lockId, "Lock Id");
        }
        if(duplicate != null) {
            dict.setStringForKey(String.valueOf(duplicate), "Duplicate");
        }
        if(hidden != null) {
            dict.setStringForKey(String.valueOf(hidden), "Hidden");
        }
        if(trashed != null) {
            dict.setStringForKey(String.valueOf(trashed), "Trashed");
        }
        if(StringUtils.isNotBlank(region)) {
            dict.setStringForKey(region, "Region");
        }
        if(StringUtils.isNotBlank(storageClass)) {
            dict.setStringForKey(storageClass, "Storage Class");
        }
        if(vault != null) {
            if(vault.attributes() == this) {
                log.debug("Skip serializing vault attribute {} to avoid recursion", vault);
            }
            else {
                dict.setObjectForKey(vault, "Vault");
            }
        }
        if(!custom.isEmpty()) {
            dict.setMapForKey(custom, "Custom");
        }
        if(verdict != null) {
            dict.setStringForKey(verdict.name(), "Verdict");
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
    @Override
    public PathAttributes setSize(final long size) {
        this.size = size;
        return this;
    }

    @Override
    public Quota.Space getQuota() {
        return quota;
    }

    @Override
    public PathAttributes setQuota(final Quota.Space quota) {
        this.quota = quota;
        return this;
    }

    @Override
    public long getModificationDate() {
        return modified;
    }

    @Override
    public PathAttributes setModificationDate(final long millis) {
        this.modified = millis;
        return this;
    }

    @Override
    public long getCreationDate() {
        return created;
    }

    @Override
    public PathAttributes setCreationDate(final long millis) {
        this.created = millis;
        return this;
    }

    @Override
    public long getAccessedDate() {
        return accessed;
    }

    @Override
    public PathAttributes setAccessedDate(final long millis) {
        this.accessed = millis;
        return this;
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
    @Override
    public PathAttributes setPermission(final Permission p) {
        this.permission = p;
        return this;
    }

    @Override
    public Acl getAcl() {
        return acl;
    }

    @Override
    public PathAttributes setAcl(final Acl acl) {
        this.acl = acl;
        return this;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public PathAttributes setOwner(final String o) {
        this.owner = o;
        return this;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public PathAttributes setGroup(final String g) {
        this.group = g;
        return this;
    }

    @Override
    public Checksum getChecksum() {
        return checksum;
    }

    @Override
    public PathAttributes setChecksum(final Checksum checksum) {
        this.checksum = checksum;
        return this;
    }

    @Override
    public String getETag() {
        return etag;
    }

    @Override
    public PathAttributes setETag(final String etag) {
        this.etag = etag;
        return this;
    }

    /**
     * @return Storage redundancy identifier.
     */
    @Override
    public String getStorageClass() {
        return storageClass;
    }

    /**
     * @param storageClass Storage redundancy identifier.
     */
    @Override
    public PathAttributes setStorageClass(final String storageClass) {
        this.storageClass = storageClass;
        return this;
    }

    @Override
    public Encryption.Algorithm getEncryption() {
        return encryption;
    }

    @Override
    public PathAttributes setEncryption(final Encryption.Algorithm encryption) {
        this.encryption = encryption;
        return this;
    }

    /**
     * A version identifying a particular revision of a file with the same path.
     *
     * @return Version Identifier or null if not versioned.
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
    @Override
    public PathAttributes setVersionId(final String versionId) {
        this.versionId = versionId;
        return this;
    }

    /**
     * A unique identifier for a file with the same path. Remains constant over its lifetime.
     *
     * @return Identifier or null if there is no such concept
     */
    @Override
    public String getFileId() {
        return fileId;
    }

    @Override
    public PathAttributes setFileId(final String fileId) {
        this.fileId = fileId;
        return this;
    }

    @Override
    public String getLockId() {
        return lockId;
    }

    @Override
    public PathAttributes setLockId(final String lockId) {
        this.lockId = lockId;
        return this;
    }

    @Override
    public String getDirectoryId() {
        return directoryId;
    }

    @Override
    public PathAttributes setDirectoryId(final String directoryId) {
        this.directoryId = directoryId;
        return this;
    }

    @Override
    public Long getRevision() {
        return revision;
    }

    @Override
    public PathAttributes setRevision(final Long revision) {
        this.revision = revision;
        return this;
    }

    @Override
    public Path getDecrypted() {
        return decrypted;
    }

    @Override
    public PathAttributes setDecrypted(final Path decrypted) {
        this.decrypted = decrypted;
        return this;
    }

    @Override
    public Path getEncrypted() {
        return encrypted;
    }

    @Override
    public PathAttributes setEncrypted(final Path encrypted) {
        this.encrypted = encrypted;
        return this;
    }

    @Override
    public PathAttributes setVault(final Path vault) {
        this.vault = vault;
        return this;
    }

    @Override
    public Path getVault() {
        return vault;
    }

    @Override
    public boolean isDuplicate() {
        return duplicate != null && duplicate;
    }

    /**
     * Attribute to mark a file as hidden by default in addition to a filename convention.
     *
     * @param duplicate Flag
     */
    @Override
    public PathAttributes setDuplicate(final boolean duplicate) {
        this.duplicate = duplicate;
        return this;
    }

    @Override
    public Boolean isHidden() {
        return hidden != null && hidden;
    }

    @Override
    public PathAttributes setHidden(final boolean hidden) {
        this.hidden = hidden;
        return this;
    }

    @Override
    public Boolean isTrashed() {
        return trashed != null && trashed;
    }

    @Override
    public PathAttributes setTrashed(final boolean trashed) {
        this.trashed = trashed;
        return this;
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public PathAttributes setMetadata(final Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public String getRegion() {
        return region;
    }

    @Override
    public PathAttributes setRegion(final String region) {
        this.region = region;
        return this;
    }

    @Override
    public String getDisplayname() {
        return displayname;
    }

    @Override
    public PathAttributes setDisplayname(final String displayname) {
        this.displayname = displayname;
        return this;
    }

    @Override
    public DescriptiveUrl getLink() {
        return link;
    }

    @Override
    public PathAttributes setLink(final DescriptiveUrl link) {
        this.link = link;
        return this;
    }

    @Override
    public Map<String, String> getCustom() {
        return custom;
    }

    @Override
    public PathAttributes setCustom(final Map<String, String> custom) {
        this.custom = custom;
        return this;
    }

    @Override
    public PathAttributes setCustom(final String key, final String value) {
        custom = new HashMap<>(custom);
        custom.put(key, value);
        return this;
    }

    @Override
    public Verdict getVerdict() {
        return verdict;
    }

    @Override
    public PathAttributes setVerdict(final Verdict verdict) {
        this.verdict = verdict;
        return this;
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof DefaultPathAttributes)) {
            return false;
        }
        final DefaultPathAttributes that = (DefaultPathAttributes) o;
        if(size != that.size) {
            return false;
        }
        if(modified != that.modified) {
            return false;
        }
        if(!Objects.equals(checksum, that.checksum)) {
            return false;
        }
        if(!Objects.equals(permission, that.permission)) {
            return false;
        }
        if(!Objects.equals(acl, that.acl)) {
            return false;
        }
        if(!Objects.equals(versionId, that.versionId)) {
            return false;
        }
        if(!Objects.equals(fileId, that.fileId)) {
            return false;
        }
        if(!Objects.equals(revision, that.revision)) {
            return false;
        }
        if(!Objects.equals(verdict, that.verdict)) {
            return false;
        }
        if(!Objects.equals(lockId, that.lockId)) {
            return false;
        }
        if(!Objects.equals(vault, that.vault)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (size ^ (size >>> 32));
        result = 31 * result + (int) (modified ^ (modified >>> 32));
        result = 31 * result + (checksum != null ? checksum.hashCode() : 0);
        result = 31 * result + (permission != null ? permission.hashCode() : 0);
        result = 31 * result + (acl != null ? acl.hashCode() : 0);
        result = 31 * result + (versionId != null ? versionId.hashCode() : 0);
        result = 31 * result + (fileId != null ? fileId.hashCode() : 0);
        result = 31 * result + (revision != null ? revision.hashCode() : 0);
        result = 31 * result + (verdict != null ? verdict.hashCode() : 0);
        result = 31 * result + (lockId != null ? lockId.hashCode() : 0);
        result = 31 * result + (vault != null ? vault.hashCode() : 0);
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
        sb.append(", fileId='").append(fileId).append('\'');
        sb.append(", lockId='").append(lockId).append('\'');
        sb.append(", duplicate=").append(duplicate);
        sb.append(", hidden=").append(hidden);
        sb.append(", trashed=").append(trashed);
        sb.append(", revision=").append(revision);
        sb.append(", region='").append(region).append('\'');
        sb.append(", metadata=").append(metadata).append('\'');
        sb.append(", custom=").append(custom).append('\'');
        sb.append(", verdict=").append(verdict).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
