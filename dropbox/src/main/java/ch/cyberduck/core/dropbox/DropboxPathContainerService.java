package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;

import org.apache.commons.lang3.StringUtils;

public class DropboxPathContainerService extends PathContainerService {

    private boolean useNamespace = false;

    /**
     * Note that this syntax of using a namespace ID in the path parameter is only supported for namespaces that are
     * mounted under the root. That means it can't be used to access the the team space itself, for members on teams
     * using team space and member folders.
     *
     * @param useNamespace Include namespace id in path
     */
    public DropboxPathContainerService withNamespace(final boolean useNamespace) {
        this.useNamespace = useNamespace;
        return this;
    }

    @Override
    public boolean isContainer(final Path file) {
        if(file.isRoot()) {
            return true;
        }
        if(super.isContainer(file)) {
            return file.getType().contains(Path.Type.volume);
        }
        return false;
    }

    @Override
    public Path getContainer(final Path file) {
        if(this.isContainer(file)) {
            // Reference container using parent namespace
            return super.getContainer(file.getParent());
        }
        return super.getContainer(file);
    }

    @Override
    public String getKey(final Path file) {
        final Path container = this.getContainer(file);
        final String namespace = container.attributes().getVersionId();
        if(StringUtils.isNotBlank(namespace)) {
            // Return path relative to the namespace root
            final String key = this.isContainer(file) ? file.getName() : super.getKey(file);
            if(file.isRoot()) {
                // Root
                if(useNamespace) {
                    return String.format("ns:%s", namespace);
                }
                return StringUtils.EMPTY;
            }
            if(useNamespace) {
                return String.format("ns:%s/%s", namespace, key);
            }
            return Path.DELIMITER + key;
        }
        return file.isRoot() ? StringUtils.EMPTY : file.getAbsolute();
    }
}
