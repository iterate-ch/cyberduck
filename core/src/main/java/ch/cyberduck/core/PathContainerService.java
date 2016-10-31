package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

public class PathContainerService {

    public boolean isContainer(final Path file) {
        if(file.isRoot()) {
            return false;
        }
        return file.getParent().isRoot();
    }

    /**
     * @return Default path or root with volume attributes set
     */
    public Path getContainer(final Path file) {
        if(file.isRoot()) {
            return file;
        }
        Path container = file;
        while(!this.isContainer(container)) {
            container = container.getParent();
        }
        return container;
    }

    /**
     * @return Path relative to the container name
     */
    public String getKey(final Path file) {
        if(file.isRoot()) {
            return null;
        }
        if(this.isContainer(file)) {
            return null;
        }
        return PathRelativizer.relativize(this.getContainer(file).getAbsolute(), file.getAbsolute());
    }
}
