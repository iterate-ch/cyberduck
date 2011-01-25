package ch.cyberduck.ui;

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

/**
 * @version $Id$
 */
public class PermissionsComparator extends BrowserComparator {

    public PermissionsComparator(boolean ascending) {
        super(ascending, new FilenameComparator(ascending));
    }

    @Override
    protected int compareFirst(Path p1, Path p2) {
        Integer perm1 = Integer.valueOf(p1.attributes().getPermission().getOctalString());
        Integer perm2 = Integer.valueOf(p2.attributes().getPermission().getOctalString());
        if(perm1 > perm2) {
            return ascending ? 1 : -1;
        }
        else if(perm1 < perm2) {
            return ascending ? -1 : 1;
        }
        return 0;
    }

    @Override
    public String getIdentifier() {
        return "permissions";
    }
}
