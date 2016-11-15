package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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
import ch.cyberduck.core.LocalFactory;

import org.apache.log4j.Logger;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class LibcWorkingDirectoryFinder implements WorkingDirectoryFinder {
    private static final Logger log = Logger.getLogger(LibcWorkingDirectoryFinder.class);

    private static final CLibrary library = (CLibrary) Native.loadLibrary("c", CLibrary.class);

    @Override
    public Local find() {
        final Local folder = LocalFactory.get(library.getcwd(null, 0L));
        if(log.isDebugEnabled()) {
            log.debug(String.format("Determined folder %s as working directory", folder));
        }
        return folder;
    }

    public interface CLibrary extends Library {
        String getcwd(Pointer buffer, long size);
    }
}
