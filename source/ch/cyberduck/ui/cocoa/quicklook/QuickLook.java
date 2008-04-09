package ch.cyberduck.ui.cocoa.quicklook;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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

import com.apple.cocoa.foundation.NSBundle;

import ch.cyberduck.core.Local;

import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public class QuickLook {
    private static Logger log = Logger.getLogger(QuickLook.class);

    private static final Object lock = new Object();

    private static boolean JNI_LOADED = false;

    private static boolean jni_load() {
        synchronized(lock) {
            if(!JNI_LOADED) {
                try {
                    NSBundle bundle = NSBundle.mainBundle();
                    String lib = bundle.resourcePath() + "/Java/" + "libQuickLook.dylib";
                    log.info("Locating libQuickLook.dylib at '" + lib + "'");
                    System.load(lib);
                    JNI_LOADED = true;
                    log.info("libQuickLook.dylib loaded");
                }
                catch(UnsatisfiedLinkError e) {
                    log.error("Could not load the libQuickLook.dylib library:" + e.getMessage());
                }
            }
            return JNI_LOADED;
        }
    }

    static {
        QuickLook.jni_load();
    }

    /**
     * @param files
     */
    private static native void select(String[] files);

    /**
     * Add this files to the Quick Look Preview shared window
     *
     * @param files
     */
    public static void select(Local[] files) {
        if(!QuickLook.jni_load()) {
            return;
        }
        final String[] paths = new String[files.length];
        for(int i = 0; i < files.length; i++) {
            paths[i] = files[i].getAbsolute();
        }
        select(paths);
    }

    public static native boolean isOpen();

    public static native void open();

    public static native void close();
}
