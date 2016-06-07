package ch.cyberduck.ui.comparator;

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

public class PermissionsComparator extends BrowserComparator {
    private static final long serialVersionUID = 6998556172282538797L;

    public PermissionsComparator(boolean ascending) {
        super(ascending, new FilenameComparator(ascending));
    }

    @Override
    protected int compareFirst(final Path p1, final Path p2) {
        if(Permission.EMPTY.equals(p1.attributes().getPermission()) && Permission.EMPTY.equals(p2.attributes().getPermission())) {
            return 0;
        }
        if(Permission.EMPTY.equals(p1.attributes().getPermission())) {
            return -1;
        }
        if(Permission.EMPTY.equals(p2.attributes().getPermission())) {
            return 1;
        }
        Integer perm1 = Integer.valueOf(p1.attributes().getPermission().getMode());
        Integer perm2 = Integer.valueOf(p2.attributes().getPermission().getMode());
        if(perm1 > perm2) {
            return ascending ? 1 : -1;
        }
        else if(perm1 < perm2) {
            return ascending ? -1 : 1;
        }
        return 0;
    }
}
