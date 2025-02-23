package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Factory;

public class FilesystemBookmarkResolverFactory<Bookmark, Resolved> extends Factory<FilesystemBookmarkResolver<Bookmark, Resolved>> {

    private FilesystemBookmarkResolverFactory() {
        super("factory.bookmarkresolver.class");
    }

    public static <Bookmark, Resolved> FilesystemBookmarkResolver<Bookmark, Resolved> get() {
        return new FilesystemBookmarkResolverFactory<Bookmark, Resolved>().create();
    }
}
