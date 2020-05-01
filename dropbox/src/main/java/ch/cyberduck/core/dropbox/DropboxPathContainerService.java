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
     * @param useNamespace Include namespace id in path
     */
    public DropboxPathContainerService withNamespace(final boolean useNamespace) {
        this.useNamespace = useNamespace;
        return this;
    }

    @Override
    public String getKey(final Path file) {
        final Path container = this.getContainer(file);
        final String namespace = container.attributes().getVersionId();
        if(StringUtils.isNotBlank(namespace)) {
            // Return path relative to the namespace root
            final String key = super.getKey(file);
            if(StringUtils.isBlank(key)) {
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
