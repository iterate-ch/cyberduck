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

import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.FactoryException;

import org.apache.log4j.Logger;

public class FilesystemBookmarkResolverFactory extends Factory<FilesystemBookmarkResolver<NSURL>> {
    private static final Logger log = Logger.getLogger(FilesystemBookmarkResolverFactory.class);

    public FilesystemBookmarkResolverFactory() {
        super("factory.bookmarkresolver.class");
    }

    public static FilesystemBookmarkResolver<NSURL> get() {
        try {
            return new FilesystemBookmarkResolverFactory().create();
        }
        catch(FactoryException e) {
            log.warn("No filesystem bookmark resolver configured");
            return new AliasFilesystemBookmarkResolver();
        }
    }
}
