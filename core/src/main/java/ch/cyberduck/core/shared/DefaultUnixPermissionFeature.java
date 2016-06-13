package ch.cyberduck.core.shared;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.preferences.PreferencesFactory;

public abstract class DefaultUnixPermissionFeature implements UnixPermission {

    @Override
    public Permission getDefault(final Local file) {
        if(PreferencesFactory.get().getBoolean("queue.upload.permissions.default")) {
            if(file.isFile()) {
                return new Permission(
                        PreferencesFactory.get().getInteger("queue.upload.permissions.file.default"));
            }
            else {
                return new Permission(
                        PreferencesFactory.get().getInteger("queue.upload.permissions.folder.default"));
            }
        }
        else {
            // Read permissions from local file
            return file.attributes().getPermission();
        }
    }
}
