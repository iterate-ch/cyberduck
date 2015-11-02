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

import java.io.File;
import java.io.IOException;

/**
 * @version $Id:$
 */
public class FinderSidebarService implements SidebarService {
    private static final Logger log = Logger.getLogger(FinderSidebarService.class);

    static {
        Native.load("findersidebarservice");
    }

    private final String kLSSharedFileListFavoriteVolumes = "com.apple.LSSharedFileList.FavoriteVolumes";
    private final String kLSSharedFileListRecentServerItems = "com.apple.LSSharedFileList.RecentServers";
    private final String kLSSharedFileListFavoriteItems = "com.apple.LSSharedFileList.FavoriteItems";

    @Override
    public void add(final Local file) throws LocalAccessDeniedException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Add %s", file));
        }
        if(!this.addItem(file.getAbsolute(), kLSSharedFileListFavoriteItems)) {
            throw new LocalAccessDeniedException(String.format("Failure adding %s to %s", file, kLSSharedFileListFavoriteItems));
        }
    }

    @Override
    public void remove(final Local file) throws LocalAccessDeniedException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Remove %s", file));
        }
        try {
            if(!this.removeItem(new File(file.getAbsolute()).getCanonicalPath(), kLSSharedFileListFavoriteItems)) {
                throw new LocalAccessDeniedException(String.format("Failure removing %s from %s", file, kLSSharedFileListFavoriteItems));
            }
        }
        catch(IOException e) {
            throw new LocalAccessDeniedException(String.format("Failure removing %s from %s", file, kLSSharedFileListFavoriteItems), e);
        }
    }

    /**
     * @param file Path
     * @param list Shared item list name
     * @return False on failure
     */
    private native boolean addItem(final String file, final String list);

    /**
     * @param file Path
     * @param list Shared item list name
     * @return False on failure
     */
    private native boolean removeItem(final String file, final String list);
}
