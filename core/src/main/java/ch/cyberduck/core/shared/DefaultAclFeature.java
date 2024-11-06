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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import java.util.EnumSet;

public abstract class DefaultAclFeature implements AclPermission {

    private final Preferences preferences = PreferencesFactory.get();

    @Override
    public Acl getDefault(final EnumSet<Path.Type> type) {
        if(preferences.getBoolean("queue.upload.permissions.default")) {
            if(type.contains(Path.Type.file)) {
                return toAcl(new Permission(preferences.getInteger("queue.upload.permissions.file.default")));
            }
            else {
                return toAcl(new Permission(preferences.getInteger("queue.upload.permissions.folder.default")));
            }
        }
        return Acl.EMPTY;
    }

    @Override
    public Acl getDefault(final Path file, final Local local) throws BackgroundException {
        if(preferences.getBoolean("queue.upload.permissions.default")) {
            return this.getDefault(local.getType());
        }
        // Read permissions from local file
        return toAcl(local.attributes().getPermission());
    }

    public static Acl toAcl(final Permission permission) {
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
