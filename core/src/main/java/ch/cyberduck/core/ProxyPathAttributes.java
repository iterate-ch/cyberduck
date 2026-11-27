package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.vault.VaultVersion;

import java.util.Map;

public class ProxyPathAttributes implements PathAttributes {

    private final PathAttributes proxy;

    public ProxyPathAttributes(final PathAttributes proxy) {
        this.proxy = proxy;
    }

    @Override
    public PathAttributes setSize(final long size) {
        proxy.setSize(size);
        return this;
    }

    @Override
    public Quota.Space getQuota() {
        return proxy.getQuota();
    }

    @Override
    public PathAttributes setQuota(final Quota.Space quota) {
        proxy.setQuota(quota);
        return this;
    }

    @Override
    public PathAttributes setModificationDate(final long millis) {
        proxy.setModificationDate(millis);
        return this;
    }

    @Override
    public PathAttributes setCreationDate(final long millis) {
        proxy.setCreationDate(millis);
        return this;
    }

    @Override
    public PathAttributes setAccessedDate(final long millis) {
        proxy.setAccessedDate(millis);
        return this;
    }

    @Override
    public PathAttributes setPermission(final Permission p) {
        proxy.setPermission(p);
        return this;
    }

    @Override
    public Acl getAcl() {
        return proxy.getAcl();
    }

    @Override
    public PathAttributes setAcl(final Acl acl) {
        proxy.setAcl(acl);
        return this;
    }

    @Override
    public PathAttributes setOwner(final String o) {
        proxy.setOwner(o);
        return this;
    }

    @Override
    public PathAttributes setGroup(final String g) {
        proxy.setGroup(g);
        return this;
    }

    @Override
    public Checksum getChecksum() {
        return proxy.getChecksum();
    }

    @Override
    public PathAttributes setChecksum(final Checksum checksum) {
        proxy.setChecksum(checksum);
        return this;
    }

    @Override
    public String getETag() {
        return proxy.getETag();
    }

    @Override
    public PathAttributes setETag(final String etag) {
        proxy.setETag(etag);
        return this;
    }

    @Override
    public String getStorageClass() {
        return proxy.getStorageClass();
    }

    @Override
    public PathAttributes setStorageClass(final String storageClass) {
        proxy.setStorageClass(storageClass);
        return this;
    }

    @Override
    public Encryption.Algorithm getEncryption() {
        return proxy.getEncryption();
    }

    @Override
    public PathAttributes setEncryption(final Encryption.Algorithm encryption) {
        proxy.setEncryption(encryption);
        return this;
    }

    @Override
    public String getVersionId() {
        return proxy.getVersionId();
    }

    @Override
    public PathAttributes setVersionId(final String versionId) {
        proxy.setVersionId(versionId);
        return this;
    }

    @Override
    public String getFileId() {
        return proxy.getFileId();
    }

    @Override
    public PathAttributes setFileId(final String fileId) {
        proxy.setFileId(fileId);
        return this;
    }

    @Override
    public String getLockId() {
        return proxy.getLockId();
    }

    @Override
    public PathAttributes setLockId(final String lockId) {
        proxy.setLockId(lockId);
        return this;
    }

    @Override
    public byte[] getDirectoryId() {
        return proxy.getDirectoryId();
    }

    @Override
    public PathAttributes setDirectoryId(final byte[] directoryId) {
        proxy.setDirectoryId(directoryId);
        return this;
    }

    @Override
    public Long getRevision() {
        return proxy.getRevision();
    }

    @Override
    public PathAttributes setRevision(final Long revision) {
        proxy.setRevision(revision);
        return this;
    }

    @Override
    public Path getDecrypted() {
        return proxy.getDecrypted();
    }

    @Override
    public PathAttributes setDecrypted(final Path decrypted) {
        proxy.setDecrypted(decrypted);
        return this;
    }

    @Override
    public VaultVersion getVaultVersion() {
        return proxy.getVaultVersion();
    }

    @Override
    public PathAttributes setVaultVersion(final VaultVersion version) {
        proxy.setVaultVersion(version);
        return this;
    }

    @Override
    public boolean isDuplicate() {
        return proxy.isDuplicate();
    }

    @Override
    public PathAttributes setDuplicate(final boolean duplicate) {
        proxy.setDuplicate(duplicate);
        return this;
    }

    @Override
    public Boolean isHidden() {
        return proxy.isHidden();
    }

    @Override
    public PathAttributes setHidden(final boolean hidden) {
        proxy.setHidden(hidden);
        return this;
    }

    @Override
    public Boolean isTrashed() {
        return proxy.isTrashed();
    }

    @Override
    public PathAttributes setTrashed(final boolean trashed) {
        proxy.setTrashed(trashed);
        return this;
    }

    @Override
    public Map<String, String> getMetadata() {
        return proxy.getMetadata();
    }

    @Override
    public PathAttributes setMetadata(final Map<String, String> metadata) {
        proxy.setMetadata(metadata);
        return this;
    }

    @Override
    public String getRegion() {
        return proxy.getRegion();
    }

    @Override
    public PathAttributes setRegion(final String region) {
        proxy.setRegion(region);
        return this;
    }

    @Override
    public String getDisplayname() {
        return proxy.getDisplayname();
    }

    @Override
    public PathAttributes setDisplayname(final String displayname) {
        proxy.setDisplayname(displayname);
        return this;
    }

    @Override
    public DescriptiveUrl getLink() {
        return proxy.getLink();
    }

    @Override
    public PathAttributes setLink(final DescriptiveUrl link) {
        proxy.setLink(link);
        return this;
    }

    @Override
    public Map<String, String> getCustom() {
        return proxy.getCustom();
    }

    @Override
    public PathAttributes setCustom(final Map<String, String> custom) {
        proxy.setCustom(custom);
        return this;
    }

    @Override
    public PathAttributes setCustom(final String key, final String value) {
        proxy.setCustom(key, value);
        return this;
    }

    @Override
    public Verdict getVerdict() {
        return proxy.getVerdict();
    }

    @Override
    public PathAttributes setVerdict(final Verdict verdict) {
        proxy.setVerdict(verdict);
        return this;
    }

    @Override
    public long getSize() {
        return proxy.getSize();
    }

    @Override
    public long getModificationDate() {
        return proxy.getModificationDate();
    }

    @Override
    public long getCreationDate() {
        return proxy.getCreationDate();
    }

    @Override
    public long getAccessedDate() {
        return proxy.getAccessedDate();
    }

    @Override
    public Permission getPermission() {
        return proxy.getPermission();
    }

    @Override
    public String getOwner() {
        return proxy.getOwner();
    }

    @Override
    public String getGroup() {
        return proxy.getGroup();
    }

    @Override
    public <T> T serialize(final Serializer<T> dict) {
        // Copy to read values from possibly overridden getters prior serialization
        return new DefaultPathAttributes(this).serialize(dict);
    }
}
