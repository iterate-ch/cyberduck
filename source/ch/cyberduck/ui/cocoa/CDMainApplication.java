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

import ch.cyberduck.ui.cocoa.application.NSApplication;
import ch.cyberduck.ui.cocoa.foundation.NSBundle;
import ch.cyberduck.ui.cocoa.threading.MainAction;

import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.cocoa.NSAutoreleasePool;

import java.awt.*;

/**
 * @version $Id:$
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
        final NSAutoreleasePool pool = NSAutoreleasePool.new_();
        try {
            CDMainApplication.jni_load();

            final Toolkit d = Toolkit.getDefaultToolkit();

            // This method also makes a connection to the window server and completes other initialization.
            // Your program should invoke this method as one of the first statements in main();
            final NSApplication app = NSApplication.sharedApplication();
            //app.run();

            final CDMainController c = new CDMainController();

            if(!NSBundle.loadNibNamed(c.getBundleName(), app.id())) {
                log.fatal("Couldn't load " + c.getBundleName() + ".nib");
            }

            //
            app.setDelegate(c.id());

            final CDBrowserController browser = c.newDocument();

            // Starts the main event loop.
            //app.run();
            synchronized(c) {
                c.wait();
            }
        }
        finally {
            pool.release();
        }
    }

    public static void invoke(final MainAction runnable) {
        invoke(runnable, false);
    }

    /**
     * Execute the passed <code>Runnable</code> on the main thread also known as NSRunLoop.DefaultRunLoopMode
     *
     * @param runnable The <code>Runnable</code> to run
     * @param front    The event is added to the front of the queue.
     *                 otherwise the event is added to the back of the queue.
     */
    public static void invoke(final MainAction runnable, boolean front) {
        if(isMainThread()) {
            runnable.run();
            return;
        }
        Foundation.runOnMainThread(runnable);
    }

    private static final String MAIN_THREAD_NAME = "AWT-AppKit";

    public static boolean isMainThread() {
        return Thread.currentThread().getName().equals(MAIN_THREAD_NAME);
    }
}