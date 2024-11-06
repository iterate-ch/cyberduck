package ch.cyberduck.core.features;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.EnumSet;
import java.util.List;

/**
 * Read and write ACLs of files
 */
@Optional
public interface AclPermission {

    /**
     * Retrieve access control list for file or folder
     *
     * @param file File or folder
     * @return Acl.EMPTY when not applicable
     */
    Acl getPermission(Path file) throws BackgroundException;

    /**
     * Write access control list for file or folder
     *
     * @param file File or folder
     * @param acl  Access control list to replace any previously set
     */
    default void setPermission(Path file, Acl acl) throws BackgroundException {
        this.setPermission(file, new TransferStatus().withAcl(acl));
    }

    void setPermission(Path file, TransferStatus status) throws BackgroundException;

    /**
     * @return List of known ACL users
     */
    List<Acl.User> getAvailableAclUsers();

    /**
     * Roles available for users in a configurable ACL.
     *
     * @param files List of files
     * @return A list of role names.
     */
    List<Acl.Role> getAvailableAclRoles(List<Path> files);

    Preferences preferences = PreferencesFactory.get();

    /**
     * @param file Remote file
     * @return Default ACL to set for file
     */
    default Acl getDefault(final Path type) throws BackgroundException {
        if(preferences.getBoolean("queue.upload.permissions.default")) {
            if(type.getType().contains(Path.Type.file)) {
                return toAcl(new Permission(preferences.getInteger("queue.upload.permissions.file.default")));
            }
            else {
                return toAcl(new Permission(preferences.getInteger("queue.upload.permissions.folder.default")));
            }
        }
        return Acl.EMPTY;
    }

    static Acl toAcl(final Permission permission) {
        final Acl acl = new Acl();
        if(permission.getOther().implies(Permission.Action.read)) {
            acl.addAll(new Acl.GroupUser(Acl.GroupUser.EVERYONE), new Acl.Role(Acl.Role.READ));
        }
        if(permission.getGroup().implies(Permission.Action.read)) {
            acl.addAll(new Acl.GroupUser(Acl.GroupUser.AUTHENTICATED), new Acl.Role(Acl.Role.READ));
        }
        if(permission.getGroup().implies(Permission.Action.write)) {
            acl.addAll(new Acl.GroupUser(Acl.GroupUser.AUTHENTICATED), new Acl.Role(Acl.Role.WRITE));
        }
        return acl;
    }
}
