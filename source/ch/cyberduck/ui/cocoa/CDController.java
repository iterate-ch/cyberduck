package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import com.apple.cocoa.foundation.*;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public abstract class CDController extends NSObject {
    private static Logger log = Logger.getLogger(CDController.class);

    public CDController() {
        // Add this object to the array to safe weak references 
        // from being garbage collected (#hack)
        instances.addObject(this);
    }

    protected static final NSMutableArray instances
            = new NSMutableArray();

    private final NSMutableArray timers
            = new NSMutableArray();

    /**
     * Execute the passed <code>Runnable</code> on the main thread also known as NSRunLoop.DefaultRunLoopMode
     *
     * @param runnable The <code>Runnable</code> to run
     */
    public void invoke(Runnable runnable) {
        this.invoke(runnable, 0f);
    }

    /**
     * Execute the passed <code>Runnable</code> on the main thread also known as NSRunLoop.DefaultRunLoopMode
     *
     * @param runnable The <code>Runnable</code> to run
     * @param delay    Number of seconds to delay the execution
     */
    protected void invoke(Runnable runnable, float delay) {
        NSTimer timer = new NSTimer(delay, this,
                new NSSelector("post", new Class[]{NSTimer.class}),
                runnable,
                false //automatically invalidate
        );
        synchronized(timers) {
            timers.addObject(timer);
            CDMainController.mainRunLoop.addTimerForMode(timer,
                    NSRunLoop.DefaultRunLoopMode);
        }
    }

    /**
     * Called by the timer to invoke the passed method in the main thread
     *
     * @param timer holds the <code>Runnable</code> object in #userInfo
     */
    protected void post(NSTimer timer) {
        log.debug("post:" + timer);
        try {
            Object info = timer.userInfo();
            if(info instanceof Runnable) {
                ((Runnable) info).run();
            }
            synchronized(timers) {
                timers.removeObject(timer);
            }
        }
        finally {
            timer.invalidate();
        }
    }

    /**
     * Free all locked resources by this controller; also remove me from all observables;
     * marks this controller to be garbage collected as soon as needed
     */
    protected void invalidate() {
        log.debug("invalidate:" + this.toString());
        for(int i = 0; i < timers.count(); i++) {
            NSTimer timer = (NSTimer) timers.objectAtIndex(i);
            timer.invalidate();
        }
        timers.removeAllObjects();
        NSNotificationCenter.defaultCenter().removeObserver(this);
        instances.removeObject(this);
        System.gc();
    }

    protected void finalize() throws java.lang.Throwable {
        log.debug("finalize:" + this.toString());
        super.finalize();
    }
}