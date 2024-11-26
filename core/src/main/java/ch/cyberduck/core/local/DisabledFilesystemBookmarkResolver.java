package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Local;

public class DisabledFilesystemBookmarkResolver implements FilesystemBookmarkResolver<Object, Object> {

    @Override
    public Void create(final Local file) {
        return null;
    }

    @Override
    public Object resolve(final Object file) {
        return null;
    }
}
