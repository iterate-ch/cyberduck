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

import java.util.Map;

/**
 * Interface for path attributes operations.
 */
public interface PathAttributes extends Attributes, Serializable {
    PathAttributes EMPTY = new DefaultPathAttributes() {
        @Override
        public <T> T serialize(final Serializer<T> dict) {
            return super.serialize(dict);
        }

        @Override
        public PathAttributes setSize(final long size) {
            return this;
        }

        @Override
        public PathAttributes setQuota(final Quota.Space quota) {
            return this;
        }

        @Override
        public PathAttributes setModificationDate(final long millis) {
            return this;
        }

        @Override
        public PathAttributes setCreationDate(final long millis) {
            return this;
        }

        @Override
        public PathAttributes setAccessedDate(final long millis) {
            return this;
        }

        @Override
        public PathAttributes setPermission(final Permission p) {
            return this;
        }

        @Override
        public PathAttributes setAcl(final Acl acl) {
            return this;
        }

        @Override
        public PathAttributes setOwner(final String o) {
            return this;
        }

        @Override
        public PathAttributes setGroup(final String g) {
            return this;
        }

        @Override
        public PathAttributes setChecksum(final Checksum checksum) {
            return this;
        }

        @Override
        public PathAttributes setETag(final String etag) {
            return this;
        }

        @Override
        public PathAttributes setStorageClass(final String storageClass) {
            return this;
        }

        @Override
        public PathAttributes setEncryption(final Encryption.Algorithm encryption) {
            return this;
        }

        @Override
        public PathAttributes setVersionId(final String versionId) {
            return this;
        }

        @Override
        public PathAttributes setFileId(final String fileId) {
            return this;
        }

        @Override
        public PathAttributes setLockId(final String lockId) {
            return this;
        }

        @Override
        public PathAttributes setDirectoryId(final String directoryId) {
            return this;
        }

        @Override
        public PathAttributes setRevision(final Long revision) {
            return this;
        }

        @Override
        public PathAttributes setDecrypted(final Path decrypted) {
            return this;
        }

        @Override
        public PathAttributes setDuplicate(final boolean duplicate) {
            return this;
        }

        @Override
        public PathAttributes setHidden(final boolean hidden) {
            return this;
        }

        @Override
        public PathAttributes setTrashed(final boolean trashed) {
            return this;
        }

        @Override
        public PathAttributes setMetadata(final Map<String, String> metadata) {
            return this;
        }

        @Override
        public PathAttributes setRegion(final String region) {
            return this;
        }

        @Override
        public PathAttributes setDisplayname(final String displayname) {
            return this;
        }

        @Override
        public PathAttributes setLink(final DescriptiveUrl link) {
            return this;
        }

        @Override
        public PathAttributes setCustom(final Map<String, String> custom) {
            return this;
        }

        @Override
        public PathAttributes setCustom(final String key, final String value) {
            return this;
        }

        @Override
        public PathAttributes setVerdict(final Verdict verdict) {
            return this;
        }
    };

    /**
     * @return length the size of file in bytes.
     */
    /**
     * @param size the size of file in bytes.
     */
    PathAttributes setSize(long size);

    Quota.Space getQuota();

    PathAttributes setQuota(Quota.Space quota);

    PathAttributes setModificationDate(long millis);

    PathAttributes setCreationDate(long millis);

    PathAttributes setAccessedDate(long millis);

    /**
     * @param p UNIX permissions
     */
    PathAttributes setPermission(Permission p);

    Acl getAcl();

    PathAttributes setAcl(Acl acl);

    PathAttributes setOwner(String o);

    PathAttributes setGroup(String g);

    Checksum getChecksum();

    PathAttributes setChecksum(Checksum checksum);

    String getETag();

    PathAttributes setETag(String etag);

    /**
     * @return Storage redundancy identifier.
     */
    String getStorageClass();

    /**
     * @param storageClass Storage redundancy identifier.
     */
    PathAttributes setStorageClass(String storageClass);

    Encryption.Algorithm getEncryption();

    PathAttributes setEncryption(Encryption.Algorithm encryption);

    /**
     * A version identifying a particular revision of a file with the same path.
     *
     * @return Version Identifier or null if not versioned.
     */
    String getVersionId();

    /**
     * Set a unique version identifier for the revision of a file.
     *
     * @param versionId Revision
     */
    PathAttributes setVersionId(String versionId);

    /**
     * A unique identifier for a file with the same path. Remains constant over its lifetime.
     *
     * @return Identifier or null if there is no such concept
     */
    String getFileId();

    PathAttributes setFileId(String fileId);

    String getLockId();

    PathAttributes setLockId(String lockId);

    String getDirectoryId();

    PathAttributes setDirectoryId(String directoryId);

    /**
     * @return The incrementing revision number of the file or null if not versioned.
     */
    Long getRevision();

    PathAttributes setRevision(Long revision);

    /**
     * @return Null if path is missing flag encrypted
     */
    Path getDecrypted();

    PathAttributes setDecrypted(Path decrypted);

    /**
     * If the path should not be displayed in a browser by default unless the user explicitly chooses to show hidden
     * files.
     *
     * @return True if hidden by default.
     */
    boolean isDuplicate();

    /**
     * Attribute to mark a file as hidden by default in addition to a filename convention.
     *
     * @param duplicate Flag
     */
    PathAttributes setDuplicate(boolean duplicate);

    Boolean isHidden();

    PathAttributes setHidden(boolean hidden);

    Boolean isTrashed();

    PathAttributes setTrashed(boolean trashed);

    Map<String, String> getMetadata();

    PathAttributes setMetadata(Map<String, String> metadata);

    String getRegion();

    PathAttributes setRegion(String region);

    String getDisplayname();

    PathAttributes setDisplayname(String displayname);

    DescriptiveUrl getLink();

    PathAttributes setLink(DescriptiveUrl link);

    Map<String, String> getCustom();

    PathAttributes setCustom(Map<String, String> custom);

    PathAttributes setCustom(String key, String value);

    Verdict getVerdict();

    PathAttributes setVerdict(Verdict verdict);

    enum Verdict {
        pending,
        clean,
        malicious
    }
}
