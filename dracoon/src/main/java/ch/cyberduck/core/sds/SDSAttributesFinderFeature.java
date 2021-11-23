package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.DeletedNode;
import ch.cyberduck.core.sds.io.swagger.client.model.DeletedNodeVersionsList;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class SDSAttributesFinderFeature implements AttributesFinder {
    private static final Logger log = Logger.getLogger(SDSAttributesFinderFeature.class);

    public static final String KEY_CNT_DOWNLOADSHARES = "count_downloadshares";
    public static final String KEY_CNT_UPLOADSHARES = "count_uploadshares";
    public static final String KEY_ENCRYPTED = "encrypted";
    public static final String KEY_CLASSIFICATION = "classification";

    private final PathContainerService containerService
        = new SDSPathContainerService();

    /**
     * Lookup previous versions
     */
    private final boolean references;

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    public SDSAttributesFinderFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this(session, nodeid, new HostPreferences(session.getHost()).getBoolean("sds.versioning.references.enable"));
    }

    public SDSAttributesFinderFeature(final SDSSession session, final SDSNodeIdProvider nodeid, final boolean references) {
        this.session = session;
        this.nodeid = nodeid;
        this.references = references;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        return this.find(file, listener, new HostPreferences(session.getHost()).getInteger("sds.listing.chunksize"));
    }

    protected PathAttributes find(final Path file, final ListProgressListener listener, final int chunksize) throws BackgroundException {
        if(file.isRoot()) {
            // {"code":400,"message":"Bad Request","debugInfo":"Node ID must be positive.","errorCode":-80001}
            final PathAttributes attributes = new PathAttributes();
            if(session.userAccount().isUserInRole(SDSPermissionsFeature.ROOM_MANAGER_ROLE)) {
                // We need to map user roles to ACLs in order to decide if creating a top-level room is allowed
                final Acl acl = new Acl();
                acl.addAll(new Acl.CanonicalUser(), SDSPermissionsFeature.CREATE_ROLE);
                attributes.setAcl(acl);
            }
            return attributes;
        }
        // Throw failure if looking up file fails
        final String id = nodeid.getVersionId(file, listener);
        try {
            return this.findNode(file, chunksize, id);
        }
        catch(NotfoundException e) {
            // Try with reset cache after failure finding node id
            return this.findNode(file, chunksize, nodeid.getVersionId(file, listener));
        }
    }

    private PathAttributes findNode(final Path file, final int chunksize, final String nodeId) throws BackgroundException {
        try {
            if(file.attributes().isDuplicate()) {
                final DeletedNode node = new NodesApi(session.getClient()).requestDeletedNode(Long.parseLong(nodeId),
                    StringUtils.EMPTY, null);
                return this.toAttributes(node);
            }
            else {
                final Node node = new NodesApi(session.getClient()).requestNode(
                    Long.parseLong(nodeId), StringUtils.EMPTY, null);
                final PathAttributes attr = this.toAttributes(node);
                if(this.toType(node).contains(Path.Type.file)) {
                    if(references) {
                        try {
                            attr.setVersions(this.findDeleted(file, chunksize));
                        }
                        catch(AccessDeniedException e) {
                            log.warn(String.format("Ignore failure %s fetching versions for %s", e, file));
                        }
                    }
                }
                return attr;
            }
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map("Failure to read attributes of {0}", e, file);
        }
    }

    protected AttributedList<Path> findDeleted(final Path file, final int chunksize) throws BackgroundException {
        try {
            int offset = 0;
            DeletedNodeVersionsList nodes;
            final AttributedList<Path> versions = new AttributedList<>();
            do {
                nodes = new NodesApi(session.getClient()).requestDeletedNodeVersions(
                    Long.parseLong(nodeid.getVersionId(file.getParent(), new DisabledListProgressListener())),
                    file.isFile() ? "file" : "folder", file.getName(), StringUtils.EMPTY, null,
                    offset, chunksize, null);
                for(DeletedNode item : nodes.getItems()) {
                    versions.add(new Path(file.getParent(), file.getName(), file.getType(),
                        this.toAttributes(item)));
                }
                offset += chunksize;
            }
            while(nodes.getItems().size() == chunksize);
            return versions;
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map("Failure to read attributes of {0}", e, file);
        }
    }

    public PathAttributes toAttributes(final Node node) throws BackgroundException {
        final PathAttributes attributes = new PathAttributes();
        attributes.setVersionId(String.valueOf(node.getId()));
        attributes.setRevision(node.getBranchVersion());
        attributes.setChecksum(Checksum.parse(node.getHash()));
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
            attributes.setQuota(node.getQuota());
        }
        attributes.setPermission(this.toPermission(node));
        attributes.setOwner(node.getUpdatedBy().getDisplayName());
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
        return attributes;
    }

    public PathAttributes toAttributes(final DeletedNode node) {
        final PathAttributes attributes = new PathAttributes();
        attributes.setDuplicate(true);
        attributes.setVersionId(String.valueOf(node.getId()));
        attributes.setCreationDate(node.getCreatedAt() != null ? node.getCreatedAt().getMillis() : -1L);
        attributes.setModificationDate(node.getUpdatedAt() != null ? node.getUpdatedAt().getMillis() : -1L);
        attributes.setSize(node.getSize());
        attributes.setOwner(node.getUpdatedBy().getDisplayName());
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

    protected Permission toPermission(final Node node) throws BackgroundException {
        final Permission permission = new Permission(Permission.Action.none, Permission.Action.none, Permission.Action.none);
        if(node.isIsEncrypted() && node.getType() == Node.TypeEnum.FILE) {
            if(null != session.keyPair()) {
                permission.setUser(permission.getUser().or(Permission.Action.read));
            }
            else {
                log.warn(String.format("Missing read permission for node %s with missing key pair", node));
            }
        }
        else {
            permission.setUser(permission.getUser().or(Permission.Action.read));
        }
        switch(node.getType()) {
            case ROOM:
            case FOLDER:
                permission.setUser(permission.getUser().or(Permission.Action.execute));
        }
        if(node.getPermissions().isChange() && node.getPermissions().isDelete()) {
            permission.setUser(permission.getUser().or(Permission.Action.write));
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Map node permissions %s to %s", node.getPermissions(), permission));
        }
        return permission;
    }

    protected Acl toAcl(final Node node) {
        final Acl acl = new Acl();
        final Acl.User user = new Acl.CanonicalUser();
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
        return acl;
    }

}
