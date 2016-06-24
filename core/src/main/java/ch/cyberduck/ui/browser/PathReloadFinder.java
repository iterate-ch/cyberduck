package ch.cyberduck.ui.browser;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Path;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PathReloadFinder {

    /**
     * @param changed Changed files and directories
     * @return Set of parent folders to reload
     */
    public Set<Path> find(final List<Path> changed) {
        final Set<Path> folders = new HashSet<Path>();
        for(Path file : changed) {
            if(file.isDirectory()) {
                folders.add(file);
            }
            folders.add(file.getParent());
        }
        return folders;
    }
}
