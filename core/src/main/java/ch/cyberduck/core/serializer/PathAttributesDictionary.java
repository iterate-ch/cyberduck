package ch.cyberduck.core.serializer;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DeserializerFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PathAttributesDictionary {

    private final DeserializerFactory factory;

    public PathAttributesDictionary() {
        this.factory = new DeserializerFactory();
    }

    public PathAttributesDictionary(final DeserializerFactory factory) {
        this.factory = factory;
    }

    public <T> PathAttributes deserialize(T serialized) {
        final Deserializer dict = factory.create(serialized);
        final PathAttributes attributes = new PathAttributes();
        final String sizeObj = dict.stringForKey("Size");
        if(sizeObj != null) {
            attributes.setSize(Long.parseLong(sizeObj));
        }
        final String quotaObj = dict.stringForKey("Quota");
        if(quotaObj != null) {
            attributes.setQuota(Long.parseLong(quotaObj));
        }
        final String modifiedObj = dict.stringForKey("Modified");
        if(modifiedObj != null) {
            attributes.setModificationDate(Long.parseLong(modifiedObj));
        }
        final String createdObj = dict.stringForKey("Created");
        if(createdObj != null) {
            attributes.setModificationDate(Long.parseLong(createdObj));
        }
        final String revisionObj = dict.stringForKey("Revision");
        if(revisionObj != null) {
            attributes.setRevision(Long.parseLong(revisionObj));
        }
        final List<T> versionsObj = dict.listForKey("Versions");
        if(versionsObj != null) {
            final AttributedList<Path> versions = new AttributedList<>();
            for(T versionDict : versionsObj) {
                versions.add(new PathDictionary(factory).deserialize(versionDict));
            }
            attributes.setVersions(versions);
        }
        final String etagObj = dict.stringForKey("ETag");
        if(etagObj != null) {
            attributes.setETag(dict.stringForKey("ETag"));
        }
        final Object permissionObj = dict.objectForKey("Permission");
        if(permissionObj != null) {
            attributes.setPermission(new PermissionDictionary().deserialize(permissionObj));
        }
        final Object aclObj = dict.objectForKey("Acl");
        if(aclObj != null) {
            attributes.setAcl(new AclDictionary().deserialize(aclObj));
        }
        final Object linkObj = dict.stringForKey("Link");
        if(linkObj != null) {
            attributes.setLink(new DescriptiveUrl(URI.create(dict.stringForKey("Link")), DescriptiveUrl.Type.http));
        }
        if(dict.mapForKey("Checksum") != null) {
            final Map<String, String> checksum = dict.mapForKey("Checksum");
            attributes.setChecksum(new Checksum(HashAlgorithm.valueOf(checksum.get("Algorithm")), checksum.get("Hash")));
        }
        else {
            attributes.setChecksum(Checksum.parse(dict.stringForKey("Checksum")));
        }
        attributes.setVersionId(dict.stringForKey("Version"));
        attributes.setFileId(dict.stringForKey("File Id"));
        attributes.setLockId(dict.stringForKey("Lock Id"));
        final String duplicateObj = dict.stringForKey("Duplicate");
        if(duplicateObj != null) {
            attributes.setDuplicate(Boolean.parseBoolean(duplicateObj));
        }
        final String hiddenObj = dict.stringForKey("Hidden");
        if(hiddenObj != null) {
            attributes.setHidden(Boolean.parseBoolean(hiddenObj));
        }
        attributes.setMetadata(Collections.emptyMap());
        attributes.setRegion(dict.stringForKey("Region"));
        attributes.setStorageClass(dict.stringForKey("Storage Class"));
        final Object vaultObj = dict.objectForKey("Vault");
        if(vaultObj != null) {
            attributes.setVault(new PathDictionary(factory).deserialize(vaultObj));
        }
        final Map<String, String> customObj = dict.mapForKey("Custom");
        if(customObj != null) {
            attributes.setCustom(customObj);
        }
        return attributes;
    }
}
