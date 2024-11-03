package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.Quota;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.sds.io.swagger.client.model.DeletedNode;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static ch.cyberduck.core.sds.SDSAttributesFinderFeature.*;

public class SDSAttributesAdapter implements AttributesAdapter<Node> {
    private static final Logger log = LogManager.getLogger(SDSAttributesFinderFeature.class);

    private final SDSSession session;

    public SDSAttributesAdapter(final SDSSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes toAttributes(final Node node) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setVersionId(String.valueOf(node.getId()));
        attributes.setRevision(node.getBranchVersion());
        if(node.isIsEncrypted() != null && !node.isIsEncrypted()) {
            attributes.setChecksum(Checksum.parse(node.getHash()));
        }
        // Legacy
        attributes.setModificationDate(node.getUpdatedAt() != null ? node.getUpdatedAt().getMillis() : -1L);
        // Override for >4.22
        if(node.getTimestampModification() != null) {
            attributes.setModificationDate(node.getTimestampModification().getMillis());
        }
        // Legacy
        attributes.setCreationDate(node.getCreatedAt() != null ? node.getCreatedAt().getMillis() : -1L);
        // Override for >4.22
        if(node.getTimestampCreation() != null) {
            attributes.setCreationDate(node.getTimestampCreation().getMillis());
        }
        if(null != node.getSize()) {
            attributes.setSize(node.getSize());
        }
        if(null != node.getQuota()) {
            // Remaining space
            attributes.setQuota(new Quota.Space(node.getSize(), node.getQuota()));
        }
        attributes.setPermission(this.toPermission(node));
        if(null != node.getUpdatedBy()) {
            attributes.setOwner(node.getUpdatedBy().getDisplayName());
        }
        attributes.setAcl(this.toAcl(node));
        final Map<String, String> custom = new HashMap<>();
        if(null != node.getCntDownloadShares()) {
            custom.put(KEY_CNT_DOWNLOADSHARES, String.valueOf(node.getCntDownloadShares()));
        }
        if(null != node.getCntUploadShares()) {
            custom.put(KEY_CNT_UPLOADSHARES, String.valueOf(node.getCntUploadShares()));
        }
        if(null != node.isIsEncrypted()) {
            custom.put(KEY_ENCRYPTED, String.valueOf(node.isIsEncrypted()));
        }
        if(null != node.getClassification()) {
            custom.put(KEY_CLASSIFICATION, String.valueOf(node.getClassification().getValue()));
        }
        attributes.setCustom(custom);
        if(null != node.getVirusProtectionInfo()) {
            switch(node.getVirusProtectionInfo().getVerdict()) {
                case CLEAN:
                    attributes.setVerdict(PathAttributes.Verdict.clean);
                    break;
                case IN_PROGRESS:
                    attributes.setVerdict(PathAttributes.Verdict.pending);
                    break;
                case MALICIOUS:
                    attributes.setVerdict(PathAttributes.Verdict.malicious);
                    break;
            }
        }
        return attributes;
    }

    public PathAttributes toAttributes(final DeletedNode node) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setDuplicate(true);
        attributes.setVersionId(String.valueOf(node.getId()));
        attributes.setCreationDate(node.getCreatedAt() != null ? node.getCreatedAt().getMillis() : -1L);
        attributes.setModificationDate(node.getUpdatedAt() != null ? node.getUpdatedAt().getMillis() : -1L);
        attributes.setSize(node.getSize());
        attributes.setOwner(node.getDeletedBy().getDisplayName());
        // Read of file in trash not supported
        attributes.setPermission(new Permission(Permission.Action.none, Permission.Action.none, Permission.Action.none));
        return attributes;
    }

    public EnumSet<Path.Type> toType(final Node node) {
        final EnumSet<Path.Type> type;
        switch(node.getType()) {
            case ROOM:
                type = EnumSet.of(Path.Type.directory, Path.Type.volume);
                break;
            case FOLDER:
                type = EnumSet.of(Path.Type.directory);
                break;
            default:
                type = EnumSet.of(Path.Type.file);
        }
        return type;
    }

    protected Permission toPermission(final Node node) {
        final Permission permission = new Permission();
        if(node.getPermissions() != null) {
            switch(node.getType()) {
                case FOLDER:
                case ROOM:
                    if(node.getPermissions().isCreate()
                            // For existing files the delete role is also required to overwrite
                            && node.getPermissions().isDelete()) {
                        permission.setUser(Permission.Action.all);
                    }
                    else {
                        permission.setUser(Permission.Action.read.or(Permission.Action.execute));
                    }
                    break;
                case FILE:
                    if(node.isIsEncrypted() != null && node.isIsEncrypted()) {
                        try {
                            if(null != session.keyPair()) {
                                permission.setUser(Permission.Action.none.or(Permission.Action.read));
                            }
                            else {
                                log.warn("Missing read permission for node {} with missing key pair", node);
                            }
                        }
                        catch(BackgroundException e) {
                            log.warn("Ignore failure {} retrieving key pair", e);
                        }
                    }
                    else {
                        if(node.getPermissions().isRead()) {
                            permission.setUser(Permission.Action.read);
                        }
                    }
                    if(node.getPermissions().isChange() && node.getPermissions().isDelete()) {
                        permission.setUser(permission.getUser().or(Permission.Action.write));
                    }
                    break;
            }
            if(log.isDebugEnabled()) {
                log.debug("Map node permissions {} to {}", node.getPermissions(), permission);
            }
        }
        return permission;
    }

    protected Acl toAcl(final Node node) {
        final Acl acl = new Acl();
        final Acl.User user = new Acl.CanonicalUser();
        if(node.getPermissions() != null) {
            if(node.getPermissions().isManage()) {
                acl.addAll(user, SDSPermissionsFeature.MANAGE_ROLE);
            }
            if(node.getPermissions().isRead()) {
                acl.addAll(user, SDSPermissionsFeature.READ_ROLE);
            }
            if(node.getPermissions().isCreate()) {
                acl.addAll(user, SDSPermissionsFeature.CREATE_ROLE);
            }
            if(node.getPermissions().isChange()) {
                acl.addAll(user, SDSPermissionsFeature.CHANGE_ROLE);
            }
            if(node.getPermissions().isDelete()) {
                acl.addAll(user, SDSPermissionsFeature.DELETE_ROLE);
            }
            if(node.getPermissions().isManageDownloadShare()) {
                acl.addAll(user, SDSPermissionsFeature.DOWNLOAD_SHARE_ROLE);
            }
            if(node.getPermissions().isManageUploadShare()) {
                acl.addAll(user, SDSPermissionsFeature.UPLOAD_SHARE_ROLE);
            }
        }
        return acl;
    }

    public static boolean isEncrypted(final PathAttributes attr) {
        if(attr.getCustom().containsKey(KEY_ENCRYPTED)) {
            return Boolean.parseBoolean(attr.getCustom().get(KEY_ENCRYPTED));
        }
        return false;
    }
}
