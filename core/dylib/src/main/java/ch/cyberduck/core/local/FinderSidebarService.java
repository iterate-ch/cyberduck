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
        Native.load("core");
    }

    private static final String kLSSharedFileListFavoriteVolumes = "com.apple.LSSharedFileList.FavoriteVolumes";
    private static final String kLSSharedFileListRecentServerItems = "com.apple.LSSharedFileList.RecentServers";
    private static final String kLSSharedFileListFavoriteItems = "com.apple.LSSharedFileList.FavoriteItems";
    private static final String kLSSharedFileListSessionLoginItems = "com.apple.LSSharedFileList.SessionLoginItems";

    private final List list;

    public FinderSidebarService() {
        this.list = List.favorite;
    }

    public FinderSidebarService(final List list) {
        this.list = list;
    }

    @Override
    public void add(final Local file) throws LocalAccessDeniedException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Add %s to %s", file, this.forList(list)));
        }
        try {
            if(!this.addItem(new File(file.getAbsolute()).getCanonicalPath(), this.forList(list))) {
                throw new LocalAccessDeniedException(String.format("Failure adding %s to %s", file, this.forList(list)));
            }
        }
        catch(IOException e) {
            throw new LocalAccessDeniedException(String.format("Failure adding %s to %s", file, this.forList(list)), e);
        }
    }

    @Override
    public void remove(final Local file) throws LocalAccessDeniedException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Remove %s in %s", file, this.forList(list)));
        }
        try {
            if(!this.removeItem(new File(file.getAbsolute()).getCanonicalPath(), this.forList(list))) {
                throw new LocalAccessDeniedException(String.format("Failure removing %s from %s", file, this.forList(list)));
            }
        }
        catch(IOException e) {
            throw new LocalAccessDeniedException(String.format("Failure removing %s from %s", file, this.forList(list)), e);
        }
    }

    private String forList(final List list) {
        switch(list) {
            case volume:
                return kLSSharedFileListFavoriteVolumes;
            case server:
                return kLSSharedFileListRecentServerItems;
            case login:
                return kLSSharedFileListSessionLoginItems;
        }
        return kLSSharedFileListFavoriteItems;
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
