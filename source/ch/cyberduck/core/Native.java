package ch.cyberduck.core;

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

import ch.cyberduck.ui.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public final class Native {
    private static Logger log = Logger.getLogger(Native.class);

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
    public static boolean load(String library) {
        synchronized(lock) {
            final long l = System.currentTimeMillis();
            final String path = Native.getPath(library);
            try {
                // Load using absolute path. Otherwise we may load
                // a libray in java.library.path that was not intended
                // because of a naming conflict.
                System.load(path);
                log.info(String.format("Loaded %s in %dms", path, System.currentTimeMillis() - l));
                return true;
            }
            catch(UnsatisfiedLinkError e) {
                log.warn(String.format("Failed to load %s:%s", path, e.getMessage()), e);
                try {
                    System.loadLibrary(library);
                }
                catch(UnsatisfiedLinkError f) {
                    log.warn(String.format("Failed to load %s:%s", library, e.getMessage()), e);
                    return false;
                }
                return false;
            }
        }
    }

    /**
     * @param name Library name
     * @return Path in application bundle
     */
    protected static String getPath(String name) {
        final String lib = String.format("%s/Java/lib%s.dylib", NSBundle.mainBundle().resourcePath(), name);
        if(log.isInfoEnabled()) {
            log.info(String.format("Locating library %s at %s", name, lib));
        }
        return lib;
    }
}