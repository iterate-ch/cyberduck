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

    I iconNamed(String name, Integer width, Integer height);

    I iconNamed(String name, Integer size);

    I iconNamed(String name);

    I documentIcon(String extension, Integer size);

    I documentIcon(String extension, Integer size, I badge);

    I folderIcon(Integer size);

    I folderIcon(Integer size, I badge);

    I fileIcon(Local item, Integer size);

    I fileIcon(Path item, Integer size);

    I aliasIcon(String extension, Integer size);

    I volumeIcon(Protocol protocol, Integer size);

    I applicationIcon(Application app, Integer size);
}
