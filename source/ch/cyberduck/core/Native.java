package ch.cyberduck.core;

import ch.cyberduck.ui.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public class Native {
    private static Logger log = Logger.getLogger(Native.class);

    private Native() {
        ;
    }

    private static final Object lock = new Object();

    /**
     * Load native library extensions
     *
     * @return
     */
    public static boolean load(String library) {
        synchronized(lock) {
            final String path = Native.getPath(library);
            try {
                // Load using absolute path. Otherwise we may load
                // a libray in java.library.path that was not intended
                // because of a naming conflict.
                System.load(path);
                log.info("Loaded " + path);
                return true;
            }
            catch(UnsatisfiedLinkError e) {
                log.error("Faied to load " + path + ":" + e.getMessage(), e);
                return false;
            }
        }
    }

    /**
     * @param name
     * @return
     */
    protected static String getPath(String name) {
        final String lib = NSBundle.mainBundle().resourcePath() + "/Java/lib" + name + ".dylib";
        log.info("Locating " + name + " at '" + lib + "'");
        return lib;
    }
}