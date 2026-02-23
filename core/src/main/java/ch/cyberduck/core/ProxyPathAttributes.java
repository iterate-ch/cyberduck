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

import java.util.Map;

public class ProxyPathAttributes implements PathAttributes {

    private final PathAttributes proxy;

    public ProxyPathAttributes(final PathAttributes proxy) {
        this.proxy = proxy;
    }

    @Override
    public PathAttributes setSize(final long size) {
        return proxy.setSize(size);
    }

    @Override
    public Quota.Space getQuota() {
        return proxy.getQuota();
    }

    @Override
    public PathAttributes setQuota(final Quota.Space quota) {
        return proxy.setQuota(quota);
    }

    @Override
    public PathAttributes setModificationDate(final long millis) {
        return proxy.setModificationDate(millis);
    }

    @Override
    public PathAttributes setCreationDate(final long millis) {
        return proxy.setCreationDate(millis);
    }

    @Override
    public PathAttributes setAccessedDate(final long millis) {
        return proxy.setAccessedDate(millis);
    }

    @Override
    public PathAttributes setPermission(final Permission p) {
        return proxy.setPermission(p);
    }

    @Override
    public Acl getAcl() {
        return proxy.getAcl();
    }

    @Override
    public PathAttributes setAcl(final Acl acl) {
        return proxy.setAcl(acl);
    }

    @Override
    public PathAttributes setOwner(final String o) {
        return proxy.setOwner(o);
    }

    @Override
    public PathAttributes setGroup(final String g) {
        return proxy.setGroup(g);
    }

    @Override
    public Checksum getChecksum() {
        return proxy.getChecksum();
    }

    @Override
    public PathAttributes setChecksum(final Checksum checksum) {
        return proxy.setChecksum(checksum);
    }

    @Override
    public String getETag() {
        return proxy.getETag();
    }

    @Override
    public PathAttributes setETag(final String etag) {
        return proxy.setETag(etag);
    }

    @Override
    public String getStorageClass() {
        return proxy.getStorageClass();
    }

    @Override
    public PathAttributes setStorageClass(final String storageClass) {
        return proxy.setStorageClass(storageClass);
    }

    @Override
    public Encryption.Algorithm getEncryption() {
        return proxy.getEncryption();
    }

    @Override
    public PathAttributes setEncryption(final Encryption.Algorithm encryption) {
        return proxy.setEncryption(encryption);
    }

    @Override
    public String getVersionId() {
        return proxy.getVersionId();
    }

    @Override
    public PathAttributes setVersionId(final String versionId) {
        return proxy.setVersionId(versionId);
    }

    @Override
    public String getFileId() {
        return proxy.getFileId();
    }

    @Override
    public PathAttributes setFileId(final String fileId) {
        return proxy.setFileId(fileId);
    }

    @Override
    public String getLockId() {
        return proxy.getLockId();
    }

    @Override
    public PathAttributes setLockId(final String lockId) {
        return proxy.setLockId(lockId);
    }

    @Override
    public String getDirectoryId() {
        return proxy.getDirectoryId();
    }

    @Override
    public PathAttributes setDirectoryId(final String directoryId) {
        return proxy.setDirectoryId(directoryId);
    }

    @Override
    public Long getRevision() {
        return proxy.getRevision();
    }

    @Override
    public PathAttributes setRevision(final Long revision) {
        return proxy.setRevision(revision);
    }

    @Override
    public Path getDecrypted() {
        return proxy.getDecrypted();
    }

    @Override
    public PathAttributes setDecrypted(final Path decrypted) {
        return proxy.setDecrypted(decrypted);
    }

    @Override
    public PathAttributes setVault(final Path vault) {
        return proxy.setVault(vault);
    }

    @Override
    public Path getVault() {
        return proxy.getVault();
    }

    @Override
    public boolean isDuplicate() {
        return proxy.isDuplicate();
    }

    @Override
    public PathAttributes setDuplicate(final boolean duplicate) {
        return proxy.setDuplicate(duplicate);
    }

    @Override
    public Boolean isHidden() {
        return proxy.isHidden();
    }

    @Override
    public PathAttributes setHidden(final boolean hidden) {
        return proxy.setHidden(hidden);
    }

    @Override
    public Boolean isTrashed() {
        return proxy.isTrashed();
    }

    @Override
    public PathAttributes setTrashed(final boolean trashed) {
        return proxy.setTrashed(trashed);
    }

    @Override
    public Map<String, String> getMetadata() {
        return proxy.getMetadata();
    }

    @Override
    public PathAttributes setMetadata(final Map<String, String> metadata) {
        return proxy.setMetadata(metadata);
    }

    @Override
    public String getRegion() {
        return proxy.getRegion();
    }

    @Override
    public PathAttributes setRegion(final String region) {
        return proxy.setRegion(region);
    }

    @Override
    public String getDisplayname() {
        return proxy.getDisplayname();
    }

    @Override
    public PathAttributes setDisplayname(final String displayname) {
        return proxy.setDisplayname(displayname);
    }

    @Override
    public DescriptiveUrl getLink() {
        return proxy.getLink();
    }

    @Override
    public PathAttributes setLink(final DescriptiveUrl link) {
        return proxy.setLink(link);
    }

    @Override
    public Map<String, String> getCustom() {
        return proxy.getCustom();
    }

    @Override
    public PathAttributes setCustom(final Map<String, String> custom) {
        return proxy.setCustom(custom);
    }

    @Override
    public PathAttributes setCustom(final String key, final String value) {
        return proxy.setCustom(key, value);
    }

    @Override
    public Verdict getVerdict() {
        return proxy.getVerdict();
    }

    @Override
    public PathAttributes setVerdict(final Verdict verdict) {
        return proxy.setVerdict(verdict);
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
        return proxy.serialize(dict);
    }
}
