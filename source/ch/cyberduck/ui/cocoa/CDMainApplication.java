package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Preferences;
import ch.cyberduck.ui.cocoa.application.NSApplication;
import ch.cyberduck.ui.cocoa.foundation.NSAutoreleasePool;
import ch.cyberduck.ui.cocoa.foundation.NSBundle;
import ch.cyberduck.ui.cocoa.threading.MainAction;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;

/**
 * @version $Id$
 */
public class CDMainApplication {
    private static Logger log = Logger.getLogger(CDMainApplication.class);

    private static boolean ROCOCOA_JNI_LOADED = false;

    private static final Object lock = new Object();

    private static boolean jni_load() {
        synchronized(lock) {
            if(!ROCOCOA_JNI_LOADED) {
                try {
                    NSBundle bundle = NSBundle.mainBundle();
                    String lib = bundle.resourcePath() + "/Java/" + "librococoa.dylib";
                    log.info("Locating librococoa.dylib at '" + lib + "'");
                    System.load(lib);
                    ROCOCOA_JNI_LOADED = true;
                    log.info("librococoa.dylib loaded");
                }
                catch(UnsatisfiedLinkError e) {
                    log.error("Could not load the librococoa.dylib library:" + e.getMessage());
                    throw e;
                }
            }
            return ROCOCOA_JNI_LOADED;
        }
    }

    /**
     * @param arguments
     */
    public static void main(String[] arguments) throws InterruptedException {
        CDMainApplication.jni_load();

        final NSAutoreleasePool pool = NSAutoreleasePool.push();

        final Logger root = Logger.getRootLogger();
        root.setLevel(Level.toLevel(Preferences.instance().getProperty("logging")));

        // This method also makes a connection to the window server and completes other initialization.
        // Your program should invoke this method as one of the first statements in main();
        final NSApplication app = NSApplication.sharedApplication();

        final CDMainController c = new CDMainController();

        // Must implement NSApplicationDelegate protocol
        app.setDelegate(c.id());

        // Starts the main event loop. The loop continues until a stop: or terminate: message is
        // received. Upon each iteration through the loop, the next available event
        // from the window server is stored and then dispatched by sending it to NSApp using sendEvent:.
        app.run();

        // 
        pool.drain();
    }

    /**
     * Execute the passed <code>Runnable</code> on the main thread also known as NSRunLoop.DefaultRunLoopMode
     *
     * @param runnable The <code>Runnable</code> to run
     */
    public static void invoke(final MainAction runnable) {
        synchronized(NSApplication.sharedApplication()) {
            Foundation.runOnMainThread(runnable);
        }
    }

    /**
     * We ensure the application runs on the AppKit thread using the <code>StartOnMainThread</code>
     * key in the Java section of the Info.plist
     */
    private static final String MAIN_THREAD_NAME = "main";

    /**
     * @return True if the current thread is not a background worker thread
     */
    public static boolean isMainThread() {
        return Thread.currentThread().getName().equals(MAIN_THREAD_NAME);
    }
}