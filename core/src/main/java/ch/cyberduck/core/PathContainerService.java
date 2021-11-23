package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

public interface PathContainerService {

    default Path getRoot(final Path file) {
        Path parent = file;
        while(!parent.isRoot()) {
            parent = parent.getParent();
        }
        return parent;
    }

    /**
     * @param file Directory
     * @return True if file is container
     */
    boolean isContainer(Path file);

    /**
     * @return Default path or root with volume attributes set
     */
    default Path getContainer(Path file) {
        if(file.isRoot()) {
            return file;
        }
        Path container = file;
        while(!this.isContainer(container)) {
            container = container.getParent();
            if(container.isRoot()) {
                return container;
            }
        }
        return container;
    }

    /**
     * @param file Path in storage
     * @return Path relative to the container name or null for root or container argument
     */
    String getKey(Path file);
}
