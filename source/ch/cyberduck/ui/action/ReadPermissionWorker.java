package ch.cyberduck.ui.action;


/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.i18n.Locale;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Id:$
 */
public abstract class ReadPermissionWorker extends Worker<List<Permission>> {

    /**
     * Selected files.
     */
    private List<Path> files;

    public ReadPermissionWorker(List<Path> files) {
        this.files = files;
    }

    @Override
    public List<Permission> run() {
        List<Permission> permissions = new ArrayList<Permission>();
        for(Path next : files) {
            if(next.attributes().getPermission().equals(Permission.EMPTY)) {
                // Read permission of every selected path
                next.readUnixPermission();
            }
            permissions.add(next.attributes().getPermission());
        }
        return permissions;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(Locale.localizedString("Getting permission of {0}", "Status"),
                this.toString(files));
    }
}
