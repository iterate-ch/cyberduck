package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2015 David Kocher & Yves Langisch. All rights reserved.
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.io
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.library.Native;

import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public class FinderSidebarService implements SidebarService {
    private static final Logger log = Logger.getLogger(FinderSidebarService.class);

    static {
        Native.load("findersidebarservice");
    }

    private final String kLSSharedFileListFavoriteVolumes = "kLSSharedFileListFavoriteVolumes";

    @Override
    public void add(final Local file) throws LocalAccessDeniedException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Add %s", file));
        }
        if(!this.addItem(file.getAbsolute(), kLSSharedFileListFavoriteVolumes)) {
            throw new LocalAccessDeniedException(String.format("Failure adding %s to kLSSharedFileListFavoriteVolumes", file));
        }
    }

    @Override
    public void remove(final Local file) throws LocalAccessDeniedException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Remove %s", file));
        }
        if(!this.removeItem(kLSSharedFileListFavoriteVolumes)) {
            throw new LocalAccessDeniedException(String.format("Failure removing %s from kLSSharedFileListFavoriteVolumes", file));
        }
    }

    private native boolean addItem(final String file, final String name);

    private native boolean removeItem(final String name);
}
