package ch.cyberduck.core.resources;

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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.local.Application;

public interface IconCache<I> {

    /**
     * @param name Icon name
     * @return Cached image
     */
    default I iconNamed(final String name) {
        return this.iconNamed(name, null);
    }

    /**
     * @param name Icon filename with extension
     * @param size Requested size
     * @return Cached image
     */
    default I iconNamed(final String name, final Integer size) {
        return this.iconNamed(name, size, size);
    }

    I iconNamed(String name, Integer width, Integer height);

    I documentIcon(String extension, Integer size);

    I documentIcon(String extension, Integer size, I badge);

    I folderIcon(Integer size);

    I folderIcon(Integer size, I badge);

    default I fileIcon(final Local item, final Integer size) {
        return this.documentIcon(item.getExtension(), size);
    }

    I fileIcon(Path item, Integer size);

    I aliasIcon(String extension, Integer size);

    default I volumeIcon(final Protocol protocol, final Integer size) {
        return this.iconNamed(protocol.disk(), size);
    }

    I applicationIcon(Application app, Integer size);
}
