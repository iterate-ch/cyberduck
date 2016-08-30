package ch.cyberduck.core.library;

/*
 *  Copyright (c) 2009 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import org.apache.log4j.Logger;

import java.io.File;

public final class Native {
    private static final Logger log = Logger.getLogger(Native.class);

    private Native() {
        //
    }

    private static final Object lock = new Object();

    /**
     * Load native library extensions
     *
     * @param library Library name
     * @return False if loading library failed
     */
    public static boolean load(final String library) {
        synchronized(lock) {
            final String path = Native.getPath(library);
            try {
                // Load using absolute path. Otherwise we may load
                // a library in java.library.path that was not intended
                // because of a naming conflict.
                System.load(path);
                return true;
            }
            catch(UnsatisfiedLinkError e) {
                log.warn(String.format("Failed to load %s:%s", path, e.getMessage()), e);
                try {
                    System.loadLibrary(library);
                    return true;
                }
                catch(UnsatisfiedLinkError f) {
                    log.warn(String.format("Failed to load %s:%s", library, e.getMessage()), e);
                    return false;
                }
            }
        }
    }

    /**
     * @param name Library name
     * @return Path in application bundle
     */
    protected static String getPath(final String name) {
        return new File(String.format("%s/%s", System.getProperty("java.library.path"), getName(name)))
                .getAbsolutePath();
    }

    protected static String getName(final String name) {
        return System.mapLibraryName(name);
    }
}