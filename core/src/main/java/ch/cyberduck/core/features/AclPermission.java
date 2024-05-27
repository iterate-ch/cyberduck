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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
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

    /**
     * @param file  Remote file
     * @param local File on local disk
     * @return Default ACL to set for file
     */
    Acl getDefault(Path file, Local local) throws BackgroundException;

    /**
     * @param type File or folder
     * @return Default ACL for new file or folder
     */
    Acl getDefault(EnumSet<Path.Type> type) throws BackgroundException;
}
