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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.EnumSet;

@Optional
public interface UnixPermission {

    void setUnixOwner(Path file, String owner) throws BackgroundException;

    void setUnixGroup(Path file, String group) throws BackgroundException;

    Permission getUnixPermission(Path file) throws BackgroundException;

    default void setUnixPermission(Path file, Permission permission) throws BackgroundException {
        this.setUnixPermission(file, new TransferStatus().withPermission(permission));
    }

    void setUnixPermission(Path file, TransferStatus status) throws BackgroundException;

    Preferences preferences = PreferencesFactory.get();

    /**
     * @param type File or folder
     * @return Default mask for new file or folder
     */
    default Permission getDefault(final EnumSet<Path.Type> type) {
        if(preferences.getBoolean("queue.upload.permissions.default")) {
            if(type.contains(Path.Type.file)) {
                return new Permission(preferences.getInteger("queue.upload.permissions.file.default"));
            }
            else {
                return new Permission(preferences.getInteger("queue.upload.permissions.folder.default"));
            }
        }
        return Permission.EMPTY;
    }
}
