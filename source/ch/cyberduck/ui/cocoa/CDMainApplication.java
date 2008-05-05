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

import com.apple.cocoa.application.NSApplication;
import com.apple.cocoa.application.NSEvent;
import com.apple.cocoa.foundation.NSMutableDictionary;
import com.apple.cocoa.foundation.NSPoint;

import ch.cyberduck.ui.cocoa.threading.MainAction;

import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public class CDMainApplication extends NSApplication {
    private static Logger log = Logger.getLogger(CDMainApplication.class);

    public void sendEvent(final NSEvent event) {
        if(event.type() == NSEvent.ApplicationDefined) {
            try {
                final MainAction runnable;
                synchronized(events) {
                    runnable = (MainAction) events.valueForKey(String.valueOf(event.subtype()));
                }
                if(null == runnable) {
                    log.fatal("Event for unknown runnable:" + event.subtype());
                    return;
                }
                if(runnable.isValid()) {
                    runnable.run();
                }
                else {
                    log.warn("Received outdated event:" + runnable);
                }
            }
            finally {
                this.remove(String.valueOf(event.subtype()));
            }
            return;
        }
        super.sendEvent(event);
    }

    private final NSMutableDictionary events
            = new NSMutableDictionary();

    private void put(Object key, MainAction runnable) {
        synchronized(events) {
            events.setObjectForKey(runnable, String.valueOf(key));
        }
        if(log.isDebugEnabled()) {
            log.debug("Event Queue Size:" + events.count());
        }
    }

    private void remove(Object key) {
        synchronized(events) {
            events.removeObjectForKey(key);
        }
    }

    /**
     * Execute the passed <code>Runnable</code> on the main thread also known as NSRunLoop.DefaultRunLoopMode
     *
     * @param runnable The <code>Runnable</code> to run
     */
    public static synchronized void invoke(final MainAction runnable) {
        if(isMainThread()) {
            runnable.run();
            return;
        }
        final short key = runnable.id();
        NSEvent event = NSEvent.otherEvent(NSEvent.ApplicationDefined,
                new NSPoint(0, 0), 0, System.currentTimeMillis() / 1000.0, 0,
                null, key, -1, -1);
        final CDMainApplication app = (CDMainApplication) sharedApplication();
        app.put(String.valueOf(key), runnable);
        // This method can also be called in subthreads. Events posted
        // in subthreads bubble up in the main thread event queue.
        app.postEvent(event, false);
    }

    private static final String MAIN_THREAD_NAME = "main";

    public static boolean isMainThread() {
        return Thread.currentThread().getName().equals(MAIN_THREAD_NAME);
    }
}