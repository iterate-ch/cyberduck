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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.StaticPermission;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.HostPreferencesFactory;

import java.util.EnumSet;

public abstract class DefaultUnixPermissionFeature implements UnixPermission {

    private final Host host;

    public DefaultUnixPermissionFeature(final Host host) {
        this.host = host;
    }

    /**
     * @param workdir Parent folder
     * @param type    File or folder
     * @return Default mask for new file or folder
     */
    @Override
    public Permission getDefault(final Path workdir, final EnumSet<Path.Type> type) {
        final HostPreferences preferences = HostPreferencesFactory.get(host);
        if(preferences.getBoolean("queue.upload.permissions.default")) {
            if(type.contains(Path.Type.file)) {
                return new StaticPermission(preferences.getInteger("queue.upload.permissions.file.default"));
            }
            else {
                return new StaticPermission(preferences.getInteger("queue.upload.permissions.folder.default"));
            }
        }
        return Permission.EMPTY;
    }
}
